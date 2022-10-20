package com.cheesejuice.fancymansion.ui.editor.start

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.models.Logic
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditStartViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceProvider: PreferenceProvider,
    private val fileRepository: FileRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel(){

    // loading
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private var _loadingText: String = ""
    val loadingText: String
        get() = _loadingText

    // config
    private var _config: Config? = null
    val config: Config?
        get() = _config

    val coverImage: File?
        get() = fileRepository.getImageFile(_config!!.bookId, _config!!.coverImage, isCover = true)

    // data
    private var _isBookUpload = false
    val isBookUpload: Boolean
        get() = _isBookUpload


    private var makeBook = false


    // logic
    private lateinit var _logic: Logic

    init {
        _loading.value = true
    }

    fun initBook(isCreate:Boolean, _bookId: Long) {
        setLoading(true, context.getString(R.string.loading_text_get_make_book))

        var bookId = _bookId
        if(isCreate || bookId == Const.ID_NOT_FOUND){
            makeBook = true
            bookId = fileRepository.getNewEditBookId()
            if(bookId != -1L){
                fileRepository.makeEmptyBook(bookId)
            }
        }

        _config = fileRepository.getConfigFromFile(bookId)
        _config?.let {
            if(it.publishCode != ""){
                viewModelScope.launch {
                    _isBookUpload = firebaseRepository.isBookUpload(it.publishCode)
                    setLoading(false)
                }
            }else{
                setLoading(false)
            }
        }?:let {
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean, loadingText: String = "") {
        _loadingText = loadingText
        _loading.value = loading
    }
}