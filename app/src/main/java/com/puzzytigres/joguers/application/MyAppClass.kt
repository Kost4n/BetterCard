package com.puzzytigres.joguers.application

import android.app.Application
import com.onesignal.OneSignal

class MyAppClass : Application() {
    private val idOneSignal = "f37d6fac-86ba-4411-8177-471890b7a5dc"

    override fun onCreate() {
        super.onCreate()

        OneSignal.initWithContext(this, idOneSignal)

    }
}