class LoxClass(val name: String, private val methods: Map<String, LoxFunction>) : LoxCallable {
    override fun arity() = 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return LoxInstance(this)
    }

    fun findMethod(name: String): LoxFunction? {
        return methods[name]
    }

    override fun toString(): String {
        return name
    }
}