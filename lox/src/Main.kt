import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    private var hadError = false
    private var hadRuntimeError = false

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }

    fun runtimeError(error: Interpreter.RuntimeError) {
        System.err.println("${error.message}\n[line ${error.token.line}]")
        hadRuntimeError = true
    }

    private fun run(source: String, environment: Environment) {
        val scanner = Scanner(this, source)
        val tokens = scanner.scanTokens()
        val parser = Parser(this, tokens)
        // throws while parsing
        // should be fixed after synchronization
        val statements = parser.parse()

        if (hadError) {
            return
        }

        val interpreter = Interpreter(this, environment)
        interpreter.interpret(statements)
    }

    @Throws(IOException::class)
    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()), Environment())

        if (hadError) {
            exitProcess(65)
        }

        if (hadRuntimeError) {
            exitProcess(70)
        }
    }

    @Throws(IOException::class)
    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        val environment = Environment()

        while (true) {
            print("> ")
            run(reader.readLine(), environment)
            hadError = false
        }
    }
}

fun main(args: Array<String>) {
    val lox = Lox()
    when {
        args.size == 1 -> lox.runFile(args[0])
        args.size > 1 -> {
            println("Usage: jlox [script]")
            exitProcess(64)
        }
        else -> lox.runPrompt()
    }
}
