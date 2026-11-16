package app.opusreader

import android.app.Application
import app.opusreader.di.AppContainer
import app.opusreader.di.DefaultAppContainer

class OpusApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
