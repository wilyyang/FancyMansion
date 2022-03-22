package com.cheesejuice.fancymansion.util

import android.content.Context
import android.util.Log
import com.cheesejuice.fancymansion.model.Condition
import com.cheesejuice.fancymansion.model.SlideBrief
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class BookUtil @Inject constructor(@ActivityContext private val context: Context){
    // Check Condition
    fun checkConditions(bookId: Long, conditions: MutableList<Condition>, mode: String): Boolean{
        var result = true
        var nextLogic = CondNext.AND
        for(condition in conditions){
            result = nextLogic.check(result, checkCondition(bookId, condition, mode))
            nextLogic = CondNext.from(condition.nextLogic)
            if(result && nextLogic == CondNext.OR) break
        }
        return result
    }

    fun checkCondition(bookId: Long, condition: Condition, mode: String): Boolean =
        condition.run{
            val count1 = getIdCount(bookId, conditionId1, mode)
            val count2 = if(conditionId2==Const.NOT_SUPPORT_COND_ID_2) conditionCount else getIdCount(bookId, conditionId2, mode)
            Log.d(Const.TAG, "mode - $mode check : $conditionId1 ($count1) $conditionOp $conditionId2 ($count2)")
            CondOp.from(conditionOp).check(count1, count2)
        }


    // Setting Pref
    fun getOnlyPlay(): Boolean{
        val pref = context.getSharedPreferences(Const.PREF_SETTING, Context.MODE_PRIVATE)
        return pref.getBoolean(Const.PREF_ONLY_PLAY, false)
    }

    fun setOnlyPlay(onlyPlay: Boolean){
        val pref = context.getSharedPreferences(Const.PREF_SETTING, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(Const.PREF_ONLY_PLAY, onlyPlay)
        editor.commit()
    }

    fun incrementBookCount():Long{
        val pref = context.getSharedPreferences(Const.PREF_SETTING, Context.MODE_PRIVATE)
        val count = pref.getLong(Const.PREF_BOOK_COUNT, 0L) + 1L
        val editor = pref.edit()
        editor.putLong(Const.PREF_BOOK_COUNT, count)
        editor.commit()
        return count
    }

    fun getBookCount():Long{
        val pref = context.getSharedPreferences(Const.PREF_SETTING, Context.MODE_PRIVATE)
        return pref.getLong(Const.PREF_BOOK_COUNT, 0L)
    }

    // Reading Book Info
    fun getSaveSlideId(bookId: Long): Long{
        val pref = context.getSharedPreferences(Const.PREF_PREFIX_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        return pref.getLong(Const.PREF_SAVE_SLIDE_ID, Const.ID_NOT_FOUND)
    }

    fun setSaveSlideId(bookId: Long, slideId: Long){
        val pref = context.getSharedPreferences(Const.PREF_PREFIX_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putLong(Const.PREF_SAVE_SLIDE_ID, slideId)
        editor.commit()
    }

    fun deleteBookPref(bookId: Long, mode: String){
        context.deleteSharedPreferences(mode+Const.PREF_PREFIX_BOOK+bookId)
    }

    // Book Count
    private fun getIdCount(bookId: Long, slideId: Long, mode: String):Int{
        val pref = context.getSharedPreferences(mode+Const.PREF_PREFIX_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        return pref.getInt(Const.PREF_PREFIX_COUNT+slideId, 0)
    }

    private fun setIdCount(bookId: Long, slideId: Long, count: Int, mode: String){
        val pref = context.getSharedPreferences(mode+Const.PREF_PREFIX_BOOK+bookId,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putInt(Const.PREF_PREFIX_COUNT+slideId, count)
        editor.commit()
    }

    fun incrementIdCount(bookId: Long, id: Long, mode: String){
        val count = getIdCount(bookId, id, mode) + 1
        setIdCount(bookId, id, count, mode)
    }

    // 00 / 00 / 00 / 00 / 00 = slide / choice / showCondition / enterId / enterCondition
    fun nextSlideId(briefs: List<SlideBrief>): Long{
        val size = briefs.size
        var idx = 0
        for (i in 1 ..99){
            i*Const.COUNT_SLIDE
            briefs[idx]

            // same -> i++
            // small -> idx++ ->
        }

        return -1L
    }
}