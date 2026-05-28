package com.huntersdiary.android

import android.app.Application
import com.huntersdiary.android.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HuntersDiaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HuntersDiaryApplication)
            modules(appModule)
        }
    }
}
