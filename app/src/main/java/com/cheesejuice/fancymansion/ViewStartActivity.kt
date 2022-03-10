package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.cheesejuice.fancymansion.databinding.ActivityViewStartBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.Const
import com.cheesejuice.fancymansion.util.Const.Companion.KEY_BOOK_ID_NOT_FOUND
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.util.Sample
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
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
            createSampleFiles()

            val bookId = intent.getLongExtra(Const.KEY_BOOK_ID, KEY_BOOK_ID_NOT_FOUND)
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
        Glide.with(applicationContext).load(fileUtil.getImageFile(config.id, config.defaultImage)).into(binding.imageSlideShowMain)
        binding.btnStartBook.isEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createSampleFiles(){
        val tempConfig = Sample.extractConfigFromJson(-1)!!
        fileUtil.makeBookFolder(tempConfig)
        fileUtil.makeConfigFile(tempConfig)
        for(i in 1 .. 9){
            val slide = Sample.extractSlideFromJson(-1, i*100000000L)
            fileUtil.makeSlideJson(tempConfig.id, slide!!)
        }

        val array = arrayOf("image_1.gif", "image_2.gif", "image_3.gif", "image_4.gif", "image_5.gif", "image_6.gif", "fish_cat.jpg", "game_end.jpg")
        for (fileName in array){
            val file = File(getExternalFilesDir(null), Const.FILE_PREFIX_BOOK+ tempConfig.id + File.separator+fileName)
            val input: InputStream = resources.openRawResource(Sample.getSampleImageId(fileName))
            val out = FileOutputStream(file)
            val buff = ByteArray(1024)
            var read = 0

            try {
                while (input.read(buff).also { read = it } > 0) {
                    out.write(buff, 0, read)
                }
            } finally {
                input.close()
                out.close()
            }
        }
    }
}