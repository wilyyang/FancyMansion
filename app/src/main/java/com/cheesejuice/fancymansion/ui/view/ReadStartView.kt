package com.cheesejuice.fancymansion.ui.view

import com.cheesejuice.fancymansion.R
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cheesejuice.fancymansion.ui.view.base.baseView
import com.cheesejuice.fancymansion.ui.viewmodel.BaseViewModel
import com.cheesejuice.fancymansion.ui.viewmodel.ReadStartViewModel
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun readStartView(viewModel: BaseViewModel, readStartViewModel: ReadStartViewModel){
    baseView(viewModel, body = { readStartBodyView(readStartViewModel = readStartViewModel) })
}

@Composable
fun readStartBodyView(readStartViewModel: ReadStartViewModel){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        readStartViewModel.imageFile.value?.also {
            GlideImage(imageModel = readStartViewModel.imageFile.value)

        }?:also{
            GlideImage(imageModel = R.drawable.add_image)
        }
    }
}

@Preview
@Composable
fun readStartPreview() {
    readStartView(BaseViewModel(loading = false, empty = false),
        ReadStartViewModel())
}