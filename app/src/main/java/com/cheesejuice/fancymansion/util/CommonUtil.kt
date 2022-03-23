package com.cheesejuice.fancymansion.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.app.AlertDialog
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cheesejuice.fancymansion.R
import dagger.hilt.android.qualifiers.ActivityContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CommonUtil @Inject constructor(@ActivityContext private val context: Context){
    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val PERMISSION_REQUEST_CODE = 20
    fun checkPermissions() :Boolean{
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED)
                return false
        }
        return true
    }

    fun checkRequestPermissions(): Boolean{
        if(!checkPermissions()){
            ActivityCompat.requestPermissions(
                context as Activity,
                permissions, PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    val formatss = SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale("ko", "KR"))
    val formatdate = SimpleDateFormat("yyyy-MM-dd", Locale("ko", "KR"))
    fun longToTimeFormatss(time: Long) = formatss.format(Date(time))
    fun longToTimeFormatdate(time: Long) = formatdate.format(Date(time))

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