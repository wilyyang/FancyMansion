package com.cheesejuice.fancymansion.ui.reader.start

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReadStartViewModel @Inject constructor(
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

    private var _config: Config? = null
    val config: Config?
        get() = _config

    val coverImage: File?
        get() = fileRepository.getImageFile(
            _config!!.bookId,
            _config!!.coverImage,
            isReadOnly = (mode != Const.EDIT_PLAY),
            publishCode = config!!.publishCode,
            isCover = true
        )

    private var _mode: String = ""
    val mode: String
        get() = _mode

    // delete
    private val _delete = MutableLiveData<Boolean>()
    val delete: LiveData<Boolean>
        get() = _delete

    // get save slide id
    val saveSlideId: Long
        get() = preferenceProvider.getSaveSlideId(_config!!.bookId, FirebaseRepository.auth.uid!!, _config!!.publishCode)

    init {
        _loading.value = true
        _init.value = false
        _delete.value = false
    }

    fun initConfig(bookId: Long, publishCode: String) {
        setLoading(true, context.getString(R.string.loading_text_get_read_book))
        if(preferenceProvider.getEditPlay()) { _mode = Const.EDIT_PLAY }
        _config = fileRepository.getConfigFromFile(bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = publishCode)
        setLoading(false)
        _init.value = true
    }

    private fun setLoading(loading: Boolean, loadingText: String = "") {
        _loading.value = loading
        _loadingText = loadingText
    }

    fun deleteBookFolder(){
        setLoading(true, context.getString(R.string.loading_text_delete_read_book))
        fileRepository.deleteBookFolder(_config!!.bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = _config!!.publishCode)
        _delete.value = true
    }

    fun deleteBookPref() =
        preferenceProvider.deleteBookPref(_config!!.bookId, FirebaseRepository.auth.uid!!, _config!!.publishCode, mode)
}