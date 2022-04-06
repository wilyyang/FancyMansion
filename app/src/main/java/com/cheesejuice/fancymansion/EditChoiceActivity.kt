package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.cheesejuice.fancymansion.databinding.ActivityEditChoiceBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.ChoiceItem
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.model.SlideLogic
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.view.RoundEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var isNew = false

    @Inject
    lateinit var bookUtil: BookUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.toolbar_edit_choice)

        binding.btnCancelChoice.setOnClickListener (this)
        binding.btnSaveChoice.setOnClickListener (this)

        slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND)
        choiceId = intent.getLongExtra(Const.INTENT_CHOICE_ID, Const.ID_NOT_FOUND)
        if(slideId == Const.ID_NOT_FOUND || choiceId == Const.ID_NOT_FOUND){
            makeNotHaveChoice()
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            val logicTemp = (application as MainApplication).logic
            val slideLogicTemp = logicTemp?.logics?.find { it.slideId == slideId }
            val choiceTemp = if (slideLogicTemp != null) {
                if (choiceId == Const.ADD_NEW_CHOICE) {
                    isNew = true
                    val nextSlideId = bookUtil.nextChoiceId(slideLogicTemp)
                    if(nextSlideId != -1L){
                        ChoiceItem(nextSlideId, getString(R.string.name_choice_prefix))
                    }else{ null }
                } else {
                    slideLogicTemp.choiceItems.find { it.id == choiceId }
                }
            }else{ null }

            withContext(Dispatchers.Main) {
                if (logicTemp != null && slideLogicTemp != null && choiceTemp != null) {
                    logic = logicTemp
                    slideLogic = slideLogicTemp
                    choice = choiceTemp

                    makeEditChoiceScreen(choice)
                } else {
                    makeNotHaveChoice()
                }
            }
        }
    }

    private fun makeEditChoiceScreen(choice: ChoiceItem) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        with(choice){
            binding.toolbar.subtitle = "# $slideId"
            binding.etChoiceTitle.setText(title)
        }

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
            R.id.btnSaveChoice -> {
                choice.title = binding.etChoiceTitle.text.toString()
                if(isNew){
                    slideLogic.choiceItems.add(choice)
                }

                (application as MainApplication).logic = logic
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
            R.id.menu_delete -> deleteChoice()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteChoice(){
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
        CoroutineScope(Dispatchers.IO).launch {
            // File IO

            withContext(Dispatchers.Main) {
                RoundEditText.onceFocus = false

                // finish
            }
        }
    }
}