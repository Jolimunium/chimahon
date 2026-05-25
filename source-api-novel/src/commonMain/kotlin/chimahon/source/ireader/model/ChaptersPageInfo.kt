package chimahon.source.ireader.model

import kotlinx.serialization.Serializable

@Serializable
data class ChaptersPageInfo(
    val chapters: List<ChapterInfo>,
    val hasNextPage: Boolean
) {
    companion object {
        fun empty(): ChaptersPageInfo = ChaptersPageInfo(emptyList(), false)
        fun singlePage(chapters: List<ChapterInfo>): ChaptersPageInfo = ChaptersPageInfo(chapters, false)
        fun lastPage(chapters: List<ChapterInfo>): ChaptersPageInfo = ChaptersPageInfo(chapters, false)
    }

    fun isEmpty(): Boolean = chapters.isEmpty()
    fun isNotEmpty(): Boolean = chapters.isNotEmpty()
    fun size(): Int = chapters.size
}
