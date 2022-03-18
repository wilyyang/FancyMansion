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
    lateinit var config: Config
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
        val slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND)
        if(bookId == Const.ID_NOT_FOUND || slideId == Const.ID_NOT_FOUND) finish()
        CoroutineScope(Dispatchers.Default).launch {
            val tempConfig = fileUtil.getConfigFromFile(bookId)
            slide = fileUtil.getSlideFromJson(bookId, slideId)

            withContext(Main) {
                if(tempConfig == null || slide == null) {
                    util.getAlertDailog(this@ViewerActivity).show()
                }else{
                    config = tempConfig

                    // if not play mode and save slide, not count
                    val isSave = ((mode == "") && slideId == bookUtil.getSaveSlideId(config.id))
                    makeSlideScreen(slide!!, !isSave)
                }
            }
        }
    }

    private fun makeSlideScreen(slide: Slide, isCount: Boolean) {
        if(isCount){ bookUtil.incrementIdCount(config.id, slide.id, mode) }

        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        with(slide){
            // Make Main Content
            Glide.with(applicationContext).load(fileUtil.getImageFile(config.id, slide.slideImage)).into(binding.imageViewShowMain)
            binding.tvSlideTitle.text = title
            binding.tvSlideDescription.text = description
            binding.tvSlideQuestion.text = question

            // Make Choice Item
            val passChoiceItems: ArrayList<ChoiceItem> = arrayListOf()
            for(choiceItem in choiceItems){
                if(bookUtil.checkConditions(config.id, choiceItem.showConditions, mode)){
                    passChoiceItems.add(choiceItem)
                }
            }
            binding.recyclerChoice.layoutManager = LinearLayoutManager(baseContext)
            val adapter = ChoiceAdapter(passChoiceItems)
            adapter.setItemClickListener(object : ChoiceAdapter.OnItemClickListener {
                override fun onClick(v: View, choiceItem: ChoiceItem) {
                    bookUtil.incrementIdCount(config.id, choiceItem.id, mode)
                    enterNextSlide(choiceItem)
                }
            })
            binding.recyclerChoice.adapter = adapter
        }
        bookUtil.setSaveSlideId(config.id, slide.id)
    }

    private fun enterNextSlide(choiceItem: ChoiceItem){
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)

        CoroutineScope(Dispatchers.Default).launch {
            var enterSlideId = Const.END_SLIDE_ID

            for(enterItem in choiceItem.enterItems) {
                if(bookUtil.checkConditions(config.id, enterItem.enterConditions, mode)){
                    bookUtil.incrementIdCount(config.id, enterItem.id, mode)
                    enterSlideId = enterItem.enterSlideId
                    break
                }
            }

            if(enterSlideId != Const.END_SLIDE_ID){
                slide = fileUtil.getSlideFromJson(config.id, enterSlideId)
            }else{
                slide = fileUtil.getSlideFromJson(config.id, config.defaultEndId)
            }

            delay(100)
            withContext(Dispatchers.Main){
                slide?.also {
                    makeSlideScreen(slide!!, true)
                }?:also {
                    util.getAlertDailog(this@ViewerActivity).show()
                }
            }
        }
    }
}