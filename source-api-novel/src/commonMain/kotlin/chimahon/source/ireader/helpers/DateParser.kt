package chimahon.source.ireader.helpers

object DateParser {
    private val RELATIVE_PATTERNS = listOf(
        Regex("""(\d+)\s*(?:second|sec)s?\s*ago""", RegexOption.IGNORE_CASE) to { n: Int -> n * 1000L },
        Regex("""(\d+)\s*(?:minute|min)s?\s*ago""", RegexOption.IGNORE_CASE) to { n: Int -> n * 60 * 1000L },
        Regex("""(\d+)\s*(?:hour|hr)s?\s*ago""", RegexOption.IGNORE_CASE) to { n: Int -> n * 60 * 60 * 1000L },
        Regex("""(\d+)\s*days?\s*ago""", RegexOption.IGNORE_CASE) to { n: Int -> n * 24 * 60 * 60 * 1000L },
        Regex("""(\d+)\s*weeks?\s*ago""", RegexOption.IGNORE_CASE) to { n: Int -> n * 7 * 24 * 60 * 60 * 1000L },
        Regex("""(\d+)\s*months?\s*ago""", RegexOption.IGNORE_CASE) to { n: Int -> n * 30L * 24 * 60 * 60 * 1000L },
        Regex("""(\d+)\s*years?\s*ago""", RegexOption.IGNORE_CASE) to { n: Int -> n * 365L * 24 * 60 * 60 * 1000L },
    )

    private val MONTHS = mapOf(
        "jan" to 1, "january" to 1,
        "feb" to 2, "february" to 2,
        "mar" to 3, "march" to 3,
        "apr" to 4, "april" to 4,
        "may" to 5,
        "jun" to 6, "june" to 6,
        "jul" to 7, "july" to 7,
        "aug" to 8, "august" to 8,
        "sep" to 9, "sept" to 9, "september" to 9,
        "oct" to 10, "october" to 10,
        "nov" to 11, "november" to 11,
        "dec" to 12, "december" to 12
    )

    fun parse(text: String): Long {
        val normalized = text.trim().lowercase()
        parseRelative(normalized)?.let { return it }
        when {
            normalized == "today" || normalized == "now" -> return System.currentTimeMillis()
            normalized == "yesterday" -> return System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        }
        return parseAbsolute(text) ?: 0L
    }

    fun parseRelative(text: String): Long? {
        val normalized = text.trim().lowercase()
        for ((pattern, calculator) in RELATIVE_PATTERNS) {
            val match = pattern.find(normalized)
            if (match != null) {
                val number = match.groupValues[1].toIntOrNull() ?: continue
                val offset = calculator(number)
                return System.currentTimeMillis() - offset
            }
        }
        return null
    }

    fun parseAbsolute(text: String): Long? {
        val normalized = text.trim()
        val isoMatch = Regex("""(\d{4})-(\d{2})-(\d{2})""").find(normalized)
        if (isoMatch != null) {
            val (year, month, day) = isoMatch.destructured
            return approximateTimestamp(year.toInt(), month.toInt(), day.toInt())
        }
        val monthNameMatch = Regex("""(\w+)\s+(\d{1,2}),?\s+(\d{4})""").find(normalized)
        if (monthNameMatch != null) {
            val monthName = monthNameMatch.groupValues[1].lowercase()
            val day = monthNameMatch.groupValues[2].toIntOrNull() ?: return null
            val year = monthNameMatch.groupValues[3].toIntOrNull() ?: return null
            val month = MONTHS[monthName] ?: return null
            return approximateTimestamp(year, month, day)
        }
        val usMatch = Regex("""(\d{1,2})/(\d{1,2})/(\d{4})""").find(normalized)
        if (usMatch != null) {
            val (month, day, year) = usMatch.destructured
            return approximateTimestamp(year.toInt(), month.toInt(), day.toInt())
        }
        return null
    }

    private fun approximateTimestamp(year: Int, month: Int, day: Int): Long {
        val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var days = 0L
        for (y in 1970 until year) {
            days += if (isLeapYear(y)) 366 else 365
        }
        for (m in 1 until month) {
            days += daysInMonth[m]
            if (m == 2 && isLeapYear(year)) days += 1
        }
        days += day - 1
        return days * 24 * 60 * 60 * 1000L
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}

fun String.parseDate(): Long = DateParser.parse(this)
fun String.parseRelativeDate(): Long? = DateParser.parseRelative(this)
