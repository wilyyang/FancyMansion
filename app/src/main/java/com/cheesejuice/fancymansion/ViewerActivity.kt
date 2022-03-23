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
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

@AndroidEntryPoint
class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var logic: Logic
    private var slide: Slide? = null
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
        if(bookId == Const.ID_NOT_FOUND || slideId == Const.ID_NOT_FOUND) finish()
        CoroutineScope(Dispatchers.Default).launch {
            logic = fileUtil.getLogicFromFile(bookId)!!

            if(logic.logics.size < 1) {
                withContext(Main) {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                    binding.layoutEmptySlide.root.visibility = View.VISIBLE
                    binding.layoutMain.visibility = View.GONE
                }
            }else{
                if(slideId == Const.FIRST_SLIDE){
                    slideId = logic!!.logics[0].slideId
                }
                slide = fileUtil.getSlideFromFile(bookId, slideId)

                withContext(Main) {
                    if(logic == null || slide == null) {
                        util.getAlertDailog(this@ViewerActivity).show()
                    }else{

                        // if not play mode and save slide, not count
                        val isSave = ((mode == "") && slideId == bookUtil.getSaveSlideId(logic.bookId))
                        makeSlideScreen(slide!!, !isSave)
                    }
                }
            }
        }
    }

    private fun makeSlideScreen(slide: Slide, isCount: Boolean) {
        if(isCount){ bookUtil.incrementIdCount(logic.bookId, slide.slideId, mode) }

        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        with(slide){
            // Make Main Content
            Glide.with(applicationContext).load(fileUtil.getImageFile(logic.bookId, slide.slideImage)).into(binding.imageViewShowMain)
            binding.tvSlideTitle.text = slideTitle
            binding.tvSlideDescription.text = description
            binding.tvSlideQuestion.text = question

            // Make Choice Item
            val passChoiceItems: ArrayList<ChoiceItem> = arrayListOf()
            logic.logics.find { it.slideId == slideId }?.let {
                for(choiceItem in it.choiceItems){
                    if(bookUtil.checkConditions(logic.bookId, choiceItem.showConditions, mode)){
                        passChoiceItems.add(choiceItem)
                    }
                }
            }

            binding.recyclerChoice.layoutManager = LinearLayoutManager(baseContext)
            val adapter = ChoiceAdapter(passChoiceItems)
            adapter.setItemClickListener(object : ChoiceAdapter.OnItemClickListener {
                override fun onClick(v: View, choiceItem: ChoiceItem) {
                    bookUtil.incrementIdCount(logic.bookId, choiceItem.id, mode)
                    enterNextSlide(choiceItem)
                }
            })
            binding.recyclerChoice.adapter = adapter
        }
        bookUtil.setSaveSlideId(logic.bookId, slide.slideId)
    }

    private fun enterNextSlide(choiceItem: ChoiceItem){
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)

        CoroutineScope(Dispatchers.Default).launch {
            var enterSlideId = Const.END_SLIDE_ID

            for(enterItem in choiceItem.enterItems) {
                if(bookUtil.checkConditions(logic.bookId, enterItem.enterConditions, mode)){
                    bookUtil.incrementIdCount(logic.bookId, enterItem.id, mode)
                    enterSlideId = enterItem.enterSlideId
                    break
                }
            }

            if(enterSlideId != Const.END_SLIDE_ID){
                slide = fileUtil.getSlideFromFile(logic.bookId, enterSlideId)
            }else{
                slide = null
            }

            delay(100)
            withContext(Dispatchers.Main){
                slide?.also {
                    makeSlideScreen(it, true)
                }?:also {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                    binding.layoutEmptySlide.root.visibility = View.VISIBLE
                    binding.layoutMain.visibility = View.GONE
                }
            }
        }
    }
}