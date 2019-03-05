package tool

import java.io.IOException
import java.io.PrintWriter
import kotlin.system.exitProcess

abstract class Expr {
    class Binary(val a: Expr) : Expr() {

    }
}

fun fieldToKotlinMemberDeclaration(field: String): String {
    val (type, name) = field.split(' ')
    return "val $name: ${if (type == "Object") "Any" else type}"
}

fun defineType(baseName: String, className: String, fields: String, writer: PrintWriter) {
    writer.println(
        fields
            .split(", ")
            .joinToString(
                prefix = "    class $className(",
                postfix = ") : $baseName()"
            ) { fieldToKotlinMemberDeclaration(it) }
    )
}

@Throws(IOException::class)
fun defineAst(outputDir: String, baseName: String, rules: ArrayList<String>) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.println("abstract class $baseName {")

    for (rule in rules) {
        var (className, fields) = rule.split(':')
        className = className.trim()
        fields = fields.trim()
        defineType(baseName, className, fields, writer)
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
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Unary    : Token operator, Expr right"
    ))
}