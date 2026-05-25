package chimahon.source.ireader.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MangaInfoTest {

    @Test
    fun `isValid returns true when key and title are present`() {
        val manga = MangaInfo(key = "https://example.com/manga", title = "Test Manga")
        assertTrue(manga.isValid())
    }

    @Test
    fun `isValid returns false when key is blank`() {
        val manga = MangaInfo(key = "", title = "Test Manga")
        assertFalse(manga.isValid())
    }

    @Test
    fun `isValid returns false when title is blank`() {
        val manga = MangaInfo(key = "https://example.com/manga", title = "")
        assertFalse(manga.isValid())
    }

    @Test
    fun `parseStatus returns ONGOING for ongoing text`() {
        assertEquals(MangaInfo.ONGOING, MangaInfo.parseStatus("ongoing"))
        assertEquals(MangaInfo.ONGOING, MangaInfo.parseStatus("Ongoing"))
        assertEquals(MangaInfo.ONGOING, MangaInfo.parseStatus("publishing"))
        assertEquals(MangaInfo.ONGOING, MangaInfo.parseStatus("serializing"))
    }

    @Test
    fun `parseStatus returns COMPLETED for completed text`() {
        assertEquals(MangaInfo.COMPLETED, MangaInfo.parseStatus("completed"))
        assertEquals(MangaInfo.COMPLETED, MangaInfo.parseStatus("Completed"))
        assertEquals(MangaInfo.COMPLETED, MangaInfo.parseStatus("complete"))
        assertEquals(MangaInfo.COMPLETED, MangaInfo.parseStatus("finished"))
    }

    @Test
    fun `parseStatus returns CANCELLED for cancelled text`() {
        assertEquals(MangaInfo.CANCELLED, MangaInfo.parseStatus("cancelled"))
        assertEquals(MangaInfo.CANCELLED, MangaInfo.parseStatus("canceled"))
        assertEquals(MangaInfo.CANCELLED, MangaInfo.parseStatus("dropped"))
    }

    @Test
    fun `parseStatus returns ON_HIATUS for hiatus text`() {
        assertEquals(MangaInfo.ON_HIATUS, MangaInfo.parseStatus("hiatus"))
        assertEquals(MangaInfo.ON_HIATUS, MangaInfo.parseStatus("on hiatus"))
        assertEquals(MangaInfo.ON_HIATUS, MangaInfo.parseStatus("on hold"))
    }

    @Test
    fun `parseStatus returns UNKNOWN for unknown text`() {
        assertEquals(MangaInfo.UNKNOWN, MangaInfo.parseStatus("unknown"))
        assertEquals(MangaInfo.UNKNOWN, MangaInfo.parseStatus(""))
        assertEquals(MangaInfo.UNKNOWN, MangaInfo.parseStatus("random"))
    }

    @Test
    fun `isOngoing returns true for ONGOING status`() {
        val manga = MangaInfo(key = "key", title = "title", status = MangaInfo.ONGOING)
        assertTrue(manga.isOngoing())
    }

    @Test
    fun `isCompleted returns true for COMPLETED status`() {
        val manga = MangaInfo(key = "key", title = "title", status = MangaInfo.COMPLETED)
        assertTrue(manga.isCompleted())
    }

    @Test
    fun `isCompleted returns true for PUBLISHING_FINISHED status`() {
        val manga = MangaInfo(key = "key", title = "title", status = MangaInfo.PUBLISHING_FINISHED)
        assertTrue(manga.isCompleted())
    }

    @Test
    fun `getCleanDescription normalizes whitespace`() {
        val manga = MangaInfo(key = "key", title = "title", description = "  Hello   world  \n\n  Test  ")
        assertEquals("Hello world Test", manga.getCleanDescription())
    }
}
