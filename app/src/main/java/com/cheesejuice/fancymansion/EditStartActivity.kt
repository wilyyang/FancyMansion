package com.cheesejuice.fancymansion

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityEditStartBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.Const.Companion.TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject
import com.cheesejuice.fancymansion.extension.*
import com.cheesejuice.fancymansion.view.RoundEditText
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    private var newUpload = true

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
            conf?.let {
                if(conf.publishCode != ""){
                    val colRef = MainApplication.db.collection("book")
                    val uploadConfig = colRef.whereEqualTo("publishCode", conf.publishCode)
                        .whereEqualTo("uid", conf.uid).get().await()

                    if(uploadConfig.documents.size > 0){
                        newUpload = false
                    }
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

    private fun makeEditReadyScreen(conf: Config) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

        if(!newUpload){
            val menuItem = binding.toolbar.menu.findItem(R.id.menu_upload)
            menuItem.title = getString(R.string.menu_book_update)
        }

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

    private fun saveConfigFile(conf : Config, isCopy : Boolean = false) {
        with(conf){
            updateTime = System.currentTimeMillis()
            version += 1
            title = binding.etConfigTitle.text.toString() + if(isCopy){ getString(R.string.title_endwith_copy) } else { "" }
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
                this@EditStartActivity.currentFocus?.clearFocus()
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(IO).launch {
                    val dbSuccess = uploadBook()
                    val fileSuccess = uploadBookFile()
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

                        if(!dbSuccess){
                            Toast.makeText(this@EditStartActivity, getString(R.string.toast_db_fail), Toast.LENGTH_SHORT).show()
                        }else if(!fileSuccess){
                            Toast.makeText(this@EditStartActivity, getString(R.string.toast_storage_fail), Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@EditStartActivity, getString(R.string.toast_query_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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
                this@EditStartActivity.currentFocus?.clearFocus()
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
                this@EditStartActivity.currentFocus?.clearFocus()
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

            R.id.menu_copy -> {
                this@EditStartActivity.currentFocus?.clearFocus()
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(IO).launch {
                    val copyBookId = fileUtil.getNewEditBookId()
                    if (copyBookId != -1L) {
                        val targetDir = File(fileUtil.bookUserPath, Const.FILE_PREFIX_BOOK + config.bookId)
                        val copyDir = File(fileUtil.bookUserPath, Const.FILE_PREFIX_BOOK + copyBookId)
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
                        showLoadingScreen(false,binding.layoutLoading.root,binding.layoutActive)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun uploadBook():Boolean{
        var result = false
        MainApplication.auth.uid?.let { userId ->
            config.apply {
                user = MainApplication.name ?: ""
                email = MainApplication.email ?: ""
                uid = userId
            }

            val colRef = MainApplication.db.collection("book")
            if(newUpload){
                colRef.add(config).await().id.let {
                    config.publishCode = it
                }
            }

            if(config.publishCode != ""){
                saveConfigFile(config)
                colRef.document(config.publishCode).set(config).await()
                result = true
            }
        }
        return result
    }

    private suspend fun uploadBookFile(): Boolean{
        var result = true
        val storage = MainApplication.storage
        val storageRef: StorageReference = storage.reference

        val localBookFile = fileUtil.compressBook(bookId = config.bookId)
        localBookFile?.listFiles()?.forEach {  subFile ->
            val subFileRef: StorageReference = storageRef.child("/book/${config.uid}/${config.publishCode}/${subFile.name}")
            subFileRef.putFile(Uri.fromFile(subFile))
                .addOnFailureListener{
                    result = false
                }.await()
        }
        return result
    }
}