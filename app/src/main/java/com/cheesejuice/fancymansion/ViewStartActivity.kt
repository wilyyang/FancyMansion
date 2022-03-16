package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewStartBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.util.Const.Companion.ID_NOT_FOUND
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

@AndroidEntryPoint
class ViewStartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStartBinding
    private var config: Config? = null
    var mode: String = ""
    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if(bookUtil.getOnlyPlay()) { mode = Const.MODE_PLAY}

        binding.btnStartBook.setOnClickListener {
            // Only Play
            if(mode != ""){
                startViewerActivity(config!!.id, config!!.startId)
            }else{
                // Save Point
                val saveSlide = bookUtil.getSaveSlideId(config!!.id)
                if(saveSlide != ID_NOT_FOUND){
                    util.getAlertDailog(
                        this@ViewStartActivity,
                        getString(R.string.record_dialog_title),
                        getString(R.string.record_dialog_question),
                        getString(R.string.dialog_ok)
                    ) { _, _ ->
                        startViewerActivity(config!!.id, saveSlide)
                    }.apply {
                        setNegativeButton(getString(R.string.dialog_no)) { _, _ ->
                            bookUtil.deleteBookPref(config!!.id, "")
                            startViewerActivity(config!!.id, config!!.startId)
                        }
                    }.show()
                }else{
                    startViewerActivity(config!!.id, config!!.startId)
                }
            }
        }

        CoroutineScope(Default).launch {
            val bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
            config = fileUtil.getConfigFromFile(bookId)
            withContext(Main) {
                config?.also {  configInfo ->
                    makeViewReadyScreen(configInfo)
                } ?: also {
                    util.getAlertDailog(this@ViewStartActivity).show()
                }
            }
        }
    }

    private fun makeViewReadyScreen(config: Config) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        with(config){
            binding.toolbar.title = title
            binding.tvConfigTitle.text = title
            binding.tvConfigDescription.text = description

            binding.tvConfigId.text = "#$id"
            binding.tvConfigTime.text = util.longToTimeFormatss(updateDate)
            binding.tvConfigWriter.text = writer
            binding.tvConfigIllustrator.text = illustrator

        }
        Glide.with(applicationContext).load(fileUtil.getImageFile(config.id, config.defaultImage)).into(binding.imageViewShowMain)
        binding.btnStartBook.isEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startViewerActivity(configId:Long, slideId: Long){
        val intent = Intent(this, ViewerActivity::class.java)
        intent.putExtra(Const.INTENT_BOOK_ID, configId)
        intent.putExtra(Const.INTENT_SLIDE_ID, slideId)
        startActivity(intent)
    }
}