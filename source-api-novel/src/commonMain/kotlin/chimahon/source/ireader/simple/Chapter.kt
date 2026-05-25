package chimahon.source.ireader.simple

import chimahon.source.ireader.model.ChapterInfo

data class Chapter(
    val url: String,
    val title: String,
    val number: Float = -1f,
    val date: Long = 0L,
    val scanlator: String = "",
    val volume: Int? = null,
    val extras: Map<String, String> = emptyMap()
) {
    fun isValid(): Boolean = url.isNotBlank() && title.isNotBlank()
    fun hasNumber(): Boolean = number >= 0f

    fun withAutoNumber(): Chapter {
        if (number >= 0f) return this
        return copy(number = extractChapterNumber(title))
    }

    fun toChapterInfo(): ChapterInfo = ChapterInfo(
        key = url,
        name = title,
        number = number,
        dateUpload = date,
        scanlator = scanlator
    )

    companion object {
        fun fromChapterInfo(chapter: ChapterInfo): Chapter = Chapter(
            url = chapter.key,
            title = chapter.name,
            number = chapter.number,
            date = chapter.dateUpload,
            scanlator = chapter.scanlator
        )

        fun extractChapterNumber(text: String): Float {
            val patterns = listOf(
                Regex("""(?:chapter|ch\.?|episode|ep\.?)\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
                Regex("""^(\d+(?:\.\d+)?)(?:\s*[-:.]|\s+)"""),
                Regex("""第\s*(\d+(?:\.\d+)?)\s*[章话]"""),
                Regex("""(\d+(?:\.\d+)?)\s*화"""),
                Regex("""(\d+(?:\.\d+)?)\s*話"""),
                Regex("""(\d+(?:\.\d+)?)""")
            )
            for (pattern in patterns) {
                val match = pattern.find(text)
                if (match != null) {
                    return match.groupValues[1].toFloatOrNull() ?: -1f
                }
            }
            return -1f
        }

        inline fun build(url: String, title: String, block: Builder.() -> Unit = {}): Chapter {
            return Builder(url, title).apply(block).build()
        }
    }

    class Builder(private val url: String, private val title: String) {
        var number: Float = -1f
        var date: Long = 0L
        var scanlator: String = ""
        var volume: Int? = null
        var extras: Map<String, String> = emptyMap()

        fun build(): Chapter = Chapter(
            url = url,
            title = title,
            number = number,
            date = date,
            scanlator = scanlator,
            volume = volume,
            extras = extras
        )
    }
}

fun List<Chapter>.sortedByNumber(): List<Chapter> = sortedBy { it.number }
fun List<Chapter>.sortedByNumberDescending(): List<Chapter> = sortedByDescending { it.number }
fun List<Chapter>.sortedByDateDescending(): List<Chapter> = sortedByDescending { it.date }
fun List<Chapter>.withAutoNumbers(): List<Chapter> = map { it.withAutoNumber() }
fun List<Chapter>.distinctByUrl(): List<Chapter> = distinctBy { it.url }
