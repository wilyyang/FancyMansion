package com.cheesejuice.fancymansion.view.base

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.view.ui.theme.Dimen

@Composable
fun LoadingView(text:String = stringResource(R.string.wait_make_object)){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp)
        )

        Text(
            modifier = Modifier.padding(all = Dimen.middle),
            text = text,
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview
@Composable
fun LoadingPreview(){
    LoadingView()
}