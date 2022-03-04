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
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.util.CommonUtil
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
    private lateinit var commonUtil: CommonUtil
    private lateinit var currentSlide: Slide

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        commonUtil = CommonUtil(applicationContext)

//        val targetId = intent.getLongExtra(Const.KEY_CURRENT_BOOK_ID, 0)
//        if(targetId == 0L) finish()

        val firstRead = intent.getBooleanExtra(Const.KEY_FIRST_READ, true)

        CoroutineScope(Dispatchers.Default).launch {
            book = extractBookFromJson("temp")
            book?.also { bookInfo ->
                slideMap = bookInfo.slides.map { slide -> slide.id to slide }.toMap()
                if(firstRead || !commonUtil.isBookReading(bookInfo.config.id) || commonUtil.getReadingSlideId(bookInfo.config.id) == Const.BOOK_FIRST_READ){
                    commonUtil.initReadingBookInfo(bookInfo.config.id, bookInfo.config.startId)
                    commonUtil.setReadingSlideId(bookInfo.config.id, bookInfo.config.startId)
                }

                withContext(Dispatchers.Main){
                    binding.toolbar.title = bookInfo.config.title

                    val currentSlideId = commonUtil.getReadingSlideId(bookInfo.config.id)
                    slideMap[currentSlideId]?.let { slide ->
                        makeSlideScreen(slide)
                    }
                }
            }?: also{
                withContext(Dispatchers.Main){
                    commonUtil.getAlertDailog(this@ViewerActivity).show()
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

    private fun makeSlideScreen(slide: Slide) {
        currentSlide = slide
        binding.layoutLoading.root.visibility = View.GONE
        binding.layoutMain.visibility = View.VISIBLE
        with(slide){
            binding.tvSlideTitle.text = title
            binding.tvSlideDescription.text = description

            binding.tvSlideQuestion.text = question

            val layoutManager = LinearLayoutManager(baseContext)
            binding.recyclerChoice.layoutManager=layoutManager
            val adapter = ChoiceAdapter(choiceItems, object : OnChoiceItemClickListener {
                override fun onItemClick(choiceId: Long) {
                    val count = commonUtil.getSlideCount(book!!.config.id, currentSlide.id)+1
                    commonUtil.setSlideCount(book!!.config.id, currentSlide.id, count)
                    val enterSlideId = currentSlide.choiceItems[0].enterItems[0].enterSlideId
                    slideMap[enterSlideId]?.also { makeSlideScreen(it) }?:also{ commonUtil.getAlertDailog(this@ViewerActivity).show() }
                }
            })
            binding.recyclerChoice.adapter=adapter
        }
        Glide.with(applicationContext).load(Sample.getSampleImageId(slide.slideImage)).into(binding.imageSlideShowMain)
    }
}