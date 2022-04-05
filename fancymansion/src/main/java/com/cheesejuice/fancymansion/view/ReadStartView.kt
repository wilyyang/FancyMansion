package com.cheesejuice.fancymansion.view

import com.cheesejuice.fancymansion.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.cheesejuice.fancymansion.view.base.BaseView
import com.cheesejuice.fancymansion.viewmodel.ReadStartViewModel
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun ReadStartView(readStartViewModel: ReadStartViewModel){
    readStartViewModel.setEmpty(false)

    BaseView(viewModel = readStartViewModel,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Top App Bar")
                },
                navigationIcon = {
                    IconButton(onClick = {  }) {
                        Icon(Icons.Filled.ArrowBack, "back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                elevation = 10.dp
            )
        },
        body = { ReadStartBodyView(readStartViewModel) })
}

@Composable
fun ReadStartBodyView(readStartViewModel: ReadStartViewModel){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

    }
}

@Preview
@Composable
fun ReadStartPreview() {
    val readStartViewModel = ReadStartViewModel()
    readStartViewModel.setEmpty(true)
    ReadStartView(readStartViewModel)
}