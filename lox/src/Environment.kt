class Environment(private val values: HashMap<String, Any?> = HashMap()) {

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        throw Interpreter.RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        throw Interpreter.RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }
}
