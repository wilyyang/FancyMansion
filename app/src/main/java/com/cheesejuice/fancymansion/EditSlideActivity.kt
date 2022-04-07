package com.cheesejuice.fancymansion

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
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

    lateinit var slideTitleListAdapter:SlideTitleListAdapter
    lateinit var slideTitleToggle: ActionBarDrawerToggle

    lateinit var editChoiceListAdapter:EditChoiceListAdapter
    var isMenuItemEnabled = true

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    private lateinit var gallaryForResult: ActivityResultLauncher<Intent>

    private val readSlideForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
            val bookId = logic.bookId
            val slideId = slide.slideId

            CoroutineScope(Default).launch {
                val logicTemp = fileUtil.getLogicFromFile(bookId)
                val slideTemp = logicTemp?.let {fileUtil.getSlideFromFile(bookId, slideId)}
                val slideLogicTemp = logicTemp?.logics?.find { it.slideId == slideId }

                withContext(Main) {
                    if(logicTemp !== null && slideTemp != null && slideLogicTemp != null) {
                        logic = logicTemp
                        slide = slideTemp
                        slideLogic = slideLogicTemp
                        makeEditSlideScreen(logic, slide, slideLogic.choiceItems)
                    }else{
                        makeNotHaveSlide()
                    }
                }
            }
        }

    private val editChoiceForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                logic = (application as MainApplication).logic!!
                slideLogic = logic.logics.first { it.slideId == slide.slideId }

                isChoiceEdit = true

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
        var slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, ID_NOT_FOUND)
        if(bookId == ID_NOT_FOUND){
            makeNotHaveSlide()
            return
        }

        CoroutineScope(Default).launch {
            val logicTemp = fileUtil.getLogicFromFile(bookId)
            val slideTemp = logicTemp?.let {
                if(slideId == Const.FIRST_SLIDE && it.logics.size > 0){
                    slideId = it.logics[0].slideId
                }
                fileUtil.getSlideFromFile(bookId, slideId)
            }
            val slideLogicTemp = logicTemp?.logics?.find { it.slideId == slideId }

            withContext(Main) {
                if(logicTemp !== null && slideTemp != null && slideLogicTemp != null) {
                    logic = logicTemp
                    slide = slideTemp
                    slideLogic = slideLogicTemp

                    initNavigationView(logic)
                    initEditChoiceListView(slideLogic.choiceItems)
                    makeEditSlideScreen(logic, slide, slideLogic.choiceItems)

                }else{
                    makeNotHaveSlide()
                }
            }
        }
    }

    private fun initNavigationView(logic: Logic){
        slideTitleListAdapter = SlideTitleListAdapter(logic.logics)
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

    private fun initEditChoiceListView(choiceItems: MutableList<ChoiceItem>){
        editChoiceListAdapter = EditChoiceListAdapter(choiceItems)
        editChoiceListAdapter.setItemClickListener(object: EditChoiceListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {

                (application as MainApplication).logic =  Json.decodeFromString<Logic>(Json.encodeToString(logic))

                val intent = Intent(this@EditSlideActivity, EditChoiceActivity::class.java)
                intent.putExtra(Const.INTENT_SLIDE_ID, slide.slideId)
                intent.putExtra(Const.INTENT_CHOICE_ID, slideLogic.choiceItems[position].id)
                editChoiceForResult.launch(intent)
            }
        })

        binding.recyclerEditChoice.layoutManager = LinearLayoutManager(baseContext)
        binding.recyclerEditChoice.adapter = editChoiceListAdapter

        val touchHelper = ItemTouchHelper(EditChoiceListDragCallback(editChoiceListAdapter))
        touchHelper.attachToRecyclerView(binding.recyclerEditChoice)
    }

    private fun selectSlideItem(position: Int){
        startAfterSaveEdits {
            CoroutineScope(IO).launch {
                val slideId = logic.logics[position].slideId
                val tempLogic = fileUtil.getLogicFromFile(logic.bookId)
                val tempSlide = fileUtil.getSlideFromFile(logic.bookId, slideId)
                val tempSlideLogic = tempLogic?.logics?.find { it.slideId == slideId }

                withContext(Main) {
                    binding.layoutActive.closeDrawers()
                    if(tempLogic != null && tempSlide != null && tempSlideLogic != null){
                        logic = tempLogic
                        slide = tempSlide
                        slideLogic = tempSlideLogic
                        slideTitleListAdapter.datas = logic.logics
                        makeEditSlideScreen(logic, slide, slideLogic.choiceItems)

                    }else{
                        makeNotHaveSlide()
                    }
                }
            }
        }
    }

    private fun makeEditSlideScreen(logic: Logic, slide: Slide, choiceItems: MutableList<ChoiceItem>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
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

        editChoiceListAdapter.datas = choiceItems
        editChoiceListAdapter.notifyDataSetChanged()

        isMenuItemEnabled = true
    }

    private fun makeNotHaveSlide() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        isMenuItemEnabled = false
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
        isChoiceEdit = false
        slideTitleListAdapter.onceMove = false
        editChoiceListAdapter.onceMove = false
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.imageViewSlideAdd -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                gallaryForResult.launch(intent)
            }
            R.id.tvAddChoice -> {
                (application as MainApplication).logic = Json.decodeFromString<Logic>(Json.encodeToString(logic))

                val intent = Intent(this, EditChoiceActivity::class.java)
                intent.putExtra(Const.INTENT_SLIDE_ID, slide.slideId)
                intent.putExtra(Const.INTENT_CHOICE_ID, Const.ADD_NEW_CHOICE)
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
        if(slideTitleToggle.onOptionsItemSelected(item)){
            return true
        }

        this@EditSlideActivity.currentFocus?.let { it.clearFocus() }
        if(!isMenuItemEnabled){
            return true
        }

        when(item.itemId) {
            R.id.menu_add -> addNewSlide()
            R.id.menu_delete -> deleteSelectedSlide()
            R.id.menu_save -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(IO).launch {
                    saveSlideFile(logic, slide)
                    withContext(Main) {
                        slideTitleListAdapter.notifyUpdateSlideTitle(slide.slideId)
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                    }
                }
            }
            R.id.menu_play -> {
                startAfterSaveEdits {
                    bookUtil.setOnlyPlay(true)
                    bookUtil.deleteBookPref(logic.bookId, Const.MODE_PLAY)

                    val intent = Intent(this, ReadSlideActivity::class.java)
                    intent.putExtra(Const.INTENT_BOOK_ID, logic.bookId)
                    intent.putExtra(Const.INTENT_SLIDE_ID, slide.slideId)
                    readSlideForResult.launch(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
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
            // Make new Slide object
            slide = Slide(slideId = nextId, slideTitle = getString(R.string.name_slide_prefix), question = getString(R.string.text_question_default))
            slideLogic = SlideLogic(slide.slideId, slide.slideTitle)
            logic.logics.add(slideLogic)

            // Save file
            fileUtil.makeSlideFile(logic.bookId, slide)
            fileUtil.makeLogicFile(logic)

            withContext(Main) {
                if(binding.layoutEmpty.root.visibility == View.VISIBLE){
                    binding.layoutEmpty.root.visibility = View.GONE
                    binding.scrollEditSlide.visibility = View.VISIBLE
                }

                RoundEditText.onceFocus = false
                updateImage = false
                isChoiceEdit = false
                slideTitleListAdapter.onceMove = false
                editChoiceListAdapter.onceMove = false

                // Make Slide screen
                slideTitleListAdapter.notifyUpdateSlideTitle(slide.slideId)
                makeEditSlideScreen(logic, slide, slideLogic.choiceItems)
                showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
            }
        }
    }

    private fun deleteSelectedSlide(){
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
        CoroutineScope(IO).launch {
            // File IO
            val position = logic.logics.indexOfFirst { it.slideId == slide.slideId  }
            logic.logics.removeAt(position)

            fileUtil.deleteSlideFile(logic.bookId, slide.slideId)
            fileUtil.makeLogicFile(logic)

            val tempSlide:Slide? = if(logic.logics.size > 1){
                if(position > 0){
                    fileUtil.getSlideFromFile(logic.bookId, logic.logics[position-1].slideId)
                }else{
                    fileUtil.getSlideFromFile(logic.bookId, logic.logics[0].slideId)
                }
            }else { null }
            val tempSlideLogic = if (tempSlide!= null) logic.logics.find { it.slideId == tempSlide.slideId } else null

            withContext(Main) {
                RoundEditText.onceFocus = false
                updateImage = false
                isChoiceEdit = false
                slideTitleListAdapter.onceMove = false
                editChoiceListAdapter.onceMove = false

                slideTitleListAdapter.notifyDeleteSlideTitle(position)

                if(tempSlide != null && tempSlideLogic != null){
                    slide = tempSlide
                    slideLogic = tempSlideLogic
                    makeEditSlideScreen(logic, slide, slideLogic.choiceItems)
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
            onlyOk = {  slideTitleListAdapter.notifyUpdateSlideTitle(slide.slideId)},
            onlyNo = {  RoundEditText.onceFocus = false; updateImage = false; isChoiceEdit = false; slideTitleListAdapter.onceMove = false; editChoiceListAdapter.onceMove = false },
            always = always
        )
    }
}