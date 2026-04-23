package eu.kanade.tachiyomi.extension.util

import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

abstract class LazySource(
    override val id: Long,
    override val name: String,
    override val lang: String,
    private val loadActualSource: () -> Source,
) : Source {
    protected val actualSource: Source by lazy(loadActualSource)

    override suspend fun getMangaDetails(manga: SManga): SManga = actualSource.getMangaDetails(manga)
    override suspend fun getChapterList(manga: SManga): List<SChapter> = actualSource.getChapterList(manga)
    override suspend fun getPageList(chapter: SChapter): List<Page> = actualSource.getPageList(chapter)
    override suspend fun getRelatedMangaList(
        manga: SManga,
        exceptionHandler: (Throwable) -> Unit,
        pushResults: suspend (relatedManga: Pair<String, List<SManga>>, completed: Boolean) -> Unit,
    ) = actualSource.getRelatedMangaList(manga, exceptionHandler, pushResults)
}

class LazyCatalogueSource(
    id: Long,
    name: String,
    lang: String,
    loadActualSource: () -> Source,
) : LazySource(id, name, lang, loadActualSource), CatalogueSource {
    private val actualCatalogueSource: CatalogueSource by lazy { actualSource as CatalogueSource }

    override val supportsLatest: Boolean get() = actualCatalogueSource.supportsLatest
    override suspend fun getPopularManga(page: Int): MangasPage = actualCatalogueSource.getPopularManga(page)
    override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage =
        actualCatalogueSource.getSearchManga(page, query, filters)
    override suspend fun getLatestUpdates(page: Int): MangasPage = actualCatalogueSource.getLatestUpdates(page)
    override fun getFilterList(): FilterList = actualCatalogueSource.getFilterList()
}

class LazyHttpSource(
    id: Long,
    name: String,
    lang: String,
    loadActualSource: () -> Source,
) : HttpSource() {
    override val id: Long = id
    override val name: String = name
    override val lang: String = lang

    private val actualHttpSource: HttpSource by lazy { loadActualSource() as HttpSource }

    override val baseUrl: String get() = actualHttpSource.baseUrl
    override val headers: Headers get() = actualHttpSource.headers
    override val client: OkHttpClient get() = actualHttpSource.client
    override val supportsLatest: Boolean get() = actualHttpSource.supportsLatest

    override fun popularMangaRequest(page: Int): Request = actualHttpSource.popularMangaRequest(page)
    override fun popularMangaParse(response: Response): MangasPage = actualHttpSource.popularMangaParse(response)
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        actualHttpSource.searchMangaRequest(page, query, filters)
    override fun searchMangaParse(response: Response): MangasPage = actualHttpSource.searchMangaParse(response)
    override fun latestUpdatesRequest(page: Int): Request = actualHttpSource.latestUpdatesRequest(page)
    override fun latestUpdatesParse(response: Response): MangasPage = actualHttpSource.latestUpdatesParse(response)
    override fun mangaDetailsParse(response: Response): SManga = actualHttpSource.mangaDetailsParse(response)
    override fun chapterListParse(response: Response): List<SChapter> = actualHttpSource.chapterListParse(response)
    override fun pageListParse(response: Response): List<Page> = actualHttpSource.pageListParse(response)
    override fun imageUrlParse(response: Response): String = actualHttpSource.imageUrlParse(response)
}
