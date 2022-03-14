package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityEditStartBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.model.SlideBrief
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.util.Const.Companion.ID_NOT_FOUND
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
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

    private var updateImage = false

    private var isCreate = false

    private val gallaryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                Glide.with(applicationContext).load(result.data!!.data).into(binding.imageViewShowMain)

                result.data!!.data?.let { returnUri ->
                    contentResolver.query(returnUri, null, null, null, null)
                }?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    config!!.defaultImage = cursor.getString(nameIndex)
                    updateImage = true
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

        binding.imageViewConfigAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            gallaryForResult.launch(intent)
        }

        binding.btnEditBook.setOnClickListener {
            bringLoading(true)
            CoroutineScope(IO).launch {
                saveConfigFile(config!!)
                withContext(Main) {
                    bringLoading(false)

                    val intent = Intent(this@EditStartActivity, EditSlideActivity::class.java)
                    intent.putExtra(Const.KEY_BOOK_ID, config!!.id)
                    intent.putExtra((Const.KEY_PREFIX_EDIT_SLIDE+config!!.id), config!!.startId)
                    startActivity(intent)
                }
            }
        }

        util.checkRequestPermissions()

        CoroutineScope(Default).launch {
            createSampleFiles()

            isCreate = intent.getBooleanExtra(Const.KEY_BOOK_CREATE, false)
            var bookId = 12345L //intent.getLongExtra(Const.KEY_BOOK_ID, KEY_BOOK_ID_NOT_FOUND)

            // if(!isCreate && bookId != KEY_BOOK_ID_NOT_FOUND && config != null){
            if(isCreate || bookId == ID_NOT_FOUND){
                isCreate = true
                val count = bookPrefUtil.incrementBookCount()
                config = Config(id = count, title = "${getString(R.string.book_default_title)} $count")
                bookId = count
            }
            config = fileUtil.getConfigFromFile(bookId)
            withContext(Main) {
                if(isCreate){
                    binding.btnEditBook.text = getString(R.string.create_book)
                    binding.toolbar.title = getString(R.string.toolbar_title_create)
                }else{
                    binding.btnEditBook.text = getString(R.string.edit_book)
                    binding.toolbar.title = getString(R.string.toolbar_title_update)
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
            binding.tvConfigId.text = "#$id (v $version)"
            binding.tvConfigTime.text = util.longToTimeFormatss(updateDate)

            binding.etConfigTitle.setText(title)
            binding.etConfigWriter.setText(writer)
            binding.etConfigIllustrator.setText(illustrator)
            binding.etConfigDescription.setText(description)
        }
        Glide.with(applicationContext).load(fileUtil.getImageFile(config.id, config.defaultImage)).into(binding.imageViewShowMain)
        binding.btnEditBook.isEnabled = true
    }

    private fun bringLoading(isLoading: Boolean){
        if(isLoading){
            val view = this.currentFocus
            if (view != null) {
                view.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            binding.layoutLoading.root.visibility = View.VISIBLE
            binding.layoutMain.visibility = View.GONE
        }else{
            binding.layoutLoading.root.visibility = View.GONE
            binding.layoutMain.visibility = View.VISIBLE
        }
    }

    private fun saveConfigFile(config : Config): Boolean {
        with(config){
            if(isCreate){
                fileUtil.makeBookFolder(this)
                val slide = Slide(id = Const.FIRST_SLIDE, title = getString(R.string.name_slide_prefix)+1, question = getString(R.string.text_question_default))
                briefs.add(SlideBrief(slide.id, slide.title))
                fileUtil.makeSlideJson(id, slide)
            }

            updateDate = System.currentTimeMillis()
            version += 1
            title = binding.etConfigTitle.text.toString()
            writer = binding.etConfigWriter.text.toString()
            illustrator = binding.etConfigIllustrator.text.toString()
            description = binding.etConfigDescription.text.toString()

            if(updateImage && !fileUtil.saveImageFile(binding.imageViewShowMain.drawable, id, defaultImage)){
                this.defaultImage = ""
            }
            fileUtil.makeConfigFile(this)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_config, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> finish()
            R.id.menu_play -> {
                if(config != null){
                    util.getAlertDailog(
                        this@EditStartActivity,
                        getString(R.string.save_dialog_title),
                        getString(R.string.save_dialog_question),
                        getString(R.string.save_dialog_ok)
                    ) { _, _ ->
                        bringLoading(true)
                        CoroutineScope(IO).launch {
                            saveConfigFile(config!!)
                            withContext(Main) {
                                bringLoading(false)
                                val intent = Intent(this@EditStartActivity, ViewStartActivity::class.java)
                                intent.putExtra(Const.KEY_BOOK_ID, config!!.id)
                                startActivity(intent)
                            }
                        }
                    }.apply {
                        setNegativeButton(getString(R.string.save_dialog_no)) { _, _ ->
                            val intent = Intent(this@EditStartActivity, ViewStartActivity::class.java)
                            intent.putExtra(Const.KEY_BOOK_ID, config!!.id)
                            startActivity(intent)
                        }
                    }.show()
                }
            }
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