package com.cheesejuice.fancymansion.util

import com.cheesejuice.fancymansion.model.Book

class Const {
    companion object{
        const val TAG = "FancyMansion"

        const val BOOK_FIRST_READ = -1L
        const val END_SLIDE_ID = -1L

        const val KEY_PREFIX_PREF_BOOK = "book_"
        const val KEY_PREFIX_PREF_COUNT = "count_"

        const val KEY_CURRENT_BOOK_ID = "KEY_CURRENT_BOOK_ID"
        const val KEY_CURRENT_SLIDE_ID = "KEY_CURRENT_SLIDE_ID"
        const val KEY_IS_READING = "KEY_IS_READING"

        const val KEY_FIRST_READ = "KEY_FIRST_READ"
    }
}

enum class CondOp(
    val opName : String,
    val check : (Int, Int) -> Boolean // 멤버변수로 추가해줍니다.
){
    OVER("over", { cond, count -> cond < count }),
    BELOW("under", { cond, count -> cond > count }),
    EQUAL("equal", { cond, count -> cond == count }),
    NOT("not", { cond, count -> cond != count }),
    ALL("all", { _ , _  -> true });

    companion object {
        fun from(type: String?): CondOp = values().find { it.opName == type } ?: ALL
    }
}