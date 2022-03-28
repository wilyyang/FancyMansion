package com.cheesejuice.fancymansion.ui.viewmodel.sub

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cheesejuice.fancymansion.model.Config

class ConfigViewModel (config:Config? = null) : ViewModel() {
    var config = mutableStateOf(config)
}