package com.cheesejuice.fancymansion.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cheesejuice.fancymansion.model.Config
import java.io.File

class ReadStartViewModel (config:Config? = null, imageFile: File? = null) : ViewModel() {
    var config = mutableStateOf<Config?>(config)
    var imageFile = mutableStateOf<File?>(imageFile)
    var mode = mutableStateOf<String>("")
}