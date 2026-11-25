package dev.afalabarce.aircrafts

import android.app.Application
import dev.afalabarce.aircrafts.presentation.ui.di.PresentationUiDependencyInjector
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class CustomApplication : Application() {
    override fun onCreate() {
        startKoin {
            androidLogger()
            androidContext(this@CustomApplication.applicationContext)
            modules(PresentationUiDependencyInjector.modules)
        }

        super.onCreate()
    }
}
