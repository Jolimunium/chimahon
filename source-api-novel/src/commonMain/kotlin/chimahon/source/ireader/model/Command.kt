package chimahon.source.ireader.model

sealed class Command<V>(val name: String, val initialValue: V) {
    var value = initialValue
    open fun isDefaultValue(): Boolean = value == initialValue
    fun reset() { value = initialValue }
    open fun getValueDescription(): String = value.toString()

    open class Fetchers(open val url: String = "", open val html: String = "") : Command<String>(url, html) {
        fun hasUrl(): Boolean = url.isNotBlank()
        fun hasHtml(): Boolean = html.isNotBlank()
        fun isValid(): Boolean = hasUrl() || hasHtml()
    }

    open class Note(name: String) : Command<Unit>(name, Unit)
    open class Text(name: String, value: String = "", val hint: String = "") : Command<String>(name, value)
    open class Select(name: String, open val options: Array<String>, value: Int = 0, open val description: String = "") : Command<Int>(name, value) {
        override fun getValueDescription(): String = options.getOrNull(value) ?: "Unknown"
    }
    open class Toggle(name: String, value: Boolean = false, open val description: String = "") : Command<Boolean>(name, value)
    open class Range(name: String, value: Int = 0, val min: Int = 0, val max: Int = 100, val step: Int = 1) : Command<Int>(name, value)

    object Detail {
        open class Fetch(override val url: String = "", override val html: String = "") : Fetchers(url, html)
    }

    object Content {
        open class Fetch(override val url: String = "", override val html: String = "") : Fetchers(url, html)
    }

    object Chapter {
        class Note(name: String) : Command.Note(name)
        open class Text(name: String, value: String = "") : Command.Text(name, value)
        open class Select(name: String, override val options: Array<String>, value: Int = 0) : Command.Select(name, options, value)
        open class Fetch(override val url: String = "", override val html: String = "") : Fetchers(url, html)
    }

    object Explore {
        open class Fetch(override val url: String = "", override val html: String = "") : Fetchers(url, html)
    }
}

typealias CommandList = List<Command<*>>

inline fun <reified T> List<Command<*>>.findInstance(): T? = filterIsInstance<T>().firstOrNull()
