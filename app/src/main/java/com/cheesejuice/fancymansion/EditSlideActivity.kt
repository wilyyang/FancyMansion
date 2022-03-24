package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.model.SlideLogic
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.databinding.ActivityEditSlideBinding
import com.cheesejuice.fancymansion.extension.*
import com.cheesejuice.fancymansion.model.Config
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
class EditSlideActivity : AppCompatActivity(), View.OnClickListener{
    private lateinit var binding: ActivityEditSlideBinding
    private lateinit var logic: Logic
    private lateinit var slide: Slide
    private var updateImage = false

    lateinit var listAdapterSlide:SlideTitleListAdapter
    lateinit var toggle: ActionBarDrawerToggle

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    private lateinit var gallaryForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSlideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutMain)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.toolbar_edit_slide)

        gallaryForResult = registerGallaryResultName(binding.imageViewShowMain) { imageName ->
            slide.slideImage = imageName
            updateImage = true
        }

        binding.imageViewSlideAdd.setOnClickListener (this)

        val bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
        var slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, ID_NOT_FOUND)
        if(bookId == ID_NOT_FOUND){
            makeNotHaveSlide()
        }

        //// CoroutineScope
        CoroutineScope(Default).launch {
            logic = fileUtil.getLogicFromFile(bookId)!!
            listAdapterSlide = SlideTitleListAdapter(logic!!.logics)
            listAdapterSlide.setItemClickListener(object: SlideTitleListAdapter.OnItemClickListener{
                override fun onClick(v: View, position: Int) {

                    startAfterSaveEdits {
                        CoroutineScope(IO).launch {
                            logic = fileUtil.getLogicFromFile(bookId)!!
                            listAdapterSlide.datas = logic!!.logics

                            slide = fileUtil.getSlideFromFile(logic!!.bookId, logic!!.logics[position].slideId)
                            withContext(Main) {
                                makeEditSlideScreen(slide!!)
                                binding.drawerEditSlide.closeDrawers()
                            }
                        }
                    }
                }
            })

            if(slideId == Const.FIRST_SLIDE && logic.logics.size > 0){
                slideId = logic.logics[0].slideId
            }
            slide = fileUtil.getSlideFromFile(bookId, slideId)
            withContext(Main) {
                binding.recyclerNavEditSlide.layoutManager = LinearLayoutManager(baseContext)
                binding.recyclerNavEditSlide.adapter = listAdapterSlide

                val touchHelper = ItemTouchHelper(SlideTitleListDragCallback(listAdapterSlide))
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

    private fun makeEditSlideScreen(logic: Logic, slide: Slide) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        with(slide){
            binding.toolbar.subtitle = "# $slideId"
            binding.etSlideTitle.setText(slideTitle)
            binding.etSlideDescription.setText(description)
            binding.etSlideQuestion.setText(question)
        }
        Glide.with(applicationContext).load(fileUtil.getImageFile(logic.bookId, slide.slideImage)).into(binding.imageViewShowMain)
    }

    private fun makeNotHaveSlide() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
        binding.layoutEmptySlide.root.visibility = View.VISIBLE
        binding.layoutMain.visibility = View.GONE
    }

    private fun saveSlideFile(logic: Logic, slide: Slide){
        with(slide){
            slideTitle = binding.etSlideTitle.text.toString()
            description = binding.etSlideDescription.text.toString()
            question = binding.etSlideQuestion.text.toString()

            if(updateImage){
                slideImage = fileUtil.makeImageFile(binding.imageViewShowMain.drawable, logic.bookId, slideImage)
            }
            fileUtil.makeSlideFile(logic.bookId, this)
        }
        logic.logics.find { it.slideId == slide.slideId }!!.slideTitle = slide.slideTitle
        fileUtil.makeLogicFile(logic)

        RoundEditText.onceFocus = false
        updateImage = false
        listAdapterSlide.onceMove = false
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.imageViewSlideAdd -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                gallaryForResult.launch(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_slide, menu)
        return true
    }

    //// onOptionsItemSelected
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
                        listAdapterSlide.onceMove = false

                        withContext(Main) {
                            if(binding.layoutEmptySlide.root.visibility == View.VISIBLE){
                                binding.layoutEmptySlide.root.visibility = View.GONE
                                binding.scrollEditSlide.visibility = View.VISIBLE
                            }

                            listAdapterSlide.notifyUpdateBrief(slide!!.slideId, slide!!.slideTitle)
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

                        fileUtil.deleteSlideFile(briefs.bookId, slide!!.slideId)
                        fileUtil.makeLogicFile(briefs)

                        RoundEditText.onceFocus = false
                        updateImage = false
                        listAdapterSlide.onceMove = false

                        if(briefs.logics.size < 1){
                            slide = null
                        }else if(position > 0){
                            slide = fileUtil.getSlideFromFile(briefs.bookId, briefs.logics[position-1].slideId)
                        }else{
                            slide = fileUtil.getSlideFromFile(briefs.bookId, briefs.logics[0].slideId)
                        }

                        withContext(Main) {
                            listAdapterSlide.notifyDeleteBrief(position)
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
                            listAdapterSlide.notifyUpdateBrief(slide!!.slideId, slide!!.slideTitle)
                            showLoadingScreen(false, binding.layoutLoading.root, binding.layoutMain)
                        }
                    }
                }

                R.id.menu_play -> {
                    if(slide == null){ return true }
                    startAfterSaveEdits {
                        bookUtil.setOnlyPlay(true)
                        bookUtil.deleteBookPref(logic!!.bookId, Const.MODE_PLAY)
                        startViewerActivity(logic!!.bookId, slide!!.slideId) }
                }
                else -> {}
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        startAfterSaveEdits{ finish() }
    }

    private fun startAfterSaveEdits(always:() ->Unit){
        showDialogAndStart(isShow = (RoundEditText.onceFocus || updateImage || listAdapterSlide.onceMove),
            loading = binding.layoutLoading.root, main = binding.layoutMain,
            title = getString(R.string.save_dialog_title), message = getString(R.string.save_dialog_question),
            onlyOkBackground = { saveSlideFile(logic, slide) },
            onlyOk = {  listAdapterSlide.notifyUpdateBrief(slide.slideId, slide.slideTitle)},
            onlyNo = {  RoundEditText.onceFocus = false; updateImage = false; listAdapterSlide.onceMove = false },
            always = always
        )
    }
}