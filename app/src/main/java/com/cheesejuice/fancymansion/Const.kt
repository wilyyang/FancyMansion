package com.cheesejuice.fancymansion

import com.cheesejuice.fancymansion.Const.Companion.FB_DB_KEY_COMMENT_TIME
import com.cheesejuice.fancymansion.Const.Companion.FB_DB_KEY_TITLE

class Const {
    companion object{
        // Const
        const val TAG = "FancyMansion"
        const val ID_NOT_FOUND = -1L
        const val END_SLIDE_ID = -1L
        const val ADD_NEW_CHOICE = -2L
        const val ADD_NEW_ENTER = -3L
        const val ADD_NEW_CONDITION = -4L
        const val EDIT_PLAY = "EDIT_PLAY"
        const val NOT_SUPPORT_COND_ID_2 = -1L
        const val ID_1_SLIDE = 1_00_00_00_00L
        const val FIRST_SLIDE = 1L
        const val VIEW_HOLDER_LOADING = -5L
        const val VIEW_HOLDER_LOADING_COMMENT = ""

        const val PAGE_COUNT = 10
        const val PAGE_COUNT_LONG = 10L
        const val COMMENT_COUNT = 10L

        const val SLIDE_TYPE_NORMAL = 1
        const val SLIDE_TYPE_START = 2
        const val SLIDE_TYPE_END = 3

        const val ORDER_LATEST_IDX = 0
        const val ORDER_OLDEST_IDX = 1
        const val ORDER_TITLE_ASC_IDX = 2
        const val ORDER_TITLE_DESC_IDX = 3
        const val ORDER_DOWNLOADS_ASC_IDX = 4
        const val ORDER_DOWNLOADS_DESC_IDX = 5
        const val ORDER_GOOD_ASC_IDX = 6
        const val ORDER_GOOD_DESC_IDX = 7

        const val COUNT_SLIDE = 1_00_00_00_00L
        const val COUNT_CHOICE = 1_00_00_00L
        const val COUNT_SHOW_COND = 1_00_00L
        const val COUNT_ENTER_ID = 1_00L
        const val COUNT_ENTER_COND = 1L

        // Intent
        const val INTENT_BOOK_CREATE = "INTENT_BOOK_CREATE"
        const val INTENT_BOOK_ID = "INTENT_BOOK_ID"
        const val INTENT_SLIDE_ID = "INTENT_SLIDE_ID"
        const val INTENT_CHOICE_ID = "INTENT_CHOICE_ID"
        const val INTENT_ENTER_ID = "INTENT_ENTER_ID"
        const val INTENT_CONDITION_ID = "INTENT_CONDITION_ID"
        const val INTENT_SHOW_CONDITION = "INTENT_SHOW_CONDITION"
        const val INTENT_PUBLISH_CODE = "INTENT_PUBLISH_CODE"

        const val INTENT_READ_ONLY = "INTENT_READ_ONLY"

        const val RESULT_NEW = 1
        const val RESULT_UPDATE = 2
        const val RESULT_NEW_DELETE = 3
        const val RESULT_DELETE = 4
        const val RESULT_CANCEL = 5

        // Pref
        const val PREF_SETTING = "PREF_SETTING"
        const val PREF_EDIT_PLAY = "PREF_EDIT_PLAY"

        const val PREF_BOOK_COUNT = "PREF_BOOK_COUNT"
        const val PREF_SAVE_SLIDE_ID = "PREF_SAVE_SLIDE_ID"

        const val PREF_PREFIX_BOOK = "book_"
        const val PREF_PREFIX_COUNT = "count_"

        const val PREF_MAKE_SAMPLE = "PREF_MAKE_SAMPLE"

        // File
        const val FILE_DIR_BOOK = "book"
        const val FILE_DIR_MEDIA = "media"
        const val FILE_DIR_SLIDE = "slide"
        const val FILE_DIR_CONTENT = "content"

        const val FILE_DIR_READONLY = "readonly"
        const val FILE_DIR_TEMP = "temp"

        const val FILE_PREFIX_BOOK = "book_"
        const val FILE_PREFIX_CONFIG = "config"
        const val FILE_PREFIX_LOGIC = "logic"
        const val FILE_PREFIX_SLIDE = "slide_"
        const val FILE_PREFIX_IMAGE = "image_"

        const val FILE_PREFIX_READ = "read_"

        // Firebase
        const val FB_DB_KEY_BOOK = "book"
        const val FB_STORAGE_BOOK = "book"
        const val FB_DB_KEY_TITLE = "title"
        const val FB_DB_KEY_PUBLISH = "publishCode"
        const val FB_DB_KEY_DOWNLOADS = "downloads"
        const val FB_DB_KEY_UID = "uid"
        const val FB_DB_KEY_GOOD = "good"
        const val FB_DB_KEY_TIME = "updateTime"

        const val FB_DB_KEY_COMMENT = "comment"
        const val FB_DB_KEY_COMMENT_ID = "id"
        const val FB_DB_KEY_COMMENT_TIME = "updateTime"

        const val FB_ALL_BOOK = -10L
        const val FB_ALL_COMMENT = -10L
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