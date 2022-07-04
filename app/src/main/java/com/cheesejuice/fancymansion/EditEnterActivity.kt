package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.cheesejuice.fancymansion.databinding.ActivityEditEnterBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.*
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.view.EditConditionListAdapter
import com.cheesejuice.fancymansion.view.EditConditionListDragCallback
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class EditEnterActivity : AppCompatActivity(), View.OnClickListener  {
    private lateinit var binding: ActivityEditEnterBinding

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
    private lateinit var enterItem: EnterItem

    // intent value
    private var enterId: Long = Const.ID_NOT_FOUND

    private var isMenuItemEnabled = false
    private var makeEnter = false

    // ui
    private lateinit var editEnterConditionListAdapter: EditConditionListAdapter
    private lateinit var selectSlideAdapter: ArrayAdapter<String>

    @Inject
    lateinit var bookUtil: BookUtil

    private val editEnterConditionForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            (application as MainApplication).enter = null
            (application as MainApplication).condition?.let {  condition ->
                when (result.resultCode) {
                    Const.RESULT_NEW -> {
                        enterItem.enterConditions.add(Json.decodeFromString(Json.encodeToString(condition)))
                    }
                    Const.RESULT_UPDATE -> {
                        enterItem.enterConditions[enterItem.enterConditions.indexOfFirst { it.id == condition.id }] =
                            Json.decodeFromString(Json.encodeToString(condition))
                    }
                    Const.RESULT_NEW_COPY -> {
                        enterItem.enterConditions.add(Json.decodeFromString(Json.encodeToString(condition)))
                        copyCondition(condition)
                    }
                    Const.RESULT_UPDATE_COPY -> {
                        enterItem.enterConditions[enterItem.enterConditions.indexOfFirst { it.id == condition.id }] =
                            Json.decodeFromString(Json.encodeToString(condition))
                        copyCondition(condition)
                    }
                    Const.RESULT_DELETE -> {
                        enterItem.enterConditions.removeIf { it.id == condition.id }
                    }
                }

                makeEditEnterScreen(logic, enterItem)
                (application as MainApplication).condition = null
            }
        }

    private fun copyCondition(condition:Condition){
        val newEnterConditionId = bookUtil.nextEnterConditionId(enterItem.enterConditions, enterItem.id)
        if (newEnterConditionId > 0) {
            enterItem.enterConditions.add(Json.decodeFromString<Condition>(Json.encodeToString(condition)).apply {
                id = newEnterConditionId
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEnterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_get_info_enter))

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnCancelEnter.setOnClickListener (this)
        binding.btnSaveEnter.setOnClickListener (this)
        binding.tvAddEnterCondition.setOnClickListener(this)

        enterId = intent.getLongExtra(Const.INTENT_ENTER_ID, Const.ID_NOT_FOUND)

        if (enterId == Const.ADD_NEW_ENTER) {
            makeEnter = true
            binding.toolbar.title = getString(R.string.toolbar_add_enter)
            binding.btnSaveEnter.text = getString(R.string.add_common)
        }else{
            binding.toolbar.title = getString(R.string.toolbar_edit_enter)
            binding.btnSaveEnter.text = getString(R.string.update_common)
        }

        val init = loadData(makeEnter)
        initEditConditionListView()
        initSelectSpinner()
        if(init) {
            makeEditEnterScreen(logic, enterItem)
        }else{
            makeNotHaveEnterItem()
        }
    }

    private fun loadData(makeEnter:Boolean = false): Boolean{
        if (makeEnter) {
            val nextEnterId = bookUtil.nextEnterId(choice.enterItems, choice.id)
            if(nextEnterId > 0){
                enterId = nextEnterId
                enterItem = EnterItem(nextEnterId)
                return true
            }
        }else{
            choice.enterItems.find { it.id == enterId }?.let {
                enterItem = Json.decodeFromString(Json.encodeToString(it))
                return true
            }
        }
        return false
    }

    private fun initEditConditionListView(){
        editEnterConditionListAdapter = EditConditionListAdapter(bookUtil, context = this@EditEnterActivity)
        editEnterConditionListAdapter.setItemClickListener(object: EditConditionListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                enterItem.enterSlideId = logic.logics[binding.spinnerSelectSlide.selectedItemPosition].slideId
                (application as MainApplication).enter = Json.decodeFromString(Json.encodeToString(enterItem))
                val intent = Intent(this@EditEnterActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_CONDITION_ID, enterItem.enterConditions[position].id)
                    putExtra(Const.INTENT_SHOW_CONDITION, false)
                }

                editEnterConditionForResult.launch(intent)
            }
        })

        binding.recyclerEditEnterCondition.layoutManager = LinearLayoutManager(baseContext)
        binding.recyclerEditEnterCondition.adapter = editEnterConditionListAdapter

        val touchHelper = ItemTouchHelper(EditConditionListDragCallback(editEnterConditionListAdapter))
        touchHelper.attachToRecyclerView(binding.recyclerEditEnterCondition)
    }

    private fun initSelectSpinner(){
        selectSlideAdapter = ArrayAdapter(this@EditEnterActivity, android.R.layout.simple_spinner_item)
        selectSlideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerSelectSlide.apply {
            adapter = selectSlideAdapter
        }
    }

    private fun makeEditEnterScreen(logic:Logic, enterItem: EnterItem) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
        binding.tvEnterId.text = "${enterItem.id}"

        editEnterConditionListAdapter.datas = enterItem.enterConditions
        editEnterConditionListAdapter.notifyDataSetChanged()

        // Spinner
        selectSlideAdapter.run {
            clear()
            addAll(logic.logics.map { "[#${it.slideId}] ${it.slideTitle}" })
            notifyDataSetChanged()
        }

        val slideIdx = logic.logics.indexOfFirst { it.slideId == enterItem.enterSlideId }
        binding.spinnerSelectSlide.setSelection(slideIdx)

        updateEmptyCondition()
        isMenuItemEnabled = true
    }

    private fun makeNotHaveEnterItem() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        isMenuItemEnabled = false
    }

    private fun updateEmptyCondition(){
        if(enterItem.enterConditions.size < 1)
        {
            binding.layoutEmptyCondition.visibility = View.VISIBLE
        }else{
            binding.layoutEmptyCondition.visibility = View.GONE
        }
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btnSaveEnter -> {
                enterItem.enterSlideId = logic.logics[binding.spinnerSelectSlide.selectedItemPosition].slideId
                (application as MainApplication).enter = enterItem
                if (makeEnter) {
                    setResult(Const.RESULT_NEW)
                } else {
                    setResult(Const.RESULT_UPDATE)
                }
                finish()
            }

            R.id.btnCancelEnter -> {
                setResult(Const.RESULT_CANCEL)
                finish()
            }

            R.id.tvAddEnterCondition -> {
                enterItem.enterSlideId = logic.logics[binding.spinnerSelectSlide.selectedItemPosition].slideId
                (application as MainApplication).enter = Json.decodeFromString(Json.encodeToString(enterItem))
                val intent = Intent(this@EditEnterActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_CONDITION_ID, Const.ADD_NEW_CONDITION)
                    putExtra(Const.INTENT_SHOW_CONDITION, false)
                }

                editEnterConditionForResult.launch(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_choice, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this@EditEnterActivity.currentFocus?.let { it.clearFocus() }
        if (!isMenuItemEnabled) {
            return true
        }

        when(item.itemId) {
            R.id.menu_copy -> {
                enterItem.enterSlideId = logic.logics[binding.spinnerSelectSlide.selectedItemPosition].slideId
                (application as MainApplication).enter = enterItem
                if (makeEnter) {
                    setResult(Const.RESULT_NEW_COPY)
                } else {
                    setResult(Const.RESULT_UPDATE_COPY)
                }
                finish()
            }

            R.id.menu_delete -> {
                if (makeEnter) {
                    setResult(Const.RESULT_NEW_DELETE)
                } else {
                    (application as MainApplication).enter = enterItem
                    setResult(Const.RESULT_DELETE)
                }
                finish()
            }

            R.id.menu_guide -> {
                val intent = Intent(this@EditEnterActivity, GuideActivity::class.java)
                intent.putExtra(Const.INTENT_GUIDE, Const.GUIDE_ENTER)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}