package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewStartBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.Const
import com.cheesejuice.fancymansion.util.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.util.Sample
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class ViewStartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStartBinding
    private var config: Config? = null
    @Inject
    lateinit var util: CommonUtil
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

        binding.btnStartBook.setOnClickListener {
            val intent = Intent(this, ViewerActivity::class.java)
            intent.putExtra(Const.KEY_CURRENT_BOOK_ID, config!!.id)
            intent.putExtra(Const.KEY_FIRST_READ, true)
            startActivity(intent)
        }

        CoroutineScope(Default).launch {
            val bookId = intent.getLongExtra(Const.KEY_BOOK_ID, ID_NOT_FOUND)
            config = fileUtil.getConfigFromFile(bookId)
            config?.also {  configInfo ->
                withContext(Dispatchers.Main) {
                    makeViewReadyScreen(configInfo)
                }
            } ?: also {
                withContext(Main){
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
}