class Interpreter(private val lox: Lox) : Expr.Visitor<Any?> {
    class RuntimeError(val token: Token, message: String) : RuntimeException(message)

    fun interpret(expr: Expr) {
        try {
            val value = evaluate(expr)
            println(stringify(value))
        } catch (e: RuntimeError) {
            lox.runtimeError(e)
        }
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
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
                    return left + right
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
            return ""
        }

        // Hack. Work around Java adding ".0" to integer-valued doubles.
        if (obj is Double) {
            val text = obj.toString()
            return text.removeSuffix(".0")
        }

        return obj.toString()
    }
}
