class LoxFunction(private val declaration: Stmt.Function,
                  private val closure: Environment): LoxCallable {
    override fun arity(): Int = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)

        for (paramToValue in declaration.params zip arguments) {
            environment.define(paramToValue.first.lexeme, paramToValue.second)
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (r: Return) {
            return r.value
        }
        return null
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment)
    }

    override fun toString(): String = "<fun ${declaration.name.lexeme}>"
}