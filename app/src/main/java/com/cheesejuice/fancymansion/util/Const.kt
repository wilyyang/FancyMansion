package com.cheesejuice.fancymansion.util

import com.cheesejuice.fancymansion.model.Book

class Const {
    companion object{
        const val TAG = "FancyMansion"

        const val BOOK_FIRST_READ = -1L
        const val END_SLIDE_ID = -1L
        const val NOT_SUPPORT_COND_ID_2 = -1L
        const val FIRST_SLIDE = 100000000L

        const val KEY_PREF_BOOK_COUNT = "KEY_PREF_BOOK_COUNT"
        const val KEY_PREF_SETTING = "KEY_PREF_SETTING"
        const val KEY_PREFIX_PREF_BOOK = "book_"
        const val KEY_PREFIX_PREF_COUNT = "count_"

        const val KEY_CURRENT_BOOK_ID = "KEY_CURRENT_BOOK_ID"
        const val KEY_CURRENT_SLIDE_ID = "KEY_CURRENT_SLIDE_ID"
        const val KEY_IS_READING = "KEY_IS_READING"

        const val KEY_EDIT_PLAY = "KEY_EDIT_PLAY"
        const val KEY_PLAY_SLIDE_ID = "KEY_PLAY_SLIDE_ID"

        const val KEY_FIRST_READ = "KEY_FIRST_READ"
        const val KEY_BOOK_CREATE = "KEY_BOOK_CREATE"
        const val KEY_BOOK_ID = "KEY_BOOK_ID"

        const val KEY_PREFIX_EDIT_SLIDE = "KEY_EDIT_SLIDE_BOOK_"

        const val ID_NOT_FOUND = -1L

        const val FILE_PREFIX_BOOK = "book_"
        const val FILE_PREFIX_CONFIG = "config_"
        const val FILE_PREFIX_SLIDE = "slide_"
        const val FILE_PREFIX_IMAGE = "image_"
    }
}

enum class CondOp(
    val opName : String,
    val check : (Int, Int) -> Boolean
){
    OVER("over", { n1, n2 -> n1 > n2 }),
    BELOW("under", { n1, n2 -> n1 < n2 }),
    EQUAL("equal", { n1, n2 -> n1 == n2 }),
    NOT("not", { n1, n2 -> n1 != n2 }),
    ALL("all", { _, _ -> true });

    companion object {
        fun from(type: String?): CondOp = values().find { it.opName == type } ?: ALL
    }
}

enum class CondNext(
    val relName : String,
    val check : (Boolean, Boolean) -> Boolean
){
    AND("and", { p1, p2 -> p1 && p2 }),
    OR("or", { p1, p2 -> p1 || p2 });

    companion object {
        fun from(type: String?): CondNext = values().find { it.relName == type } ?: OR
    }
}