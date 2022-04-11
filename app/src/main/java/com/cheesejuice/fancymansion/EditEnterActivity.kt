package com.cheesejuice.fancymansion

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
    var isMenuItemEnabled = true

    private lateinit var editConditionListAdapter: EditConditionListAdapter

    private val editConditionForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // NOT IMPLEMENTED
            }
        }

    @Inject
    lateinit var bookUtil: BookUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEnterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnCancelEnter.setOnClickListener (this)
        binding.btnSaveEnter.setOnClickListener (this)
        binding.tvSelectEnterSlide.setOnClickListener(this)
        binding.tvAddEnterCondition.setOnClickListener(this)

        slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND)
        choiceId = intent.getLongExtra(Const.INTENT_CHOICE_ID, Const.ID_NOT_FOUND)
        enterId = intent.getLongExtra(Const.INTENT_ENTER_ID, Const.ID_NOT_FOUND)
        if(slideId == Const.ID_NOT_FOUND || choiceId == Const.ID_NOT_FOUND || enterId == Const.ID_NOT_FOUND){
            makeNotHaveEnterItem()
            return
        }

        var makeEnter = false
        if (choiceId == Const.ADD_NEW_ENTER) {
            makeEnter = true
            binding.toolbar.title = getString(R.string.toolbar_add_enter)
            binding.btnSaveEnter.text = getString(R.string.add_common)
        }else{
            binding.toolbar.title = getString(R.string.toolbar_edit_enter)
            binding.btnSaveEnter.text = getString(R.string.update_common)
        }

        val init = loadData(slideId, choiceId, enterId, makeEnter)
        initEditConditionListView()
        if(init) {
            makeEditChoiceScreen(enterItem)
        }else{
            makeNotHaveEnterItem()
        }
    }

    private fun loadData(slideId:Long, choiceId:Long, enterId:Long, makeChoice:Boolean = false): Boolean{
        (application as MainApplication).logic?.also {  itLogic ->
            logic = itLogic
            val tempEnter: EnterItem? = logic.logics.find {
                it.slideId == slideId }?.choiceItems?.find{
                it.id == choiceId }?.enterItems?.find {
                it.id == enterId}

            if(tempEnter != null){
                enterItem = tempEnter
                return true
            }
        }
        return false
    }

    private fun initEditConditionListView(){
        editConditionListAdapter = EditConditionListAdapter()
        editConditionListAdapter.setItemClickListener(object: EditConditionListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // NOT IMPLEMENTED
            }
        })

        binding.recyclerEditEnterCondition.layoutManager = LinearLayoutManager(baseContext)
        binding.recyclerEditEnterCondition.adapter = editConditionListAdapter

        val touchHelper = ItemTouchHelper(EditConditionListDragCallback(editConditionListAdapter))
        touchHelper.attachToRecyclerView(binding.recyclerEditEnterCondition)
    }

    private fun makeEditChoiceScreen(enterItem: EnterItem) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.toolbar.subtitle = "# ${enterItem.id}"

        editConditionListAdapter.datas = enterItem.enterConditions
        editConditionListAdapter.logic = logic
        editConditionListAdapter.notifyDataSetChanged()

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
                // NOT IMPLEMENTED
                setResult(Activity.RESULT_OK)
                finish()
            }

            R.id.btnCancelEnter -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            R.id.tvAddEnterCondition -> {
                // NOT IMPLEMENTED
            }

            R.id.tvSelectEnterSlide -> {
                // NOT IMPLEMENTED
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
                logic.logics.find {
                    it.slideId == slideId }?.choiceItems?.find{
                    it.id == choiceId }?.enterItems?.
                    removeIf { it.id == enterId }
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}