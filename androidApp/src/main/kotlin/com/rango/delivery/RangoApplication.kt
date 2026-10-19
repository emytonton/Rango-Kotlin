package com.rango.delivery

import android.app.Application
import com.rango.delivery.di.AppContainer
import com.rango.delivery.data.storage.AndroidSecureStorage

class RangoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(secureStorage = AndroidSecureStorage(this))
    }
}
