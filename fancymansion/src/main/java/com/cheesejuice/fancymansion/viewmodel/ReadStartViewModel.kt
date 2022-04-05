package com.cheesejuice.fancymansion.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.cheesejuice.fancymansion.common.Const
import com.cheesejuice.fancymansion.data.storage.model.Config
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReadStartViewModel @Inject constructor() : BaseViewModel() {
    private var config = mutableStateOf<Config?>(null)
    private var imageFile = mutableStateOf<File?>(null)
    private var mode = mutableStateOf("")

    fun getConfig(bookId:Long = Const.ID_DEFAULT): Config?{
        return if(bookId == Const.ID_DEFAULT){
            config.value
        }else{
            null
        }
    }

    fun getImageFile() = imageFile.value
}