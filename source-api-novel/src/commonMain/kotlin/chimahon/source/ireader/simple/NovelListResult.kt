package chimahon.source.ireader.simple

data class NovelListResult(
    val novels: List<Novel>,
    val hasNextPage: Boolean
) {
    fun isEmpty(): Boolean = novels.isEmpty()
    fun isNotEmpty(): Boolean = novels.isNotEmpty()
    val size: Int get() = novels.size

    fun toMangasPageInfo(): chimahon.source.ireader.model.MangasPageInfo {
        return chimahon.source.ireader.model.MangasPageInfo(
            mangas = novels.map { it.toMangaInfo() },
            hasNextPage = hasNextPage
        )
    }

    companion object {
        fun empty(): NovelListResult = NovelListResult(emptyList(), false)
        fun lastPage(novels: List<Novel>): NovelListResult = NovelListResult(novels, false)
    }
}
