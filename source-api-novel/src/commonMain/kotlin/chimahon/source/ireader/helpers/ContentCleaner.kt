package chimahon.source.ireader.helpers

import org.jsoup.nodes.Element
import org.jsoup.nodes.Document

object ContentCleaner {
    private val AD_SELECTORS = listOf(
        ".ad", ".ads", ".advertisement", ".advert",
        "[class*='ad-']", "[class*='ads-']", "[id*='ad-']", "[id*='ads-']",
        ".google-ad", ".adsense", ".adsbygoogle",
        ".social", ".social-share", ".share-buttons", ".sharing",
        ".nav", ".navigation", ".menu", ".breadcrumb",
        ".header", ".footer", ".sidebar",
        ".comments", ".comment-section", "#comments",
        ".popup", ".modal", ".overlay", ".lightbox",
        ".author-note", ".translator-note", ".tn", ".an",
        ".hidden", ".invisible", "[style*='display:none']", "[style*='display: none']",
        "script", "style", "noscript", "iframe", "svg"
    )

    fun Element.removeAds(): Element {
        AD_SELECTORS.forEach { selector ->
            try { this.select(selector).remove() } catch (e: Exception) {}
        }
        return this
    }

    fun Element.removeSelectors(selectors: List<String>): Element {
        selectors.forEach { selector ->
            try { this.select(selector).remove() } catch (e: Exception) {}
        }
        return this
    }

    fun Element.removeScripts(): Element { this.select("script").remove(); return this }
    fun Element.removeStyles(): Element { this.select("style").remove(); return this }

    fun Element.removeComments(): Element {
        this.childNodes()
            .filter { it.nodeName() == "#comment" }
            .forEach { it.remove() }
        return this
    }

    fun Element.removeEmpty(): Element {
        this.select("*").forEach { element ->
            if (element.text().isBlank() && element.select("img").isEmpty()) {
                element.remove()
            }
        }
        return this
    }

    fun Element.cleanAll(): Element {
        return this.removeAds().removeScripts().removeStyles().removeComments().removeEmpty()
    }

    fun Element.extractParagraphs(): List<String> {
        val paragraphs = this.select("p")
        if (paragraphs.isNotEmpty()) {
            return paragraphs.map { it.text().trim() }.filter { it.isNotBlank() && it.length > 1 }
        }
        val html = this.html()
        if (html.contains("<br", ignoreCase = true)) {
            return html
                .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("<[^>]+>"), "")
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
        val text = this.text()
        val split = text.split(Regex("\n{2,}"))
        if (split.size > 1) {
            return split.map { it.trim() }.filter { it.isNotBlank() }
        }
        return listOf(text.trim()).filter { it.isNotBlank() }
    }

    fun Element.extractText(): String = extractParagraphs().joinToString("\n\n")
    fun Element.extractNormalizedText(): String = this.text().replace(Regex("\\s+"), " ").trim()
}

fun Element.removeAds(): Element = ContentCleaner.run { this@removeAds.removeAds() }
fun Element.removeSelectors(selectors: List<String>): Element = ContentCleaner.run { this@removeSelectors.removeSelectors(selectors) }
fun Element.removeScripts(): Element = ContentCleaner.run { this@removeScripts.removeScripts() }
fun Element.removeStyles(): Element = ContentCleaner.run { this@removeStyles.removeStyles() }
fun Element.removeComments(): Element = ContentCleaner.run { this@removeComments.removeComments() }
fun Element.removeEmpty(): Element = ContentCleaner.run { this@removeEmpty.removeEmpty() }
fun Element.cleanAll(): Element = ContentCleaner.run { this@cleanAll.cleanAll() }
fun Element.extractParagraphs(): List<String> = ContentCleaner.run { this@extractParagraphs.extractParagraphs() }
fun Element.extractText(): String = ContentCleaner.run { this@extractText.extractText() }
fun Element.extractNormalizedText(): String = ContentCleaner.run { this@extractNormalizedText.extractNormalizedText() }

fun Document.removeAds(): Document { (this as Element).removeAds(); return this }
fun Document.cleanAll(): Document { (this as Element).cleanAll(); return this }
