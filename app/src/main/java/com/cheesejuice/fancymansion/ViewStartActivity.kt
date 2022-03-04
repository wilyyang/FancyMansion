package com.cheesejuice.fancymansion

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewStartBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.Const
import com.cheesejuice.fancymansion.util.Sample
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main

class ViewStartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStartBinding
    private var config: Config? = null
    private lateinit var util: CommonUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        util = CommonUtil(applicationContext)
        binding.btnStartBook.setOnClickListener {
            val intent = Intent(this, ViewerActivity::class.java)
            intent.putExtra(Const.KEY_CURRENT_BOOK_ID, config!!.id)
            intent.putExtra(Const.KEY_FIRST_READ, true)
            startActivity(intent)
        }

        CoroutineScope(Default).launch {
            config = extractConfigFromJson("temp")
            config?.also {  configInfo ->
                withContext(Dispatchers.Main) {
                    makeReadyScreen(configInfo)
                }
            } ?: also {
                withContext(Main){
                    util.getAlertDailog(this@ViewStartActivity).show()
                }
            }
        }
    }

    private fun extractConfigFromJson(fileName: String): Config?{
        val configJson = Sample.getSampleConfig()
        var result:Config? = null
        try{
            result = Gson().fromJson(configJson, Config::class.java)
        }catch (e : Exception){
            Log.e(Const.TAG, "Exception : "+e.message)
            return null
        }
        return result
    }

    private fun makeReadyScreen(config: Config) {
        binding.layoutLoading.root.visibility = View.GONE
        binding.layoutMain.visibility = View.VISIBLE
        with(config){
            binding.toolbar.title = title
            binding.tvSlideTitle.text = title
            binding.tvSlideDescription.text = description

            binding.tvSlideConfigId.text = "#$id  (${util.longToTimeFormatss(updateDate)})"
            binding.tvSlideConfigWriter.text = writer
            binding.tvSlideConfigIllustrator.text = illustrator

        }
        Glide.with(applicationContext).load(Sample.getSampleImageId(config.defaultImage)).into(binding.imageSlideShowMain)
        binding.btnStartBook.isEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}