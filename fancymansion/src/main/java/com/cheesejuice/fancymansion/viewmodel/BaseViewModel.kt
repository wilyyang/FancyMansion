package com.cheesejuice.fancymansion.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

open class BaseViewModel: ViewModel(){
    private var isLoading = mutableStateOf(true)
    private var isEmpty = mutableStateOf(true)

    fun isLoading() = isLoading.value
    fun isEmpty() = isEmpty.value

    fun setLoading(loading:Boolean){
        isLoading.value = loading
    }

    fun setEmpty(empty:Boolean){
        isLoading.value = false
        isEmpty.value = empty
    }
}