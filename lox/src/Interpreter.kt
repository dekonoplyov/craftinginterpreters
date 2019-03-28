class Interpreter(val lox: Lox) : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    class RuntimeError(val token: Token, message: String) : RuntimeException(message)

    private val globals = Environment()

    private var environment = globals
    private val locals = HashMap<Expr, Int>()

    init {
        globals.define("clock", object : LoxCallable {
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return System.currentTimeMillis() / 1000.0
            }

            override fun toString(): String = "<native fun 'clock'>"
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (e: RuntimeError) {
            lox.runtimeError(e)
        }
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(statement: Stmt) {
        statement.accept(this)
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals[expr]
        if (distance == null) {
            globals.assign(expr.name, value)
        } else {
            environment.assignAt(distance, expr.name, value)
        }

        return value
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> numberOperation(left, right, expr.operator)
            TokenType.STAR -> numberOperation(left, right, expr.operator)
            TokenType.SLASH -> numberOperation(left, right, expr.operator)
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    numberOperation(left, right, expr.operator)
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(expr.operator, "Invalid operand types for '+' operator")
                }
            }
            TokenType.LESS -> numberOperation(left, right, expr.operator)
            TokenType.LESS_EQUAL -> numberOperation(left, right, expr.operator)
            TokenType.GREATER_EQUAL -> numberOperation(left, right, expr.operator)
            TokenType.GREATER -> numberOperation(left, right, expr.operator)
            TokenType.EQUAL_EQUAL -> isEqual(left, right)
            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.COMMA -> right
            else -> throw RuntimeError(expr.operator, "Invalid binary operator ${expr.operator.type}")
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left
        } else if (expr.operator.type == TokenType.AND) {
            if (!isTruthy(left))
                return left
        } else {
            lox.runtimeError(
                Interpreter.RuntimeError(
                    expr.operator,
                    "Wrong token '${expr.operator.type}' in logical expression"
                )
            )
        }

        return evaluate(expr.right)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)
            TokenType.MINUS -> {
                if (right is Double) {
                    -right
                } else {
                    throw RuntimeError(expr.operator, "unary minus with not number")
                }
            }
            else -> throw RuntimeError(expr.operator, "Invalid unary operator ${expr.operator.type}")
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = ArrayList(expr.arguments.map { evaluate(it) })

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }

        if (callee.arity() != arguments.size) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments, got ${arguments.size}.")
        }

        return callee.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj)
        if (obj is LoxInstance) {
            return obj.get(expr.name)
        }

        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return lookUpVariable(expr)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        if (lox.mode == Lox.Mode.REPL) {
            val value = evaluate(stmt.expression)
            println(stringify(value))
        } else {
            evaluate(stmt.expression)
        }
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        // two-stage variable binding process
        // allows references to the class inside its own methods
        environment.define(stmt.name.lexeme, null)
        val klass = LoxClass(stmt.name.lexeme)
        environment.assign(stmt.name, klass)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        val value = evaluate(stmt.value)

        throw Return(value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = evaluate(stmt.initializer)
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    private fun lookUpVariable(expr: Expr.Variable): Any? {
        val distance = locals[expr]
        return if (distance == null) {
            globals.get(expr.name)
        } else {
            environment.getAt(distance, expr.name.lexeme)
        }
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment

        try {
            this.environment = environment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    private fun numberOperation(left: Any?, right: Any?, token: Token): Any? {
        if (left !is Double) {
            throw RuntimeError(token, "left operand should be number")
        }

        if (right !is Double) {
            throw RuntimeError(token, "right operand should be number")
        }

        return when (token.type) {
            TokenType.MINUS -> left - right
            TokenType.PLUS -> left + right
            TokenType.STAR -> left * right
            TokenType.SLASH -> left / right
            TokenType.LESS -> left < right
            TokenType.LESS_EQUAL -> left <= right
            TokenType.GREATER_EQUAL -> left >= right
            TokenType.GREATER -> left > right
            else -> throw RuntimeError(token, "Invalid binary number operator ${token.type}")
        }
    }

    private fun isEqual(left: Any?, right: Any?): Boolean {
        if (left == null && right == null) {
            return true
        }
        if (left == null) {
            return false
        }

        return left == right
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }

        if (obj is Boolean) {
            return obj
        }

        return true
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) {
            return "nil"
        }

        // Hack. Work around Java adding ".0" to integer-valued doubles.
        if (obj is Double) {
            val text = obj.toString()
            return text.removeSuffix(".0")
        }

        return obj.toString()
    }
}
