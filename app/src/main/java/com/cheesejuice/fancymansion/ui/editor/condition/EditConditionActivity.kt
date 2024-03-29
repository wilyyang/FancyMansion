package com.cheesejuice.fancymansion.ui.editor.condition

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.cheesejuice.fancymansion.*
import com.cheesejuice.fancymansion.databinding.ActivityEditConditionBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.data.models.ChoiceItem
import com.cheesejuice.fancymansion.data.models.Condition
import com.cheesejuice.fancymansion.data.models.Logic
import com.cheesejuice.fancymansion.data.models.SlideLogic
import com.cheesejuice.fancymansion.ui.editor.guide.GuideActivity
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject


@AndroidEntryPoint
class EditConditionActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditConditionBinding

    // not modified member
    private val logic: Logic by lazy {
        (application as MainApplication).logic!!
    }
    private val slideLogic: SlideLogic by lazy {
        (application as MainApplication).slideLogic!!
    }
    private val choice: ChoiceItem by lazy {
        (application as MainApplication).choice!!
    }

    // copy member
    private lateinit var condition: Condition

    // intent value
    private var conditionId: Long = Const.ID_NOT_FOUND

    private var isMenuItemEnabled = false
    private var isShowCondition = false
    private var makeCondition = false

    // ui
    private lateinit var selectId1SlideAdapter: ArrayAdapter<String>
    private lateinit var selectId1ChoiceAdapter: ArrayAdapter<String>
    private lateinit var selectId2SlideAdapter: ArrayAdapter<String>
    private lateinit var selectId2ChoiceAdapter: ArrayAdapter<String>

    private var choice1InitPos = -1
    private var choice2InitPos = -1

    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditConditionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_get_info_condition))

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnSaveCondition.setOnClickListener (this)
        binding.btnCancelCondition.setOnClickListener (this)

        conditionId = intent.getLongExtra(Const.INTENT_CONDITION_ID, Const.ID_NOT_FOUND)
        isShowCondition = intent.getBooleanExtra(Const.INTENT_SHOW_CONDITION, false)

        if (conditionId == Const.ADD_NEW_CONDITION) {
            makeCondition = true
            binding.toolbar.title = getString(R.string.toolbar_add_condition)
            binding.btnSaveCondition.text = getString(R.string.add_common)
        }else{
            binding.toolbar.title = getString(R.string.toolbar_edit_condition)
            binding.btnSaveCondition.text = getString(R.string.update_common)
        }

        val init = loadData(conditionId, isShowCondition, makeCondition)
        initSelectSpinner()
        if(init) {
            makeEditConditionScreen(logic, condition)
        }else{
            makeNotHaveCondition()
        }
    }

    private fun loadData(_conditionId: Long, isShowCondition: Boolean, makeCondition: Boolean = false): Boolean {
        if (isShowCondition) {
            if (makeCondition) {
                // next show condition id
                val showConditionId = preferenceProvider.nextShowConditionId(choice.showConditions, choice.id)
                if (showConditionId > 0) {
                    conditionId = showConditionId
                    condition = Condition(showConditionId)
                    return true
                }
            } else {
                // find show condition
                choice.showConditions.find { it.id == _conditionId }?.let {
                    condition = Json.decodeFromString(Json.encodeToString(it))
                    return true
                }
            }
        } else {
            (application as MainApplication).enter?.also { enter ->
                if (makeCondition) {
                    // next enter condition id
                    val enterConditionId = preferenceProvider.nextEnterConditionId(enter.enterConditions, enter.id)
                    if (enterConditionId > 0) {
                        conditionId = enterConditionId
                        condition = Condition(enterConditionId)
                        return true
                    }
                } else {
                    // find enter condition
                    enter.enterConditions.find { it.id == _conditionId }?.let {
                        condition = Json.decodeFromString(Json.encodeToString(it))
                        return true
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
            addAll(resources.getStringArray(R.array.cond_operator).toList())
        }

        binding.spinnerOperator.apply {
            adapter = opAdapter
        }

        val nextAdapter = ArrayAdapter<String>(this@EditConditionActivity, android.R.layout.simple_spinner_item).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            addAll(resources.getStringArray(R.array.cond_next).toList())
        }

        binding.spinnerNext.apply {
            adapter = nextAdapter
        }
    }

    private fun makeEditConditionScreen(logic: Logic, condition: Condition) {
        with(binding){
            showLoadingScreen(false, layoutLoading.root, layoutActive, "")
            binding.tvCondId.text = "${condition.id}"

            // init condition 1 spinner
            selectId1SlideAdapter.run {
                clear()
                addAll(logic.logics.map { "[#${it.slideId}] ${it.slideTitle}" })
                notifyDataSetChanged()
            }

            // slide spinner 1 select listener
            spinnerSlideCondition1.run {
                onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectId1ChoiceAdapter.run {
                            clear()
                            addAll(logic.logics[position].choiceItems.map { "[#${it.id}] ${it.title}" })
                            add("[${getString(R.string.choice_only_slide_enter)}]")
                            notifyDataSetChanged()
                        }
                        if(choice1InitPos > -1){
                            spinnerChoiceCondition1.setSelection(choice1InitPos)
                            choice1InitPos = -1
                        }else{
                            spinnerChoiceCondition1.setSelection(logic.logics[position].choiceItems.size)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            // select radio button
            radioGroupCondOption.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radioCondCount -> {
                        layoutCondCount.visibility = View.VISIBLE
                        layoutCondId2.visibility = View.INVISIBLE
                    }

                    R.id.radioCondId -> {
                        layoutCondCount.visibility = View.INVISIBLE
                        layoutCondId2.visibility = View.VISIBLE
                    }
                }
            }

            // init number picker layout
            pickerCount.apply {
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

            // slide spinner 2 select listener
            spinnerSlideCondition2.run {
                onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectId2ChoiceAdapter.run {
                            clear()
                            addAll(logic.logics[position].choiceItems.map { "[#${it.id}] ${it.title}" })
                            add("[${getString(R.string.choice_only_slide_enter)}]")
                            notifyDataSetChanged()
                        }
                        if(choice2InitPos > -1){
                            spinnerChoiceCondition2.setSelection(choice2InitPos)
                            choice2InitPos = -1
                        }else{
                            spinnerChoiceCondition2.setSelection(logic.logics[position].choiceItems.size)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            // set operator / next spinner
            val opIdx = CondOp.values().indexOfFirst { it.opName == condition.conditionOp }
            val nextIdx = CondNext.values().indexOfFirst { it.relName == condition.conditionNext }
            spinnerOperator.setSelection(opIdx)
            spinnerNext.setSelection(nextIdx)

            // set first value
            val slideIdx1 = logic.logics.indexOfFirst {  it.slideId == preferenceProvider.getSlideIdFromOther(condition.conditionId1) }.let {
                if (it > 0) { it } else { 0 }
            }
            logic.logics[slideIdx1].choiceItems.let { list ->
                list.indexOfFirst { it.id == condition.conditionId1 }.let {
                    if (it != -1) { choice1InitPos = it }
                }
            }
            spinnerSlideCondition1.setSelection(slideIdx1)

            // set second value
            if(condition.conditionId2 < 1){
                radioCondCount.isChecked = true
                radioCondId.isChecked = false

                pickerCount.value = condition.conditionCount
            }else{
                radioCondCount.isChecked = false
                radioCondId.isChecked = true

                val slideIdx2 = logic.logics.indexOfFirst { it.slideId == preferenceProvider.getSlideIdFromOther(condition.conditionId2) }.let {
                    if (it > 0) { it } else { 0 }
                }
                logic.logics[slideIdx2].choiceItems.let { list ->
                    list.indexOfFirst { it.id == condition.conditionId2 }.let {
                        if (it != -1){ choice2InitPos = it }
                    }
                }
                spinnerSlideCondition2.setSelection(slideIdx2)
            }
        }
        isMenuItemEnabled = true
    }

    private fun makeNotHaveCondition() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        isMenuItemEnabled = false
    }


    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btnSaveCondition -> {
                updateCondition()

                (application as MainApplication).condition = condition
                if (makeCondition) {
                    setResult(Const.RESULT_NEW)
                } else {
                    setResult(Const.RESULT_UPDATE)
                }
                finish()
            }

            R.id.btnCancelCondition -> {
                setResult(Const.RESULT_CANCEL)
                finish()
            }
        }
    }

    fun updateCondition(){
        condition.apply {
            val slideLogic1 = logic.logics[binding.spinnerSlideCondition1.selectedItemPosition]
            conditionId1 = if(slideLogic1.choiceItems.size == binding.spinnerChoiceCondition1.selectedItemPosition){
                slideLogic1.slideId
            }else{
                slideLogic1.choiceItems[binding.spinnerChoiceCondition1.selectedItemPosition].id
            }

            conditionOp = CondOp.values()[binding.spinnerOperator.selectedItemPosition].opName

            when(binding.radioGroupCondOption.checkedRadioButtonId){
                binding.radioCondCount.id -> {
                    conditionCount = binding.pickerCount.value
                    conditionId2 = Const.ID_NOT_FOUND
                }
                binding.radioCondId.id -> {
                    conditionCount = 0
                    val slideLogic2 = logic.logics[binding.spinnerSlideCondition2.selectedItemPosition]
                    conditionId2 = if(slideLogic2.choiceItems.size == binding.spinnerChoiceCondition2.selectedItemPosition){
                        slideLogic2.slideId
                    }else{
                        slideLogic2.choiceItems[binding.spinnerChoiceCondition2.selectedItemPosition].id
                    }
                }
            }
            conditionNext = CondNext.values()[binding.spinnerNext.selectedItemPosition].relName
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
            R.id.menu_copy -> {
                updateCondition()

                (application as MainApplication).condition = condition
                if (makeCondition) {
                    setResult(Const.RESULT_NEW_COPY)
                } else {
                    setResult(Const.RESULT_UPDATE_COPY)
                }
                finish()
            }

            R.id.menu_delete -> {
                if (makeCondition) {
                    setResult(Const.RESULT_NEW_DELETE)
                } else {
                    (application as MainApplication).condition = condition
                    setResult(Const.RESULT_DELETE)
                }
                finish()
            }

            R.id.menu_guide -> {
                val intent = Intent(this@EditConditionActivity, GuideActivity::class.java)
                intent.putExtra(Const.INTENT_GUIDE, Const.GUIDE_CONDITION)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}