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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityEditSlideBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.model.SlideLogic
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
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
    private lateinit var logic: Logic
    private var slide: Slide? = null
    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    lateinit var adapter:BriefAdapter

    lateinit var toggle: ActionBarDrawerToggle
    private var updateImage = false

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
        binding.toolbar.title = getString(R.string.toolbar_edit_slide)

        binding.imageViewSlideAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            gallaryForResult.launch(intent)
        }

        CoroutineScope(Default).launch {
            val bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
            val slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, ID_NOT_FOUND)

            if(bookId == ID_NOT_FOUND){
                util.getAlertDailog(this@EditSlideActivity)
                return@launch
            }

            logic = fileUtil.getLogicFromFile(bookId)!!
            adapter = BriefAdapter(logic!!.logics)
            adapter.setItemClickListener(object: BriefAdapter.OnItemClickListener{
                override fun onClick(v: View, position: Int) {
                    startAfterSaveEdits {
                        CoroutineScope(IO).launch {
                            logic = fileUtil.getLogicFromFile(bookId)!!
                            adapter.data = logic!!.logics

                            slide = fileUtil.getSlideFromFile(logic!!.bookId, logic!!.logics[position].slideId)
                            withContext(Main) {
                                makeEditSlideScreen(slide!!)
                                binding.drawerEditSlide.closeDrawers()
                            }
                        }
                    }
                }
            })

            slide = fileUtil.getSlideFromFile(bookId, slideId)
            withContext(Main) {
                binding.recyclerNavEditSlide.layoutManager = LinearLayoutManager(baseContext)
                binding.recyclerNavEditSlide.adapter = adapter

                val touchHelper = ItemTouchHelper(BriefDragCallback(adapter))
                touchHelper.attachToRecyclerView(binding.recyclerNavEditSlide)

                toggle = ActionBarDrawerToggle(this@EditSlideActivity, binding.drawerEditSlide, R.string.drawer_opened, R.string.drawer_closed)
                toggle.syncState()

                if(slide == null){
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                    binding.layoutEmptySlide.root.visibility = View.VISIBLE
                    binding.scrollEditSlide.visibility = View.GONE
                }else{
                    makeEditSlideScreen(slide!!)
                }
            }
        }
    }

    private fun makeEditSlideScreen(_slide: Slide) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        with(_slide){
            binding.toolbar.subtitle = "# $slideId"
            binding.etSlideTitle.setText(slideTitle)
            binding.etSlideDescription.setText(description)
            binding.etSlideQuestion.setText(question)
        }
        Glide.with(applicationContext).load(fileUtil.getImageFile(logic!!.bookId, _slide.slideImage)).into(binding.imageViewShowMain)
    }

    private fun saveSlideFile(slide: Slide){
        with(slide){
            slideTitle = binding.etSlideTitle.text.toString()
            description = binding.etSlideDescription.text.toString()
            question = binding.etSlideQuestion.text.toString()

            if(updateImage){
                slideImage = fileUtil.makeImageFile(binding.imageViewShowMain.drawable, logic!!.bookId, slideImage)
            }
            fileUtil.makeSlideFile(logic!!.bookId, this)
        }

        logic!!.logics.find { it.slideId == slide.slideId }!!.slideTitle = slide.slideTitle
        fileUtil.makeLogicFile(logic!!)

        RoundEditText.onceFocus = false
        updateImage = false
        adapter.onceMove = false
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

        this@EditSlideActivity.currentFocus?.let { it.clearFocus() }
        logic?.let { briefs ->
            when(item.itemId) {
                R.id.menu_add -> {
                    val nextId = bookUtil.nextSlideId(briefs.logics)
                    if(nextId < 0){
                        Toast.makeText(this@EditSlideActivity, R.string.alert_max_count, Toast.LENGTH_SHORT).show()
                        return true
                    }

                    showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
                    CoroutineScope(IO).launch {

                        slide = Slide(slideId = nextId, slideTitle = getString(R.string.name_slide_prefix), question = getString(R.string.text_question_default))
                        briefs.logics.add(SlideLogic(slide!!.slideId, slide!!.slideTitle))

                        fileUtil.makeSlideFile(briefs.bookId, slide!!)
                        fileUtil.makeLogicFile(briefs)

                        RoundEditText.onceFocus = false
                        updateImage = false
                        adapter.onceMove = false

                        withContext(Main) {
                            if(binding.layoutEmptySlide.root.visibility == View.VISIBLE){
                                binding.layoutEmptySlide.root.visibility = View.GONE
                                binding.scrollEditSlide.visibility = View.VISIBLE
                            }

                            adapter.notifyUpdateBrief(slide!!.slideId, slide!!.slideTitle)
                            makeEditSlideScreen(slide!!)
                            showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                        }
                    }
                }
                R.id.menu_delete -> {
                    if(slide == null){ return true }
                    showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
                    CoroutineScope(IO).launch {
                        val position = briefs.logics.indexOfFirst { it.slideId == slide!!.slideId  }
                        briefs.logics.removeAt(position)

                        fileUtil.deleteSlideFile(briefs.bookId, slide!!)
                        fileUtil.makeLogicFile(briefs)

                        RoundEditText.onceFocus = false
                        updateImage = false
                        adapter.onceMove = false

                        if(briefs.logics.size < 1){
                            slide = null
                        }else if(position > 0){
                            slide = fileUtil.getSlideFromFile(briefs.bookId, briefs.logics[position-1].slideId)
                        }else{
                            slide = fileUtil.getSlideFromFile(briefs.bookId, briefs.logics[0].slideId)
                        }

                        withContext(Main) {
                            adapter.notifyDeleteBrief(position)
                            slide?.also { makeEditSlideScreen(it) }?:also {
                                binding.layoutEmptySlide.root.visibility = View.VISIBLE
                                binding.scrollEditSlide.visibility = View.GONE
                            }

                            showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                        }
                    }
                }

                R.id.menu_save -> {
                    if(slide == null){ return true }
                    showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
                    CoroutineScope(IO).launch {
                        saveSlideFile(slide!!)
                        withContext(Main) {
                            adapter.notifyUpdateBrief(slide!!.slideId, slide!!.slideTitle)
                            showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                        }
                    }
                }

                R.id.menu_play -> {
                    if(slide == null){ return true }
                    startAfterSaveEdits { startViewerActivity() }
                }
                else -> {}
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startAfterSaveEdits(start:()->Unit){
        if(RoundEditText.onceFocus || updateImage || adapter.onceMove){
            this@EditSlideActivity.currentFocus?.let { it.clearFocus() }
            util.getAlertDailog(
                this@EditSlideActivity,
                getString(R.string.save_dialog_title),
                getString(R.string.save_dialog_question),
                getString(R.string.dialog_ok)
            ) { _, _ ->
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)
                CoroutineScope(IO).launch {
                    saveSlideFile(slide!!)
                    withContext(Main) {
                        adapter.notifyUpdateBrief(slide!!.slideId, slide!!.slideTitle)
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                        start()
                    }
                }
            }.apply {
                setNegativeButton(getString(R.string.dialog_no)) { _, _ ->
                    RoundEditText.onceFocus = false
                    updateImage = false
                    adapter.onceMove = false
                    start()
                }
            }.show()
        }else{
            start()
        }
    }

    private fun startViewerActivity(){
        bookUtil.setOnlyPlay(true)
        bookUtil.deleteBookPref(logic!!.bookId, Const.MODE_PLAY)

        val intent = Intent(this@EditSlideActivity, ViewerActivity::class.java)
        intent.putExtra(Const.INTENT_BOOK_ID, logic!!.bookId)
        intent.putExtra(Const.INTENT_SLIDE_ID, slide!!.slideId)
        startActivity(intent)
    }

    override fun onBackPressed() {
        startAfterSaveEdits { finish() }
    }
}