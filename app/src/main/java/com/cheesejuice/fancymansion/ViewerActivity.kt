package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewerBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.*
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.view.ChoiceAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

@AndroidEntryPoint
class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var logic: Logic
    private lateinit var slide: Slide
    var mode: String = ""

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if(bookUtil.getOnlyPlay()) { mode = Const.MODE_PLAY}

        // init config & slide object
        val bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, Const.ID_NOT_FOUND)
        var slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND)
        CoroutineScope(Default).launch {
            val logicTemp = fileUtil.getLogicFromFile(bookId)
            val slideTemp = logicTemp?.let {
                // Slide is First
                if(slideId == Const.FIRST_SLIDE && it.logics.size > 0){
                    slideId = it.logics[0].slideId
                }
                fileUtil.getSlideFromFile(bookId, slideId)
            }

            withContext(Main) {
                if(logicTemp != null && slideTemp != null) {
                    logic = logicTemp
                    slide = slideTemp

                    // if not play mode and have save, not count save
                    val isSave = ((mode != Const.MODE_PLAY) && slideId == bookUtil.getSaveSlideId(logic.bookId))
                    if(!isSave){ bookUtil.incrementIdCount(logic.bookId, slide.slideId, mode) }

                    makeSlideScreen(logic, slide)
                }else{
                    makeNotHaveSlide()
                }
            }
        }
    }

    private fun makeSlideScreen(logic: Logic, slide: Slide) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        with(slide){
            // Make Main Content
            Glide.with(applicationContext).load(fileUtil.getImageFile(logic.bookId, slideImage)).into(binding.imageViewShowMain)
            binding.tvSlideTitle.text = slideTitle
            binding.tvSlideDescription.text = description
            binding.tvSlideQuestion.text = question

            // Select Choice Item
            val passChoiceItems: ArrayList<ChoiceItem> = arrayListOf()
            logic.logics.find { it.slideId == slideId }?.let {
                for(choiceItem in it.choiceItems){
                    if(bookUtil.checkConditions(logic.bookId, choiceItem.showConditions, mode)){
                        passChoiceItems.add(choiceItem)
                    }
                }
            }

            // Make Choice Item
            binding.recyclerChoice.layoutManager = LinearLayoutManager(baseContext)
            val adapter = ChoiceAdapter(passChoiceItems)
            adapter.setItemClickListener(object : ChoiceAdapter.OnItemClickListener {
                override fun onClick(v: View, choiceItem: ChoiceItem) {
                    bookUtil.incrementIdCount(logic.bookId, choiceItem.id, mode)
                    enterNextSlide(logic, choiceItem)
                }
            })
            binding.recyclerChoice.adapter = adapter
        }

        // Save read slide point
        bookUtil.setSaveSlideId(logic.bookId, slide.slideId)
    }

    private fun makeNotHaveSlide() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        binding.layoutEmptySlide.root.visibility = View.VISIBLE
        binding.layoutMain.visibility = View.GONE
    }

    private fun enterNextSlide(logic: Logic, choiceItem: ChoiceItem){
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)

        CoroutineScope(Default).launch {
            // Get Next Slide Id
            var nextSlideId = Const.END_SLIDE_ID
            for(enterItem in choiceItem.enterItems) {
                if(bookUtil.checkConditions(logic.bookId, enterItem.enterConditions, mode)){
                    bookUtil.incrementIdCount(logic.bookId, enterItem.id, mode)
                    nextSlideId = enterItem.enterSlideId
                    break
                }
            }

            val slideNext = fileUtil.getSlideFromFile(logic.bookId, nextSlideId)

            delay(100)
            withContext(Main){
                slideNext?.also {
                    slide = it
                    bookUtil.incrementIdCount(logic.bookId, slide.slideId, mode)
                    makeSlideScreen(logic, slide)
                }?:also {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                    binding.layoutEmptySlide.root.visibility = View.VISIBLE
                    binding.layoutMain.visibility = View.GONE
                }
            }
        }
    }
}