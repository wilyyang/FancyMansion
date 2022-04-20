package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.model.SlideLogic
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.databinding.ActivityEditSlideBinding
import com.cheesejuice.fancymansion.extension.*
import com.cheesejuice.fancymansion.model.ChoiceItem
import com.cheesejuice.fancymansion.view.*
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
import javax.inject.Inject

@AndroidEntryPoint
class EditSlideActivity : AppCompatActivity(), View.OnClickListener{
    private lateinit var binding: ActivityEditSlideBinding
    private lateinit var logic: Logic
    private lateinit var slide: Slide
    private lateinit var slideLogic: SlideLogic
    private var updateImage = false
    private var isChoiceEdit = false
    private var isMenuItemEnabled = true

    private lateinit var slideTitleToggle: ActionBarDrawerToggle
    private lateinit var slideTitleListAdapter:SlideTitleListAdapter
    private lateinit var editChoiceListAdapter:EditChoiceListAdapter

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    private lateinit var gallaryForResult: ActivityResultLauncher<Intent>

    private val readSlideForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
            CoroutineScope(Default).launch {
                val init = loadData(logic.bookId, slide.slideId)
                withContext(Main) {
                    if(init) {
                        makeEditSlideScreen(logic, slide, slideLogic)
                    }else{
                        makeNotHaveSlide()
                    }
                }
            }
        }

    private val editChoiceForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            (application as MainApplication).logic = null
            (application as MainApplication).slideLogic = null
            (application as MainApplication).choice?.let {  choiceItem ->
                when (result.resultCode) {
                    Const.RESULT_NEW -> {
                        slideLogic.choiceItems.add(Json.decodeFromString(Json.encodeToString(choiceItem)))
                    }
                    Const.RESULT_UPDATE -> {
                        slideLogic.choiceItems[slideLogic.choiceItems.indexOfFirst { it.id == choiceItem.id }] =
                            Json.decodeFromString(Json.encodeToString(choiceItem))
                    }
                    Const.RESULT_DELETE -> {
                        slideLogic.choiceItems.removeIf { it.id == choiceItem.id }
                    }
                }
                (application as MainApplication).choice = null

                isChoiceEdit = true
                // Keep user slide edit (not call makeEditSlideScreen)
                slideTitleListAdapter.datas = logic.logics
                slideTitleListAdapter.notifyDataSetChanged()

                editChoiceListAdapter.datas = slideLogic.choiceItems
                editChoiceListAdapter.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSlideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.toolbar_edit_slide)

        gallaryForResult = registerGallaryResultName(binding.imageViewShowMain) { imageName ->
            slide.slideImage = imageName
            updateImage = true
        }

        binding.imageViewSlideAdd.setOnClickListener(this)
        binding.tvAddChoice.setOnClickListener(this)

        val bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
        val slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, ID_NOT_FOUND)
        if(bookId == ID_NOT_FOUND){
            util.getAlertDailog(this@EditSlideActivity).show()
        }

        CoroutineScope(Default).launch {
            val init = loadData(bookId, slideId)

            withContext(Main) {
                initNavigationView()
                initEditChoiceListView()

                if(init) {
                    makeEditSlideScreen(logic, slide, slideLogic)
                }else{
                    makeNotHaveSlide()
                }
            }
        }
    }

    private fun loadData(bookId:Long, pSlideId:Long = Const.FIRST_SLIDE): Boolean{
        var slideId = pSlideId
        fileUtil.getLogicFromFile(bookId)?.also { itLogic ->
            logic = itLogic

            if(slideId == Const.FIRST_SLIDE && logic.logics.size > 0){
                slideId = logic.logics[0].slideId
            }

            logic.logics.find { it.slideId == slideId }?.also{ itSlideLogic ->
                slideLogic = itSlideLogic

                fileUtil.getSlideFromFile(bookId, slideId)?.let { itSlide ->
                    slide = itSlide
                    return true
                }
            }
        }
        return false
    }

    private fun initNavigationView(){
        slideTitleListAdapter = SlideTitleListAdapter()
        slideTitleListAdapter.setItemClickListener(object: SlideTitleListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                selectSlideItem(position)
            }
        })

        binding.recyclerNavEditSlide.layoutManager = LinearLayoutManager(baseContext)
        binding.recyclerNavEditSlide.adapter = slideTitleListAdapter

        val touchHelper = ItemTouchHelper(SlideTitleListDragCallback(slideTitleListAdapter))
        touchHelper.attachToRecyclerView(binding.recyclerNavEditSlide)

        slideTitleToggle = ActionBarDrawerToggle(this@EditSlideActivity, binding.layoutActive, R.string.drawer_opened, R.string.drawer_closed)
        slideTitleToggle.syncState()
    }

    private fun initEditChoiceListView(){
        editChoiceListAdapter = EditChoiceListAdapter()
        editChoiceListAdapter.setItemClickListener(object: EditChoiceListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                (application as MainApplication).logic = Json.decodeFromString<Logic>(Json.encodeToString(logic))
                (application as MainApplication).slideLogic = Json.decodeFromString<SlideLogic>(Json.encodeToString(slideLogic))

                val intent = Intent(this@EditSlideActivity, EditChoiceActivity::class.java).apply {
                    putExtra(Const.INTENT_CHOICE_ID, slideLogic.choiceItems[position].id)
                }
                editChoiceForResult.launch(intent)
            }
        })

        binding.recyclerEditChoice.layoutManager = LinearLayoutManager(baseContext)
        binding.recyclerEditChoice.adapter = editChoiceListAdapter

        val touchHelper = ItemTouchHelper(EditChoiceListDragCallback(editChoiceListAdapter))
        touchHelper.attachToRecyclerView(binding.recyclerEditChoice)
    }

    private fun setSaveFlag(flag:Boolean){
        RoundEditText.onceFocus = flag
        updateImage = flag
        Log.d(TAG, "setSaveFlag : $updateImage")
        isChoiceEdit = flag
        slideTitleListAdapter.onceMove = flag
        editChoiceListAdapter.onceMove = flag
    }

    private fun makeEditSlideScreen(logic:Logic, slide: Slide, slideLogic: SlideLogic) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.layoutEmpty.root.visibility = View.GONE
        binding.layoutContain.visibility = View.VISIBLE

        slideTitleListAdapter.datas = logic.logics
        slideTitleListAdapter.notifyDataSetChanged()

        with(slide){
            binding.toolbar.subtitle = "# $slideId"
            binding.etSlideTitle.setText(slideTitle)
            binding.etSlideDescription.setText(description)
            binding.etSlideQuestion.setText(question)
        }
        Glide.with(applicationContext).load(
            fileUtil.getImageFile(logic.bookId, slide.slideImage)?.apply{
                this
            }?:let {
                R.drawable.add_image
            }
        ).into(binding.imageViewShowMain)

        editChoiceListAdapter.datas = slideLogic.choiceItems
        editChoiceListAdapter.notifyDataSetChanged()

        isMenuItemEnabled = true
    }

    private fun makeNotHaveSlide() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        binding.toolbar.subtitle = ""

        isMenuItemEnabled = false
    }

    private fun saveSlideFile(logic: Logic, slide: Slide){
        with(slide){
            slideTitle = binding.etSlideTitle.text.toString()
            description = binding.etSlideDescription.text.toString()
            question = binding.etSlideQuestion.text.toString()

            if(updateImage){
                Log.d(TAG, "$updateImage")
                slideImage = fileUtil.makeImageFile(binding.imageViewShowMain.drawable, logic.bookId, slideImage)
            }
            fileUtil.makeSlideFile(logic.bookId, this)
        }
        logic.logics.find { it.slideId == slide.slideId }!!.slideTitle = slide.slideTitle
        fileUtil.makeLogicFile(logic)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.imageViewSlideAdd -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    type = "image/*"
                }
                gallaryForResult.launch(intent)
            }
            R.id.tvAddChoice -> {
                (application as MainApplication).logic = Json.decodeFromString<Logic>(Json.encodeToString(logic))
                (application as MainApplication).slideLogic = Json.decodeFromString<SlideLogic>(Json.encodeToString(slideLogic))

                val intent = Intent(this@EditSlideActivity, EditChoiceActivity::class.java).apply {
                    putExtra(Const.INTENT_CHOICE_ID, Const.ADD_NEW_CHOICE)
                }
                editChoiceForResult.launch(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_slide, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        this@EditSlideActivity.currentFocus?.let { it.clearFocus() }

        if(item.itemId == R.id.menu_add) {
            startAfterSaveEdits {
                addNewSlide()
            }
        }

        if(!isMenuItemEnabled){
            return true
        }

        if(slideTitleToggle.onOptionsItemSelected(item)){
            return true
        }

        when(item.itemId) {
            R.id.menu_delete -> {
                startAfterSaveEdits {
                    deleteSelectedSlide()
                }
            }
            R.id.menu_save -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(IO).launch {
                    saveSlideFile(logic, slide)
                    setSaveFlag(false)
                    withContext(Main) {
                        slideTitleListAdapter.notifyUpdateSlideTitle(slide.slideId)
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                    }
                }
            }
            R.id.menu_play -> {
                startAfterSaveEdits {
                    bookUtil.setOnlyPlay(true)
                    bookUtil.deleteBookPref(logic.bookId, "", Const.MODE_PLAY)

                    val intent = Intent(this, ReadSlideActivity::class.java).apply {
                        putExtra(Const.INTENT_BOOK_ID, logic.bookId)
                        putExtra(Const.INTENT_SLIDE_ID, slide.slideId)
                    }
                    readSlideForResult.launch(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun selectSlideItem(position: Int){
        startAfterSaveEdits {
            CoroutineScope(IO).launch {
                val init = loadData(logic.bookId, logic.logics[position].slideId)
                withContext(Main) {
                    binding.layoutActive.closeDrawers()
                    if(init){
                        makeEditSlideScreen(logic, slide, slideLogic)
                    }else{
                        makeNotHaveSlide()
                    }
                }
            }
        }
    }

    private fun addNewSlide(){
        // Get new slide id
        val nextId = bookUtil.nextSlideId(logic.logics)
        if(nextId < 0){
            Toast.makeText(this@EditSlideActivity, R.string.alert_max_count, Toast.LENGTH_SHORT).show()
            return
        }

        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
        CoroutineScope(IO).launch {
            // Update Logic (save or not save)
            logic = fileUtil.getLogicFromFile(logic.bookId)!!

            // Object Process
            slide = Slide(slideId = nextId, slideTitle = getString(R.string.name_slide_prefix), question = getString(R.string.text_question_default))
            slideLogic = SlideLogic(slide.slideId, slide.slideTitle)
            logic.logics.add(slideLogic)

            // File Process
            fileUtil.makeSlideFile(logic.bookId, slide)
            fileUtil.makeLogicFile(logic)

            // Make Slide screen
            withContext(Main) {
                if(binding.layoutEmpty.root.visibility == View.VISIBLE){
                    binding.layoutEmpty.root.visibility = View.GONE
                    binding.scrollEditSlide.visibility = View.VISIBLE
                }

                makeEditSlideScreen(logic, slide, slideLogic)
            }
        }
    }

    private fun deleteSelectedSlide(){
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
        CoroutineScope(IO).launch {
            // Update Logic (save or not save)
            logic = fileUtil.getLogicFromFile(logic.bookId)!!

            // Object Process
            val position = logic.logics.indexOfFirst { it.slideId == slide.slideId  }
            logic.logics.removeAt(position)

            // File Process
            fileUtil.deleteSlideFile(logic.bookId, slide.slideId)
            fileUtil.makeLogicFile(logic)

            // Get next slide
            val nextId = if(logic.logics.size > 0 && position > 0) logic.logics[position-1].slideId else Const.FIRST_SLIDE
            val init = loadData(logic.bookId, nextId)

            // Make Slide screen
            withContext(Main) {
                if(init){
                    makeEditSlideScreen(logic, slide, slideLogic)
                }else{
                    makeNotHaveSlide()
                }
            }
        }
    }

    override fun onBackPressed() {
        startAfterSaveEdits{ finish() }
    }

    private fun startAfterSaveEdits(always:() ->Unit){
        showDialogAndStart(isShow = (RoundEditText.onceFocus || updateImage || isChoiceEdit || slideTitleListAdapter.onceMove || editChoiceListAdapter.onceMove),
            loading = binding.layoutLoading.root, main = binding.layoutActive,
            title = getString(R.string.save_dialog_title), message = getString(R.string.save_dialog_question),
            onlyOkBackground = { saveSlideFile(logic, slide) },
            onlyOk = { setSaveFlag(false); slideTitleListAdapter.notifyUpdateSlideTitle(slide.slideId) },
            onlyNo = { setSaveFlag(false) },
            always = always
        )
    }
}