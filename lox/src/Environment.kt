class Environment(private val enclosing: Environment? = null) {
    private val values: HashMap<String, Any?> = HashMap()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw Interpreter.RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance)?.values?.put(name.lexeme, value)
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        if (enclosing != null) {
            return enclosing.get(name)
        }

        throw Interpreter.RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance)?.values?.get(name)
    }

    private fun ancestor(distance: Int): Environment? {
        var environment: Environment? = this
        repeat(distance) {
            environment = environment?.enclosing
        }

        return environment
    }
}
