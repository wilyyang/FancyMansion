package com.cheesejuice.fancymansion.ui.view.base

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.cheesejuice.fancymansion.ui.viewmodel.BaseViewModel

@Composable
fun baseView(
    viewModel: BaseViewModel, loadingText: String? = null, emptyText: String? = null,
    nav: @Composable() () -> Unit = {}, topBar: @Composable() () -> Unit = {},
    header: @Composable() () -> Unit = {},
    body  : @Composable() () -> Unit = {},
    tail  : @Composable() () -> Unit = {}
) {
    Box {
        if (viewModel.isLoading.value) {
            if (loadingText != null) {
                loadingView(loadingText)
            } else {
                loadingView()
            }
        } else {
            Scaffold(
                drawerGesturesEnabled = !viewModel.isEmpty.value,
                drawerContent = {
                    // nav
                    nav()
                },
                topBar = {
                    TopAppBar {
                        // topBar
                        topBar()
                    }
                }) {

                if (viewModel.isEmpty.value) {
                    if (emptyText != null) {
                        emptyView(emptyText)
                    } else {
                        emptyView()
                    }
                } else {
                    Column() {
                        // header
                        header()
                        // body
                        body()
                        // tail
                        tail()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun basePreview() {
    val viewModel = BaseViewModel(loading = false, empty = false)
    baseView(viewModel, loadingText = "test loading", emptyText = "test empty")
}