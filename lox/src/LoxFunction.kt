class LoxFunction(private val declaration: Stmt.Function): LoxCallable {
    override fun arity(): Int = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(interpreter.globals)

        for (paramToValue in declaration.params zip arguments) {
            environment.define(paramToValue.first.lexeme, paramToValue.second)
        }

        interpreter.executeBlock(declaration.body, environment)
        return null
    }

    override fun toString(): String = "<fun ${declaration.name.lexeme}>"
}