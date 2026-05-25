package chimahon.source.ireader.helpers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DateParserTest {

    @Test
    fun `parseRelative handles seconds ago`() {
        val result = DateParser.parse("30 seconds ago")
        assertTrue(result > 0)
    }

    @Test
    fun `parseRelative handles minutes ago`() {
        val result = DateParser.parse("5 minutes ago")
        assertTrue(result > 0)
    }

    @Test
    fun `parseRelative handles hours ago`() {
        val result = DateParser.parse("2 hours ago")
        assertTrue(result > 0)
    }

    @Test
    fun `parseRelative handles days ago`() {
        val result = DateParser.parse("3 days ago")
        assertTrue(result > 0)
    }

    @Test
    fun `parseRelative handles weeks ago`() {
        val result = DateParser.parse("1 week ago")
        assertTrue(result > 0)
    }

    @Test
    fun `parseRelative handles months ago`() {
        val result = DateParser.parse("2 months ago")
        assertTrue(result > 0)
    }

    @Test
    fun `parseRelative handles years ago`() {
        val result = DateParser.parse("1 year ago")
        assertTrue(result > 0)
    }

    @Test
    fun `parseRelative is case insensitive`() {
        val result1 = DateParser.parse("5 Minutes Ago")
        val result2 = DateParser.parse("5 MINUTES AGO")
        assertTrue(result1 > 0)
        assertTrue(result2 > 0)
    }

    @Test
    fun `parseAbsolute handles ISO date format`() {
        val result = DateParser.parse("2024-01-15")
        assertTrue(result > 0)
    }

    @Test
    fun `parseAbsolute handles US date format`() {
        val result = DateParser.parse("01/15/2024")
        assertTrue(result > 0)
    }

    @Test
    fun `parse returns 0 for unparseable date`() {
        val result = DateParser.parse("not a date")
        assertEquals(0L, result)
    }

    @Test
    fun `parse handles empty string`() {
        val result = DateParser.parse("")
        assertEquals(0L, result)
    }

    @Test
    fun `parseDate extension function works`() {
        val result = "2024-01-15".parseDate()
        assertTrue(result > 0)
    }

    @Test
    fun `parseRelativeDate extension function works`() {
        val result = "5 minutes ago".parseRelativeDate()
        assertTrue(result != null)
        assertTrue(result!! > 0)
    }
}
