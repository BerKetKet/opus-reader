package app.opusreader.di

import android.content.Context
import app.opusreader.data.repository.LibraryRepository
import app.opusreader.data.repository.SettingsRepository

interface AppContainer {
    val libraryRepository: LibraryRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepository.create(context)
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository.create(context)
    }
}
