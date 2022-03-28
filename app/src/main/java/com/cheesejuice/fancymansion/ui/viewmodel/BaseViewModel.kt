package com.cheesejuice.fancymansion.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class BaseViewModel (loading: Boolean = false, empty:Boolean = false) : ViewModel() {
    var isLoading = mutableStateOf(loading)
    var isEmpty = mutableStateOf(empty)
}