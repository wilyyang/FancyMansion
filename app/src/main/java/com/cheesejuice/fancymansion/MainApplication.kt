package com.cheesejuice.fancymansion

import androidx.multidex.MultiDexApplication
import com.cheesejuice.fancymansion.model.Logic
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: MultiDexApplication() {
    var logic: Logic? = null

    override fun onCreate() {
        super.onCreate()
    }
}