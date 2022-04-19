package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.cheesejuice.fancymansion.model.EnterItem
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.view.EditConditionListAdapter
import com.cheesejuice.fancymansion.view.EditConditionListDragCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditEnterActivity : AppCompatActivity(), View.OnClickListener  {
    private lateinit var binding: ActivityEditEnterBinding
    private lateinit var logic: Logic
    private lateinit var enterItem: EnterItem
    private var slideId: Long = Const.ID_NOT_FOUND
    private var choiceId: Long = Const.ID_NOT_FOUND
    private var enterId: Long = Const.ID_NOT_FOUND
    private var isMenuItemEnabled = true
    private var makeEnter = false

    private lateinit var editEnterConditionListAdapter: EditConditionListAdapter
    private lateinit var selectSlideAdapter: ArrayAdapter<String>

    @Inject
    lateinit var bookUtil: BookUtil

    private val editEnterConditionForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val init = loadData(slideId, choiceId)
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

        slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND)
        choiceId = intent.getLongExtra(Const.INTENT_CHOICE_ID, Const.ID_NOT_FOUND)
        enterId = intent.getLongExtra(Const.INTENT_ENTER_ID, Const.ID_NOT_FOUND)
        if(slideId == Const.ID_NOT_FOUND || choiceId == Const.ID_NOT_FOUND || enterId == Const.ID_NOT_FOUND){
            makeNotHaveEnterItem()
            return
        }

        if (enterId == Const.ADD_NEW_ENTER) {
            makeEnter = true
            binding.toolbar.title = getString(R.string.toolbar_add_enter)
            binding.btnSaveEnter.text = getString(R.string.add_common)
        }else{
            binding.toolbar.title = getString(R.string.toolbar_edit_enter)
            binding.btnSaveEnter.text = getString(R.string.update_common)
        }

        val init = loadData(slideId, choiceId, makeEnter)
        initEditConditionListView()
        initSelectSpinner()
        if(init) {
            makeEditEnterScreen(logic, enterItem)
        }else{
            makeNotHaveEnterItem()
        }
    }

    private fun loadData(slideId:Long, choiceId:Long, makeEnter:Boolean = false): Boolean{
        (application as MainApplication).logic?.also {  itLogic ->
            logic = itLogic
            logic.logics.find {it.slideId == slideId }?.choiceItems?.find{it.id == choiceId }?.enterItems?.also {  enterList ->
                if (makeEnter) {
                    val nextEnterId = bookUtil.nextEnterId(enterList, choiceId)
                    if(nextEnterId > 0){
                        enterId = nextEnterId
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

    private fun initEditConditionListView(){
        editEnterConditionListAdapter = EditConditionListAdapter(bookUtil)
        editEnterConditionListAdapter.setItemClickListener(object: EditConditionListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(this@EditEnterActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choiceId)
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
                setResult(Activity.RESULT_OK)
                finish()
            }

            R.id.btnCancelEnter -> {
                if(makeEnter){
                    deleteEnter(logic, slideId, choiceId, enterId)
                }

                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            R.id.tvAddEnterCondition -> {
                val intent = Intent(this@EditEnterActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choiceId)
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
                deleteEnter(logic, slideId, choiceId, enterId)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteEnter(logic: Logic, slideId:Long, choiceId:Long, enterId:Long){
        logic.logics.find {
            it.slideId == slideId }?.choiceItems?.find{
            it.id == choiceId }?.enterItems?.
        removeIf { it.id == enterId }
    }
}