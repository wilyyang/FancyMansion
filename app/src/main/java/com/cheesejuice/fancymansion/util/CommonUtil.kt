package com.cheesejuice.fancymansion.util

import java.text.SimpleDateFormat
import java.util.*

class CommonUtil{
    val formatss = SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale("ko", "KR"))
    val formatdate = SimpleDateFormat("yyyy-MM-dd", Locale("ko", "KR"))
    fun longToTimeFormatss(time: Long) = formatss.format(Date(time))
    fun longToTimeFormatdate(time: Long) = formatdate.format(Date(time))
}