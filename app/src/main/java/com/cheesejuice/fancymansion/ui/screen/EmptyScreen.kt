package com.cheesejuice.fancymansion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintSet
import com.cheesejuice.fancymansion.R

class EmptyScreenLayout : ConstraintSet.Layout(){
    @Composable
    fun emptyScreen(isVisible: Boolean){
        if(isVisible){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(all = dimensionResource(id = R.dimen.margin_bsub).value.dp),
                    text = stringResource(R.string.alert_not_found_file),
                    fontSize = dimensionResource(id = R.dimen.size_smain).value.sp
                )
            }
        }
    }

    @Preview
    @Composable
    fun emptyPreview(){
        emptyScreen(true)
    }
}


