package com.cheesejuice.fancymansion

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.cheesejuice.fancymansion.databinding.ActivityEditConditionBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Condition
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

    private lateinit var selectId1SlideAdapter: ArrayAdapter<String>
    private lateinit var selectId1ChoiceAdapter: ArrayAdapter<String>
    private lateinit var selectId2SlideAdapter: ArrayAdapter<String>
    private lateinit var selectId2ChoiceAdapter: ArrayAdapter<String>

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

        if( slideId == Const.ID_NOT_FOUND || choiceId == Const.ID_NOT_FOUND || conditionId == Const.ID_NOT_FOUND ||
            ( !isShowCondition && enterId == Const.ID_NOT_FOUND) ){
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

        val init = loadData(slideId, choiceId, enterId, conditionId, isShowCondition, makeCondition)
        initSelectSpinner()

        if(init) {
            makeEditConditionScreen(logic, condition)
        }else{
            makeNotHaveCondition()
        }
    }

    private fun loadData(slideId:Long, choiceId:Long, enterId: Long, conditionId:Long, isShowCondition:Boolean, makeCondition:Boolean = false): Boolean{
        (application as MainApplication).logic?.also {  itLogic ->
            logic = itLogic
            logic.logics.find {it.slideId == slideId }?.choiceItems?.find{it.id == choiceId }?.also{  itChoice ->
                if(isShowCondition){
                    if(makeCondition){
                        // next show condition id
                        val showConditionId = bookUtil.nextShowConditionId(itChoice.showConditions, choiceId)
                        if(showConditionId > 0){
                            // make show condition
                            condition = Condition(showConditionId)
                            // add  show condition
                            itChoice.showConditions.add(condition)
                            return true
                        }
                    }else{
                        // find show condition
                        itChoice.showConditions.find {it.id == conditionId}?.let {
                            // set  show condition
                            condition = it
                            return true
                        }
                    }
                }else{
                    itChoice.enterItems.find { it.id == enterId }?.enterConditions?.also { itEnterConditions ->
                        if(makeCondition){
                            // next enter condition id
                            val enterConditionId = bookUtil.nextEnterConditionId(itEnterConditions, enterId)
                            if(enterConditionId > 0){

                                // make enter condition
                                condition = Condition(enterConditionId)
                                // add  enter condition
                                itEnterConditions.add(condition)
                                return true
                            }
                        }else{
                            // find enter condition
                            itEnterConditions.find { it.id == conditionId }?.let {
                                // set  enter condition
                                condition = it
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun initSelectSpinner(){
        selectId1SlideAdapter = ArrayAdapter(this@EditConditionActivity, android.R.layout.simple_spinner_item)
        selectId1SlideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerSlideCondition1.apply {
            adapter = selectId1SlideAdapter
        }

        selectId1ChoiceAdapter = ArrayAdapter(this@EditConditionActivity, android.R.layout.simple_spinner_item)
        selectId1ChoiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerChoiceCondition1.apply {
            adapter = selectId1ChoiceAdapter
        }

        selectId2SlideAdapter = ArrayAdapter(this@EditConditionActivity, android.R.layout.simple_spinner_item)
        selectId2SlideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerSlideCondition2.apply {
            adapter = selectId2SlideAdapter
        }

        selectId2ChoiceAdapter = ArrayAdapter(this@EditConditionActivity, android.R.layout.simple_spinner_item)
        selectId2ChoiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerChoiceCondition2.apply {
            adapter = selectId2ChoiceAdapter
        }

        val opAdapter = ArrayAdapter<String>(this@EditConditionActivity, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addAll(CondOp.values().map { it.opName })
        }

        binding.spinnerOperator.apply {
            adapter = opAdapter
        }

        val nextAdapter = ArrayAdapter<String>(this@EditConditionActivity, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addAll(CondNext.values().map { it.relName })
        }

        binding.spinnerNext.apply {
            adapter = nextAdapter
        }
    }

    private fun makeEditConditionScreen(logic:Logic, condition: Condition) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.toolbar.subtitle = "id : ${condition.id}"

        // init condition 1 spinner
        selectId1SlideAdapter.run {
            clear()
            addAll(logic.logics.map { "[#${it.slideId}] ${it.slideTitle}" })
            notifyDataSetChanged()
        }

        binding.spinnerSlideCondition1.run {
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectId1ChoiceAdapter.run {
                        clear()
                        addAll(logic.logics[position].choiceItems.map { "[#${it.id}] ${it.title}" })
                        add("[${getString(R.string.choice_only_slide_enter)}]")
                        notifyDataSetChanged()

                        binding.spinnerChoiceCondition1.setSelection(logic.logics[position].choiceItems.size)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // select radio button
        binding.radioGroupCondOption.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioCondCount -> {
                    binding.layoutCondCount.visibility = View.VISIBLE
                    binding.layoutCondId2.visibility = View.INVISIBLE
                }

                R.id.radioCondId -> {
                    binding.layoutCondCount.visibility = View.INVISIBLE
                    binding.layoutCondId2.visibility = View.VISIBLE
                }
            }
        }

        // init number picker layout
        binding.pickerCount.apply {
            minValue = 0
            maxValue = 99
            wrapSelectorWheel = false
        }

        // init comparison layout
        selectId2SlideAdapter.run {
            clear()
            addAll(logic.logics.map { "[#${it.slideId}] ${it.slideTitle}" })
            notifyDataSetChanged()
        }

        binding.spinnerSlideCondition2.run {
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectId2ChoiceAdapter.run {
                        clear()
                        addAll(logic.logics[position].choiceItems.map { "[#${it.id}] ${it.title}" })
                        add("[${getString(R.string.choice_only_slide_enter)}]")
                        notifyDataSetChanged()

                        binding.spinnerChoiceCondition2.setSelection(logic.logics[position].choiceItems.size)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // init operator / next spinner
        val opIdx = CondOp.values().indexOfFirst { it.opName == condition.conditionOp }
        val nextIdx = CondNext.values().indexOfFirst { it.relName == condition.conditionNext }

        // set first value
        binding.spinnerSlideCondition1.run {
            val slideIdx1 = logic.logics.indexOfFirst {  it.slideId == bookUtil.getSlideIdFromOther(condition.conditionId1) }
            setSelection(slideIdx1)

            val choiceIdx1 = logic.logics[slideIdx1].choiceItems.let { list ->
                list.indexOfFirst { it.id == condition.conditionId1 }.let {
                    if (it == -1){
                        list.size
                    }else{
                        it
                    }
                }
            }
            binding.spinnerChoiceCondition1.setSelection(choiceIdx1)
        }

        // set second value
        if(condition.conditionId2 < 1){
            with(binding){
                radioCondCount.isChecked = true
                radioCondId.isChecked = false

                pickerCount.value = condition.conditionCount
            }
        }else{
            with(binding){
                radioCondCount.isChecked = false
                radioCondId.isChecked = true

                spinnerSlideCondition2.run {
                    val slideIdx2 = logic.logics.indexOfFirst { it.slideId == bookUtil.getSlideIdFromOther(condition.conditionId2) }
                    setSelection(slideIdx2)

                    val choiceIdx2 = logic.logics[slideIdx2].choiceItems.let { list ->
                        list.indexOfFirst { it.id == condition.conditionId2 }.let {
                            if (it == -1){
                                list.size
                            }else{
                                it
                            }
                        }
                    }
                    spinnerChoiceCondition2.setSelection(choiceIdx2)
                }
            }
        }

        // set operator / next value
        binding.spinnerOperator.setSelection(opIdx)
        binding.spinnerNext.setSelection(nextIdx)

        isMenuItemEnabled = true
    }

    private fun makeNotHaveCondition() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        isMenuItemEnabled = false
    }


    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btnSaveCondition -> {
                // NOT IMPLEMENTED
            }

            R.id.btnCancelCondition -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_choice, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this@EditConditionActivity.currentFocus?.let { it.clearFocus() }
        if (!isMenuItemEnabled) {
            return true
        }

        when(item.itemId) {
            R.id.menu_delete -> {
                // NOT IMPLEMENTED
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}