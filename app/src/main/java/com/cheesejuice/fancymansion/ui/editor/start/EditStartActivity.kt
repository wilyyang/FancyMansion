package com.cheesejuice.fancymansion.ui.editor.start

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import com.cheesejuice.fancymansion.databinding.ActivityEditStartBinding
import com.cheesejuice.fancymansion.extension.*
import com.cheesejuice.fancymansion.ui.RoundEditText
import com.cheesejuice.fancymansion.ui.editor.guide.GuideActivity
import com.cheesejuice.fancymansion.ui.reader.start.ReadStartActivity
import com.cheesejuice.fancymansion.util.Formatter
import com.cheesejuice.fancymansion.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EditStartActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditStartBinding
    private lateinit var config: Config
    private var updateImage = false

    @Inject
    lateinit var util: Util
    @Inject
    lateinit var preferenceProvider: PreferenceProvider
    @Inject
    lateinit var fileRepository: FileRepository
    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    private var makeBook = false
    private var isBookUpload = false

    private lateinit var gallaryForResult: ActivityResultLauncher<Intent>

    private val readStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_get_make_book))

            CoroutineScope(Default).launch {
                val conf = fileRepository.getConfigFromFile(config.bookId)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_get_make_book))

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.toolbar_title_update)

        gallaryForResult = registerGallaryResultName(binding.imageViewShowMain) { imageName ->
            config.coverImage = imageName; updateImage = true
        }

        binding.imageViewConfigAdd.setOnClickListener(this)
        binding.imageViewConfigCrop.setOnClickListener(this)
        binding.btnEditBook.setOnClickListener(this)

        val isCreate = intent.getBooleanExtra(Const.INTENT_BOOK_CREATE, false)
        var bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)

        CoroutineScope(Default).launch {
            if(isCreate || bookId == ID_NOT_FOUND){
                makeBook = true
                bookId = fileRepository.getNewEditBookId()

                if(bookId != -1L){
                    fileRepository.makeEmptyBook(bookId)
                }
            }

            val conf = fileRepository.getConfigFromFile(bookId)
            conf?.let {
                if(conf.publishCode != ""){
                    isBookUpload = firebaseRepository.isBookUpload(conf.publishCode)
                }
            }


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

    @SuppressLint("SetTextI18n")
    private fun makeEditReadyScreen(conf: Config) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")

        if(isBookUpload){
            val menuItem = binding.toolbar.menu.findItem(R.id.menu_upload)
            menuItem.title = getString(R.string.menu_book_update)
        }

        with(conf){
            binding.tvConfigVersion.text = "v ${Formatter.versionToString(version)}"
            binding.tvConfigTime.text = Formatter.longToTimeUntilSecond(updateTime)

            binding.etConfigTitle.setText(title)
            binding.etConfigWriter.setText(writer)
            binding.etConfigIllustrator.setText(illustrator)
            binding.etConfigDescription.setText(description)
            binding.tvConfigPub.text = getString(R.string.book_config_pub)+publishCode
        }
        fileRepository.getImageFile(conf.bookId, conf.coverImage, isCover = true)?.also {
            Glide.with(applicationContext).load(it).into(binding.imageViewShowMain)
        }?:also {
            Glide.with(applicationContext).load(R.drawable.default_image).into(binding.imageViewShowMain)
        }

        binding.btnEditBook.isEnabled = true
    }

    private fun saveConfigFile(conf : Config, isCopy : Boolean = false) {
        with(conf){
            updateTime = System.currentTimeMillis()
            version += 1
            title = binding.etConfigTitle.text.toString() + if(isCopy){ getString(R.string.title_endwith_copy) } else { "" }
            writer = binding.etConfigWriter.text.toString()
            illustrator = binding.etConfigIllustrator.text.toString()
            description = binding.etConfigDescription.text.toString()

            if(updateImage){
                coverImage = fileRepository.makeImageFile(binding.imageViewShowMain.drawable,
                    bookId, coverImage, isCover = true)
            }
            fileRepository.makeConfigFile(this)
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
            R.id.imageViewConfigCrop -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                    putExtra("crop", true)
                }
                gallaryForResult.launch(intent)
            }
            R.id.btnEditBook -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(
                    R.string.loading_text_save_make_book
                ))
                CoroutineScope(IO).launch {
                    saveConfigFile(config)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
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
                this@EditStartActivity.currentFocus?.clearFocus()

                val current = System.currentTimeMillis()
                if(!isBookUpload && FirebaseRepository.userInfo!!.uploadBookTime+ Const.CONST_TIME_LIMIT_BOOK > current){

                    val leftTime = (FirebaseRepository.userInfo!!.uploadBookTime+ Const.CONST_TIME_LIMIT_BOOK - current) / 60000
                    util.getAlertDailog(
                        context = this@EditStartActivity,
                        title = getString(R.string.dialog_time_limit_title),
                        message = String.format(getString(R.string.dialog_time_limit_book), leftTime),
                        click = { _, _ -> }
                    ).show()
                    return true
                }

                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(
                    R.string.loading_text_upload_make_book
                ))
                CoroutineScope(IO).launch {
                    val dbSuccess = uploadBook()
                    val fileSuccess = uploadBookFile()

                    isBookUpload = firebaseRepository.isBookUpload(config.publishCode)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")

                        if(!dbSuccess){
                            Toast.makeText(this@EditStartActivity, getString(R.string.toast_db_fail), Toast.LENGTH_SHORT).show()
                        }else if(!fileSuccess){
                            Toast.makeText(this@EditStartActivity, getString(R.string.toast_storage_fail), Toast.LENGTH_SHORT).show()
                        }else{
                            if(isBookUpload){
                                val menuItem = binding.toolbar.menu.findItem(R.id.menu_upload)
                                menuItem.title = getString(R.string.menu_book_update)
                            }
                            Toast.makeText(this@EditStartActivity, getString(R.string.toast_query_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            R.id.menu_play -> {
                startAfterSaveEdits{
                    preferenceProvider.setEditPlay(true); preferenceProvider.deleteBookPref(config.bookId, FirebaseRepository.auth.uid!!, config.publishCode,
                    Const.EDIT_PLAY
                );
                    val intent = Intent(this, ReadStartActivity::class.java).apply {
                        putExtra(Const.INTENT_BOOK_ID, config.bookId)
                    }
                    readStartForResult.launch(intent)
                }
            }

            R.id.menu_save -> {
                this@EditStartActivity.currentFocus?.clearFocus()
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(
                    R.string.loading_text_save_make_book
                ))
                CoroutineScope(IO).launch {
                    saveConfigFile(config)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
                        finish()
                    }
                }
            }

            R.id.menu_delete -> {
                this@EditStartActivity.currentFocus?.clearFocus()
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(
                    R.string.loading_text_delete_make_book
                ))
                CoroutineScope(IO).launch {
                    fileRepository.deleteBookFolder(config.bookId)
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

            R.id.menu_copy -> {
                this@EditStartActivity.currentFocus?.clearFocus()
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(
                    R.string.loading_text_copy_make_book
                ))
                CoroutineScope(IO).launch {
                    val copyBookId = fileRepository.getNewEditBookId()
                    if (copyBookId != -1L) {
                        val targetDir = File(fileRepository.bookUserPath, Const.FILE_PREFIX_BOOK + config.bookId)
                        val copyDir = File(fileRepository.bookUserPath, Const.FILE_PREFIX_BOOK + copyBookId)
                        targetDir.copyRecursively(copyDir, overwrite = true)
                        val copyConfig = Json.decodeFromString<Config>(Json.encodeToString(config)).apply {
                            bookId = copyBookId
                            publishCode = ""
                            user = ""
                            email = ""
                            uid = ""
                        }
                        saveConfigFile(copyConfig, isCopy = true)
                    }
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
                    }
                }
            }
            R.id.menu_guide -> {
                val intent = Intent(this@EditStartActivity, GuideActivity::class.java)
                intent.putExtra(Const.INTENT_GUIDE, Const.GUIDE_COVER)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        startAfterSaveEdits{ finish() }
    }

    private fun startAfterSaveEdits(always:() ->Unit){
        showDialogAndStart(isShow = (RoundEditText.onceFocus || updateImage),
            loading = binding.layoutLoading.root, main = binding.layoutActive,
            title = getString(R.string.save_dialog_title), message = getString(R.string.save_dialog_question),
            onlyOkBackground = { saveConfigFile(config) },
            onlyOk = { setSaveFlag(false) },
            onlyNo = { setSaveFlag(false) },
            always = always,
            loadingText = getString(R.string.loading_text_save_make_book)
        )
    }

    private fun setSaveFlag(flag:Boolean){
        RoundEditText.onceFocus = flag
        updateImage = flag
    }

    private suspend fun uploadBook():Boolean{
        var result = false
        FirebaseRepository.auth.uid?.let { userId ->
            config.apply {
                user = firebaseRepository.name ?: ""
                email = firebaseRepository.email ?: ""
                uid = userId
                updateTime = System.currentTimeMillis()
            }

            if(!isBookUpload){
                config.publishCode = firebaseRepository.uploadBookConfig(config)
            }

            if(config.publishCode != ""){
                saveConfigFile(config)
                firebaseRepository.updateBookConfig(config)
                result = true
            }
        }
        return result
    }

    private suspend fun uploadBookFile(): Boolean{
        var result = false
        val localBookFile = fileRepository.compressBook(bookId = config.bookId)

        val total = localBookFile?.listFiles()?.sumOf { it.length() }?:0L
        var current = 0L
        localBookFile?.listFiles()?.let { fileList ->
            for(subFile in fileList){
                result = firebaseRepository.uploadBookFile("/${Const.FB_STORAGE_BOOK}/${config.uid}/${config.publishCode}/${subFile.name}", subFile)
                if(!result){
                    break
                }
                current += subFile.length()

                withContext(Main){
                    showLoadingPercent(binding.layoutLoading.root, getString(R.string.loading_text_upload_file_percent)+subFile.name, ((current.toFloat() / total)*100).toInt())
                }
            }
        }
        fileRepository.deleteTempFile(config.bookId, config.publishCode)
        return result
    }
}