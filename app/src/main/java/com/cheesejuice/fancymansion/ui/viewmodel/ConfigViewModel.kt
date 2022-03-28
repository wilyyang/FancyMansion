package com.cheesejuice.fancymansion.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cheesejuice.fancymansion.model.Config

class ConfigViewModel (config:Config) : ViewModel() {
    var config = mutableStateOf(config)
}