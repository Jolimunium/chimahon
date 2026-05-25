package chimahon.source.ireader.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MangasPageInfoTest {

    @Test
    fun `empty returns empty page with no next`() {
        val page = MangasPageInfo.empty()
        assertTrue(page.isEmpty())
        assertFalse(page.hasNextPage)
    }

    @Test
    fun `lastPage returns page with no next`() {
        val mangas = listOf(
            MangaInfo(key = "1", title = "Manga 1"),
            MangaInfo(key = "2", title = "Manga 2")
        )
        val page = MangasPageInfo.lastPage(mangas)
        assertEquals(2, page.size())
        assertFalse(page.hasNextPage)
    }

    @Test
    fun `isEmpty returns true for empty list`() {
        val page = MangasPageInfo(emptyList(), false)
        assertTrue(page.isEmpty())
    }

    @Test
    fun `isEmpty returns false for non-empty list`() {
        val page = MangasPageInfo(listOf(MangaInfo(key = "1", title = "title")), false)
        assertFalse(page.isEmpty())
    }

    @Test
    fun `isNotEmpty returns true for non-empty list`() {
        val page = MangasPageInfo(listOf(MangaInfo(key = "1", title = "title")), false)
        assertTrue(page.isNotEmpty())
    }

    @Test
    fun `size returns correct count`() {
        val mangas = listOf(
            MangaInfo(key = "1", title = "Manga 1"),
            MangaInfo(key = "2", title = "Manga 2"),
            MangaInfo(key = "3", title = "Manga 3")
        )
        val page = MangasPageInfo(mangas, true)
        assertEquals(3, page.size())
    }

    @Test
    fun `filter returns filtered page`() {
        val mangas = listOf(
            MangaInfo(key = "1", title = "Manga 1", status = MangaInfo.ONGOING),
            MangaInfo(key = "2", title = "Manga 2", status = MangaInfo.COMPLETED),
            MangaInfo(key = "3", title = "Manga 3", status = MangaInfo.ONGOING)
        )
        val page = MangasPageInfo(mangas, true)
        val filtered = page.filter { it.status == MangaInfo.ONGOING }
        assertEquals(2, filtered.size())
        assertTrue(filtered.hasNextPage)
    }

    @Test
    fun `map transforms mangas`() {
        val mangas = listOf(
            MangaInfo(key = "1", title = "manga 1"),
            MangaInfo(key = "2", title = "manga 2")
        )
        val page = MangasPageInfo(mangas, false)
        val mapped = page.map { it.copy(title = it.title.uppercase()) }
        assertEquals("MANGA 1", mapped.mangas[0].title)
        assertEquals("MANGA 2", mapped.mangas[1].title)
    }
}

class ChaptersPageInfoTest {

    @Test
    fun `empty returns empty page with no next`() {
        val page = ChaptersPageInfo.empty()
        assertTrue(page.isEmpty())
        assertFalse(page.hasNextPage)
    }

    @Test
    fun `singlePage returns page with no next`() {
        val chapters = listOf(
            ChapterInfo(key = "1", name = "Chapter 1"),
            ChapterInfo(key = "2", name = "Chapter 2")
        )
        val page = ChaptersPageInfo.singlePage(chapters)
        assertEquals(2, page.size())
        assertFalse(page.hasNextPage)
    }

    @Test
    fun `isEmpty returns true for empty list`() {
        val page = ChaptersPageInfo(emptyList(), false)
        assertTrue(page.isEmpty())
    }

    @Test
    fun `isNotEmpty returns true for non-empty list`() {
        val page = ChaptersPageInfo(listOf(ChapterInfo(key = "1", name = "Chapter 1")), false)
        assertTrue(page.isNotEmpty())
    }

    @Test
    fun `size returns correct count`() {
        val chapters = listOf(
            ChapterInfo(key = "1", name = "Chapter 1"),
            ChapterInfo(key = "2", name = "Chapter 2"),
            ChapterInfo(key = "3", name = "Chapter 3")
        )
        val page = ChaptersPageInfo(chapters, true)
        assertEquals(3, page.size())
    }
}
