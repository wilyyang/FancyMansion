package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.cheesejuice.fancymansion.databinding.ActivityEditConditionBinding
import com.cheesejuice.fancymansion.databinding.ActivityEditEnterBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Condition
import com.cheesejuice.fancymansion.model.EnterItem
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.util.BookUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditConditionActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditConditionBinding
    private lateinit var logic: Logic
    private lateinit var condition: Condition
    private var slideId: Long = Const.ID_NOT_FOUND
    private var choiceId: Long = Const.ID_NOT_FOUND
    private var enterId: Long = Const.ID_NOT_FOUND
    private var conditionId: Long = Const.ID_NOT_FOUND
    var isMenuItemEnabled = true
    var isShowCondition = false

    @Inject
    lateinit var bookUtil: BookUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditConditionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnSaveCondition.setOnClickListener (this)
        binding.btnCancelCondition.setOnClickListener (this)

        slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND)
        choiceId = intent.getLongExtra(Const.INTENT_CHOICE_ID, Const.ID_NOT_FOUND)
        enterId = intent.getLongExtra(Const.INTENT_ENTER_ID, Const.ID_NOT_FOUND)
        conditionId = intent.getLongExtra(Const.INTENT_CONDITION_ID, Const.ID_NOT_FOUND)
        isShowCondition = intent.getBooleanExtra(Const.INTENT_SHOW_CONDITION, false)


        if( (slideId == Const.ID_NOT_FOUND || choiceId == Const.ID_NOT_FOUND || conditionId == Const.ID_NOT_FOUND) &&
            (isShowCondition || enterId==Const.ID_NOT_FOUND) ){
            makeNotHaveCondition()
            return
        }

        var makeCondition = false
        if (conditionId == Const.ADD_NEW_CONDITION) {
            makeCondition = true
            binding.toolbar.title = getString(R.string.toolbar_add_condition)
            binding.btnSaveCondition.text = getString(R.string.add_common)
        }else{
            binding.toolbar.title = getString(R.string.toolbar_edit_condition)
            binding.btnSaveCondition.text = getString(R.string.update_common)
        }

        val init = loadData(slideId, choiceId, enterId, makeEnter)
        initSelectSpinner()

        if(init) {
            makeEditConditionScreen(logic, enterItem)
        }else{
            makeNotHaveCondition()
        }
    }

    private fun loadData(slideId:Long, choiceId:Long, enterId: Long, conditionId:Long, isShowCondition:Boolean, makeCondition:Boolean = false): Boolean{
        (application as MainApplication).logic?.also {  itLogic ->
            logic = itLogic
            logic.logics.find {it.slideId == slideId }?.choiceItems?.find{it.id == choiceId }?.enterItems?.also {  enterList ->
                if (makeEnter) {
                    val nextEnterId = bookUtil.nextEnterId(enterList, choiceId)
                    if(nextEnterId > 0){
                        enterItem = EnterItem(nextEnterId)
                        enterList.add(enterItem)
                        return true
                    }
                }else{
                    enterList.find {it.id == enterId}?.let {
                        enterItem = it
                        return true
                    }
                }
            }
        }
        return false
    }



    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }
}