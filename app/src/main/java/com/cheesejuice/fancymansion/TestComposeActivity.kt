package com.cheesejuice.fancymansion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.cheesejuice.fancymansion.extension.createSampleFiles
import com.cheesejuice.fancymansion.ui.view.readStartView
import com.cheesejuice.fancymansion.ui.viewmodel.BaseViewModel
import com.cheesejuice.fancymansion.ui.viewmodel.ReadStartViewModel
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TestComposeActivity : ComponentActivity() {
    var mode: String = ""
    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    lateinit var base : BaseViewModel
    lateinit var viewModel : ReadStartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        base =  ViewModelProvider(this).get(BaseViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ReadStartViewModel::class.java)
        setContent {readStartView(base, viewModel)}

        if(bookUtil.getOnlyPlay()) { viewModel.mode.value = Const.MODE_PLAY }

        val bookId = 12345L//intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
        CoroutineScope(Dispatchers.Default).launch {
            createSampleFiles()
            val conf = fileUtil.getConfigFromFile(bookId)
            base.isLoading.value = false
            conf?.also{
                base.isEmpty.value = false
                viewModel.config.value = it
                viewModel.imageFile.value = fileUtil.getImageFile(bookId, it.coverImage)
            }?:also{
                base.isEmpty.value = true
            }
        }
    }
}