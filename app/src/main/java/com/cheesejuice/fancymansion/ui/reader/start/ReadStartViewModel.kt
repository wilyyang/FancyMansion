package com.cheesejuice.fancymansion.ui.reader.start

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReadStartViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceProvider: PreferenceProvider,
    private val fileRepository: FileRepository,
) : ViewModel(){

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private var _loadingText: String = ""
    val loadingText: String
        get() = _loadingText

    private var _config: Config? = null
    val config: Config?
        get() = _config

    private var _mode: String = ""
    val mode: String
        get() = _mode

    private var _coverImage = null
    val coverImage: File?
        get() = getCoverImageFromFile()

    init {
        _loading.value = true
    }

    fun initConfig(bookId: Long, publishCode: String) {
        setLoading(true, context.getString(R.string.loading_text_get_read_book))
        if(preferenceProvider.getEditPlay()) { _mode = Const.EDIT_PLAY }
        _config = fileRepository.getConfigFromFile(bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = publishCode)
        setLoading(false)
    }

    private fun getCoverImageFromFile(): File? {
        return fileRepository.getImageFile(_config!!.bookId, _config!!.coverImage, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = config!!.publishCode, isCover = true)
    }

    fun setLoading(loading: Boolean, loadingText: String = "") {
        _loading.value = loading
        _loadingText = loadingText
    }

    fun deleteBookFolder(){
        fileRepository.deleteBookFolder(config!!.bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = config!!.publishCode)

    }
}