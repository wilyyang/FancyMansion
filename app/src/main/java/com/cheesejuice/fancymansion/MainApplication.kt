package com.cheesejuice.fancymansion

import androidx.multidex.MultiDexApplication
import com.cheesejuice.fancymansion.model.Book
import com.cheesejuice.fancymansion.util.CommonUtil

class MainApplication: MultiDexApplication() {
    companion object{
        lateinit var commonUtil: CommonUtil
    }

    override fun onCreate() {
        super.onCreate()
        commonUtil = CommonUtil()
    }
}