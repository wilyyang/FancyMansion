package com.cheesejuice.fancymansion.util

import android.content.Context
import android.util.Log
import com.cheesejuice.fancymansion.model.Book
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Slide
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class FileUtil @Inject constructor(@ActivityContext private val context: Context){
    fun extractConfigFromJson(bookId: Long): Config?{
        val configJson = Sample.getSampleConfig(bookId)
        var result: Config? = null
        try{
            result = Json.decodeFromString<Config>(configJson)

        }catch (e : Exception){
            Log.e(Const.TAG, "Exception : "+e.printStackTrace())
            return null
        }
        return result
    }

    fun extractSlideFromJson(bookId: Long, slideId: Long): Slide?{
        val slideJson = Sample.getSlideJson(bookId, slideId)
        var result: Slide? = null
        try{
            result = Json.decodeFromString<Slide>(slideJson)

        }catch (e : Exception){
            Log.e(Const.TAG, "Exception : "+e.printStackTrace())
            return null
        }
        return result
    }

    fun extractBookFromJson(bookId: Long): Book?{
        val bookJson = Sample.getSampleJson()
        var result: Book? = null
        try{
            result = Json.decodeFromString<Book>(bookJson)

        }catch (e : Exception){
            Log.e(Const.TAG, "Exception : "+e.message)
            return null
        }
        return result
    }
}