package com.cheesejuice.fancymansion.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.cheesejuice.fancymansion.R
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class Util @Inject constructor(@ActivityContext private val context: Context){
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