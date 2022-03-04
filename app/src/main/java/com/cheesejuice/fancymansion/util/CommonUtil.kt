package com.cheesejuice.fancymansion.util

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.app.AlertDialog
import com.cheesejuice.fancymansion.R
import java.text.SimpleDateFormat
import java.util.*

class CommonUtil(val context: Context){
    val formatss = SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale("ko", "KR"))
    val formatdate = SimpleDateFormat("yyyy-MM-dd", Locale("ko", "KR"))
    fun longToTimeFormatss(time: Long) = formatss.format(Date(time))
    fun longToTimeFormatdate(time: Long) = formatdate.format(Date(time))

    fun isBookReading(bookId: Long): Boolean{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId, MODE_PRIVATE)
        return pref.getBoolean(Const.KEY_IS_READING, false)
    }

    fun getAlertDailog(context: Context,
                       title:String = context.getString(R.string.alert_default_title),
                       message:String = context.getString(R.string.alert_default_message),
                       buttonText:String = context.getString(R.string.alert_default_button),
                       click: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ -> (context as Activity).finish() }
    ) = AlertDialog.Builder(context).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(buttonText, click)
    }

    fun deleteReadingBookInfo(bookId: Long){
        context.deleteSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId)
    }

    fun initReadingBookInfo(bookId: Long, firstSlide: Long){
        deleteReadingBookInfo(bookId)
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(Const.KEY_IS_READING, true)
        editor.putLong(Const.KEY_CURRENT_SLIDE_ID, firstSlide)
        editor.commit()
    }

    fun getReadingSlideId(bookId: Long): Long{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId, MODE_PRIVATE)
        return pref.getLong(Const.KEY_CURRENT_SLIDE_ID, Const.BOOK_FIRST_READ)
    }

    fun setReadingSlideId(bookId: Long, slideId: Long){
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(Const.KEY_CURRENT_SLIDE_ID, slideId)
        editor.commit()
    }

    fun getSlideCount(bookId: Long, slideId: Long):Int{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId, MODE_PRIVATE)
        return pref.getInt(Const.KEY_PREFIX_PREF_COUNT+slideId, 0)
    }

    fun setSlideCount(bookId: Long, slideId: Long, count: Int){
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId, MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt(Const.KEY_PREFIX_PREF_COUNT+slideId, count)
    }
}