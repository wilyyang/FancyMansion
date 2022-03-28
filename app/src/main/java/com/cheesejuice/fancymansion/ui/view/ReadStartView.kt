package com.cheesejuice.fancymansion.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberImagePainter
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.ui.view.base.baseView
import com.cheesejuice.fancymansion.ui.viewmodel.BaseViewModel
import java.io.File

@Composable
fun readStartView(viewModel: BaseViewModel, config: Config){
    baseView(viewModel, body = { readStartBodyView(config = config) })
}

@Composable
fun readStartBodyView(config: Config){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = rememberImagePainter(data = File("")),
            contentDescription = ""
        )
    }
}

@Preview
@Composable
fun readStartPreview() {
    baseView(BaseViewModel(loading = false, empty = false),
        body = { readStartBodyView(config = Config(bookId = 123456L, title = "Title")) })
}