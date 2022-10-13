package com.cheesejuice.fancymansion.ui.reader.slide

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ActivityReadSlideBinding
import com.cheesejuice.fancymansion.data.models.ChoiceItem
import com.cheesejuice.fancymansion.data.models.Logic
import com.cheesejuice.fancymansion.data.models.Slide
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.util.Util
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import com.cheesejuice.fancymansion.extension.getAlertDialog
import com.cheesejuice.fancymansion.ui.ChoiceAdapter
import com.cheesejuice.fancymansion.ui.reader.start.ReadStartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ReadSlideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadSlideBinding

    private val viewModel: ReadSlideViewModel by viewModels()

    private lateinit var logic: Logic
    private lateinit var slide: Slide

//    private lateinit var publishCode: String
    private lateinit var adapter: ChoiceAdapter

    @Inject
    lateinit var preferenceProvider: PreferenceProvider
    @Inject
    lateinit var fileRepository: FileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadSlideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.loading.observe(this) { isLoading ->
            binding.layoutLoading.root.findViewById<TextView>(R.id.tvLoading).text = viewModel.loadingText
            binding.layoutLoading.root.visibility = if(isLoading) View.VISIBLE else View.GONE
            binding.layoutActive.visibility = if(isLoading) View.GONE else View.VISIBLE
        }

        viewModel.init.observe(this) { isInit ->
            if(isInit){
                viewModel.config?.also{
                    makeViewReadyScreen(it)
                }?:also{
                    getAlertDialog().show()
                }
            }
        }

        showReadBookLoadingScreen(true)

        // init config & slide object
        viewModel.initLogicSlide(
            bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, Const.ID_NOT_FOUND),
            slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND),
            publishCode = intent.getStringExtra(Const.INTENT_PUBLISH_CODE).orEmpty())

        CoroutineScope(Default).launch {
            val logicTemp = fileRepository.getLogicFromFile(bookId, isReadOnly = (viewModel.mode != Const.EDIT_PLAY), publishCode = viewModel.publishCode)
            val slideTemp = logicTemp?.let {
                // Slide is First
                if(slideId == Const.FIRST_SLIDE && it.logics.size > 0){
                    slideId = it.logics[0].slideId
                }
                fileRepository.getSlideFromFile(bookId, slideId, isReadOnly = (viewModel.mode != Const.EDIT_PLAY), publishCode = viewModel.publishCode)
            }

            withContext(Main) {
                if(logicTemp != null && slideTemp != null) {
                    logic = logicTemp
                    slide = slideTemp

                    // if not play mode and have save, not count save
                    val isSave = ((viewModel.mode != Const.EDIT_PLAY) && slideId == preferenceProvider.getSaveSlideId(logic.bookId, FirebaseRepository.auth.uid!!, viewModel.publishCode))
                    if(!isSave){ preferenceProvider.incrementIdCount(logic.bookId, FirebaseRepository.auth.uid!!, viewModel.publishCode, slide.slideId, viewModel.mode) }

                    makeSlideScreen(logic, slide)
                }else{
                    makeNotHaveSlide()
                }
            }
        }
    }

    private fun makeSlideScreen(logic: Logic, slide: Slide) {
        showReadBookLoadingScreen(false)
        with(slide){
            // Make Main Content
            fileRepository.getImageFile(logic.bookId, slideImage, isReadOnly = (viewModel.mode != Const.EDIT_PLAY), publishCode = viewModel.publishCode)
                ?.also {
                    binding.layoutShow.visibility = View.VISIBLE
                    Glide.with(applicationContext).load(it).into(binding.imageViewShowMain)
                } ?: also {
                binding.layoutShow.visibility = View.GONE
            }

            binding.tvSlideTitle.text = slideTitle
            binding.tvSlideDescription.text = description
            binding.tvSlideQuestion.text = question

            // Select Choice Item
            val passChoiceItems: ArrayList<ChoiceItem> = arrayListOf()
            logic.logics.find { it.slideId == slideId }?.let {
                if(it.type == Const.SLIDE_TYPE_END){
                    binding.tvEndingType.visibility = View.VISIBLE
                }else{
                    binding.tvEndingType.visibility = View.INVISIBLE
                }

                for(choiceItem in it.choiceItems){
                    if(preferenceProvider.checkConditions(logic.bookId, FirebaseRepository.auth.uid!!, viewModel.publishCode, choiceItem.showConditions, viewModel.mode)){
                        passChoiceItems.add(choiceItem)
                    }
                }
            }

            // Make Choice Item
            binding.recyclerChoice.layoutManager = LinearLayoutManager(baseContext)
            adapter = ChoiceAdapter(passChoiceItems)
            adapter.setItemClickListener(object : ChoiceAdapter.OnItemClickListener {
                override fun onClick(v: View, choiceItem: ChoiceItem) {
                    preferenceProvider.incrementIdCount(logic.bookId, FirebaseRepository.auth.uid!!, viewModel.publishCode, choiceItem.id, viewModel.mode)
                    enterNextSlide(logic, choiceItem)
                }
            })
            binding.recyclerChoice.adapter = adapter
        }

        // Save read slide point
        if(viewModel.mode != Const.EDIT_PLAY){
            preferenceProvider.setSaveSlideId(logic.bookId, FirebaseRepository.auth.uid!!, viewModel.publishCode, slide.slideId)
        }
    }

    private fun makeNotHaveSlide() {
        showReadBookLoadingScreen(false)
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutActive.visibility = View.GONE
    }

    private fun enterNextSlide(logic: Logic, choiceItem: ChoiceItem){
        adapter.setDisabled(disabled = true)
        showReadBookLoadingScreen(true)

        CoroutineScope(Default).launch {
            // Get Next Slide Id
            var nextSlideId = Const.END_SLIDE_ID
            for(enterItem in choiceItem.enterItems) {
                if(preferenceProvider.checkConditions(logic.bookId, FirebaseRepository.auth.uid!!, viewModel.publishCode, enterItem.enterConditions, viewModel.mode)){
                    preferenceProvider.incrementIdCount(logic.bookId, FirebaseRepository.auth.uid!!, viewModel.publishCode, enterItem.id, viewModel.mode)
                    nextSlideId = enterItem.enterSlideId
                    break
                }
            }

            val slideNext = fileRepository.getSlideFromFile(logic.bookId, nextSlideId, isReadOnly = (viewModel.mode != Const.EDIT_PLAY), publishCode = viewModel.publishCode)

            delay(500)
            withContext(Main){
                slideNext?.also {
                    slide = it
                    preferenceProvider.incrementIdCount(logic.bookId, FirebaseRepository.auth.uid!!, viewModel.publishCode, slide.slideId, viewModel.mode)
                    makeSlideScreen(logic, slide)
                }?:also {
                    showReadBookLoadingScreen(false)
                    binding.layoutEmpty.root.visibility = View.VISIBLE
                    binding.layoutContain.visibility = View.GONE
                }
            }
        }
    }

    private fun showReadBookLoadingScreen(isLoading: Boolean) {
        binding.layoutLoading.root.visibility = if(isLoading) View.VISIBLE else View.GONE
        binding.recyclerChoice.isEnabled = !isLoading
    }
}