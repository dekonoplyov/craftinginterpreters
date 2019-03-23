package tool

import java.io.IOException
import java.io.PrintWriter
import kotlin.system.exitProcess

fun fieldToKotlinMemberDeclaration(field: String): String {
    val (type, name) = field.split(' ')
    return "val $name: ${if (type == "Object") "Any?" else type}"
}

fun defineType(baseName: String, className: String, fields: String, writer: PrintWriter) {
    writer.println(
        fields
            .split(", ")
            .joinToString(
                prefix = "    class $className(",
                postfix = ") : $baseName() {"
            ) { fieldToKotlinMemberDeclaration(it) }
    )
    writer.println("        override fun<R> accept(visitor: Visitor<R>): R {")
    writer.println("            return visitor.visit$className$baseName(this)")
    writer.println("        }")
    writer.println("    }")
}
fun defineVisitMethod(type: String, baseName: String): String {
    val className = type.split(":")[0].trim()
    return "        fun visit$className$baseName(${baseName.toLowerCase()}: $className): R"
}

fun defineVisitor(baseName: String, types: ArrayList<String>, writer: PrintWriter) {
    writer.println(
        types
            .joinToString(
                prefix = "    interface Visitor<R> {\n",
                postfix = "\n    }",
                separator = "\n"
            ) {defineVisitMethod(it, baseName)}
    )
}

@Throws(IOException::class)
fun defineAst(outputDir: String, baseName: String, types: ArrayList<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.println("abstract class $baseName {")

    defineVisitor(baseName, types, writer)
    writer.println()
    writer.println("    abstract fun<R> accept(visitor: $baseName.Visitor<R>): R")
    writer.println()

    for (type in types) {
        var (className, fields) = type.split(':')
        className = className.trim()
        fields = fields.trim()
        defineType(baseName, className, fields, writer)
        writer.println()
    }

    writer.println("}")
    writer.close()
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: generate_ast <output directory>")
        exitProcess(1)
    }

    val outputDir = args[0]
    defineAst(outputDir, "Expr", arrayListOf(
        "Assign   : Token name, Expr value",
        "Binary   : Expr left, Token operator, Expr right",
        "Call     : Expr callee, Token paren, List<Expr> arguments",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Logical  : Expr left, Token operator, Expr right",
        "Unary    : Token operator, Expr right",
        "Variable : Token name"
    ))

    defineAst(outputDir, "Stmt", arrayListOf(
        "Block      : List<Stmt> statements",
        "Expression : Expr expression",
        "Function   : Token name, List<Token> params, List<Stmt> body",
        "If         : Expr condition, Stmt thenBranch, Stmt? elseBranch",
        "Print      : Expr expression",
        "Var        : Token name, Expr initializer",
        "While      : Expr condition, Stmt body"
    ))
}