package chimahon.source.ireader.source

import chimahon.source.ireader.model.ChapterInfo
import chimahon.source.ireader.model.Command
import chimahon.source.ireader.model.MangaInfo
import chimahon.source.ireader.model.Page

interface Source {
    val id: Long
    val name: String
    val lang: String

    suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo
    suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo>
    suspend fun getChapterListPaged(manga: MangaInfo, page: Int, commands: List<Command<*>>): chimahon.source.ireader.model.ChaptersPageInfo {
        val chapters = getChapterList(manga, commands)
        return chimahon.source.ireader.model.ChaptersPageInfo.singlePage(chapters)
    }
    suspend fun getChapterPageCount(manga: MangaInfo): Int = 1
    fun supportsPaginatedChapters(): Boolean = false
    suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page>
    fun getRegex(): Regex = Regex("")
    fun getSourceKey(): String = "$name-$lang-$id"
    fun matchesId(sourceId: Long): Boolean = this.id == sourceId
}
