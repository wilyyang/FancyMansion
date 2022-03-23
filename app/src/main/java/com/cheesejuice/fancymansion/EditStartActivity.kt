package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityEditStartBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.util.Const.Companion.ID_NOT_FOUND
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.cheesejuice.fancymansion.extension.*
import com.cheesejuice.fancymansion.view.RoundEditText
import kotlinx.coroutines.delay

@AndroidEntryPoint
class EditStartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditStartBinding
    private var bookId: Long = ID_NOT_FOUND
    private var config: Config? = null
    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
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
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.toolbar_title_update)

        binding.imageViewConfigAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            gallaryForResult.launch(intent)
        }

        binding.btnEditBook.setOnClickListener {
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
            CoroutineScope(IO).launch {
                saveConfigFile(config!!)
                withContext(Main) {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                    startEditSlideActivity()
                }
            }
        }

        // temp code
        util.checkRequestPermissions()

//        createSampleFiles()
        isCreate = false //intent.getBooleanExtra(Const.KEY_BOOK_CREATE, false)
        bookId = 12345L //intent.getLongExtra(Const.KEY_BOOK_ID, KEY_BOOK_ID_NOT_FOUND)
        if(isCreate || bookId == ID_NOT_FOUND){
            isCreate = true
            bookId = bookUtil.incrementBookCount()

            fileUtil.initBook(bookId)
        }

        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
        CoroutineScope(Default).launch {
            config = fileUtil.getConfigFromFile(bookId)
            withContext(Main) {
                config!!.updateDate = System.currentTimeMillis()
                makeEditReadyScreen(config!!)
            }
        }
    }

    private fun makeEditReadyScreen(_config: Config) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)

        with(_config){
            binding.tvConfigId.text = "#$id (v $version)"
            binding.tvConfigTime.text = util.longToTimeFormatss(updateDate)

            binding.etConfigTitle.setText(title)
            binding.etConfigWriter.setText(writer)
            binding.etConfigIllustrator.setText(illustrator)
            binding.etConfigDescription.setText(description)
        }
        Glide.with(applicationContext).load(fileUtil.getImageFile(_config.id, _config.defaultImage)).into(binding.imageViewShowMain)
        binding.btnEditBook.isEnabled = true
    }

    private fun saveConfigFile(config : Config) {
        with(config){
            updateDate = System.currentTimeMillis()
            version += 1
            title = binding.etConfigTitle.text.toString()
            writer = binding.etConfigWriter.text.toString()
            illustrator = binding.etConfigIllustrator.text.toString()
            description = binding.etConfigDescription.text.toString()

            if(updateImage){
                defaultImage = fileUtil.saveImageFile(binding.imageViewShowMain.drawable, id, defaultImage)
            }
            fileUtil.makeConfigFile(this)
        }
        RoundEditText.onceFocus = false
        updateImage = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_config, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> finish()

            R.id.menu_save -> {
                this@EditStartActivity.currentFocus?.let { it.clearFocus() }
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
                CoroutineScope(IO).launch {
                    saveConfigFile(config!!)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                    }
                }
            }

            R.id.menu_play -> {
                if(config != null){
                    startAfterSaveEdits { startViewStartActivity() }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startAfterSaveEdits(start:()->Unit){
        if(RoundEditText.onceFocus || updateImage){
            this@EditStartActivity.currentFocus?.let { it.clearFocus() }

            util.getAlertDailog(
                this@EditStartActivity,
                getString(R.string.save_dialog_title),
                getString(R.string.save_dialog_question),
                getString(R.string.dialog_ok)
            ) { _, _ ->
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
                CoroutineScope(IO).launch {
                    saveConfigFile(config!!)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                        start()
                    }
                }
            }.apply {
                setNegativeButton(getString(R.string.dialog_no)) { _, _ ->
                    RoundEditText.onceFocus = false
                    updateImage = false
                    start()
                }
            }.show()
        }else{
            start()
        }
    }

    private fun startViewStartActivity(){
        bookUtil.setOnlyPlay(true)
        bookUtil.deleteBookPref(config!!.id, Const.MODE_PLAY)

        val intent = Intent(this@EditStartActivity, ViewStartActivity::class.java)
        intent.putExtra(Const.INTENT_BOOK_ID, config!!.id)
        startActivity(intent)
    }

    private fun startEditSlideActivity(){
        val intent = Intent(this@EditStartActivity, EditSlideActivity::class.java)
        intent.putExtra(Const.INTENT_BOOK_ID, config!!.id)
        if(config!!.briefs.size > 0){
            intent.putExtra(Const.INTENT_SLIDE_ID, config!!.briefs[0].slideId)
        }
        startActivity(intent)


    }
}