package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewerBinding
import com.cheesejuice.fancymansion.model.*
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.view.ChoiceAdapter
import com.cheesejuice.fancymansion.view.OnChoiceItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var config: Config
    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookPrefUtil: BookPrefUtil
    @Inject
    lateinit var fileUtil: FileUtil
    private var currentSlide: Slide? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initConfigObject()
    }

    private fun initConfigObject(){
        val bookId = intent.getLongExtra(Const.KEY_CURRENT_BOOK_ID, 0)
         if(bookId == 0L) finish()
        CoroutineScope(Dispatchers.Default).launch {
            fileUtil.getConfigFromFile(bookId)?.also { conf ->
                config = conf
                var firstRead = intent.getBooleanExtra(Const.KEY_FIRST_READ, true)
                val isReading = bookPrefUtil.isBookReading(config.id)
                var slideId = bookPrefUtil.getReadingSlideId(config.id)
                if(firstRead || !isReading || slideId == Const.BOOK_FIRST_READ){
                    firstRead = true
                    slideId = config.startId
                    bookPrefUtil.initReadingBookInfo(config.id, slideId)
                    bookPrefUtil.setReadingSlideId(config.id, slideId)
                }

                currentSlide = fileUtil.getSlideFromJson(bookId, slideId)
                withContext(Dispatchers.Main){
                    makeSlideScreen(currentSlide, firstRead)
                }
            }?: also{
                withContext(Dispatchers.Main){
                    util.getAlertDailog(this@ViewerActivity).show()
                }
            }
        }
    }

    private fun makeSlideScreen(slide: Slide?, isCount: Boolean) {
        slide?:let {
            util.getAlertDailog(this@ViewerActivity).show()
            return
        }

        if(isCount){ bookPrefUtil.incrementIdCount(config.id, slide.id) }

        binding.layoutLoading.root.visibility = View.GONE
        binding.layoutMain.visibility = View.VISIBLE
        with(slide){
            Glide.with(applicationContext).load(fileUtil.getImageFile(config.id, slide.slideImage)).into(binding.imageSlideShowMain)
            binding.tvSlideTitle.text = title
            binding.tvSlideDescription.text = description
            binding.tvSlideQuestion.text = question

            val passChoiceItems: ArrayList<ChoiceItem> = arrayListOf()
            for(choiceItem in choiceItems){
                if(bookPrefUtil.checkConditions(config.id, choiceItem.showConditions)){
                    passChoiceItems.add(choiceItem)
                }
            }

            binding.recyclerChoice.layoutManager=LinearLayoutManager(baseContext)
            binding.recyclerChoice.adapter = ChoiceAdapter(passChoiceItems, object : OnChoiceItemClickListener {
                override fun onItemClick(choiceItem: ChoiceItem) {
                    bookPrefUtil.incrementIdCount(config.id, choiceItem.id)
                    enterNextSlide(choiceItem)
                }
            })
        }
    }

    private fun enterNextSlide(choiceItem: ChoiceItem){
        binding.layoutLoading.root.visibility = View.VISIBLE
        binding.layoutMain.visibility = View.GONE

        CoroutineScope(Dispatchers.Default).launch {
            var enterSlideId = Const.END_SLIDE_ID

            for(enterItem in choiceItem.enterItems) {
                if(bookPrefUtil.checkConditions(config.id, enterItem.enterConditions)){
                    bookPrefUtil.incrementIdCount(config.id, enterItem.id)
                    enterSlideId = enterItem.enterSlideId
                    break
                }
            }

            if(enterSlideId != Const.END_SLIDE_ID){
                currentSlide = fileUtil.getSlideFromJson(config.id, enterSlideId)
            }else{
                currentSlide = fileUtil.getSlideFromJson(config.id, config.defaultEndId)
            }

            delay(100)
            withContext(Dispatchers.Main){
                makeSlideScreen(currentSlide, true)
            }
        }
    }
}