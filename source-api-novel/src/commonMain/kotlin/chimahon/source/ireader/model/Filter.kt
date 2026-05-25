package chimahon.source.ireader.model

@Suppress("unused")
sealed class Filter<V>(val name: String, val initialValue: V) {
    var value = initialValue

    open fun isDefaultValue(): Boolean = initialValue == value
    fun reset() { value = initialValue }
    open fun isValid(): Boolean = true

    class Note(name: String) : Filter<Unit>(name, Unit)
    open class Text(name: String, value: String = "") : Filter<String>(name, value) {
        override fun isValid(): Boolean = value.length <= 200
    }
    open class Check(name: String, val allowsExclusion: Boolean = false, value: Boolean? = null) : Filter<Boolean?>(name, value)
    open class Select(name: String, val options: Array<String>, value: Int = 0) : Filter<Int>(name, value) {
        override fun isValid(): Boolean = value in options.indices
        fun getSelectedOption(): String? = options.getOrNull(value)
    }
    open class Group(name: String, val filters: List<Filter<*>>) : Filter<Unit>(name, Unit)
    open class Sort(name: String, val options: Array<String>, value: Selection? = null) : Filter<Sort.Selection?>(name, value) {
        data class Selection(val index: Int, val ascending: Boolean)
    }

    class Title(name: String = "Title") : Text(name)
    class Author(name: String = "Author") : Text(name)
    class Artist(name: String = "Artist") : Text(name)
    class Genre(name: String, allowsExclusion: Boolean = false) : Check(name, allowsExclusion)
}

typealias FilterList = List<Filter<*>>
