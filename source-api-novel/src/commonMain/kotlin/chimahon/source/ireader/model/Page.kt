package chimahon.source.ireader.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class Page

@Serializable
data class PageUrl(val url: String) : Page()

@Serializable
sealed class PageComplete : Page()

@Serializable
data class ImageUrl(val url: String) : PageComplete()

@Serializable
data class ImageBase64(val data: String) : PageComplete()

@Serializable
data class Text(val text: String) : PageComplete()

@Serializable
data class MovieUrl(val url: String) : PageComplete()

@Serializable
data class Subtitle(val url: String, val language: String? = null, val name: String? = null) : PageComplete()

val json = Json { ignoreUnknownKeys = true }

fun String.decode(): List<Page> {
    if (this.isBlank()) return emptyList()
    return runCatching {
        json.decodeFromString<List<Page>>(this)
    }.getOrElse { emptyList() }
}

fun List<Page>.encode(): String {
    if (this.isEmpty()) return "[]"
    return runCatching {
        json.encodeToString(this)
    }.getOrElse { "[]" }
}
