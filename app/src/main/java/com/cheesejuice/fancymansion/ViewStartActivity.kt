package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewStartBinding
import com.cheesejuice.fancymansion.model.Book
import com.cheesejuice.fancymansion.util.Sample
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser


class ViewStartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        test()
    }

    fun test() {
        val sample = Sample()
        val jsonString = sample.getSampleJson()
        val book = Gson().fromJson(jsonString, Book::class.java)

        binding.toolbar.title = book.config.title
        binding.tvSlideTitle.text = book.config.title
        binding.tvSlideDescription.text = book.config.description

        Glide.with(this).load(R.raw.image_1).into(binding.imageSlideShowMain)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}