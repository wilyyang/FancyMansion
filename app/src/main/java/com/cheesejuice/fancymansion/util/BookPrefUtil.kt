package com.cheesejuice.fancymansion.util

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class BookPrefUtil @Inject constructor(@ActivityContext private val context: Context){
    fun isBookReading(bookId: Long): Boolean{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        return pref.getBoolean(Const.KEY_IS_READING, false)
    }

    fun deleteReadingBookInfo(bookId: Long){
        context.deleteSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId)
    }

    fun initReadingBookInfo(bookId: Long, firstSlide: Long){
        deleteReadingBookInfo(bookId)
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putBoolean(Const.KEY_IS_READING, true)
        editor.putLong(Const.KEY_CURRENT_SLIDE_ID, firstSlide)
        editor.commit()
    }

    fun getReadingSlideId(bookId: Long): Long{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        return pref.getLong(Const.KEY_CURRENT_SLIDE_ID, Const.BOOK_FIRST_READ)
    }

    fun setReadingSlideId(bookId: Long, slideId: Long){
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putLong(Const.KEY_CURRENT_SLIDE_ID, slideId)
        editor.commit()
    }

    fun getIdCount(bookId: Long, slideId: Long):Int{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        return pref.getInt(Const.KEY_PREFIX_PREF_COUNT+slideId, 0)
    }

    fun setIdCount(bookId: Long, slideId: Long, count: Int){
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putInt(Const.KEY_PREFIX_PREF_COUNT+slideId, count)
        editor.commit()
    }

    fun incrementIdCount(bookId: Long, id: Long){
        val count = getIdCount(bookId, id) + 1
        setIdCount(bookId, id, count)
    }
}