package com.example.sebasiao.yuno.challenge

import android.app.Application
import com.example.sebasiao.yuno.challenge.di.AppContainer
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator

class MerchantApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        YunoThreeDSAuthenticator.initialize(this)
        container = AppContainer(this)
    }
}
