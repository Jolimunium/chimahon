package chimahon.source.ireader.model

import kotlinx.serialization.Serializable

@Serializable
data class MangaInfo(
    val key: String,
    val title: String,
    val artist: String = "",
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Long = UNKNOWN,
    val cover: String = ""
) {
    companion object {
        const val UNKNOWN = 0L
        const val ONGOING = 1L
        const val COMPLETED = 2L
        const val LICENSED = 3L
        const val PUBLISHING_FINISHED = 4L
        const val CANCELLED = 5L
        const val ON_HIATUS = 6L

        fun parseStatus(statusText: String): Long {
            return when (statusText.trim().lowercase()) {
                "ongoing", "publishing", "serializing" -> ONGOING
                "completed", "complete", "finished" -> COMPLETED
                "licensed" -> LICENSED
                "cancelled", "canceled", "dropped" -> CANCELLED
                "hiatus", "on hiatus", "on hold" -> ON_HIATUS
                else -> UNKNOWN
            }
        }
    }

    fun isOngoing(): Boolean = status == ONGOING
    fun isCompleted(): Boolean = status == COMPLETED || status == PUBLISHING_FINISHED
    fun isValid(): Boolean = key.isNotBlank() && title.isNotBlank()
    fun getCleanDescription(): String = description.replace(Regex("\\s+"), " ").trim()
}
