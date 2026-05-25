package chimahon.source.ireader.simple

import chimahon.source.ireader.model.MangaInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NovelTest {

    @Test
    fun `isValid returns true when url and title are present`() {
        val novel = Novel(url = "https://example.com/novel", title = "Test Novel")
        assertTrue(novel.isValid())
    }

    @Test
    fun `isValid returns false when url is blank`() {
        val novel = Novel(url = "", title = "Test Novel")
        assertFalse(novel.isValid())
    }

    @Test
    fun `isValid returns false when title is blank`() {
        val novel = Novel(url = "https://example.com/novel", title = "")
        assertFalse(novel.isValid())
    }

    @Test
    fun `toMangaInfo converts correctly`() {
        val novel = Novel(
            url = "https://example.com/novel",
            title = "Test Novel",
            author = "Test Author",
            description = "Test Description",
            genres = listOf("Fantasy", "Adventure"),
            status = NovelStatus.ONGOING,
            cover = "https://example.com/cover.jpg"
        )

        val mangaInfo = novel.toMangaInfo()

        assertEquals("https://example.com/novel", mangaInfo.key)
        assertEquals("Test Novel", mangaInfo.title)
        assertEquals("Test Author", mangaInfo.author)
        assertEquals("Test Description", mangaInfo.description)
        assertEquals(listOf("Fantasy", "Adventure"), mangaInfo.genres)
        assertEquals(MangaInfo.ONGOING, mangaInfo.status)
        assertEquals("https://example.com/cover.jpg", mangaInfo.cover)
    }

    @Test
    fun `fromMangaInfo converts correctly`() {
        val mangaInfo = MangaInfo(
            key = "https://example.com/novel",
            title = "Test Novel",
            author = "Test Author",
            description = "Test Description",
            genres = listOf("Fantasy", "Adventure"),
            status = MangaInfo.ONGOING,
            cover = "https://example.com/cover.jpg"
        )

        val novel = Novel.fromMangaInfo(mangaInfo)

        assertEquals("https://example.com/novel", novel.url)
        assertEquals("Test Novel", novel.title)
        assertEquals("Test Author", novel.author)
        assertEquals("Test Description", novel.description)
        assertEquals(listOf("Fantasy", "Adventure"), novel.genres)
        assertEquals(NovelStatus.ONGOING, novel.status)
        assertEquals("https://example.com/cover.jpg", novel.cover)
    }

    @Test
    fun `cleanDescription normalizes whitespace`() {
        val novel = Novel(
            url = "url",
            title = "title",
            description = "  Hello   world  \n\n  Test  "
        )
        assertEquals("Hello world Test", novel.cleanDescription())
    }

    @Test
    fun `builder creates novel correctly`() {
        val novel = Novel.build("https://example.com", "Test Novel") {
            author = "Test Author"
            description = "Test Description"
            status = NovelStatus.ONGOING
        }

        assertEquals("https://example.com", novel.url)
        assertEquals("Test Novel", novel.title)
        assertEquals("Test Author", novel.author)
        assertEquals("Test Description", novel.description)
        assertEquals(NovelStatus.ONGOING, novel.status)
    }
}

class ChapterTest {

    @Test
    fun `isValid returns true when url and title are present`() {
        val chapter = Chapter(url = "https://example.com/ch1", title = "Chapter 1")
        assertTrue(chapter.isValid())
    }

    @Test
    fun `isValid returns false when url is blank`() {
        val chapter = Chapter(url = "", title = "Chapter 1")
        assertFalse(chapter.isValid())
    }

    @Test
    fun `hasNumber returns true when number is positive`() {
        val chapter = Chapter(url = "url", title = "title", number = 5.0f)
        assertTrue(chapter.hasNumber())
    }

    @Test
    fun `hasNumber returns false when number is negative`() {
        val chapter = Chapter(url = "url", title = "title", number = -1.0f)
        assertFalse(chapter.hasNumber())
    }

    @Test
    fun `toChapterInfo converts correctly`() {
        val chapter = Chapter(
            url = "https://example.com/ch1",
            title = "Chapter 1",
            number = 1.0f,
            date = 1234567890L,
            scanlator = "Test Scanlator"
        )

        val chapterInfo = chapter.toChapterInfo()

        assertEquals("https://example.com/ch1", chapterInfo.key)
        assertEquals("Chapter 1", chapterInfo.name)
        assertEquals(1.0f, chapterInfo.number)
        assertEquals(1234567890L, chapterInfo.dateUpload)
        assertEquals("Test Scanlator", chapterInfo.scanlator)
    }

    @Test
    fun `fromChapterInfo converts correctly`() {
        val chapterInfo = chimahon.source.ireader.model.ChapterInfo(
            key = "https://example.com/ch1",
            name = "Chapter 1",
            number = 1.0f,
            dateUpload = 1234567890L,
            scanlator = "Test Scanlator"
        )

        val chapter = Chapter.fromChapterInfo(chapterInfo)

        assertEquals("https://example.com/ch1", chapter.url)
        assertEquals("Chapter 1", chapter.title)
        assertEquals(1.0f, chapter.number)
        assertEquals(1234567890L, chapter.date)
        assertEquals("Test Scanlator", chapter.scanlator)
    }

    @Test
    fun `extractChapterNumber extracts from chapter prefix`() {
        assertEquals(5.0f, Chapter.extractChapterNumber("Chapter 5"))
        assertEquals(5.0f, Chapter.extractChapterNumber("chapter 5"))
        assertEquals(5.0f, Chapter.extractChapterNumber("Ch. 5"))
    }

    @Test
    fun `extractChapterNumber extracts from number at start`() {
        assertEquals(10.0f, Chapter.extractChapterNumber("10 - The Beginning"))
        assertEquals(10.5f, Chapter.extractChapterNumber("10.5 - Special"))
    }

    @Test
    fun `extractChapterNumber returns -1 for no number`() {
        assertEquals(-1.0f, Chapter.extractChapterNumber("Prologue"))
        assertEquals(-1.0f, Chapter.extractChapterNumber(""))
    }

    @Test
    fun `withAutoNumber extracts number from title`() {
        val chapter = Chapter(url = "url", title = "Chapter 7", number = -1.0f)
        val updated = chapter.withAutoNumber()
        assertEquals(7.0f, updated.number)
    }

    @Test
    fun `withAutoNumber does not change existing number`() {
        val chapter = Chapter(url = "url", title = "Chapter 7", number = 3.0f)
        val updated = chapter.withAutoNumber()
        assertEquals(3.0f, updated.number)
    }

    @Test
    fun `sortedByNumber sorts ascending`() {
        val chapters = listOf(
            Chapter(url = "3", title = "Ch 3", number = 3.0f),
            Chapter(url = "1", title = "Ch 1", number = 1.0f),
            Chapter(url = "2", title = "Ch 2", number = 2.0f)
        )
        val sorted = chapters.sortedByNumber()
        assertEquals(1.0f, sorted[0].number)
        assertEquals(2.0f, sorted[1].number)
        assertEquals(3.0f, sorted[2].number)
    }

    @Test
    fun `sortedByNumberDescending sorts descending`() {
        val chapters = listOf(
            Chapter(url = "1", title = "Ch 1", number = 1.0f),
            Chapter(url = "3", title = "Ch 3", number = 3.0f),
            Chapter(url = "2", title = "Ch 2", number = 2.0f)
        )
        val sorted = chapters.sortedByNumberDescending()
        assertEquals(3.0f, sorted[0].number)
        assertEquals(2.0f, sorted[1].number)
        assertEquals(1.0f, sorted[2].number)
    }

    @Test
    fun `distinctByUrl removes duplicates`() {
        val chapters = listOf(
            Chapter(url = "same", title = "Ch 1"),
            Chapter(url = "same", title = "Ch 2"),
            Chapter(url = "different", title = "Ch 3")
        )
        val distinct = chapters.distinctByUrl()
        assertEquals(2, distinct.size)
    }
}

class NovelStatusTest {

    @Test
    fun `parse returns ONGOING for ongoing text`() {
        assertEquals(NovelStatus.ONGOING, NovelStatus.parse("ongoing"))
        assertEquals(NovelStatus.ONGOING, NovelStatus.parse("Ongoing"))
        assertEquals(NovelStatus.ONGOING, NovelStatus.parse("publishing"))
    }

    @Test
    fun `parse returns COMPLETED for completed text`() {
        assertEquals(NovelStatus.COMPLETED, NovelStatus.parse("completed"))
        assertEquals(NovelStatus.COMPLETED, NovelStatus.parse("Completed"))
        assertEquals(NovelStatus.COMPLETED, NovelStatus.parse("finished"))
    }

    @Test
    fun `parse returns CANCELLED for cancelled text`() {
        assertEquals(NovelStatus.CANCELLED, NovelStatus.parse("cancelled"))
        assertEquals(NovelStatus.CANCELLED, NovelStatus.parse("canceled"))
        assertEquals(NovelStatus.CANCELLED, NovelStatus.parse("dropped"))
    }

    @Test
    fun `parse returns ON_HIATUS for hiatus text`() {
        assertEquals(NovelStatus.ON_HIATUS, NovelStatus.parse("hiatus"))
        assertEquals(NovelStatus.ON_HIATUS, NovelStatus.parse("on hiatus"))
    }

    @Test
    fun `parse returns UNKNOWN for unknown text`() {
        assertEquals(NovelStatus.UNKNOWN, NovelStatus.parse("unknown"))
        assertEquals(NovelStatus.UNKNOWN, NovelStatus.parse(""))
    }

    @Test
    fun `toLegacyStatus converts correctly`() {
        assertEquals(0L, NovelStatus.UNKNOWN.toLegacyStatus())
        assertEquals(1L, NovelStatus.ONGOING.toLegacyStatus())
        assertEquals(2L, NovelStatus.COMPLETED.toLegacyStatus())
        assertEquals(3L, NovelStatus.LICENSED.toLegacyStatus())
        assertEquals(4L, NovelStatus.PUBLISHING_FINISHED.toLegacyStatus())
        assertEquals(5L, NovelStatus.CANCELLED.toLegacyStatus())
        assertEquals(6L, NovelStatus.ON_HIATUS.toLegacyStatus())
    }

    @Test
    fun `fromLegacy converts correctly`() {
        assertEquals(NovelStatus.UNKNOWN, NovelStatus.fromLegacy(0L))
        assertEquals(NovelStatus.ONGOING, NovelStatus.fromLegacy(1L))
        assertEquals(NovelStatus.COMPLETED, NovelStatus.fromLegacy(2L))
        assertEquals(NovelStatus.LICENSED, NovelStatus.fromLegacy(3L))
        assertEquals(NovelStatus.PUBLISHING_FINISHED, NovelStatus.fromLegacy(4L))
        assertEquals(NovelStatus.CANCELLED, NovelStatus.fromLegacy(5L))
        assertEquals(NovelStatus.ON_HIATUS, NovelStatus.fromLegacy(6L))
    }

    @Test
    fun `fromLegacy returns UNKNOWN for invalid value`() {
        assertEquals(NovelStatus.UNKNOWN, NovelStatus.fromLegacy(99L))
    }
}

class ChapterContentTest {

    @Test
    fun `text creates TextContent`() {
        val content = ChapterContent.text(listOf("Paragraph 1", "Paragraph 2"))
        assertTrue(content is ChapterContent.TextContent)
        val textContent = content as ChapterContent.TextContent
        assertEquals(2, textContent.paragraphs.size)
    }

    @Test
    fun `text from string splits by double newline`() {
        val content = ChapterContent.text("Paragraph 1\n\nParagraph 2\n\nParagraph 3")
        assertTrue(content is ChapterContent.TextContent)
        val textContent = content as ChapterContent.TextContent
        assertEquals(3, textContent.paragraphs.size)
    }

    @Test
    fun `images creates ImageContent`() {
        val content = ChapterContent.images(listOf("https://example.com/1.jpg", "https://example.com/2.jpg"))
        assertTrue(content is ChapterContent.ImageContent)
        val imageContent = content as ChapterContent.ImageContent
        assertEquals(2, imageContent.urls.size)
    }

    @Test
    fun `textContent fullText joins paragraphs`() {
        val content = ChapterContent.text(listOf("Paragraph 1", "Paragraph 2"))
        val textContent = content as ChapterContent.TextContent
        assertEquals("Paragraph 1\n\nParagraph 2", textContent.fullText())
    }

    @Test
    fun `textContent wordCount counts correctly`() {
        val content = ChapterContent.text(listOf("Hello world", "Test paragraph"))
        val textContent = content as ChapterContent.TextContent
        assertEquals(4, textContent.wordCount())
    }

    @Test
    fun `textContent isEmpty returns true for empty list`() {
        val content = ChapterContent.text(emptyList())
        val textContent = content as ChapterContent.TextContent
        assertTrue(textContent.isEmpty())
    }

    @Test
    fun `imageContent pageCount returns correct count`() {
        val content = ChapterContent.images(listOf("1.jpg", "2.jpg", "3.jpg"))
        val imageContent = content as ChapterContent.ImageContent
        assertEquals(3, imageContent.pageCount())
    }
}

class NovelListResultTest {

    @Test
    fun `empty returns empty result`() {
        val result = NovelListResult.empty()
        assertTrue(result.isEmpty())
        assertFalse(result.hasNextPage)
    }

    @Test
    fun `lastPage returns result with no next`() {
        val novels = listOf(
            Novel(url = "1", title = "Novel 1"),
            Novel(url = "2", title = "Novel 2")
        )
        val result = NovelListResult.lastPage(novels)
        assertEquals(2, result.size)
        assertFalse(result.hasNextPage)
    }

    @Test
    fun `toMangasPageInfo converts correctly`() {
        val novels = listOf(
            Novel(url = "1", title = "Novel 1"),
            Novel(url = "2", title = "Novel 2")
        )
        val result = NovelListResult(novels, true)
        val page = result.toMangasPageInfo()
        assertEquals(2, page.size())
        assertTrue(page.hasNextPage)
    }
}
