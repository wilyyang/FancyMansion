package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityEditSlideBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.ChoiceItem
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.model.SlideBrief
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.util.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.view.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class EditSlideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditSlideBinding
    private var config: Config? = null
    private var slide: Slide? = null
    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookPrefUtil: BookPrefUtil
    @Inject
    lateinit var fileUtil: FileUtil

    private var updateImage = false

    lateinit var toggle: ActionBarDrawerToggle

    private val gallaryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                Glide.with(applicationContext).load(result.data!!.data).into(binding.imageViewShowMain)

                result.data!!.data?.let { returnUri ->
                    contentResolver.query(returnUri, null, null, null, null)
                }?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    slide!!.slideImage = cursor.getString(nameIndex)
                    updateImage = true
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSlideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.imageViewSlideAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            gallaryForResult.launch(intent)
        }

        CoroutineScope(Default).launch {
            var bookId = intent.getLongExtra(Const.KEY_BOOK_ID, ID_NOT_FOUND)
            var slideId = intent.getLongExtra(Const.KEY_PREFIX_EDIT_SLIDE+bookId, ID_NOT_FOUND)

            if(bookId == ID_NOT_FOUND || slideId == ID_NOT_FOUND){
                util.getAlertDailog(this@EditSlideActivity)
                return@launch
            }

            config = fileUtil.getConfigFromFile(bookId)
            slide = fileUtil.getSlideFromJson(bookId, slideId)
            withContext(Main) {
                binding.toolbar.title = "# ${slide!!.id}"
                makeEditSlideScreen(slide!!)
            }
        }
    }

    private fun makeEditSlideScreen(_slide: Slide) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        with(_slide){
            binding.etSlideTitle.setText(title)
            binding.etSlideDescription.setText(description)
            binding.etSlideQuestion.setText(question)
        }
        Glide.with(applicationContext).load(fileUtil.getImageFile(config!!.id, _slide.slideImage)).into(binding.imageViewShowMain)

        binding.recyclerNavEditSlide.layoutManager= LinearLayoutManager(baseContext)
        binding.recyclerNavEditSlide.adapter = BriefAdapter(config!!.briefs, object : OnBriefItemClickListener {
            override fun onItemClick(brief: SlideBrief) {
                startAfterSaveEdits {
                    CoroutineScope(IO).launch {
                        RoundEditText.onceFocus = false
                        updateImage = false

                        config = fileUtil.getConfigFromFile(config!!.id)
                        slide = fileUtil.getSlideFromJson(config!!.id, brief.slideId)
                        withContext(Main) {
                            binding.toolbar.title = "# ${slide!!.id}"
                            makeEditSlideScreen(slide!!)
                        }
                    }
                }
            }
        })

        toggle = ActionBarDrawerToggle(this@EditSlideActivity, binding.drawerEditSlide, R.string.drawer_opened, R.string.drawer_closed)
        toggle.syncState()
    }

    private fun saveSlideFile(slide: Slide){
        with(slide){
            title = binding.etSlideTitle.text.toString()
            description = binding.etSlideDescription.text.toString()
            question = binding.etSlideQuestion.text.toString()

            if(updateImage){
                slideImage = fileUtil.saveImageFile(binding.imageViewShowMain.drawable, config!!.id, slideImage)
            }
            fileUtil.makeSlideJson(config!!.id, this)
        }
        RoundEditText.onceFocus = false
        updateImage = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_slide, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        when(item.itemId) {
            android.R.id.home -> finish()

            R.id.menu_save -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
                CoroutineScope(IO).launch {
                    this@EditSlideActivity.currentFocus?.let { it.clearFocus() }

                    saveSlideFile(slide!!)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                    }
                }
            }

            R.id.menu_play -> {
                if(config != null){
                    startAfterSaveEdits { startViewerActivity() }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startAfterSaveEdits(start:()->Unit){
        if(RoundEditText.onceFocus || updateImage){
            this@EditSlideActivity.currentFocus?.let { it.clearFocus() }

            util.getAlertDailog(
                this@EditSlideActivity,
                getString(R.string.save_dialog_title),
                getString(R.string.save_dialog_question),
                getString(R.string.save_dialog_ok)
            ) { _, _ ->
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
                CoroutineScope(IO).launch {
                    saveSlideFile(slide!!)
                    withContext(Main) {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                        start()
                    }
                }
            }.apply {
                setNegativeButton(getString(R.string.save_dialog_no)) { _, _ ->
                    start()
                }
            }.show()
        }else{
            start()
        }
    }

    private fun startViewerActivity(){
        val intent = Intent(this@EditSlideActivity, ViewerActivity::class.java)
        intent.putExtra(Const.KEY_CURRENT_BOOK_ID, config!!.id)
        intent.putExtra(Const.KEY_EDIT_PLAY, true)
        intent.putExtra(Const.KEY_PLAY_SLIDE_ID, slide!!.id)
        startActivity(intent)
    }
}