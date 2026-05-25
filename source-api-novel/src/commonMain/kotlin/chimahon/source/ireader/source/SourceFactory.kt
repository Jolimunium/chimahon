package chimahon.source.ireader.source

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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class SourceFactory : HttpSource() {
    open val detailFetcher: Detail = Detail()
    open val chapterFetcher: Chapters = Chapters()
    open val contentFetcher: Content = Content()
    open val exploreFetchers: List<BaseExploreFetcher> = listOf()

    class LatestListing() : Listing(name = "Latest")
    class FetcherListing(val key: String, name: String) : Listing(name)

    open fun getCustomBaseUrl(): String = baseUrl

    override fun getListings(): List<Listing> {
        val nonSearchFetchers = exploreFetchers.filter { it.type != Type.Search }
        return if (nonSearchFetchers.isNotEmpty()) {
            nonSearchFetchers.map { fetcher ->
                FetcherListing(
                    key = fetcher.key,
                    name = fetcher.key.replaceFirstChar { it.uppercase() }
                )
            }
        } else {
            listOf(LatestListing())
        }
    }

    open fun bookListParse(
        document: Document,
        elementSelector: String,
        baseExploreFetcher: BaseExploreFetcher,
        parser: (element: Element) -> MangaInfo,
        page: Int,
    ): MangasPageInfo {
        val books = document.select(elementSelector).mapNotNull { element ->
            runCatching { parser(element) }
                .getOrNull()
                ?.takeIf { it.isValid() }
        }
        val hasNextPage = determineHasNextPage(document, baseExploreFetcher, page)
        return MangasPageInfo(books, hasNextPage)
    }

    private fun determineHasNextPage(
        document: Document,
        fetcher: BaseExploreFetcher,
        page: Int
    ): Boolean = when {
        fetcher.infinitePage -> true
        fetcher.maxPage != -1 -> page < fetcher.maxPage
        else -> {
            val nextPageText = selectorReturnerStringType(
                document,
                fetcher.nextPageSelector,
                fetcher.nextPageAtt
            ).trim()
            fetcher.nextPageValue?.let { it == nextPageText } ?: nextPageText.isNotBlank()
        }
    }

    open fun getUserAgent(): String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"

    open fun requestBuilder(url: String): Request {
        return Request.Builder()
            .url(url)
            .header("User-Agent", getUserAgent())
            .header("Cache-Control", "max-age=0")
            .build()
    }

    open val page = "{page}"
    open val query = "{query}"

    open suspend fun getListRequest(
        baseExploreFetcher: BaseExploreFetcher,
        page: Int,
        query: String = "",
    ): Document {
        val url = buildFetcherUrl(baseExploreFetcher, page, query)
        val response = client.newCall(requestBuilder(url)).execute()
        return Jsoup.parse(response.body?.string() ?: "")
    }

    protected fun buildFetcherUrl(
        fetcher: BaseExploreFetcher,
        page: Int,
        query: String = ""
    ): String {
        val endpoint = fetcher.endpoint.orEmpty()
        val processedQuery = fetcher.onQuery(query)
        val processedPage = fetcher.onPage(page.toString())
        return buildString {
            append(getCustomBaseUrl())
            append(endpoint
                .replace(this@SourceFactory.page, processedPage)
                .replace(this@SourceFactory.query, processedQuery)
            )
        }
    }

    open suspend fun getLists(
        baseExploreFetcher: BaseExploreFetcher,
        page: Int,
        query: String = "",
        filters: FilterList,
    ): MangasPageInfo {
        val selector = baseExploreFetcher.selector
            ?: return MangasPageInfo(emptyList(), false)
        val document = getListRequest(baseExploreFetcher, page, query)
        return bookListParse(
            document = document,
            elementSelector = selector,
            page = page,
            baseExploreFetcher = baseExploreFetcher,
            parser = { element -> parseMangaFromElement(element, baseExploreFetcher) }
        )
    }

    protected open fun parseMangaFromElement(
        element: Element,
        fetcher: BaseExploreFetcher
    ): MangaInfo {
        val title = selectorReturnerStringType(element, fetcher.nameSelector, fetcher.nameAtt)
            .trim()
            .let { fetcher.onName(it, fetcher.key) }
        val url = selectorReturnerStringType(element, fetcher.linkSelector, fetcher.linkAtt)
            .trim()
            .let { fetcher.onLink(it, fetcher.key) }
            .let { if (fetcher.addBaseUrlToLink) buildAbsoluteUrl(it) else it }
        val cover = selectorReturnerStringType(element, fetcher.coverSelector, fetcher.coverAtt)
            .trim()
            .let { fetcher.onCover(it, fetcher.key) }
            .let { if (fetcher.addBaseurlToCoverLink) buildAbsoluteUrl(it) else it }
        return MangaInfo(key = url, title = title, cover = cover)
    }

    protected fun buildAbsoluteUrl(path: String): String {
        return when {
            path.startsWith("http://") || path.startsWith("https://") -> path
            path.startsWith("//") -> "https:$path"
            path.startsWith("/") -> "$baseUrl$path"
            else -> "$baseUrl/$path"
        }
    }

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        val nonSearchFetchers = exploreFetchers.filter { it.type != Type.Search }
        val fetcher = when (sort) {
            is FetcherListing -> nonSearchFetchers.find { it.key == sort.key }
            else -> nonSearchFetchers.firstOrNull()
        } ?: return emptyMangaPage()
        return getLists(fetcher, page, "", emptyList())
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val titleFilter = filters.filterIsInstance<Filter.Title>().firstOrNull()
        titleFilter?.value?.takeIf { it.isNotBlank() }?.let { query ->
            val searchFetcher = exploreFetchers.firstOrNull { it.type == Type.Search }
                ?: return emptyMangaPage()
            return getLists(searchFetcher, page, query, filters)
        }
        return emptyMangaPage()
    }

    protected fun emptyMangaPage(): MangasPageInfo = MangasPageInfo(emptyList(), false)

    open fun chapterFromElement(element: Element): ChapterInfo {
        val link = selectorReturnerStringType(element, chapterFetcher.linkSelector, chapterFetcher.linkAtt)
            .trim()
            .let { chapterFetcher.onLink(it) }
            .let { if (chapterFetcher.addBaseUrlToLink) buildAbsoluteUrl(it) else it }
        val name = selectorReturnerStringType(element, chapterFetcher.nameSelector, chapterFetcher.nameAtt)
            .trim()
            .let { chapterFetcher.onName(it) }
        val translator = selectorReturnerStringType(element, chapterFetcher.translatorSelector, chapterFetcher.translatorAtt)
            .trim()
            .let { chapterFetcher.onTranslator(it) }
        val releaseDate = selectorReturnerStringType(element, chapterFetcher.uploadDateSelector, chapterFetcher.uploadDateAtt)
            .trim()
            .let { chapterFetcher.uploadDateParser(it) }
        val number = selectorReturnerStringType(element, chapterFetcher.numberSelector, chapterFetcher.numberAtt)
            .trim()
            .let { chapterFetcher.onNumber(it) }
            .toFloatOrNull() ?: ChapterInfo.extractChapterNumber(name)
        return ChapterInfo(
            name = name,
            key = link,
            number = number,
            dateUpload = releaseDate,
            scanlator = translator
        )
    }

    open fun chaptersParse(document: Document): List<ChapterInfo> {
        val selector = chapterFetcher.selector ?: return emptyList()
        return document.select(selector).mapNotNull { element ->
            runCatching { chapterFromElement(element) }
                .getOrNull()
                ?.takeIf { it.isValid() }
        }
    }

    open suspend fun getChapterListRequest(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): Document {
        val response = client.newCall(requestBuilder(manga.key)).execute()
        return Jsoup.parse(response.body?.string() ?: "")
    }

    override suspend fun getChapterList(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): List<ChapterInfo> {
        commands.filterIsInstance<Command.Chapter.Fetch>().firstOrNull()?.let { cmd ->
            val chapters = chaptersParse(Jsoup.parse(cmd.html))
            return applyChapterSorting(chapters)
        }
        val document = getChapterListRequest(manga, commands)
        val chapters = chaptersParse(document)
        return applyChapterSorting(chapters)
    }

    protected open fun applyChapterSorting(chapters: List<ChapterInfo>): List<ChapterInfo> {
        return if (chapterFetcher.reverseChapterList) chapters else chapters.reversed()
    }

    open fun statusParser(text: String): Long = detailFetcher.onStatus(text)

    open fun detailParse(document: Document): MangaInfo {
        val title = selectorReturnerStringType(document, detailFetcher.nameSelector, detailFetcher.nameAtt)
            .trim()
            .let { detailFetcher.onName(it) }
        val cover = selectorReturnerStringType(document, detailFetcher.coverSelector, detailFetcher.coverAtt)
            .trim()
            .let { detailFetcher.onCover(it) }
            .let { if (detailFetcher.addBaseurlToCoverLink) buildAbsoluteUrl(it) else it }
        val author = selectorReturnerStringType(document, detailFetcher.authorBookSelector, detailFetcher.authorBookAtt)
            .trim()
            .let { detailFetcher.onAuthor(it) }
        val status = selectorReturnerStringType(document, detailFetcher.statusSelector, detailFetcher.statusAtt)
            .trim()
            .let { statusParser(it) }
        val description = selectorReturnerListType(document, detailFetcher.descriptionSelector, detailFetcher.descriptionBookAtt)
            .let { detailFetcher.onDescription(it) }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
        val genres = selectorReturnerListType(document, detailFetcher.categorySelector, detailFetcher.categoryAtt)
            .let { detailFetcher.onCategory(it) }
            .filter { it.isNotBlank() }
        return MangaInfo(
            key = "",
            title = title,
            cover = cover,
            description = description,
            author = author,
            genres = genres,
            status = status
        )
    }

    open suspend fun getMangaDetailsRequest(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): Document {
        val response = client.newCall(requestBuilder(manga.key)).execute()
        return Jsoup.parse(response.body?.string() ?: "")
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        commands.filterIsInstance<Command.Detail.Fetch>().firstOrNull()?.let { cmd ->
            return detailParse(Jsoup.parse(cmd.html)).copy(key = cmd.url)
        }
        val document = getMangaDetailsRequest(manga, commands)
        return detailParse(document).copy(key = manga.key)
    }

    open suspend fun getContentRequest(chapter: ChapterInfo, commands: List<Command<*>>): Document {
        val response = client.newCall(requestBuilder(chapter.key)).execute()
        return Jsoup.parse(response.body?.string() ?: "")
    }

    open suspend fun getContents(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return pageContentParse(getContentRequest(chapter, commands))
    }

    open fun pageContentParse(document: Document): List<Page> {
        val content = selectorReturnerListType(document, contentFetcher.pageContentSelector, contentFetcher.pageContentAtt)
            .let { contentFetcher.onContent(it) }
            .filter { it.isNotBlank() }
        val title = selectorReturnerStringType(document, contentFetcher.pageTitleSelector, contentFetcher.pageTitleAtt)
            .trim()
            .let { contentFetcher.onTitle(it) }
        return buildList {
            if (title.isNotBlank()) add(title.toPage())
            addAll(content.map { it.toPage() })
        }
    }

    open fun String.toPage(): Page = Text(this)
    open fun List<String>.toPage(): List<Page> = map { it.toPage() }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        commands.filterIsInstance<Command.Content.Fetch>().firstOrNull()?.let { cmd ->
            return pageContentParse(Jsoup.parse(cmd.html))
        }
        return getContents(chapter, commands)
    }

    data class BaseExploreFetcher(
        val key: String,
        val endpoint: String? = null,
        val selector: String? = null,
        val addBaseUrlToLink: Boolean = false,
        val nextPageSelector: String? = null,
        val nextPageAtt: String? = null,
        val nextPageValue: String? = null,
        val addBaseurlToCoverLink: Boolean = false,
        val linkSelector: String? = null,
        val linkAtt: String? = null,
        val onLink: (url: String, key: String) -> String = { url, _ -> url },
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val onName: (String, key: String) -> String = { url, _ -> url },
        val coverSelector: String? = null,
        val coverAtt: String? = null,
        val onCover: (String, key: String) -> String = { url, _ -> url },
        val onQuery: (query: String) -> String = { query -> query },
        val onPage: (page: String) -> String = { page -> page },
        val infinitePage: Boolean = false,
        val maxPage: Int = -1,
        val type: Type = Type.Others,
    )

    data class Detail(
        val addBaseurlToCoverLink: Boolean = false,
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val onName: (String) -> String = { it },
        val coverSelector: String? = null,
        val coverAtt: String? = null,
        val onCover: (String) -> String = { it },
        val descriptionSelector: String? = null,
        val descriptionBookAtt: String? = null,
        val onDescription: (List<String>) -> List<String> = { it },
        val authorBookSelector: String? = null,
        val authorBookAtt: String? = null,
        val onAuthor: (String) -> String = { it },
        val categorySelector: String? = null,
        val categoryAtt: String? = null,
        val onCategory: (List<String>) -> List<String> = { it },
        val statusSelector: String? = null,
        val statusAtt: String? = null,
        val onStatus: (String) -> Long = { MangaInfo.UNKNOWN },
        val type: Type = Type.Detail,
    )

    data class Chapters(
        val selector: String? = null,
        val addBaseUrlToLink: Boolean = false,
        val reverseChapterList: Boolean = false,
        val linkSelector: String? = null,
        val onLink: ((String) -> String) = { it },
        val linkAtt: String? = null,
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val onName: ((String) -> String) = { it },
        val numberSelector: String? = null,
        val numberAtt: String? = null,
        val onNumber: ((String) -> String) = { it },
        val uploadDateSelector: String? = null,
        val uploadDateAtt: String? = null,
        val uploadDateParser: (String) -> Long = { 0L },
        val translatorSelector: String? = null,
        val translatorAtt: String? = null,
        val onTranslator: ((String) -> String) = { it },
        val type: Type = Type.Chapters,
    )

    data class Content(
        val pageTitleSelector: String? = null,
        val pageTitleAtt: String? = null,
        val onTitle: (String) -> String = { it },
        val pageContentSelector: String? = null,
        val pageContentAtt: String? = null,
        val onContent: (List<String>) -> List<String> = { it },
        val type: Type = Type.Content,
    )

    enum class Type {
        Search,
        Detail,
        Chapters,
        Content,
        Others
    }

    open fun selectorReturnerStringType(
        document: Document,
        selector: String? = null,
        att: String? = null,
    ): String {
        return try {
            when {
                selector.isNullOrBlank() && !att.isNullOrBlank() -> document.attr(att)
                !selector.isNullOrBlank() && att.isNullOrBlank() -> document.select(selector).text()
                !selector.isNullOrBlank() && !att.isNullOrBlank() -> document.select(selector).attr(att)
                else -> ""
            }
        } catch (e: Exception) { "" }
    }

    open fun selectorReturnerStringType(
        element: Element,
        selector: String? = null,
        att: String? = null,
    ): String {
        return try {
            when {
                selector.isNullOrBlank() && !att.isNullOrBlank() -> element.attr(att)
                !selector.isNullOrBlank() && att.isNullOrBlank() -> element.select(selector).text()
                !selector.isNullOrBlank() && !att.isNullOrBlank() -> element.select(selector).attr(att)
                else -> ""
            }
        } catch (e: Exception) { "" }
    }

    open fun selectorReturnerListType(
        element: Element,
        selector: String? = null,
        att: String? = null,
    ): List<String> {
        return try {
            when {
                selector.isNullOrBlank() && !att.isNullOrBlank() -> {
                    val value = element.attr(att)
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                !selector.isNullOrBlank() && att.isNullOrBlank() -> {
                    element.select(selector).eachText().filter { it.isNotBlank() }
                }
                !selector.isNullOrBlank() && !att.isNullOrBlank() -> {
                    val value = element.select(selector).attr(att)
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                else -> emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }

    open fun selectorReturnerListType(
        document: Document,
        selector: String? = null,
        att: String? = null,
    ): List<String> {
        return try {
            when {
                selector.isNullOrBlank() && !att.isNullOrBlank() -> {
                    val value = document.attr(att)
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                !selector.isNullOrBlank() && att.isNullOrBlank() -> {
                    document.select(selector).mapNotNull {
                        val text = it.text()
                        if (text.isNotBlank()) text else null
                    }
                }
                !selector.isNullOrBlank() && !att.isNullOrBlank() -> {
                    val value = document.select(selector).attr(att)
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                else -> emptyList()
            }
        } catch (e: Exception) { emptyList() }
    }
}

fun exploreFetchers(block: ExploreFetchersBuilder.() -> Unit): List<SourceFactory.BaseExploreFetcher> {
    return ExploreFetchersBuilder().apply(block).build()
}

class ExploreFetchersBuilder {
    private val fetchers = mutableListOf<SourceFactory.BaseExploreFetcher>()

    fun fetcher(key: String, block: ExploreFetcherBuilder.() -> Unit) {
        fetchers.add(ExploreFetcherBuilder(key).apply(block).build())
    }

    fun search(block: ExploreFetcherBuilder.() -> Unit) {
        fetchers.add(ExploreFetcherBuilder("search").apply {
            block()
            asSearch()
        }.build())
    }

    fun popular(block: ExploreFetcherBuilder.() -> Unit) {
        fetchers.add(ExploreFetcherBuilder("popular").apply(block).build())
    }

    fun latest(block: ExploreFetcherBuilder.() -> Unit) {
        fetchers.add(ExploreFetcherBuilder("latest").apply(block).build())
    }

    fun build(): List<SourceFactory.BaseExploreFetcher> = fetchers.toList()
}

class ExploreFetcherBuilder(private val key: String) {
    var endpoint: String? = null
    var selector: String? = null
    var addBaseUrlToLink: Boolean = false
    var nextPageSelector: String? = null
    var nextPageAtt: String? = null
    var nextPageValue: String? = null
    var addBaseurlToCoverLink: Boolean = false
    var linkSelector: String? = null
    var linkAtt: String? = null
    var nameSelector: String? = null
    var nameAtt: String? = null
    var coverSelector: String? = null
    var coverAtt: String? = null
    var infinitePage: Boolean = false
    var maxPage: Int = -1
    var type: SourceFactory.Type = SourceFactory.Type.Others

    private var onLinkFn: (String, String) -> String = { url, _ -> url }
    private var onNameFn: (String, String) -> String = { name, _ -> name }
    private var onCoverFn: (String, String) -> String = { cover, _ -> cover }
    private var onQueryFn: (String) -> String = { it }
    private var onPageFn: (String) -> String = { it }

    fun onLink(block: (url: String, key: String) -> String) { onLinkFn = block }
    fun onName(block: (name: String, key: String) -> String) { onNameFn = block }
    fun onCover(block: (cover: String, key: String) -> String) { onCoverFn = block }
    fun onQuery(block: (String) -> String) { onQueryFn = block }
    fun onPage(block: (String) -> String) { onPageFn = block }

    fun asSearch() { type = SourceFactory.Type.Search }
    fun pagination(maxPages: Int) { maxPage = maxPages }
    fun infinitePagination() { infinitePage = true }

    fun build() = SourceFactory.BaseExploreFetcher(
        key = key,
        endpoint = endpoint,
        selector = selector,
        addBaseUrlToLink = addBaseUrlToLink,
        nextPageSelector = nextPageSelector,
        nextPageAtt = nextPageAtt,
        nextPageValue = nextPageValue,
        addBaseurlToCoverLink = addBaseurlToCoverLink,
        linkSelector = linkSelector,
        linkAtt = linkAtt,
        onLink = onLinkFn,
        nameSelector = nameSelector,
        nameAtt = nameAtt,
        onName = onNameFn,
        coverSelector = coverSelector,
        coverAtt = coverAtt,
        onCover = onCoverFn,
        onQuery = onQueryFn,
        onPage = onPageFn,
        infinitePage = infinitePage,
        maxPage = maxPage,
        type = type
    )
}

// ==================== DSL Builders ====================

fun detail(block: DetailBuilder.() -> Unit): SourceFactory.Detail {
    return DetailBuilder().apply(block).build()
}

class DetailBuilder {
    var addBaseurlToCoverLink: Boolean = false
    var nameSelector: String? = null
    var nameAtt: String? = null
    var coverSelector: String? = null
    var coverAtt: String? = null
    var descriptionSelector: String? = null
    var descriptionBookAtt: String? = null
    var authorBookSelector: String? = null
    var authorBookAtt: String? = null
    var categorySelector: String? = null
    var categoryAtt: String? = null
    var statusSelector: String? = null
    var statusAtt: String? = null

    private var onNameFn: (String) -> String = { it }
    private var onCoverFn: (String) -> String = { it }
    private var onDescriptionFn: (List<String>) -> List<String> = { it }
    private var onAuthorFn: (String) -> String = { it }
    private var onCategoryFn: (List<String>) -> List<String> = { it }
    private var onStatusFn: (String) -> Long = { MangaInfo.UNKNOWN }

    fun onName(block: (String) -> String) { onNameFn = block }
    fun onCover(block: (String) -> String) { onCoverFn = block }
    fun onDescription(block: (List<String>) -> List<String>) { onDescriptionFn = block }
    fun onAuthor(block: (String) -> String) { onAuthorFn = block }
    fun onCategory(block: (List<String>) -> List<String>) { onCategoryFn = block }
    fun onStatus(block: (String) -> Long) { onStatusFn = block }

    fun build() = SourceFactory.Detail(
        addBaseurlToCoverLink = addBaseurlToCoverLink,
        nameSelector = nameSelector,
        nameAtt = nameAtt,
        onName = onNameFn,
        coverSelector = coverSelector,
        coverAtt = coverAtt,
        onCover = onCoverFn,
        descriptionSelector = descriptionSelector,
        descriptionBookAtt = descriptionBookAtt,
        onDescription = onDescriptionFn,
        authorBookSelector = authorBookSelector,
        authorBookAtt = authorBookAtt,
        onAuthor = onAuthorFn,
        categorySelector = categorySelector,
        categoryAtt = categoryAtt,
        onCategory = onCategoryFn,
        statusSelector = statusSelector,
        statusAtt = statusAtt,
        onStatus = onStatusFn
    )
}

fun chapters(block: ChaptersBuilder.() -> Unit): SourceFactory.Chapters {
    return ChaptersBuilder().apply(block).build()
}

class ChaptersBuilder {
    var selector: String? = null
    var addBaseUrlToLink: Boolean = false
    var reverseChapterList: Boolean = false
    var linkSelector: String? = null
    var linkAtt: String? = null
    var nameSelector: String? = null
    var nameAtt: String? = null
    var numberSelector: String? = null
    var numberAtt: String? = null
    var uploadDateSelector: String? = null
    var uploadDateAtt: String? = null
    var translatorSelector: String? = null
    var translatorAtt: String? = null

    private var onLinkFn: (String) -> String = { it }
    private var onNameFn: (String) -> String = { it }
    private var onNumberFn: (String) -> String = { it }
    private var uploadDateParserFn: (String) -> Long = { 0L }
    private var onTranslatorFn: (String) -> String = { it }

    fun onLink(block: (String) -> String) { onLinkFn = block }
    fun onName(block: (String) -> String) { onNameFn = block }
    fun onNumber(block: (String) -> String) { onNumberFn = block }
    fun uploadDateParser(block: (String) -> Long) { uploadDateParserFn = block }
    fun onTranslator(block: (String) -> String) { onTranslatorFn = block }

    fun build() = SourceFactory.Chapters(
        selector = selector,
        addBaseUrlToLink = addBaseUrlToLink,
        reverseChapterList = reverseChapterList,
        linkSelector = linkSelector,
        linkAtt = linkAtt,
        onLink = onLinkFn,
        nameSelector = nameSelector,
        nameAtt = nameAtt,
        onName = onNameFn,
        numberSelector = numberSelector,
        numberAtt = numberAtt,
        onNumber = onNumberFn,
        uploadDateSelector = uploadDateSelector,
        uploadDateAtt = uploadDateAtt,
        uploadDateParser = uploadDateParserFn,
        translatorSelector = translatorSelector,
        translatorAtt = translatorAtt,
        onTranslator = onTranslatorFn
    )
}

fun content(block: ContentBuilder.() -> Unit): SourceFactory.Content {
    return ContentBuilder().apply(block).build()
}

class ContentBuilder {
    var pageTitleSelector: String? = null
    var pageTitleAtt: String? = null
    var pageContentSelector: String? = null
    var pageContentAtt: String? = null

    private var onTitleFn: (String) -> String = { it }
    private var onContentFn: (List<String>) -> List<String> = { it }

    fun onTitle(block: (String) -> String) { onTitleFn = block }
    fun onContent(block: (List<String>) -> List<String>) { onContentFn = block }

    fun build() = SourceFactory.Content(
        pageTitleSelector = pageTitleSelector,
        pageTitleAtt = pageTitleAtt,
        onTitle = onTitleFn,
        pageContentSelector = pageContentSelector,
        pageContentAtt = pageContentAtt,
        onContent = onContentFn
    )
}
