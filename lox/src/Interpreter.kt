class Interpreter(private val lox: Lox) : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    class RuntimeError(val token: Token, message: String) : RuntimeException(message)

    private var environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (e: RuntimeError) {
            lox.runtimeError(e)
        }
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(statement: Stmt) {
        statement.accept(this)
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
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
            else -> throw RuntimeError(expr.operator, "Invalid binary operator ${expr.operator.type}")
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
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

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        if (lox.mode == Lox.Mode.REPL) {
            val value = evaluate(stmt.expression)
            println(stringify(value))
        } else {
            evaluate(stmt.expression)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = evaluate(stmt.initializer)
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
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
