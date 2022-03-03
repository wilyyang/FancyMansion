package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewStartBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.Const
import com.cheesejuice.fancymansion.util.Sample
import com.google.gson.Gson

class ViewStartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStartBinding
    private lateinit var config: Config
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnStartBook.setOnClickListener {
            val intent = Intent(this, ViewerActivity::class.java)
            intent.putExtra(Const.KEY_TARGET_BOOK_ID, config.id)
            startActivity(intent)
        }

        val sample = Sample()
        val configJson = sample.getSampleConfig()
        config = Gson().fromJson(configJson, Config::class.java)

        makeReadyScreen(config)
    }

    private fun makeReadyScreen(config: Config) {
        with(config){
            binding.toolbar.title = title
            binding.tvSlideTitle.text = title
            binding.tvSlideDescription.text = description

            binding.tvSlideConfigId.text = "#$id  (${MainApplication.commonUtil.longToTimeFormatss(updateDate)})"
            binding.tvSlideConfigWriter.text = writer
            binding.tvSlideConfigIllustrator.text = illustrator

        }
        Glide.with(applicationContext).load(R.raw.image_1).into(binding.imageSlideShowMain)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}