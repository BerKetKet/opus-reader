package app.opusreader.di

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import app.opusreader.data.repository.LibraryRepository
import app.opusreader.data.repository.SettingsRepository
import app.opusreader.engine.FakeReaderEngine
import app.opusreader.engine.ReaderEngine

interface AppContainer {
    val libraryRepository: LibraryRepository
    val settingsRepository: SettingsRepository
    val readerEngine: ReaderEngine
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepository.create(context)
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository.create(context)
    }

    override val readerEngine: ReaderEngine by lazy {
        val bookerlyTypeface = try {
            ResourcesCompat.getFont(context, app.opusreader.design.R.font.bookerly_regular)
        } catch (_: Throwable) {
            null
        }
        FakeReaderEngine(defaultTypeface = bookerlyTypeface)
    }
}
