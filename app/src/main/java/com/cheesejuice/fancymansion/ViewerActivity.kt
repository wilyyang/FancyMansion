package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewerBinding
import com.cheesejuice.fancymansion.model.Book
import com.cheesejuice.fancymansion.model.ChoiceItem
import com.cheesejuice.fancymansion.model.Condition
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.view.ChoiceAdapter
import com.cheesejuice.fancymansion.view.OnChoiceItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private var book: Book? = null
    private lateinit var slideMap: Map<Long, Slide>
    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookPrefUtil: BookPrefUtil
    @Inject
    lateinit var fileUtil: FileUtil
    private lateinit var currentSlide: Slide

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initBookAndSlide()
    }

    private fun initBookAndSlide(){
        //        val targetId = intent.getLongExtra(Const.KEY_CURRENT_BOOK_ID, 0)
        //        if(targetId == 0L) finish()

        var firstRead = intent.getBooleanExtra(Const.KEY_FIRST_READ, true)
        CoroutineScope(Dispatchers.Default).launch {
            book = fileUtil.extractBookFromJson("temp")
            book?.also { bookInfo ->
                slideMap = bookInfo.slides.map { slide -> slide.id to slide }.toMap()

                if(firstRead || !bookPrefUtil.isBookReading(bookInfo.config.id) || bookPrefUtil.getReadingSlideId(bookInfo.config.id) == Const.BOOK_FIRST_READ){
                    bookPrefUtil.initReadingBookInfo(bookInfo.config.id, bookInfo.config.startId)
                    bookPrefUtil.setReadingSlideId(bookInfo.config.id, bookInfo.config.startId)
                    firstRead = true
                }

                withContext(Dispatchers.Main){
                    binding.toolbar.title = bookInfo.config.title

                    val currentSlideId = bookPrefUtil.getReadingSlideId(bookInfo.config.id)
                    makeSlideScreen(slideMap[currentSlideId], firstRead)
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
        if(isCount){ bookPrefUtil.incrementIdCount(book!!.config.id, slide.id) }

        currentSlide = slide
        binding.layoutLoading.root.visibility = View.GONE
        binding.layoutMain.visibility = View.VISIBLE
        with(currentSlide){
            Glide.with(applicationContext).load(Sample.getSampleImageId(slideImage)).into(binding.imageSlideShowMain)
            binding.tvSlideTitle.text = title
            binding.tvSlideDescription.text = description
            binding.tvSlideQuestion.text = question

            val passChoiceItems: ArrayList<ChoiceItem> = arrayListOf()
            for(choiceItem in choiceItems){
                if(checkConditions(choiceItem.showConditions)){
                    passChoiceItems.add(choiceItem)
                }
            }

            binding.recyclerChoice.layoutManager=LinearLayoutManager(baseContext)
            binding.recyclerChoice.adapter = ChoiceAdapter(passChoiceItems, object : OnChoiceItemClickListener {
                override fun onItemClick(choiceItem: ChoiceItem) {
                    bookPrefUtil.incrementIdCount(book!!.config.id, choiceItem.id)
                    enterNextSlide(choiceItem)
                }
            })
        }
    }

    private fun checkConditions(conditions: ArrayList<Condition>): Boolean{
        var result = true
        var nextLogic = CondNext.AND
        for(condition in conditions){
            result = nextLogic.check(result, checkCondition(condition))
            nextLogic = CondNext.from(condition.nextLogic)
            if(result && nextLogic == CondNext.OR) break
        }
        return result
    }

    private fun checkCondition(condition: Condition): Boolean =
        condition.run{
            val count1 = bookPrefUtil.getIdCount(book!!.config.id, conditionId1)
            val count2 = if(conditionId2==Const.NOT_SUPPORT_COND_ID_2) conditionCount else bookPrefUtil.getIdCount(book!!.config.id, conditionId2)
            Log.d(Const.TAG, "check : $conditionId1 ($count1) $conditionOp $conditionId2 ($count2)")
            CondOp.from(conditionOp).check(count1, count2)
        }

    private fun enterNextSlide(choiceItem: ChoiceItem){
        binding.layoutLoading.root.visibility = View.VISIBLE
        binding.layoutMain.visibility = View.GONE

        CoroutineScope(Dispatchers.Default).launch {
            var enterSlideId = Const.END_SLIDE_ID

            for(enterItem in choiceItem.enterItems) {
                if(checkConditions(enterItem.enterConditions)){
                    bookPrefUtil.incrementIdCount(book!!.config.id, enterItem.id)
                    enterSlideId = enterItem.enterSlideId
                    break
                }
            }

            delay(100)
            withContext(Dispatchers.Main){
                if(enterSlideId != Const.END_SLIDE_ID){
                    makeSlideScreen(slideMap[enterSlideId], true)
                }else{
                    makeSlideScreen(slideMap[book!!.config.defaultEndId], true)
                }
            }
        }
    }
}