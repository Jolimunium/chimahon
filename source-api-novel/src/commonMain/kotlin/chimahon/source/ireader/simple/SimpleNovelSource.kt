package chimahon.source.ireader.simple

import chimahon.source.ireader.model.ChapterInfo
import chimahon.source.ireader.model.Command
import chimahon.source.ireader.model.CommandList
import chimahon.source.ireader.model.Filter
import chimahon.source.ireader.model.FilterList
import chimahon.source.ireader.model.Listing
import chimahon.source.ireader.model.MangaInfo
import chimahon.source.ireader.model.MangasPageInfo
import chimahon.source.ireader.model.Page
import chimahon.source.ireader.model.Text
import chimahon.source.ireader.source.CatalogSource
import chimahon.source.ireader.source.HttpSource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class SimpleNovelSource : CatalogSource {
    abstract override val name: String
    abstract val baseUrl: String
    abstract val language: String

    open val versionId: Int = 1
    open val hasCloudflare: Boolean = false
    open val rateLimit: Double = 2.0

    override val lang: String get() = language

    override val id: Long by lazy {
        val key = "${name.lowercase()}/$language/$versionId"
        HttpSource.generateSourceId(key)
    }

    protected open val httpClient: OkHttpClient
        get() = OkHttpClient()

    abstract suspend fun searchNovels(query: String, page: Int): NovelListResult
    abstract suspend fun getNovelDetails(novel: Novel): Novel
    abstract suspend fun getChapters(novel: Novel): List<Chapter>
    abstract suspend fun getChapterContent(chapter: Chapter): List<String>

    open suspend fun getPopularNovels(page: Int): NovelListResult = NovelListResult.empty()
    open suspend fun getLatestNovels(page: Int): NovelListResult = NovelListResult.empty()
    open fun getSearchFilters(): List<Filter<*>> = emptyList()

    protected suspend fun fetchDocument(url: String): Document {
        val absoluteUrl = absoluteUrl(url)
        val request = Request.Builder()
            .url(absoluteUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()
        val response = httpClient.newCall(request).execute()
        return Jsoup.parse(response.body?.string() ?: "")
    }

    protected fun absoluteUrl(path: String): String {
        return when {
            path.startsWith("http://") || path.startsWith("https://") -> path
            path.startsWith("//") -> "https:$path"
            path.startsWith("/") -> "$baseUrl$path"
            else -> "$baseUrl/$path"
        }
    }

    protected fun parseNovelList(
        doc: Document,
        itemSelector: String,
        nextPageSelector: String? = null,
        parser: (Element) -> Novel
    ): NovelListResult {
        val novels = doc.select(itemSelector).mapNotNull { element ->
            try { parser(element) } catch (e: Exception) { null }
        }
        val hasNext = nextPageSelector?.let { doc.selectFirst(it) != null } ?: false
        return NovelListResult(novels, hasNext)
    }

    protected fun parseStatus(text: String): NovelStatus = NovelStatus.parse(text)

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        val result = when (sort) {
            is PopularListing -> getPopularNovels(page)
            is LatestListing -> getLatestNovels(page)
            else -> getPopularNovels(page)
        }
        return result.toMangasPageInfo()
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val query = filters.filterIsInstance<Filter.Title>()
            .firstOrNull()?.value ?: ""
        val result = searchNovels(query, page)
        return result.toMangasPageInfo()
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        val novel = Novel.fromMangaInfo(manga)
        val details = getNovelDetails(novel)
        return details.toMangaInfo()
    }

    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        val novel = Novel.fromMangaInfo(manga)
        val chapters = getChapters(novel)
        return chapters.map { it.toChapterInfo() }
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        val chapterObj = Chapter.fromChapterInfo(chapter)
        val content = getChapterContent(chapterObj)
        return content.map { Text(it) }
    }

    override fun getListings(): List<Listing> = listOf(PopularListing(), LatestListing())
    override fun getFilters(): FilterList = getSearchFilters()
    override fun getCommands(): CommandList = emptyList()
}

class PopularListing : Listing("Popular")
class LatestListing : Listing("Latest")
