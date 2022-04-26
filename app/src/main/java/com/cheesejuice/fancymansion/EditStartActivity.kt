package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityEditStartBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
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

@AndroidEntryPoint
class EditStartActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditStartBinding
    private lateinit var config: Config
    private var updateImage = false

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    private var makeBook = false

    private lateinit var gallaryForResult: ActivityResultLauncher<Intent>

    private val readStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
            makeEditReadyScreen(config)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.toolbar_title_update)

        gallaryForResult = registerGallaryResultName(binding.imageViewShowMain) { imageName ->
            config.coverImage = imageName; updateImage = true
        }

        binding.imageViewConfigAdd.setOnClickListener(this)
        binding.btnEditBook.setOnClickListener(this)

        val isCreate = intent.getBooleanExtra(Const.INTENT_BOOK_CREATE, false)
        var bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)

        CoroutineScope(Default).launch {
            if(isCreate || bookId == ID_NOT_FOUND){
                makeBook = true
                bookId = fileUtil.getNewEditBookId()

                if(bookId != -1L){
                    fileUtil.makeEmptyBook(bookId)
                }
            }

            val conf = fileUtil.getConfigFromFile(bookId)
            withContext(Main) {
                conf?.also{
                    config = it
                    makeEditReadyScreen(config)
                }?:also{
                    util.getAlertDailog(this@EditStartActivity).show()
                }
            }
        }
    }

    private fun makeEditReadyScreen(conf: Config) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

        with(conf){
            binding.tvConfigId.text = "#$bookId (v $version)"
            binding.tvConfigTime.text = CommonUtil.longToTimeFormatss(updateTime)

            binding.etConfigTitle.setText(title)
            binding.etConfigWriter.setText(writer)
            binding.etConfigIllustrator.setText(illustrator)
            binding.etConfigDescription.setText(description)
        }
        fileUtil.getImageFile(conf.bookId, conf.coverImage, isCover = true)?.also {
            Glide.with(applicationContext).load(it).into(binding.imageViewShowMain)
        }?:also {
            Glide.with(applicationContext).load(R.drawable.add_image).into(binding.imageViewShowMain)
        }

        binding.btnEditBook.isEnabled = true
    }

    private fun saveConfigFile(conf : Config) {
        with(conf){
            updateTime = System.currentTimeMillis()
            version += 1
            title = binding.etConfigTitle.text.toString()
            writer = binding.etConfigWriter.text.toString()
            illustrator = binding.etConfigIllustrator.text.toString()
            description = binding.etConfigDescription.text.toString()

            if(updateImage){
                coverImage = fileUtil.makeImageFile(binding.imageViewShowMain.drawable,
                    bookId, coverImage, isCover = true)
            }
            fileUtil.makeConfigFile(this)
        }
        RoundEditText.onceFocus = false
        updateImage = false
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.imageViewConfigAdd -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                }
                gallaryForResult.launch(intent)
            }

            R.id.btnEditBook -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(IO).launch {
                    saveConfigFile(config)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                        startEditSlideActivity(config.bookId)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_config, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> {
                setResult(Const.RESULT_CANCEL)
                finish()
            }

            R.id.menu_upload -> {

            }

            R.id.menu_play -> {
                showDialogAndStart(isShow = (RoundEditText.onceFocus || updateImage),
                    loading = binding.layoutLoading.root, main = binding.layoutActive,
                    title = getString(R.string.save_dialog_title), message = getString(R.string.save_dialog_question),
                    onlyOkBackground = { saveConfigFile(config) },
                    onlyNo = { RoundEditText.onceFocus = false; updateImage = false },
                    always = { bookUtil.setEditPlay(true); bookUtil.deleteBookPref(config.bookId, config.publishCode, Const.EDIT_PLAY);
                        val intent = Intent(this, ReadStartActivity::class.java).apply {
                            putExtra(Const.INTENT_BOOK_ID, config.bookId)
                        }
                        readStartForResult.launch(intent)
                    }
                )
            }

            R.id.menu_save -> {
                this@EditStartActivity.currentFocus?.let { it.clearFocus() }
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(IO).launch {
                    saveConfigFile(config)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                        finish()
                    }
                }
            }

            R.id.menu_delete -> {
                this@EditStartActivity.currentFocus?.let { it.clearFocus() }
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(IO).launch {
                    fileUtil.deleteBookFolder(config.bookId)
                    withContext(Main) {
                        if (makeBook) {
                            setResult(Const.RESULT_NEW_DELETE)
                        } else {
                            setResult(Const.RESULT_DELETE)
                        }
                        finish()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}