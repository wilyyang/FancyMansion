package com.cheesejuice.fancymansion.util

import android.content.Context
import android.util.Log
import com.cheesejuice.fancymansion.model.Book
import com.cheesejuice.fancymansion.model.Config
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class FileUtil @Inject constructor(@ActivityContext private val context: Context){
    fun extractConfigFromJson(fileName: String): Config?{
        val configJson = Sample.getSampleConfig()
        var result: Config? = null
        try{
            result = Gson().fromJson(configJson, Config::class.java)
        }catch (e : Exception){
            Log.e(Const.TAG, "Exception : "+e.printStackTrace())
            return null
        }
        return result
    }

    fun extractBookFromJson(fileName: String): Book?{
        val bookJson = Sample.getSampleJson()
        var result: Book? = null
        try{
            result = Gson().fromJson(bookJson, Book::class.java)
        }catch (e : Exception){
            Log.e(Const.TAG, "Exception : "+e.message)
            return null
        }
        return result
    }
}