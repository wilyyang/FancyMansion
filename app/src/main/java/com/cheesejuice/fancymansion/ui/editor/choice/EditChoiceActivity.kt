package com.cheesejuice.fancymansion.ui.editor.choice

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.cheesejuice.fancymansion.*
import com.cheesejuice.fancymansion.data.models.*
import com.cheesejuice.fancymansion.databinding.ActivityEditChoiceBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.ui.editor.condition.EditConditionActivity
import com.cheesejuice.fancymansion.ui.editor.enter.EditEnterActivity
import com.cheesejuice.fancymansion.ui.editor.guide.GuideActivity
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
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

    private var isMenuItemEnabled = false
    private var makeChoice = false

    // ui
    private lateinit var editEnterListAdapter: EditEnterListAdapter
    private lateinit var editShowConditionListAdapter: EditConditionListAdapter

    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    private val editEnterForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            (application as MainApplication).choice = null
            (application as MainApplication).enter?.let { enterItem ->
                when (result.resultCode) {
                    Const.RESULT_NEW -> {
                        choice.enterItems.add(Json.decodeFromString(Json.encodeToString(enterItem)))
                    }
                    Const.RESULT_UPDATE -> {
                        choice.enterItems[choice.enterItems.indexOfFirst { it.id == enterItem.id }] =
                            Json.decodeFromString(Json.encodeToString(enterItem))
                    }
                    Const.RESULT_NEW_COPY -> {
                        choice.enterItems.add(Json.decodeFromString(Json.encodeToString(enterItem)))
                        copyEnterItem(enterItem)
                    }
                    Const.RESULT_UPDATE_COPY -> {
                        choice.enterItems[choice.enterItems.indexOfFirst { it.id == enterItem.id }] =
                            Json.decodeFromString(Json.encodeToString(enterItem))
                        copyEnterItem(enterItem)
                    }
                    Const.RESULT_DELETE -> {
                        choice.enterItems.removeIf { it.id == enterItem.id }
                    }
                }
                makeEditChoiceScreen(choice)
                (application as MainApplication).enter = null
            }
        }

    private val editShowConditionForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            (application as MainApplication).choice = null
            (application as MainApplication).condition?.let { condition ->
                when (result.resultCode) {
                    Const.RESULT_NEW -> {
                        choice.showConditions.add(Json.decodeFromString(Json.encodeToString(condition)))
                    }
                    Const.RESULT_UPDATE -> {
                        choice.showConditions[choice.showConditions.indexOfFirst { it.id == condition.id }] =
                            Json.decodeFromString(Json.encodeToString(condition))
                    }
                    Const.RESULT_NEW_COPY -> {
                        choice.showConditions.add(Json.decodeFromString(Json.encodeToString(condition)))
                        copyCondition(condition)
                    }
                    Const.RESULT_UPDATE_COPY -> {
                        choice.showConditions[choice.showConditions.indexOfFirst { it.id == condition.id }] =
                            Json.decodeFromString(Json.encodeToString(condition))
                        copyCondition(condition)
                    }
                    Const.RESULT_DELETE -> {
                        choice.showConditions.removeIf { it.id == condition.id }
                    }
                }
                makeEditChoiceScreen(choice)
                (application as MainApplication).condition = null
            }
        }

    private fun copyEnterItem(enterItem: EnterItem){
        val nextEnterId = preferenceProvider.nextEnterId(choice.enterItems, choice.id)
        if(nextEnterId > 0){
            choice.enterItems.add(Json.decodeFromString<EnterItem>(Json.encodeToString(enterItem)).apply {
                id = nextEnterId
                preferenceProvider.applyEnterConditionsId(enterConditions, nextEnterId)
            })
        }
    }

    private fun copyCondition(condition: Condition){
        val newShowConditionId = preferenceProvider.nextShowConditionId(choice.showConditions, choice.id)
        if (newShowConditionId > 0) {
            choice.showConditions.add(Json.decodeFromString<Condition>(Json.encodeToString(condition)).apply {
                id = newShowConditionId
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_get_info_choice))

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
            val nextChoiceId = preferenceProvider.nextChoiceId(slideLogic)
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
                (application as MainApplication).choice = Json.decodeFromString<ChoiceItem>(Json.encodeToString(choice))

                val intent = Intent(this@EditChoiceActivity, EditEnterActivity::class.java).apply {
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
        editShowConditionListAdapter = EditConditionListAdapter(preferenceProvider, context = this@EditChoiceActivity)
        editShowConditionListAdapter.setItemClickListener(object: EditConditionListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                choice.title = binding.etChoiceTitle.text.toString()
                (application as MainApplication).choice = Json.decodeFromString<ChoiceItem>(Json.encodeToString(choice))

                val intent = Intent(this@EditChoiceActivity, EditConditionActivity::class.java).apply {
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
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
        with(choice){
            binding.tvChoiceId.text = "${choice.id}"
            binding.etChoiceTitle.setText(title)
        }

        editEnterListAdapter.datas = choice.enterItems
        editEnterListAdapter.logic = logic
        editEnterListAdapter.notifyDataSetChanged()

        editShowConditionListAdapter.datas = choice.showConditions
        editShowConditionListAdapter.notifyDataSetChanged()

        updateEmptyEnterAndCondition()

        isMenuItemEnabled = true
    }

    private fun makeNotHaveChoice() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        isMenuItemEnabled = false
    }

    private fun updateEmptyEnterAndCondition(){
        if(choice.enterItems.size < 1)
        {
            binding.layoutEmptyEnter.visibility = View.VISIBLE
        }else{
            binding.layoutEmptyEnter.visibility = View.GONE
        }

        if(choice.showConditions.size < 1)
        {
            binding.layoutEmptyCondition.visibility = View.VISIBLE
        }else{
            binding.layoutEmptyCondition.visibility = View.GONE
        }
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.tvAddEnter -> {
                choice.title = binding.etChoiceTitle.text.toString()
                (application as MainApplication).choice = Json.decodeFromString<ChoiceItem>(Json.encodeToString(choice))

                val intent = Intent(this, EditEnterActivity::class.java).apply {
                    putExtra(Const.INTENT_ENTER_ID, Const.ADD_NEW_ENTER)
                }

                editEnterForResult.launch(intent)
            }

            R.id.tvAddShowCondition -> {
                choice.title = binding.etChoiceTitle.text.toString()
                (application as MainApplication).choice = Json.decodeFromString<ChoiceItem>(Json.encodeToString(choice))

                val intent = Intent(this@EditChoiceActivity, EditConditionActivity::class.java).apply {
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
            R.id.menu_copy -> {
                choice.title = binding.etChoiceTitle.text.toString()
                (application as MainApplication).choice = choice
                if(makeChoice){
                    setResult(Const.RESULT_NEW_COPY)
                }else{
                    setResult(Const.RESULT_UPDATE_COPY)
                }
                finish()
            }
            R.id.menu_delete -> {
                if(makeChoice){
                    setResult(Const.RESULT_NEW_DELETE)
                }else{
                    (application as MainApplication).choice = choice
                    setResult(Const.RESULT_DELETE)
                }
                finish()
            }

            R.id.menu_guide -> {
                val intent = Intent(this@EditChoiceActivity, GuideActivity::class.java)
                intent.putExtra(Const.INTENT_GUIDE, Const.GUIDE_CHOICE)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}