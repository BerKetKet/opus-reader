package app.opusreader.di

import android.content.Context
import app.opusreader.data.repository.LibraryRepository

interface AppContainer {
    val libraryRepository: LibraryRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepository.create(context)
    }
}
