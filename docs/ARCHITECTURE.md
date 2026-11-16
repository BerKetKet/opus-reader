# Architecture

## Why a restart
The first attempt matched screenshots screen by screen with fixed spacers, in-screen state and fixture branches. Interaction could not be added on top. This design puts state, persistence and the engine boundary first, and makes the look a property of shared components and tokens.

## Modules (Gradle)
- `:design`: tokens (colour, type, spacing, shapes, light and dark), fonts, components, the sheet, a debug component gallery. Knows nothing about data.
- `:engine`: `ReaderEngine` interface, page model types, `FakeReaderEngine`, EPUB reading for the fake. Knows nothing about data or UI.
- `:data`: Room database, repositories, EPUB import, Gutendex catalog, downloads, DataStore settings.
- `:app`: features (`feature/library`, `home`, `more`, `settings`, `notifications`, `reader`, `annotations`, `displaysettings`), navigation, `AppContainer`.
- `src/debug` in `:app`: seed data, fixture controller, deep links.

## State
Repositories expose Flows. ViewModels combine them into one UiState per screen and accept events. Sheets and dialogs are state (`activeSheet`) in the ViewModel, so back handling and fixtures work. Everything user-visible persists: library, read state, locators, bookmarks, annotations, collections, sort, filter, view mode, reader settings, themes, notifications, checkboxes.

## Locators and annotations
`Locator(spineIndex, charOffset)` is stable across reflow. Annotations store a start and end locator plus a text snippet for re-anchoring. The displayed "Location N of M" is derived from cumulative characters; Kindle-exact numbers are out of scope.

## Engine boundary
```kotlin
interface ReaderEngine {
    suspend fun open(book: BookSource, settings: ReaderSettings): BookInfo
    fun pageAt(locator: Locator): Page
    fun next(page: Page): Page?
    fun previous(page: Page): Page?
    fun progress(page: Page): Progress
    fun hitTest(page: Page, x: Float, y: Float): Locator?
    fun rectsFor(page: Page, range: LocatorRange): List<RectF>
    fun draw(page: Page, canvas: Canvas, theme: PageTheme)
    suspend fun search(query: String): List<SearchHit>
    val toc: List<TocEntry>
}
data class Page(val start: Locator, val end: Locator, val words: List<WordBox>, val links: List<LinkBox>)
data class WordBox(val text: String, val rect: RectF, val start: Locator, val end: Locator)
```
The engine draws page text. The app draws everything on top (selection, highlights, ruler, bookmark ribbon, chrome). Selection works from word boxes, so it works the same with the fake and the real engine. Contract tests assert page models for fixed EPUBs, so the real engine can be swapped in.

## Navigation
One NavHost, tab state saved per tab. The reader hides the tab bar and system bars (immersive). The resume cover lives in the shell.

## Debug fixtures
The debug controller seeds Room with deterministic data (including book covers from src/debug/res), then applies ViewModel state for a screen id. Production and fixture run identical composables.
