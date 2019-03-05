import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    private var hadError = false

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    private fun run(source: String) {
        val scanner = Scanner(this, source)
        val tokens = scanner.scanTokens()

        for (token in tokens) {
            println(token)
        }
    }

    @Throws(IOException::class)
    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        if (hadError) {
            exitProcess(65)
        }
    }

    @Throws(IOException::class)
    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            run(reader.readLine())
            hadError = false
        }
    }
}

fun main(args: Array<String>) {
    val expression = Expr.Binary(
        Expr.Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Expr.Literal(123)
        ),
        Token(TokenType.STAR, "*", null, 1),
        Expr.Grouping(
            Expr.Literal(45.67)
        )
    )

    println(AstPrinter().print(expression))
//    val lox = Lox()
//    when {
//        args.size > 1 -> {
//            println("Usage: jlox [script]")
//            exitProcess(64)
//        }
//        args.size == 1 -> lox.runFile(args[0])
//        else -> lox.runPrompt()
//    }
}
