class Parser(private val lox: Lox, private val tokens: List<Token>) {
    class ParseError : RuntimeException()

    private var current = 0

    fun parse(): Expr? {
        return try {
            expression()
        } catch (error: ParseError) {
            null
        }
    }

    private fun expression(): Expr {
        return comma()
    }

    private fun comma(): Expr {
        var expr = equality()

        while (match(TokenType.COMMA)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = addition()

        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            val operator = previous()
            val right = addition()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun addition(): Expr {
        var expr = multiplication()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operator = previous()
            val right = multiplication()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun multiplication(): Expr {
        var expr = unary()

        while (match(TokenType.STAR, TokenType.SLASH)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.MINUS, TokenType.BANG)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        when {
            match(TokenType.TRUE) -> return Expr.Literal(true)
            match(TokenType.FALSE) -> return Expr.Literal(false)
            match(TokenType.NIL) -> return Expr.Literal(null)
            match(TokenType.NUMBER, TokenType.STRING) -> return Expr.Literal(previous().literal)
            match(TokenType.LEFT_PAREN) -> {
                val expr = expression()
                consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
                return Expr.Grouping(expr)
            }
        }
        throw error(lox, peek(), "Expect expression")
    }

    // advances tokens if any of types match
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) {
            return advance()
        }

        throw error(lox, peek(), message)
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return
            }

            when (peek().type) {
                TokenType.CLASS -> return
                TokenType.FUN -> return
                TokenType.VAR -> return
                TokenType.FOR -> return
                TokenType.IF -> return
                TokenType.WHILE -> return
                TokenType.PRINT -> return
                TokenType.RETURN -> return
                else -> advance()
            }
        }
    }

    private fun advance(): Token {
        current++
        return previous()
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) {
            return false
        }
        return peek().type == type
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun isAtEnd(): Boolean {
        check(current < tokens.size) { "Invalid state where we advanced too much"}
        return peek().type == TokenType.EOF
    }
}

fun error(lox: Lox, token: Token, message: String): Parser.ParseError {
    lox.error(token, message)
    return Parser.ParseError()
}