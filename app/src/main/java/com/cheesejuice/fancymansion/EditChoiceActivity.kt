package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.cheesejuice.fancymansion.databinding.ActivityEditChoiceBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.ChoiceItem
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.model.SlideLogic
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.view.EditConditionListAdapter
import com.cheesejuice.fancymansion.view.EditConditionListDragCallback
import com.cheesejuice.fancymansion.view.EditEnterListAdapter
import com.cheesejuice.fancymansion.view.EditEnterListDragCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditChoiceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditChoiceBinding
    private lateinit var logic: Logic
    private lateinit var slideLogic: SlideLogic
    private lateinit var choice: ChoiceItem
    private var slideId: Long = Const.ID_NOT_FOUND
    private var choiceId: Long = Const.ID_NOT_FOUND
    var isMenuItemEnabled = true

    private lateinit var editEnterListAdapter: EditEnterListAdapter
    private lateinit var editShowConditionListAdapter: EditConditionListAdapter

    @Inject
    lateinit var bookUtil: BookUtil

    private val editEnterForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val init = loadData(slideId)
                initEditEnterListView()
                if(init) {
                    makeEditChoiceScreen(choice)
                }else{
                    makeNotHaveChoice()
                }
            }
        }

    private val editShowConditionForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val init = loadData(slideId)
                initEditShowConditionListView()
                if(init) {
                    makeEditChoiceScreen(choice)
                }else{
                    makeNotHaveChoice()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnCancelChoice.setOnClickListener (this)
        binding.btnSaveChoice.setOnClickListener (this)
        binding.tvAddEnter.setOnClickListener(this)
        binding.tvAddShowCondition.setOnClickListener(this)

        slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND)
        choiceId = intent.getLongExtra(Const.INTENT_CHOICE_ID, Const.ID_NOT_FOUND)
        if(slideId == Const.ID_NOT_FOUND || choiceId == Const.ID_NOT_FOUND){
            makeNotHaveChoice()
            return
        }

        var makeChoice = false
        if (choiceId == Const.ADD_NEW_CHOICE) {
            makeChoice = true
            binding.toolbar.title = getString(R.string.toolbar_add_choice)
            binding.btnSaveChoice.text = getString(R.string.add_common)
        }else{
            binding.toolbar.title = getString(R.string.toolbar_edit_choice)
            binding.btnSaveChoice.text = getString(R.string.update_common)
        }

        val init = loadData(slideId, makeChoice)
        initEditEnterListView()
        initEditShowConditionListView()
        if(init) {
            makeEditChoiceScreen(choice)
        }else{
            makeNotHaveChoice()
        }
    }

    private fun loadData(slideId:Long, makeChoice:Boolean = false): Boolean{
        (application as MainApplication).logic?.also {  itLogic ->
            logic = itLogic
            logic.logics.find { it.slideId == slideId }?.also { itSlideLogic ->
                slideLogic = itSlideLogic
                if (makeChoice) {
                    val nextChoiceId = bookUtil.nextChoiceId(slideLogic)
                    if(nextChoiceId > 0){
                        choiceId = nextChoiceId
                        choice = ChoiceItem(nextChoiceId, getString(R.string.name_choice_prefix))
                        slideLogic.choiceItems.add(choice)
                        return true
                    }
                } else {
                    slideLogic.choiceItems.find { it.id == choiceId }?.let {
                        choice = it
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun initEditEnterListView(){
        editEnterListAdapter = EditEnterListAdapter()
        editEnterListAdapter.setItemClickListener(object: EditEnterListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                choice.title = binding.etChoiceTitle.text.toString()

                val intent = Intent(this@EditChoiceActivity, EditEnterActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choiceId)
                    putExtra(Const.INTENT_ENTER_ID, choice.enterItems[position].id)
                }

                editEnterForResult.launch(intent)
            }
        })

        binding.recyclerEditEnter.layoutManager = LinearLayoutManager(baseContext)
        binding.recyclerEditEnter.adapter = editEnterListAdapter

        val touchHelper = ItemTouchHelper(EditEnterListDragCallback(editEnterListAdapter))
        touchHelper.attachToRecyclerView(binding.recyclerEditEnter)
    }

    private fun initEditShowConditionListView(){
        editShowConditionListAdapter = EditConditionListAdapter(bookUtil)
        editShowConditionListAdapter.setItemClickListener(object: EditConditionListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                val intent = Intent(this@EditChoiceActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choiceId)
                    putExtra(Const.INTENT_CONDITION_ID, choice.showConditions[position].id)
                    putExtra(Const.INTENT_SHOW_CONDITION, true)
                }

                editShowConditionForResult.launch(intent)
            }
        })

        binding.recyclerEditShowCondition.layoutManager = LinearLayoutManager(baseContext)
        binding.recyclerEditShowCondition.adapter = editShowConditionListAdapter

        val touchHelper = ItemTouchHelper(EditConditionListDragCallback(editShowConditionListAdapter))
        touchHelper.attachToRecyclerView(binding.recyclerEditShowCondition)
    }

    private fun makeEditChoiceScreen(choice: ChoiceItem) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        with(choice){
            binding.toolbar.subtitle = "id : ${choice.id}"
            binding.etChoiceTitle.setText(title)
        }

        editEnterListAdapter.datas = choice.enterItems
        editEnterListAdapter.logic = logic
        editEnterListAdapter.notifyDataSetChanged()

        editShowConditionListAdapter.datas = choice.showConditions
        editShowConditionListAdapter.notifyDataSetChanged()

        isMenuItemEnabled = true
    }

    private fun makeNotHaveChoice() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        isMenuItemEnabled = false
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.tvAddEnter -> {
                choice.title = binding.etChoiceTitle.text.toString()

                val intent = Intent(this, EditEnterActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choiceId)
                    putExtra(Const.INTENT_ENTER_ID, Const.ADD_NEW_ENTER)
                }

                editEnterForResult.launch(intent)
            }

            R.id.tvAddShowCondition -> {
                val intent = Intent(this@EditChoiceActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choiceId)
                    putExtra(Const.INTENT_CONDITION_ID, Const.ADD_NEW_CONDITION)
                    putExtra(Const.INTENT_SHOW_CONDITION, true)
                }

                editShowConditionForResult.launch(intent)
            }

            R.id.btnSaveChoice -> {
                choice.title = binding.etChoiceTitle.text.toString()
                setResult(Activity.RESULT_OK)
                finish()
            }

            R.id.btnCancelChoice -> {
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
        this@EditChoiceActivity.currentFocus?.let { it.clearFocus() }
        if (!isMenuItemEnabled) {
            return true
        }

        when(item.itemId) {
            R.id.menu_delete -> {
                slideLogic.choiceItems.removeIf { it.id == choiceId }
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}