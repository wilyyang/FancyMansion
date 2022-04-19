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
import com.cheesejuice.fancymansion.model.ChoiceItem
import com.cheesejuice.fancymansion.model.EnterItem
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.model.SlideLogic
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
            if (result.resultCode == Activity.RESULT_OK) {
                val init = loadData()
                initEditConditionListView()
                if(init) {
                    makeEditEnterScreen(logic, enterItem)
                }else{
                    makeNotHaveEnterItem()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEnterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

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
        editEnterConditionListAdapter = EditConditionListAdapter(bookUtil)
        editEnterConditionListAdapter.setItemClickListener(object: EditConditionListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(this@EditEnterActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideLogic.slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choice.id)
                    putExtra(Const.INTENT_ENTER_ID, enterItem.id)
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
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.toolbar.subtitle = "id : ${enterItem.id}"

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

        isMenuItemEnabled = true
    }

    private fun makeNotHaveEnterItem() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        isMenuItemEnabled = false
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
                val intent = Intent(this@EditEnterActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideLogic.slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choice.id)
                    putExtra(Const.INTENT_ENTER_ID, enterItem.id)
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
            R.id.menu_delete -> {
                if (makeEnter) {
                    setResult(Const.RESULT_NEW_DELETE)
                } else {
                    (application as MainApplication).enter = enterItem
                    setResult(Const.RESULT_DELETE)
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}