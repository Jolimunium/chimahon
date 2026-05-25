package chimahon.source.ireader.simple

import chimahon.source.ireader.model.ImageUrl
import chimahon.source.ireader.model.Page
import chimahon.source.ireader.model.Text

sealed class ChapterContent {
    data class TextContent(val paragraphs: List<String>) : ChapterContent() {
        fun fullText(): String = paragraphs.joinToString("\n\n")
        fun wordCount(): Int = paragraphs.sumOf { it.split(Regex("\\s+")).size }
        fun isEmpty(): Boolean = paragraphs.isEmpty() || paragraphs.all { it.isBlank() }
    }

    data class ImageContent(val urls: List<String>) : ChapterContent() {
        fun pageCount(): Int = urls.size
        fun isEmpty(): Boolean = urls.isEmpty()
    }

    data class MixedContent(val items: List<ContentItem>) : ChapterContent() {
        fun textItems(): List<String> = items.filterIsInstance<ContentItem.TextItem>().map { it.text }
        fun imageItems(): List<String> = items.filterIsInstance<ContentItem.ImageItem>().map { it.url }
        fun isEmpty(): Boolean = items.isEmpty()
    }

    fun toPages(): List<Page> = when (this) {
        is TextContent -> paragraphs.map { Text(it) }
        is ImageContent -> urls.map { ImageUrl(it) }
        is MixedContent -> items.map { item ->
            when (item) {
                is ContentItem.TextItem -> Text(item.text)
                is ContentItem.ImageItem -> ImageUrl(item.url)
            }
        }
    }

    companion object {
        fun text(paragraphs: List<String>): ChapterContent = TextContent(paragraphs)
        fun text(content: String): ChapterContent = TextContent(
            content.split(Regex("\n{2,}")).filter { it.isNotBlank() }
        )
        fun images(urls: List<String>): ChapterContent = ImageContent(urls)
        fun mixed(items: List<ContentItem>): ChapterContent = MixedContent(items)

        fun fromPages(pages: List<Page>): ChapterContent {
            val textPages = pages.filterIsInstance<Text>()
            val imagePages = pages.filterIsInstance<ImageUrl>()
            return when {
                textPages.isNotEmpty() && imagePages.isEmpty() ->
                    TextContent(textPages.map { it.text })
                imagePages.isNotEmpty() && textPages.isEmpty() ->
                    ImageContent(imagePages.map { it.url })
                else -> MixedContent(pages.mapNotNull { page ->
                    when (page) {
                        is Text -> ContentItem.TextItem(page.text)
                        is ImageUrl -> ContentItem.ImageItem(page.url)
                        else -> null
                    }
                })
            }
        }
    }
}

sealed class ContentItem {
    data class TextItem(val text: String) : ContentItem()
    data class ImageItem(val url: String) : ContentItem()
}
