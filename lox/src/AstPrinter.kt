class AstPrinter : Expr.Visitor<String> {
    override fun visitThisExpr(expr: Expr.This): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitSetExpr(expr: Expr.Set): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitGetExpr(expr: Expr.Get): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return expr.name.lexeme
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()

        builder.append("(")
        builder.append(name)
        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")

        return builder.toString()
    }
}

class RpnPrinter : Expr.Visitor<String> {
    override fun visitThisExpr(expr: Expr.This): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitSetExpr(expr: Expr.Set): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitGetExpr(expr: Expr.Get): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return "${expr.left.accept(this)} ${expr.right.accept(this)} ${expr.operator.lexeme}"
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return expr.expression.accept(this)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return "${expr.operator.lexeme}${expr.right.accept(this)}"
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return expr.name.lexeme
    }
}
