package com.cheesejuice.fancymansion

import androidx.multidex.MultiDexApplication
import com.cheesejuice.fancymansion.model.*
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: MultiDexApplication() {
    var logic: Logic? = null
    var slideLogic: SlideLogic? = null
    var choice: ChoiceItem? = null
    var enter: EnterItem? = null
    var condition: Condition? = null
}