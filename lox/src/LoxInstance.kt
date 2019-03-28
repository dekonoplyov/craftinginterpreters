class LoxInstance(val klass: LoxClass) {
    private val fields = HashMap<String, Any?>()

    override fun toString(): String {
        return "$klass instance"
    }

    fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        throw Interpreter.RuntimeError(name, "Undefined property ${name.lexeme}.")
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }
}