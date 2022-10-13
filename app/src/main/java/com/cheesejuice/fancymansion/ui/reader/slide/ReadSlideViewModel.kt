package com.cheesejuice.fancymansion.ui.reader.slide

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.models.Logic
import com.cheesejuice.fancymansion.data.models.Slide
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReadSlideViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceProvider: PreferenceProvider,
    private val fileRepository: FileRepository,
) : ViewModel(){

    // loading
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private var _loadingText: String = ""
    val loadingText: String
        get() = _loadingText

    // init
    private val _init = MutableLiveData<Boolean>()
    val init: LiveData<Boolean>
        get() = _init

    private var _publishCode: String = ""
    val publishCode: String
        get() = _publishCode

    private var _logic: Logic? = null
    val logic: Logic?
        get() = _logic

    private var _slide: Slide? = null
    val slide: Slide?
        get() = _slide

    val coverImage: File?
        get() = fileRepository.getImageFile(_logic!!.bookId, _slide!!.slideImage, isReadOnly = (_mode != Const.EDIT_PLAY), publishCode = _publishCode)

    private var _mode: String = ""
    val mode: String
        get() = _mode

    init {
        _loading.value = true
        _init.value = false
    }

    fun initLogicSlide(bookId: Long, slideId: Long, publishCode: String) {
//        setLoading(true, context.getString(R.string.loading_text_get_read_book))
        if(preferenceProvider.getEditPlay()) { _mode = Const.EDIT_PLAY }
        _publishCode = publishCode
//        _config = fileRepository.getConfigFromFile(bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = publishCode)
//        setLoading(false)
//        _init.value = true
    }
}