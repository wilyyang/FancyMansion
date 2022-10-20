package com.cheesejuice.fancymansion.util

import java.text.SimpleDateFormat
import java.util.*

class Formatter {
    companion object {
        private val formatUntilSecond = SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale("ko", "KR"))
        fun longToTimeUntilSecond(time: Long): String = formatUntilSecond.format(Date(time))

        fun versionToString(version: Long) = "${(version / 1000)}.${(version / 10) % 100}.${(version % 10)}"
    }
}