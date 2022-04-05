package com.cheesejuice.fancymansion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cheesejuice.fancymansion.view.ReadStartView
import com.cheesejuice.fancymansion.view.ui.theme.FancyMansionTheme
import com.cheesejuice.fancymansion.viewmodel.BaseViewModel
import com.cheesejuice.fancymansion.viewmodel.ReadStartViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReadStartActivity : ComponentActivity() {

    private val readStartViewModel:ReadStartViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FancyMansionTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ReadStartView(readStartViewModel)
                }
            }
        }
    }
}