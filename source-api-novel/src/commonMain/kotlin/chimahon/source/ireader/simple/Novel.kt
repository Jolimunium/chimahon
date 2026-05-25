package chimahon.source.ireader.simple

import chimahon.source.ireader.model.MangaInfo

data class Novel(
    val url: String,
    val title: String,
    val author: String = "",
    val artist: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: NovelStatus = NovelStatus.UNKNOWN,
    val cover: String = "",
    val alternativeTitles: List<String> = emptyList(),
    val rating: Float? = null,
    val views: Long? = null,
    val extras: Map<String, String> = emptyMap()
) {
    fun isValid(): Boolean = url.isNotBlank() && title.isNotBlank()
    fun cleanDescription(): String = description.replace(Regex("\\s+"), " ").trim()

    fun toMangaInfo(): MangaInfo = MangaInfo(
        key = url,
        title = title,
        author = author,
        artist = artist,
        description = description,
        genres = genres,
        status = status.toLegacyStatus(),
        cover = cover
    )

    companion object {
        fun fromMangaInfo(manga: MangaInfo): Novel = Novel(
            url = manga.key,
            title = manga.title,
            author = manga.author,
            artist = manga.artist,
            description = manga.description,
            genres = manga.genres,
            status = NovelStatus.fromLegacy(manga.status),
            cover = manga.cover
        )

        inline fun build(url: String, title: String, block: Builder.() -> Unit = {}): Novel {
            return Builder(url, title).apply(block).build()
        }
    }

    class Builder(private val url: String, private val title: String) {
        var author: String = ""
        var artist: String = ""
        var description: String = ""
        var genres: List<String> = emptyList()
        var status: NovelStatus = NovelStatus.UNKNOWN
        var cover: String = ""
        var alternativeTitles: List<String> = emptyList()
        var rating: Float? = null
        var views: Long? = null
        var extras: Map<String, String> = emptyMap()

        fun build(): Novel = Novel(
            url = url,
            title = title,
            author = author,
            artist = artist,
            description = description,
            genres = genres,
            status = status,
            cover = cover,
            alternativeTitles = alternativeTitles,
            rating = rating,
            views = views,
            extras = extras
        )
    }
}
