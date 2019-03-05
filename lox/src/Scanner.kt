import kotlin.test.assert

class Scanner(private val lox: Lox, private val source: String) {
    companion object {
        private val keywords = hashMapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType. RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
    }

    private val tokens = ArrayList<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            ' ', '\t', '\r' -> {}
            '\n' -> line++
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                when {
                    match('/') -> processComment()
                    match('*') -> processMultiLineComment()
                    else -> addToken(TokenType.SLASH)
                }
            }
            '"' -> processString()
            else -> {
                when {
                    c.isDigit() -> processNumber()
                    isAlpha(c) -> processIdentifier()
                    else -> lox.error(line, "Unexpected character. $c")
                }
            }
        }
    }
    
    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val lexeme = source.substring(start, current)
        tokens.add(Token(type, lexeme, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected) {
            return false
        }
        
        advance()
        return true
    }

    private fun processComment() {
        // A comment goes until the end of the line.
        while (peek() != '\n' && !isAtEnd()) {
            advance()
        }
    }

    private fun processMultiLineComment() {
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
            }
            advance()
        }

        // Skip the trailing '*/'.
        repeat(2) {
            if (isAtEnd()) {
                lox.error(line, "Unterminated multiline comment.")
                return
            }
            advance()
        }
    }

    private fun processString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
            }
            advance()
        }

        // Trim the closing ".
        if (isAtEnd()) {
            lox.error(line, "Unterminated string.")
            return
        }
        advance()

        // Trim the surrounding quotes.
        val str = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, str)
    }

    private fun processNumber() {
        while (peek().isDigit() && !isAtEnd()) {
            advance()
        }

        if (peek() == '.' && peekNext().isDigit()) {
            // Skip dot.
            advance()

            while (peek().isDigit() && !isAtEnd()) {
                advance()
            }
        }

        val num = source.substring(start, current)
        addToken(TokenType.NUMBER, num.toDouble())
    }

    private fun processIdentifier() {
        while (isAlphaNumerical(peek())) {
            advance()
        }

        val text = source.substring(start, current)
        val type = keywords[text]

        if (type == null) {
            addToken(TokenType.IDENTIFIER)
        } else {
            addToken(type)
        }
    }

    private fun peek(): Char {
        return if (isAtEnd()) 0.toChar() else source[current]
    }

    private fun peekNext(): Char {
        return if (current + 1 >= source.length) 0.toChar() else source[current + 1]
    }

    private fun advance(): Char {
        check(!isAtEnd()) { "Trying to advance at end source"}
        return source[current++]
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }
}

fun isAlpha(c: Char): Boolean {
    return c.isLetter() || c == '_'
}

fun isAlphaNumerical(c: Char): Boolean {
    return isAlpha(c) || c.isDigit()
}