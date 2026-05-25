package chimahon.source.ireader.adapter

import chimahon.source.ireader.model.ChapterInfo
import chimahon.source.ireader.model.ImageUrl
import chimahon.source.ireader.model.MangaInfo
import chimahon.source.ireader.model.Text
import eu.kanade.tachiyomi.sourcenovel.model.ChapterContent
import eu.kanade.tachiyomi.sourcenovel.model.SNChapter
import eu.kanade.tachiyomi.sourcenovel.model.SNNovel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IReaderAdapterTest {

    @Test
    fun `SNNovel toMangaInfo converts correctly`() {
        val novel = SNNovel(
            url = "https://example.com/novel",
            title = "Test Novel",
            author = "Test Author",
            artist = "Test Artist",
            description = "Test Description",
            genre = "Fantasy, Adventure",
            status = 1,
            thumbnail_url = "https://example.com/cover.jpg"
        )

        val mangaInfo = novel.toMangaInfo()

        assertEquals("https://example.com/novel", mangaInfo.key)
        assertEquals("Test Novel", mangaInfo.title)
        assertEquals("Test Author", mangaInfo.author)
        assertEquals("Test Artist", mangaInfo.artist)
        assertEquals("Test Description", mangaInfo.description)
        assertEquals(listOf("Fantasy", "Adventure"), mangaInfo.genres)
        assertEquals(1L, mangaInfo.status)
        assertEquals("https://example.com/cover.jpg", mangaInfo.cover)
    }

    @Test
    fun `MangaInfo toSNNovel converts correctly`() {
        val mangaInfo = MangaInfo(
            key = "https://example.com/novel",
            title = "Test Novel",
            author = "Test Author",
            artist = "Test Artist",
            description = "Test Description",
            genres = listOf("Fantasy", "Adventure"),
            status = 1L,
            cover = "https://example.com/cover.jpg"
        )

        val novel = mangaInfo.toSNNovel()

        assertEquals("https://example.com/novel", novel.url)
        assertEquals("Test Novel", novel.title)
        assertEquals("Test Author", novel.author)
        assertEquals("Test Artist", novel.artist)
        assertEquals("Test Description", novel.description)
        assertEquals("Fantasy, Adventure", novel.genre)
        assertEquals(1, novel.status)
        assertEquals("https://example.com/cover.jpg", novel.thumbnail_url)
    }

    @Test
    fun `SNChapter toChapterInfo converts correctly`() {
        val chapter = SNChapter(
            name = "Chapter 1",
            url = "https://example.com/ch1",
            date_upload = 1234567890L,
            chapter_number = 1.0f,
            scanlator = "Test Scanlator"
        )

        val chapterInfo = chapter.toChapterInfo()

        assertEquals("https://example.com/ch1", chapterInfo.key)
        assertEquals("Chapter 1", chapterInfo.name)
        assertEquals(1234567890L, chapterInfo.dateUpload)
        assertEquals(1.0f, chapterInfo.number)
        assertEquals("Test Scanlator", chapterInfo.scanlator)
    }

    @Test
    fun `ChapterInfo toSNChapter converts correctly`() {
        val chapterInfo = ChapterInfo(
            key = "https://example.com/ch1",
            name = "Chapter 1",
            dateUpload = 1234567890L,
            number = 1.0f,
            scanlator = "Test Scanlator"
        )

        val chapter = chapterInfo.toSNChapter()

        assertEquals("https://example.com/ch1", chapter.url)
        assertEquals("Chapter 1", chapter.name)
        assertEquals(1234567890L, chapter.date_upload)
        assertEquals(1.0f, chapter.chapter_number)
        assertEquals("Test Scanlator", chapter.scanlator)
    }

    @Test
    fun `text pages toChapterContent converts correctly`() {
        val pages = listOf(
            Text("Paragraph 1"),
            Text("Paragraph 2"),
            Text("Paragraph 3")
        )

        val content = pages.toChapterContent()

        assertNotNull(content)
        assertTrue(content is ChapterContent.Text)
        val textContent = content as ChapterContent.Text
        assertEquals(3, textContent.paragraphs.size)
        assertEquals("Paragraph 1", textContent.paragraphs[0])
        assertEquals("Paragraph 2", textContent.paragraphs[1])
        assertEquals("Paragraph 3", textContent.paragraphs[2])
    }

    @Test
    fun `image pages toChapterContent converts correctly`() {
        val pages = listOf(
            ImageUrl("https://example.com/page1.jpg"),
            ImageUrl("https://example.com/page2.jpg")
        )

        val content = pages.toChapterContent()

        assertNotNull(content)
        assertTrue(content is ChapterContent.Images)
        val imageContent = content as ChapterContent.Images
        assertEquals(2, imageContent.urls.size)
        assertEquals("https://example.com/page1.jpg", imageContent.urls[0])
        assertEquals("https://example.com/page2.jpg", imageContent.urls[1])
    }

    @Test
    fun `SNNovel handles null fields gracefully`() {
        val novel = SNNovel(
            url = "https://example.com/novel",
            title = "Test Novel"
        )

        val mangaInfo = novel.toMangaInfo()

        assertEquals("https://example.com/novel", mangaInfo.key)
        assertEquals("Test Novel", mangaInfo.title)
        assertEquals("", mangaInfo.author)
        assertEquals("", mangaInfo.artist)
        assertEquals("", mangaInfo.description)
        assertEquals(emptyList(), mangaInfo.genres)
        assertEquals("", mangaInfo.cover)
    }

    @Test
    fun `MangaInfo handles empty genres gracefully`() {
        val mangaInfo = MangaInfo(
            key = "key",
            title = "title",
            genres = emptyList()
        )

        val novel = mangaInfo.toSNNovel()

        assertEquals(null, novel.genre)
    }

    private fun assertTrue(condition: Boolean) {
        kotlin.test.assertTrue(condition)
    }
}
