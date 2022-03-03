package com.cheesejuice.fancymansion

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewerBinding
import com.cheesejuice.fancymansion.model.Book
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.util.Const
import com.cheesejuice.fancymansion.util.Sample
import com.cheesejuice.fancymansion.view.ChoiceAdapter
import com.google.gson.Gson

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var book: Book
    private lateinit var slideMap: Map<Long, Slide>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val targetId = intent.getLongExtra(Const.KEY_TARGET_BOOK_ID, 0)
        if(targetId == 0L || !extractBookFromJson()) finish()
        binding.toolbar.title = book.config.title
        slideMap[book.config.startId]?.let { makeSlideScreen(it) }
    }

    private fun extractBookFromJson(): Boolean{
        try{
            val sample = Sample()
            val bookJson = sample.getSampleJson()
            book = Gson().fromJson(bookJson, Book::class.java)
            slideMap = book.slides.map { it.id to it }.toMap()
        }catch(e:Exception){
            return false
        }
        return true
    }

    fun makeSlideScreen(slide: Slide) {
        with(slide){
            binding.tvSlideTitle.text = title
            binding.tvSlideDescription.text = description

            binding.tvSlideQuestion.text = question

            val layoutManager = LinearLayoutManager(baseContext)
            binding.recyclerChoice.layoutManager=layoutManager
            val adapter = ChoiceAdapter(slideItems)
            binding.recyclerChoice.adapter=adapter
        }
        Glide.with(applicationContext).load(R.raw.image_1).into(binding.imageSlideShowMain)
    }

}