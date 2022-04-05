package com.cheesejuice.fancymansion.view.base

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.cheesejuice.fancymansion.viewmodel.BaseViewModel

@Composable
fun BaseView(
    viewModel:BaseViewModel,
    loadingText: String? = null, emptyText: String? = null,
    nav: @Composable() (() -> Unit)? = null, topBar: @Composable() () -> Unit = {},
    header: @Composable() () -> Unit = {},
    body  : @Composable() () -> Unit = {},
    tail  : @Composable() () -> Unit = {}
) {
    Box {
        if (viewModel.isLoading()) {
            if (loadingText != null) {
                LoadingView(loadingText)
            } else {
                LoadingView()
            }
        } else {
            Scaffold(
                drawerGesturesEnabled = !viewModel.isEmpty() && nav != null,
                drawerContent = {
                    // nav
                    if (nav != null) {
                        nav()
                    }
                },
                topBar = {
                    // topBar
                    topBar()
                }
            ) {

                if (viewModel.isEmpty()) {
                    if (emptyText != null) {
                        EmptyView(emptyText)
                    } else {
                        EmptyView()
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
fun BasePreview() {
    val viewModel = BaseViewModel()
    viewModel.setEmpty(true)
    BaseView(viewModel, loadingText = "test loading", emptyText = "test empty")
}