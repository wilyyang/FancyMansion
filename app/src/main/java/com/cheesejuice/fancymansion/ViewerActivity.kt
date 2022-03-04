package com.cheesejuice.fancymansion

import android.app.AlertDialog
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
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.CondOp
import com.cheesejuice.fancymansion.util.Const
import com.cheesejuice.fancymansion.util.Sample
import com.cheesejuice.fancymansion.view.ChoiceAdapter
import com.cheesejuice.fancymansion.view.OnChoiceItemClickListener
import com.google.gson.Gson
import kotlinx.coroutines.*

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private var book: Book? = null
    private lateinit var slideMap: Map<Long, Slide>
    private lateinit var util: CommonUtil
    private lateinit var currentSlide: Slide

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        util = CommonUtil(applicationContext)

        initBookAndSlide()
    }

    private fun initBookAndSlide(){
        //        val targetId = intent.getLongExtra(Const.KEY_CURRENT_BOOK_ID, 0)
        //        if(targetId == 0L) finish()

        var firstRead = intent.getBooleanExtra(Const.KEY_FIRST_READ, true)
        CoroutineScope(Dispatchers.Default).launch {
            book = extractBookFromJson("temp")
            book?.also { bookInfo ->
                slideMap = bookInfo.slides.map { slide -> slide.id to slide }.toMap()

                if(firstRead || !util.isBookReading(bookInfo.config.id) || util.getReadingSlideId(bookInfo.config.id) == Const.BOOK_FIRST_READ){
                    util.initReadingBookInfo(bookInfo.config.id, bookInfo.config.startId)
                    util.setReadingSlideId(bookInfo.config.id, bookInfo.config.startId)
                    firstRead = true
                }

                withContext(Dispatchers.Main){
                    binding.toolbar.title = bookInfo.config.title

                    val currentSlideId = util.getReadingSlideId(bookInfo.config.id)
                    makeSlideScreen(slideMap[currentSlideId], firstRead)
                }
            }?: also{
                withContext(Dispatchers.Main){
                    util.getAlertDailog(this@ViewerActivity).show()
                }
            }
        }
    }

    private fun extractBookFromJson(fileName: String): Book?{
        val bookJson = Sample.getSampleJson()
        var result:Book? = null
        try{
            result = Gson().fromJson(bookJson, Book::class.java)
        }catch (e : Exception){
            Log.e(Const.TAG, "Exception : "+e.message)
            return null
        }
        return result
    }

    private fun incrementSlideCount(slide: Slide){
        val count = util.getSlideCount(book!!.config.id, slide.id) + 1
        util.setSlideCount(book!!.config.id, slide.id, count)
    }

    private fun makeSlideScreen(slide: Slide?, isCount: Boolean) {
        slide?:let {
            util.getAlertDailog(this@ViewerActivity).show()
            return
        }
        if(isCount){ incrementSlideCount(slide) }

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
                    enterNextSlide(choiceItem)
                }
            })
        }
    }

    private fun checkConditions(conditions: ArrayList<Condition>): Boolean{
        for(condition in conditions){
            if(!checkCondition(condition)){
                return false
            }
        }
        return true
    }

    private fun checkCondition(condition: Condition): Boolean =
        condition.run{
            Log.d(Const.TAG, "$conditionId : $conditionCount $conditionOp ${util.getSlideCount(book!!.config.id, conditionId)}")
            CondOp.from(conditionOp).check(conditionCount, util.getSlideCount(book!!.config.id, conditionId))
        }

    private fun enterNextSlide(choiceItem: ChoiceItem){
        binding.layoutLoading.root.visibility = View.VISIBLE
        binding.layoutMain.visibility = View.GONE

        CoroutineScope(Dispatchers.Default).launch {
            var enterSlideId = Const.END_SLIDE_ID

            for(enterItem in choiceItem.enterItems) {
                if(checkConditions(enterItem.enterConditions)){
                    enterSlideId = enterItem.enterSlideId
                    break
                }
            }

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