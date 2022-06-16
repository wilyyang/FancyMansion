package com.cheesejuice.fancymansion.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.app.AlertDialog
import com.cheesejuice.fancymansion.R
import dagger.hilt.android.qualifiers.ActivityContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CommonUtil @Inject constructor(@ActivityContext private val context: Context){
    companion object{
        val formatss = SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale("ko", "KR"))
        val formatdate = SimpleDateFormat("yyyy-MM-dd", Locale("ko", "KR"))
        fun longToTimeFormatss(time: Long) = formatss.format(Date(time))
        fun longToTimeFormatdate(time: Long) = formatdate.format(Date(time))

        fun versionToString(version:Long) = "${(version / 1000)}.${ (version / 10) % 100 }.${(version % 10)}"
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
}