package chimahon.source.ireader.model

import kotlinx.serialization.Serializable

@Serializable
data class MangasPageInfo(
    val mangas: List<MangaInfo>,
    val hasNextPage: Boolean
) {
    companion object {
        fun empty(): MangasPageInfo = MangasPageInfo(emptyList(), false)
        fun lastPage(mangas: List<MangaInfo>): MangasPageInfo = MangasPageInfo(mangas, false)
    }

    fun isEmpty(): Boolean = mangas.isEmpty()
    fun isNotEmpty(): Boolean = mangas.isNotEmpty()
    fun size(): Int = mangas.size

    fun filter(predicate: (MangaInfo) -> Boolean): MangasPageInfo {
        return copy(mangas = mangas.filter(predicate))
    }

    fun map(transform: (MangaInfo) -> MangaInfo): MangasPageInfo {
        return copy(mangas = mangas.map(transform))
    }
}
