package chimahon.source.ireader.source

import chimahon.source.ireader.model.CommandList
import chimahon.source.ireader.model.FilterList
import chimahon.source.ireader.model.Listing
import chimahon.source.ireader.model.MangasPageInfo

interface CatalogSource : Source {
    companion object {
        const val TYPE_NOVEL = 0
        const val TYPE_MANGA = 1
        const val TYPE_MOVIE = 2

        fun getTypeName(type: Int): String {
            return when (type) {
                TYPE_NOVEL -> "Novel"
                TYPE_MANGA -> "Manga"
                TYPE_MOVIE -> "Movie"
                else -> "Unknown"
            }
        }
    }

    override val lang: String

    suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo
    suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo

    fun getListings(): List<Listing>
    fun getFilters(): FilterList
    fun getCommands(): CommandList

    fun supportsSearch(): Boolean = getFilters().isNotEmpty()
    fun supportsLatest(): Boolean = getListings().isNotEmpty()
    fun hasFilters(): Boolean = getFilters().isNotEmpty()
    fun hasCommands(): Boolean = getCommands().isNotEmpty()

    fun getCapabilities(): SourceCapabilities = SourceCapabilities()
}

data class SourceCapabilities(
    val supportsLatest: Boolean = true,
    val supportsSearch: Boolean = true,
    val supportsFilters: Boolean = true,
    val supportsDeepLinks: Boolean = false,
    val supportsCommands: Boolean = false,
    val type: Int = CatalogSource.TYPE_NOVEL
)
