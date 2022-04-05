package com.cheesejuice.fancymansion

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FancyMansionApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
    }
}