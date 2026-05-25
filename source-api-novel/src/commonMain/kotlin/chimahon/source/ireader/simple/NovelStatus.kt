package chimahon.source.ireader.simple

enum class NovelStatus {
    UNKNOWN,
    ONGOING,
    COMPLETED,
    LICENSED,
    PUBLISHING_FINISHED,
    CANCELLED,
    ON_HIATUS;

    fun toLegacyStatus(): Long = when (this) {
        UNKNOWN -> 0L
        ONGOING -> 1L
        COMPLETED -> 2L
        LICENSED -> 3L
        PUBLISHING_FINISHED -> 4L
        CANCELLED -> 5L
        ON_HIATUS -> 6L
    }

    companion object {
        fun fromLegacy(status: Long): NovelStatus = when (status) {
            1L -> ONGOING
            2L -> COMPLETED
            3L -> LICENSED
            4L -> PUBLISHING_FINISHED
            5L -> CANCELLED
            6L -> ON_HIATUS
            else -> UNKNOWN
        }

        fun parse(text: String): NovelStatus {
            return when (text.trim().lowercase()) {
                "ongoing", "publishing", "serializing" -> ONGOING
                "completed", "complete", "finished" -> COMPLETED
                "licensed" -> LICENSED
                "cancelled", "canceled", "dropped" -> CANCELLED
                "hiatus", "on hiatus", "on hold" -> ON_HIATUS
                else -> UNKNOWN
            }
        }
    }
}
