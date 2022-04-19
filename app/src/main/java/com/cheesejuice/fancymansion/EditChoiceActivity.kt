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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class EditChoiceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditChoiceBinding

    // not modified member
    private val logic: Logic by lazy {
        (application as MainApplication).logic!!
    }
    private val slideLogic: SlideLogic by lazy {
        (application as MainApplication).slideLogic!!
    }

    // copy member
    private lateinit var choice: ChoiceItem

    // intent value
    private var choiceId: Long = Const.ID_NOT_FOUND

    private var isMenuItemEnabled = true
    private var makeChoice = false

    // ui
    private lateinit var editEnterListAdapter: EditEnterListAdapter
    private lateinit var editShowConditionListAdapter: EditConditionListAdapter

    @Inject
    lateinit var bookUtil: BookUtil

    private val editEnterForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val init = loadData()
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
                val init = loadData()
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

        choiceId = intent.getLongExtra(Const.INTENT_CHOICE_ID, Const.ID_NOT_FOUND)
        if (choiceId == Const.ADD_NEW_CHOICE) {
            makeChoice = true
            binding.toolbar.title = getString(R.string.toolbar_add_choice)
            binding.btnSaveChoice.text = getString(R.string.add_common)
        }else{
            binding.toolbar.title = getString(R.string.toolbar_edit_choice)
            binding.btnSaveChoice.text = getString(R.string.update_common)
        }

        val init = loadData(makeChoice)
        initEditEnterListView()
        initEditShowConditionListView()
        if(init) {
            makeEditChoiceScreen(choice)
        }else{
            makeNotHaveChoice()
        }
    }

    private fun loadData(makeChoice:Boolean = false): Boolean{
        if (makeChoice) {
            val nextChoiceId = bookUtil.nextChoiceId(slideLogic)
            if(nextChoiceId > 0){
                choiceId = nextChoiceId
                choice = ChoiceItem(nextChoiceId, getString(R.string.name_choice_prefix))
                return true
            }
        } else {
            slideLogic.choiceItems.find { it.id == choiceId }?.let {
                choice = Json.decodeFromString(Json.encodeToString(it))
                return true
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
                    putExtra(Const.INTENT_SLIDE_ID, slideLogic.slideId)
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
                    putExtra(Const.INTENT_SLIDE_ID, slideLogic.slideId)
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
                    putExtra(Const.INTENT_SLIDE_ID, slideLogic.slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choiceId)
                    putExtra(Const.INTENT_ENTER_ID, Const.ADD_NEW_ENTER)
                }

                editEnterForResult.launch(intent)
            }

            R.id.tvAddShowCondition -> {
                val intent = Intent(this@EditChoiceActivity, EditConditionActivity::class.java).apply {
                    putExtra(Const.INTENT_SLIDE_ID, slideLogic.slideId)
                    putExtra(Const.INTENT_CHOICE_ID, choiceId)
                    putExtra(Const.INTENT_CONDITION_ID, Const.ADD_NEW_CONDITION)
                    putExtra(Const.INTENT_SHOW_CONDITION, true)
                }

                editShowConditionForResult.launch(intent)
            }

            R.id.btnSaveChoice -> {
                choice.title = binding.etChoiceTitle.text.toString()
                (application as MainApplication).choice = choice
                if(makeChoice){
                    setResult(Const.RESULT_NEW)
                }else{
                    setResult(Const.RESULT_UPDATE)
                }
                finish()
            }

            R.id.btnCancelChoice -> {
                setResult(Const.RESULT_CANCEL)
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
                if(makeChoice){
                    setResult(Const.RESULT_NEW_DELETE)
                }else{
                    (application as MainApplication).choice = choice
                    setResult(Const.RESULT_DELETE)
                }
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}