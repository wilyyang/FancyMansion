package com.cheesejuice.fancymansion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.ui.view.readStartBodyView
import com.cheesejuice.fancymansion.ui.view.readStartView
import com.cheesejuice.fancymansion.ui.viewmodel.BaseViewModel
import com.cheesejuice.fancymansion.ui.viewmodel.ConfigViewModel

class TestComposeActivity : ComponentActivity() {
    private lateinit var viewModel: BaseViewModel
    private lateinit var configViewModel: ConfigViewModel
    private lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = BaseViewModel(loading = false, empty = false)

        configViewModel = ConfigViewModel(Config(bookId = 123456L, title = "Title"))
        configViewModel.config.value!!.title


            setContent { readStartBodyView(configViewModel) }
    }

    override fun onBackPressed() {
//        val loading = viewModel.isLoading.value
//        val emtpy = viewModel.isEmpty.value
//        viewModel.isLoading.value = emtpy
//        viewModel.isEmpty.value = !loading

        config = Config(346346L, title = "MAMAMAMA")
        configViewModel.config.value!!.title = config.title
//        configViewModel.config.value!!.title = config.title
    }
}

@Preview
@Composable
fun preview(){
    val viewModel = BaseViewModel(loading = false, empty = false)
    val config = Config(bookId = 123456L, title = "Title")
    val configViewModel = ConfigViewModel(config)

    readStartView(viewModel, configViewModel)
}