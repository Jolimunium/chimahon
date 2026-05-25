package chimahon.source.ireader.helpers

import org.jsoup.nodes.Element
import org.jsoup.nodes.Document

fun Element.selectValue(selector: String): String {
    if (selector.contains("|")) {
        for (part in selector.split("|")) {
            val result = selectValueSingle(part.trim())
            if (result.isNotBlank()) return result
        }
        return ""
    }
    return selectValueSingle(selector)
}

private fun Element.selectValueSingle(selector: String): String {
    val parts = selector.split("@", limit = 2)
    val cssSelector = parts[0].trim()
    val attribute = parts.getOrNull(1)?.trim()?.lowercase() ?: "text"
    val element = if (cssSelector.isBlank()) this else this.selectFirst(cssSelector)
    element ?: return ""
    return when (attribute) {
        "text" -> element.text().trim()
        "html", "innerhtml" -> element.html()
        "outerhtml" -> element.outerHtml()
        "owntext" -> element.ownText().trim()
        else -> element.attr(attribute).trim()
    }
}

fun Element.selectText(selector: String): String {
    if (selector.contains("@")) return selectValue(selector)
    return this.selectFirst(selector)?.text()?.trim() ?: ""
}

fun Element.selectTexts(selector: String): List<String> {
    val parts = selector.split("@", limit = 2)
    val cssSelector = parts[0].trim()
    val attribute = parts.getOrNull(1)?.trim()?.lowercase() ?: "text"
    return this.select(cssSelector).mapNotNull { element ->
        val value = when (attribute) {
            "text" -> element.text()
            else -> element.attr(attribute)
        }
        value.trim().takeIf { it.isNotBlank() }
    }
}

fun Element.selectUrl(selector: String, baseUrl: String = ""): String {
    val url = if (selector.contains("@")) {
        selectValue(selector)
    } else {
        this.selectFirst(selector)?.attr("abs:href")
            ?: this.selectFirst(selector)?.attr("href")
            ?: ""
    }
    return normalizeUrl(url, baseUrl)
}

fun Element.selectImage(selector: String, baseUrl: String = ""): String {
    val element = if (selector.contains("@")) {
        return normalizeUrl(selectValue(selector), baseUrl)
    } else {
        this.selectFirst(selector)
    } ?: return ""
    val url = element.attr("abs:src")
        .ifBlank { element.attr("abs:data-src") }
        .ifBlank { element.attr("abs:data-lazy-src") }
        .ifBlank { element.attr("abs:data-original") }
        .ifBlank { element.attr("src") }
        .ifBlank { element.attr("data-src") }
        .ifBlank { element.attr("data-lazy-src") }
        .ifBlank { element.attr("data-original") }
    return normalizeUrl(url, baseUrl)
}

fun Element.exists(selector: String): Boolean = this.selectFirst(selector) != null

fun Element.selectFirstOrNull(selector: String): Element? {
    return try { this.selectFirst(selector) } catch (e: Exception) { null }
}

fun Element.selectFirstAny(vararg selectors: String): Element? {
    for (selector in selectors) {
        val element = selectFirstOrNull(selector)
        if (element != null) return element
    }
    return null
}

private fun normalizeUrl(url: String, baseUrl: String): String {
    val trimmed = url.trim()
    return when {
        trimmed.isBlank() -> ""
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.startsWith("//") -> "https:$trimmed"
        trimmed.startsWith("/") && baseUrl.isNotBlank() -> {
            val base = baseUrl.trimEnd('/')
            "$base$trimmed"
        }
        baseUrl.isNotBlank() -> {
            val base = baseUrl.trimEnd('/')
            "$base/$trimmed"
        }
        else -> trimmed
    }
}

fun Document.selectValue(selector: String): String = (this as Element).selectValue(selector)
fun Document.selectText(selector: String): String = (this as Element).selectText(selector)
fun Document.selectTexts(selector: String): List<String> = (this as Element).selectTexts(selector)
fun Document.selectUrl(selector: String, baseUrl: String = ""): String = (this as Element).selectUrl(selector, baseUrl)
fun Document.selectImage(selector: String, baseUrl: String = ""): String = (this as Element).selectImage(selector, baseUrl)
fun Document.exists(selector: String): Boolean = (this as Element).exists(selector)
