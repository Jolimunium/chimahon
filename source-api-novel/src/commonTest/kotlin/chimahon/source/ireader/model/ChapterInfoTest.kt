package chimahon.source.ireader.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChapterInfoTest {

    @Test
    fun `isValid returns true when key and name are present`() {
        val chapter = ChapterInfo(key = "https://example.com/ch1", name = "Chapter 1")
        assertTrue(chapter.isValid())
    }

    @Test
    fun `isValid returns false when key is blank`() {
        val chapter = ChapterInfo(key = "", name = "Chapter 1")
        assertFalse(chapter.isValid())
    }

    @Test
    fun `isValid returns false when name is blank`() {
        val chapter = ChapterInfo(key = "https://example.com/ch1", name = "")
        assertFalse(chapter.isValid())
    }

    @Test
    fun `hasValidNumber returns true when number is positive`() {
        val chapter = ChapterInfo(key = "key", name = "name", number = 5.0f)
        assertTrue(chapter.hasValidNumber())
    }

    @Test
    fun `hasValidNumber returns false when number is negative`() {
        val chapter = ChapterInfo(key = "key", name = "name", number = -1.0f)
        assertFalse(chapter.hasValidNumber())
    }

    @Test
    fun `extractChapterNumber extracts from chapter prefix`() {
        assertEquals(5.0f, ChapterInfo.extractChapterNumber("Chapter 5"))
        assertEquals(5.0f, ChapterInfo.extractChapterNumber("chapter 5"))
        assertEquals(5.0f, ChapterInfo.extractChapterNumber("Ch. 5"))
        assertEquals(5.0f, ChapterInfo.extractChapterNumber("ch 5"))
    }

    @Test
    fun `extractChapterNumber extracts from episode prefix`() {
        assertEquals(3.0f, ChapterInfo.extractChapterNumber("Episode 3"))
        assertEquals(3.0f, ChapterInfo.extractChapterNumber("ep. 3"))
    }

    @Test
    fun `extractChapterNumber extracts from number at start`() {
        assertEquals(10.0f, ChapterInfo.extractChapterNumber("10 - The Beginning"))
        assertEquals(10.5f, ChapterInfo.extractChapterNumber("10.5 - Special"))
    }

    @Test
    fun `extractChapterNumber extracts decimal numbers`() {
        assertEquals(5.5f, ChapterInfo.extractChapterNumber("Chapter 5.5"))
    }

    @Test
    fun `extractChapterNumber returns -1 for no number`() {
        assertEquals(-1.0f, ChapterInfo.extractChapterNumber("Prologue"))
        assertEquals(-1.0f, ChapterInfo.extractChapterNumber(""))
    }

    @Test
    fun `withAutoNumber extracts number from name`() {
        val chapter = ChapterInfo(key = "key", name = "Chapter 7", number = -1.0f)
        val updated = chapter.withAutoNumber()
        assertEquals(7.0f, updated.number)
    }

    @Test
    fun `withAutoNumber does not change existing number`() {
        val chapter = ChapterInfo(key = "key", name = "Chapter 7", number = 3.0f)
        val updated = chapter.withAutoNumber()
        assertEquals(3.0f, updated.number)
    }
}
