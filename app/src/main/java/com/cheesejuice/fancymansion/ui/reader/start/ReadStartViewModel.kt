package com.cheesejuice.fancymansion.ui.reader.start

import androidx.lifecycle.ViewModel
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReadStartViewModel @Inject constructor(
    private val preferenceProvider: PreferenceProvider,
    private val fileRepository: FileRepository,
) : ViewModel(){

    private var _config: Config? = null
    val config: Config?
        get() = _config

    private var _mode: String = ""
    val mode: String
        get() = _mode

    private var _coverImage = null
    val coverImage: File?
        get() = getCoverImage()

    fun initConfig(bookId: Long, publishCode: String) {
        if(preferenceProvider.getEditPlay()) { _mode = Const.EDIT_PLAY
        }
        _config = fileRepository.getConfigFromFile(bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = publishCode)
    }

    @JvmName("getCoverImage1")
    fun getCoverImage(): File? {
        return fileRepository.getImageFile(_config!!.bookId, _config!!.coverImage, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = config!!.publishCode, isCover = true)
    }

    fun deleteBookFolder(){
        fileRepository.deleteBookFolder(config!!.bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = config!!.publishCode)

    }
}