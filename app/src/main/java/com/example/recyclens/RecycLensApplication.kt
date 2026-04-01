package com.example.recyclens

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.recyclens.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class RecycLensApplication(): Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Use androidLogger(Level.ERROR) in production to only log errors
            androidLogger()
            // Declare Android context
            androidContext(this@RecycLensApplication)
            // Load our module
            modules(appModule)
        }
        val config = mapOf(
            "cloud_name" to "dd6of956q",
            "api_key" to "312824972158817",
            "api_secret" to "TxTbjz6_qnMG_CYFfhVAIObEB7o"
        )
        MediaManager.init(this, config)
    }
}