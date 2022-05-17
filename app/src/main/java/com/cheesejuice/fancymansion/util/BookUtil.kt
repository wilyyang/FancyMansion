package com.cheesejuice.fancymansion.util

import android.content.Context
import android.util.Log
import com.cheesejuice.fancymansion.CondNext
import com.cheesejuice.fancymansion.CondOp
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.COUNT_SLIDE
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.model.Condition
import com.cheesejuice.fancymansion.model.EnterItem
import com.cheesejuice.fancymansion.model.SlideLogic
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class BookUtil @Inject constructor(@ActivityContext private val context: Context){
    // Setting Pref
    fun isSampleMake():Boolean{
        val pref = context.getSharedPreferences(Const.PREF_SETTING, Context.MODE_PRIVATE)
        if(!pref.getBoolean(Const.PREF_MAKE_SAMPLE, false)){
            val editor = pref.edit()
            editor.putBoolean(Const.PREF_MAKE_SAMPLE, true)
            editor.commit()
            return false
        }
        return true
    }

    fun getEditPlay(): Boolean{
        val pref = context.getSharedPreferences(Const.PREF_SETTING, Context.MODE_PRIVATE)
        return pref.getBoolean(Const.PREF_EDIT_PLAY, false)
    }

    fun setEditPlay(editPlay: Boolean){
        val pref = context.getSharedPreferences(Const.PREF_SETTING, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(Const.PREF_EDIT_PLAY, editPlay)
        editor.commit()
    }

    // Reading Book Info
    fun getSaveSlideId(bookId: Long, uid:String, publishCode:String): Long{
        val pref = context.getSharedPreferences(
            Const.PREF_PREFIX_BOOK+bookId+"_"+uid+"_"+publishCode,
            Context.MODE_PRIVATE
        )
        return pref.getLong(Const.PREF_SAVE_SLIDE_ID, Const.ID_NOT_FOUND)
    }

    fun setSaveSlideId(bookId: Long, uid:String, publishCode:String, slideId: Long){
        val pref = context.getSharedPreferences(
            Const.PREF_PREFIX_BOOK+bookId+"_"+uid+"_"+publishCode,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putLong(Const.PREF_SAVE_SLIDE_ID, slideId)
        editor.commit()
    }

    fun deleteBookPref(bookId: Long, uid:String, publishCode:String, mode: String){
        context.deleteSharedPreferences(mode+ Const.PREF_PREFIX_BOOK+bookId+"_"+uid+"_"+publishCode)
    }

    // Check Condition
    fun checkConditions(bookId: Long, uid:String, publishCode: String, conditions: MutableList<Condition>, mode: String): Boolean{
        var result = true
        var nextLogic = CondNext.AND
        for(condition in conditions){
            result = nextLogic.check(result, checkCondition(bookId, uid, publishCode, condition, mode))
            nextLogic = CondNext.from(condition.conditionNext)
            if(result && nextLogic == CondNext.OR) break
        }
        return result
    }

    fun checkCondition(bookId: Long, uid:String, publishCode: String, condition: Condition, mode: String): Boolean =
        condition.run{
            val count1 = getIdCount(bookId, uid, publishCode, conditionId1, mode)
            val count2 = if(conditionId2== Const.NOT_SUPPORT_COND_ID_2) conditionCount else getIdCount(bookId, uid, publishCode, conditionId2, mode)
            Log.d(Const.TAG, "mode - $mode check : $conditionId1 ($count1) $conditionOp $conditionId2 ($count2)")
            CondOp.from(conditionOp).check(count1, count2)
        }

    // Book Count
    private fun getIdCount(bookId: Long, uid:String, publishCode:String, slideId: Long, mode: String):Int{
        val pref = context.getSharedPreferences(mode+ Const.PREF_PREFIX_BOOK+bookId+"_"+uid+"_"+publishCode,
            Context.MODE_PRIVATE
        )
        return pref.getInt(Const.PREF_PREFIX_COUNT+slideId, 0)
    }

    private fun setIdCount(bookId: Long, uid:String, publishCode:String, slideId: Long, count: Int, mode: String){
        val pref = context.getSharedPreferences(mode+ Const.PREF_PREFIX_BOOK+bookId+"_"+uid+"_"+publishCode,
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putInt(Const.PREF_PREFIX_COUNT+slideId, count)
        editor.commit()
    }

    fun incrementIdCount(bookId: Long, uid:String, publishCode:String, id: Long, mode: String){
        val count = getIdCount(bookId, uid, publishCode, id, mode) + 1
        setIdCount(bookId, uid, publishCode, id, count, mode)
    }

    // Operator language
    fun translateOp(op: String):String {
        val opString = context.resources.getStringArray(R.array.cond_operator)
        return opString.getOrNull(CondOp.from(op).ordinal).toString()
    }

    fun translateNext(next: String):String {
        val nextString = context.resources.getStringArray(R.array.cond_next)
        return nextString.getOrNull(CondNext.from(next).ordinal).toString()
    }

    fun translateText(text: String):String = when(text){
        "entries" -> context.getString(R.string.cond_text_entries)
        "count" -> context.getString(R.string.cond_text_count)
        else -> "Unknown"
    }

    // 00 / 00 / 00 / 00 / 00 = slide / choice / showCondition / enterId / enterCondition
    fun nextSlideId(logics: List<SlideLogic>): Long{
        var idMap = Array(100){ i -> false }
        idMap[0] = true
        logics.map { (it.slideId / Const.COUNT_SLIDE).toInt() }.forEach { idMap[it] = true }

        val result = idMap.indexOf(false)
        return if(result != -1)(result* Const.COUNT_SLIDE) else { -1 }
    }

    fun nextChoiceId(logic: SlideLogic): Long{
        var idMap = Array(100){ i -> false }
        idMap[0] = true
        logic.choiceItems.map { ( (it.id - logic.slideId) / Const.COUNT_CHOICE ).toInt() }.forEach { idMap[it] = true }

        val result = idMap.indexOf(false)
        return if(result != -1)( result * Const.COUNT_CHOICE + logic.slideId ) else { -1 }
    }

    fun nextEnterId(enterItems: MutableList<EnterItem>, choiceId:Long): Long{
        var idMap = Array(100){ i -> false }
        idMap[0] = true
        enterItems.map { ( (it.id - choiceId) / Const.COUNT_ENTER_ID ).toInt() }.forEach { idMap[it] = true }

        val result = idMap.indexOf(false)
        return if(result != -1)( result * Const.COUNT_ENTER_ID + choiceId ) else { -1 }
    }

    fun nextShowConditionId(conditions: MutableList<Condition>, choiceId:Long): Long{
        var idMap = Array(100){ i -> false }
        idMap[0] = true
        conditions.map { ( (it.id - choiceId) / Const.COUNT_SHOW_COND ).toInt() }.forEach { idMap[it] = true }
        val result = idMap.indexOf(false)
        return if(result != -1)( result * Const.COUNT_SHOW_COND + choiceId ) else { -1 }
    }

    fun nextEnterConditionId(conditions: MutableList<Condition>, enterId:Long): Long{
        var idMap = Array(100){ i -> false }
        idMap[0] = true
        conditions.map { ( it.id - enterId ).toInt() }.forEach { idMap[it] = true }
        val result = idMap.indexOf(false)
        return if(result != -1)( result + enterId ) else { -1 }
    }

    fun getSlideIdFromOther(id:Long): Long{
        return (id / COUNT_SLIDE)*COUNT_SLIDE
    }
}