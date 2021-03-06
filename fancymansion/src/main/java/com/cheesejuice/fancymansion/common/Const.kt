package com.cheesejuice.fancymansion.common

class Const {
    companion object{
        // Const
        const val TAG = "FancyMansion"
        const val ID_NOT_FOUND = -1L
        const val ID_DEFAULT = -1L
        const val END_SLIDE_ID = -1L
        const val MODE_PLAY = "play"
        const val NOT_SUPPORT_COND_ID_2 = -1L
        const val ID_1_SLIDE = 1_00_00_00_00L
        const val FIRST_SLIDE = 1L

        const val COUNT_SLIDE = 1_00_00_00_00L
        const val COUNT_CHOICE = 1_00_00_00L
        const val COUNT_SHOW_COND = 1_00_00L
        const val COUNT_ENTER_ID = 1_00L
        const val COUNT_ENTER_COND = 1L

        // Intent
        const val INTENT_BOOK_ID = "INTENT_BOOK_ID"
        const val INTENT_SLIDE_ID = "INTENT_SLIDE_ID"

        // Pref
        const val PREF_SETTING = "PREF_SETTING"
        const val PREF_ONLY_PLAY = "PREF_ONLY_PLAY"

        const val PREF_BOOK_COUNT = "PREF_BOOK_COUNT"
        const val PREF_SAVE_SLIDE_ID = "PREF_SAVE_SLIDE_ID"

        const val PREF_PREFIX_BOOK = "book_"
        const val PREF_PREFIX_COUNT = "count_"

        // File
        const val FILE_PREFIX_BOOK = "book_"
        const val FILE_PREFIX_CONFIG = "config_"
        const val FILE_PREFIX_LOGIC = "logic_"
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