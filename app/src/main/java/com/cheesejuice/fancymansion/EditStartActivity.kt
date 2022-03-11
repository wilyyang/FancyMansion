package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityEditStartBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.util.Const.Companion.KEY_BOOK_ID_NOT_FOUND
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class EditStartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditStartBinding
    private var config: Config? = null
    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookPrefUtil: BookPrefUtil
    @Inject
    lateinit var fileUtil: FileUtil

    private val gallaryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                try{

                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.imageConfigAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            gallaryForResult.launch(intent)
        }
        binding.btnEditBook.setOnClickListener {
        }

        CoroutineScope(Default).launch {
            createSampleFiles()

            var isCreate = intent.getBooleanExtra(Const.KEY_BOOK_CREATE, false)
            var bookId = 12345L //intent.getLongExtra(Const.KEY_BOOK_ID, KEY_BOOK_ID_NOT_FOUND)

            // if(!isCreate && bookId != KEY_BOOK_ID_NOT_FOUND && config != null){
            if(isCreate || bookId == KEY_BOOK_ID_NOT_FOUND){
                isCreate = true
                val count = bookPrefUtil.incrementBookCount()
                config = Config(id = count, title = "${getString(R.string.book_default_title)} $count")
                fileUtil.makeBookFolder(config!!)
                fileUtil.makeConfigFile(config!!)
                bookId = count
            }
            config = fileUtil.getConfigFromFile(bookId)
            withContext(Dispatchers.Main) {
                if(isCreate){
                    binding.btnEditBook.text = getString(R.string.create_book)
                }
                config!!.updateDate = System.currentTimeMillis()
                makeEditReadyScreen(config!!)
            }
        }
    }

    private fun makeEditReadyScreen(config: Config) {
        binding.layoutLoading.root.visibility = View.GONE
        binding.layoutMain.visibility = View.VISIBLE
        with(config){
            binding.toolbar.title = title
            binding.tvSlideConfigId.text = "#$id"
            binding.tvSlideConfigTime.text = util.longToTimeFormatss(updateDate)

            binding.etConfigTitle.setText(title)
            binding.etConfigVersion.setText(""+version)
            binding.etConfigWriter.setText(writer)
            binding.etConfigIllustrator.setText(illustrator)
            binding.etConfigDescription.setText(description)
        }
        Glide.with(applicationContext).load(fileUtil.getImageFile(config.id, config.defaultImage)).into(binding.imageSlideShowMain)
        binding.btnEditBook.isEnabled = true
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