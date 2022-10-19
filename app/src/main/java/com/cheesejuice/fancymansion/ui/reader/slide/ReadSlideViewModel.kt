package com.cheesejuice.fancymansion.ui.reader.slide

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.data.models.*
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReadSlideViewModel @Inject constructor(
    private val preferenceProvider: PreferenceProvider,
    private val fileRepository: FileRepository,
) : ViewModel(){

    // loading
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    // data
    private var mode: String = ""
    private var publishCode: String = ""

    // logic
    private lateinit var _logic: Logic

    // slide
    private var _slide: Slide? = null
    val slide: Slide?
        get() = _slide

    private var _slideLogic: SlideLogic? = null
    val slideLogic: SlideLogic?
        get() = _slideLogic

    private var _passChoiceItems: ArrayList<ChoiceItem> = arrayListOf()
    val passChoiceItems: ArrayList<ChoiceItem>
        get() = _passChoiceItems

    val coverImage: File?
        get() = fileRepository.getImageFile(_logic.bookId, _slide!!.slideImage, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = publishCode)

    fun initLogicSlide(bookId: Long, slideId: Long, publishCode: String) {
        _loading.value = true

        if(preferenceProvider.getEditPlay()) { mode = Const.EDIT_PLAY }
        this.publishCode = publishCode
        _logic = fileRepository.getLogicFromFile(bookId, isReadOnly = mode != Const.EDIT_PLAY, publishCode = publishCode)!!

        makeSlideFromFile(if(slideId == Const.FIRST_SLIDE && _logic.logics.size > 0) _logic.logics[0].slideId else slideId)

        /** [#SAVE] Check Save */
        val savedSlideId = preferenceProvider.getSaveSlideId(_logic.bookId, FirebaseRepository.auth.uid!!, publishCode)
        val firstPlay = (mode != Const.EDIT_PLAY) && _slide!!.slideId != savedSlideId

        if (firstPlay) {
            /** [#ID_COUNT] Slide Id (if first play slide) */
            preferenceProvider.incrementIdCount(_logic.bookId, FirebaseRepository.auth.uid!!, publishCode, _slide!!.slideId, mode)
        }

        _loading.value = false
    }

    fun moveToNextSlide(choiceItem: ChoiceItem) {
        _loading.value = true

        /** [#ID_COUNT] Choice Item */
        preferenceProvider.incrementIdCount(_logic.bookId, FirebaseRepository.auth.uid!!, publishCode, choiceItem.id, mode)

        var nextSlideId = Const.END_SLIDE_ID
        for(enterItem in choiceItem.enterItems) {
            if(preferenceProvider.checkConditions(_logic.bookId, FirebaseRepository.auth.uid!!, publishCode, enterItem.enterConditions, mode)){
                /** [#ID_COUNT] Enter Item */
                preferenceProvider.incrementIdCount(_logic.bookId, FirebaseRepository.auth.uid!!, publishCode, enterItem.id, mode)
                nextSlideId = enterItem.enterSlideId
                break
            }
        }

        makeSlideFromFile(nextSlideId)

        _slide?.let {
            /** [#ID_COUNT] Slide Id */
            preferenceProvider.incrementIdCount(_logic.bookId, FirebaseRepository.auth.uid!!, publishCode, it.slideId, mode)

            /** [#SAVE] Save Slide */
            if(mode != Const.EDIT_PLAY){
                preferenceProvider.setSaveSlideId(_logic.bookId, FirebaseRepository.auth.uid!!, publishCode, it.slideId)
            }
        }

        viewModelScope.launch {
            delay(500L)
            _loading.value = false
        }
    }

    private fun makeSlideFromFile(slideId:Long) {
        _slide = fileRepository.getSlideFromFile(_logic.bookId, slideId, isReadOnly = mode != Const.EDIT_PLAY, publishCode = publishCode)
        _slideLogic = _logic.logics.find { it.slideId == _slide?.slideId }

        _passChoiceItems = arrayListOf()
        for(choiceItem in _slideLogic!!.choiceItems){
            if(preferenceProvider.checkConditions(_logic.bookId, FirebaseRepository.auth.uid!!, publishCode, choiceItem.showConditions, mode)){
                _passChoiceItems.add(choiceItem)
            }
        }
    }
}