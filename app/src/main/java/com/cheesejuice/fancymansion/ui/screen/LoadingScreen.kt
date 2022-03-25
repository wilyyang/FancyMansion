package com.cheesejuice.fancymansion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cheesejuice.fancymansion.R

@Composable
fun loadingScreen(isVisible: Boolean){
    if(isVisible){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(100.dp)
            )

            Text(
                modifier = Modifier.padding(all = dimensionResource(id = R.dimen.margin_bsub).value.dp),
                text = stringResource(R.string.wait_make_object),
                fontSize = dimensionResource(id = R.dimen.size_smain).value.sp
            )
        }
    }
}

@Preview
@Composable
fun loadingPreview(){
    loadingScreen(true)
}