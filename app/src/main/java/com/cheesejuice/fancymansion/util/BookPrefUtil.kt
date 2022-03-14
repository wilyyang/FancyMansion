package com.cheesejuice.fancymansion.util

import android.content.Context
import android.util.Log
import com.cheesejuice.fancymansion.model.Condition
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class BookPrefUtil @Inject constructor(@ActivityContext private val context: Context){
    fun incrementBookCount():Long{
        val pref = context.getSharedPreferences(Const.KEY_PREF_SETTING, Context.MODE_PRIVATE)
        val count = pref.getLong(Const.KEY_PREF_BOOK_COUNT, 0L) + 1L
        val editor = pref.edit()
        editor.putLong(Const.KEY_PREF_BOOK_COUNT, count)
        editor.commit()
        return count
    }

    fun getBookCount():Long{
        val pref = context.getSharedPreferences(Const.KEY_PREF_SETTING, Context.MODE_PRIVATE)
        return pref.getLong(Const.KEY_PREF_BOOK_COUNT, 0L)
    }

    fun checkConditions(bookId: Long, conditions: MutableList<Condition>): Boolean{
        var result = true
        var nextLogic = CondNext.AND
        for(condition in conditions){
            result = nextLogic.check(result, checkCondition(bookId, condition))
            nextLogic = CondNext.from(condition.nextLogic)
            if(result && nextLogic == CondNext.OR) break
        }
        return result
    }

    fun checkCondition(bookId: Long, condition: Condition): Boolean =
        condition.run{
            val count1 = getIdCount(bookId, conditionId1)
            val count2 = if(conditionId2==Const.NOT_SUPPORT_COND_ID_2) conditionCount else getIdCount(bookId, conditionId2)
            Log.d(Const.TAG, "check : $conditionId1 ($count1) $conditionOp $conditionId2 ($count2)")
            CondOp.from(conditionOp).check(count1, count2)
        }

    fun isBookReading(bookId: Long): Boolean{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        return pref.getBoolean(Const.KEY_IS_READING, false)
    }

    fun deleteReadingBookInfo(bookId: Long){
        context.deleteSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId)
    }

    fun initReadingBookInfo(bookId: Long, firstSlide: Long){
        deleteReadingBookInfo(bookId)
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putBoolean(Const.KEY_IS_READING, true)
        editor.putLong(Const.KEY_CURRENT_SLIDE_ID, firstSlide)
        editor.commit()
    }

    fun getReadingSlideId(bookId: Long): Long{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        return pref.getLong(Const.KEY_CURRENT_SLIDE_ID, Const.BOOK_FIRST_READ)
    }

    fun setReadingSlideId(bookId: Long, slideId: Long){
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putLong(Const.KEY_CURRENT_SLIDE_ID, slideId)
        editor.commit()
    }

    fun getIdCount(bookId: Long, slideId: Long):Int{
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        return pref.getInt(Const.KEY_PREFIX_PREF_COUNT+slideId, 0)
    }

    fun setIdCount(bookId: Long, slideId: Long, count: Int){
        val pref = context.getSharedPreferences(Const.KEY_PREFIX_PREF_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putInt(Const.KEY_PREFIX_PREF_COUNT+slideId, count)
        editor.commit()
    }

    fun incrementIdCount(bookId: Long, id: Long){
        val count = getIdCount(bookId, id) + 1
        setIdCount(bookId, id, count)
    }
}