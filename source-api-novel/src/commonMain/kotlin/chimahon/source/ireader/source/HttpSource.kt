package chimahon.source.ireader.source

import chimahon.source.ireader.model.CommandList
import chimahon.source.ireader.model.Listing
import chimahon.source.ireader.model.PageComplete
import chimahon.source.ireader.model.PageUrl
import okhttp3.OkHttpClient

abstract class HttpSource : CatalogSource {
    abstract val baseUrl: String
    open val versionId = 1

    override val id: Long by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        generateSourceId(key)
    }

    open val client: OkHttpClient
        get() = OkHttpClient()

    open val type: Int = TYPE_NOVEL

    override fun toString() = "$name (${lang.uppercase()})"

    open suspend fun getPage(page: PageUrl): PageComplete {
        throw Exception("Incomplete source implementation")
    }

    override fun getListings(): List<Listing> = emptyList()
    override fun getCommands(): CommandList = emptyList()

    protected fun getAbsoluteUrl(path: String): String {
        return when {
            path.startsWith("http://") || path.startsWith("https://") -> path
            path.startsWith("//") -> "https:$path"
            path.startsWith("/") -> "$baseUrl$path"
            else -> "$baseUrl/$path"
        }
    }

    companion object {
        const val TYPE_NOVEL = 0
        const val TYPE_MANGA = 1
        const val TYPE_MOVIE = 2

        fun generateSourceId(key: String): Long {
            var hash = 0L
            for (char in key) {
                hash = 31 * hash + char.code
            }
            return hash and Long.MAX_VALUE
        }
    }
}
