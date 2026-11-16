package app.opusreader.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "opus_settings")

data class ReaderSettings(
    val themeIndex: Int = 3, // Default Black / Dark
    val fontFamily: String = "Bookerly",
    val fontSize: Float = 16f,
    val lineSpacing: Float = 1.25f,
    val marginWidth: Int = 16,
    val continuousScroll: Boolean = false,
    val brightness: Float = 0.8f,
    val libraryViewMode: String = "GRID",
    val librarySortMode: String = "RECENT",
    val libraryFilter: String = "ALL"
)

interface SettingsRepository {
    val settingsFlow: Flow<ReaderSettings>
    suspend fun updateTheme(themeIndex: Int)
    suspend fun updateFontFamily(fontFamily: String)
    suspend fun updateFontSize(fontSize: Float)
    suspend fun updateLineSpacing(lineSpacing: Float)
    suspend fun updateContinuousScroll(enabled: Boolean)
    suspend fun updateBrightness(brightness: Float)
    suspend fun updateLibraryViewMode(mode: String)
    suspend fun updateLibrarySortMode(mode: String)
    suspend fun updateLibraryFilter(filter: String)

    companion object {
        fun create(context: Context): SettingsRepository {
            return DefaultSettingsRepository(context)
        }
    }
}

class DefaultSettingsRepository(
    private val context: Context
) : SettingsRepository {

    private object Keys {
        val THEME_INDEX = intPreferencesKey("theme_index")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val MARGIN_WIDTH = intPreferencesKey("margin_width")
        val CONTINUOUS_SCROLL = booleanPreferencesKey("continuous_scroll")
        val BRIGHTNESS = floatPreferencesKey("brightness")
        val LIBRARY_VIEW_MODE = stringPreferencesKey("library_view_mode")
        val LIBRARY_SORT_MODE = stringPreferencesKey("library_sort_mode")
        val LIBRARY_FILTER = stringPreferencesKey("library_filter")
    }

    override val settingsFlow: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        ReaderSettings(
            themeIndex = prefs[Keys.THEME_INDEX] ?: 3,
            fontFamily = prefs[Keys.FONT_FAMILY] ?: "Bookerly",
            fontSize = prefs[Keys.FONT_SIZE] ?: 16f,
            lineSpacing = prefs[Keys.LINE_SPACING] ?: 1.25f,
            marginWidth = prefs[Keys.MARGIN_WIDTH] ?: 16,
            continuousScroll = prefs[Keys.CONTINUOUS_SCROLL] ?: false,
            brightness = prefs[Keys.BRIGHTNESS] ?: 0.8f,
            libraryViewMode = prefs[Keys.LIBRARY_VIEW_MODE] ?: "GRID",
            librarySortMode = prefs[Keys.LIBRARY_SORT_MODE] ?: "RECENT",
            libraryFilter = prefs[Keys.LIBRARY_FILTER] ?: "ALL"
        )
    }

    override suspend fun updateTheme(themeIndex: Int) {
        context.dataStore.edit { it[Keys.THEME_INDEX] = themeIndex }
    }

    override suspend fun updateFontFamily(fontFamily: String) {
        context.dataStore.edit { it[Keys.FONT_FAMILY] = fontFamily }
    }

    override suspend fun updateFontSize(fontSize: Float) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = fontSize }
    }

    override suspend fun updateLineSpacing(lineSpacing: Float) {
        context.dataStore.edit { it[Keys.LINE_SPACING] = lineSpacing }
    }

    override suspend fun updateContinuousScroll(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CONTINUOUS_SCROLL] = enabled }
    }

    override suspend fun updateBrightness(brightness: Float) {
        context.dataStore.edit { it[Keys.BRIGHTNESS] = brightness }
    }

    override suspend fun updateLibraryViewMode(mode: String) {
        context.dataStore.edit { it[Keys.LIBRARY_VIEW_MODE] = mode }
    }

    override suspend fun updateLibrarySortMode(mode: String) {
        context.dataStore.edit { it[Keys.LIBRARY_SORT_MODE] = mode }
    }

    override suspend fun updateLibraryFilter(filter: String) {
        context.dataStore.edit { it[Keys.LIBRARY_FILTER] = filter }
    }
}
