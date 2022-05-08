package com.cheesejuice.fancymansion.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.*
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.databinding.FragmentStoreBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.view.ReadBookAdapter
import com.cheesejuice.fancymansion.view.StoreBookAdapter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class StoreFragment : Fragment() {
    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private var storeBookList : MutableList<Config> = mutableListOf()

    private var isListLoading = false

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    // ui
    private lateinit var storeBookAdapter: StoreBookAdapter

    private val displayBookForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        binding.toolbar.title = getString(R.string.frag_main_store)

        CoroutineScope(Dispatchers.Default).launch {
            val documents = MainApplication.db.collection("book").orderBy("title").orderBy("publishCode")
                .limit(Const.PAGE_COUNT_LONG).get().await().documents
            for (document in documents){
                val item = document.toObject(Config::class.java)
                if (item != null) {
                    storeBookList.add(item)
                }
            }
            withContext(Main){
                _binding?.let {
                    makeStoreList(storeBookList)
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun makeStoreList(_storeList : MutableList<Config>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

        storeBookAdapter = StoreBookAdapter(_storeList, requireActivity())
        storeBookAdapter.setItemClickListener(object: StoreBookAdapter.OnItemClickListener{
            override fun onClick(v: View, config: Config) {
                val intent = Intent(activity, DisplayBookActivity::class.java).apply {
                    putExtra(Const.INTENT_PUBLISH_CODE, config.publishCode)
                }
                displayBookForResult.launch(intent)
            }
        })

        binding.recyclerStoreBook.layoutManager = LinearLayoutManager(context)
        binding.recyclerStoreBook.adapter = storeBookAdapter

        binding.recyclerStoreBook.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if(!isListLoading){
                    if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == storeBookList.size - 1){
                        addMoreStoreBook()
                    }
                }
            }
        })
    }

    private fun addMoreStoreBook(){
        isListLoading = true

        val lastConfig = storeBookList.lastOrNull()

        storeBookList.add(Config(bookId = Const.VIEW_HOLDER_LOADING))
        storeBookAdapter.notifyItemInserted(storeBookList.size -1)

        CoroutineScope(Dispatchers.Default).launch {
            delay(500L)

            val documents = if(lastConfig == null){
                MainApplication.db.collection("book").orderBy("title").orderBy("publishCode")
                    .limit(Const.PAGE_COUNT_LONG).get().await().documents
            }else{
                MainApplication.db.collection("book").orderBy("title").orderBy("publishCode")
                    .startAfter(lastConfig.title, lastConfig.publishCode).limit(Const.PAGE_COUNT_LONG).get().await().documents
            }

            val addList = mutableListOf<Config>()
            for (document in documents){
                val item = document.toObject(Config::class.java)
                if (item != null) {
                    addList.add(item)
                }
            }

            withContext(Main) {
                val beforeSize = storeBookList.size
                storeBookList.removeAt(beforeSize - 1)
                storeBookAdapter.notifyItemRemoved(beforeSize - 1)
                storeBookList.addAll(addList)
                storeBookAdapter.notifyItemRangeInserted(beforeSize, addList.size)

                isListLoading = false
            }
        }
    }
}