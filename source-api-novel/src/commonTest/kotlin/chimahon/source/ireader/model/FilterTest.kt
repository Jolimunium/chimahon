package chimahon.source.ireader.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterTest {

    @Test
    fun `Filter Title has correct default value`() {
        val filter = Filter.Title()
        assertEquals("", filter.value)
        assertEquals("Title", filter.name)
    }

    @Test
    fun `Filter Title isDefaultValue returns true for empty string`() {
        val filter = Filter.Title()
        assertTrue(filter.isDefaultValue())
    }

    @Test
    fun `Filter Title isDefaultValue returns false after change`() {
        val filter = Filter.Title()
        filter.value = "test"
        assertFalse(filter.isDefaultValue())
    }

    @Test
    fun `Filter Title reset restores initial value`() {
        val filter = Filter.Title()
        filter.value = "test"
        filter.reset()
        assertEquals("", filter.value)
        assertTrue(filter.isDefaultValue())
    }

    @Test
    fun `Filter Title isValid returns true for short text`() {
        val filter = Filter.Title()
        filter.value = "test"
        assertTrue(filter.isValid())
    }

    @Test
    fun `Filter Title isValid returns false for very long text`() {
        val filter = Filter.Title()
        filter.value = "a".repeat(201)
        assertFalse(filter.isValid())
    }

    @Test
    fun `Filter Author has correct default value`() {
        val filter = Filter.Author()
        assertEquals("", filter.value)
        assertEquals("Author", filter.name)
    }

    @Test
    fun `Filter Select has correct default index`() {
        val filter = Filter.Select("Status", arrayOf("All", "Ongoing", "Completed"))
        assertEquals(0, filter.value)
    }

    @Test
    fun `Filter Select getSelectedOption returns correct option`() {
        val filter = Filter.Select("Status", arrayOf("All", "Ongoing", "Completed"))
        filter.value = 1
        assertEquals("Ongoing", filter.getSelectedOption())
    }

    @Test
    fun `Filter Select getSelectedOption returns null for invalid index`() {
        val filter = Filter.Select("Status", arrayOf("All", "Ongoing", "Completed"))
        filter.value = 5
        assertEquals(null, filter.getSelectedOption())
    }

    @Test
    fun `Filter Select isValid returns true for valid index`() {
        val filter = Filter.Select("Status", arrayOf("All", "Ongoing", "Completed"))
        filter.value = 2
        assertTrue(filter.isValid())
    }

    @Test
    fun `Filter Select isValid returns false for invalid index`() {
        val filter = Filter.Select("Status", arrayOf("All", "Ongoing", "Completed"))
        filter.value = 5
        assertFalse(filter.isValid())
    }

    @Test
    fun `Filter Check has correct default value`() {
        val filter = Filter.Check("NSFW")
        assertEquals(null, filter.value)
    }

    @Test
    fun `Filter Genre has correct name`() {
        val filter = Filter.Genre("Fantasy")
        assertEquals("Fantasy", filter.name)
    }
}
