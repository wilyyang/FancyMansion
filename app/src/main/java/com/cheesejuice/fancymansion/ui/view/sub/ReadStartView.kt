package com.cheesejuice.fancymansion.ui.view.sub

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.ui.view.base.baseView
import com.cheesejuice.fancymansion.ui.viewmodel.base.BaseViewModel
import com.cheesejuice.fancymansion.ui.viewmodel.sub.ConfigViewModel

@Composable
fun readStartView(viewModel: BaseViewModel, configViewModel: ConfigViewModel){
    baseView(viewModel, loadingText = "MEro", emptyText = "fifif",
    body = {readStartBodyView(viewModel = configViewModel)})
}

@Composable
fun readStartBodyView(viewModel: ConfigViewModel){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(viewModel.config.value != null){
            Text(
                text = viewModel.config.value.toString(),
                style = MaterialTheme.typography.body1
            )
        }else{
            Text(
                text = "Config is Null",
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Preview
@Composable
fun readStartPreview() {
    var config = Config(bookId = 123456L, title = "Title")
    val configViewModel = ConfigViewModel(config)
    val viewModel = BaseViewModel(loading = false, empty = false)
    baseView(viewModel, loadingText = "MEro", emptyText = "fifif",
        body = {readStartBodyView(viewModel = configViewModel)})
}