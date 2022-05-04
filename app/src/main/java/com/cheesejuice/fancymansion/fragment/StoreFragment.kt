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

    private lateinit var storeBookList : MutableList<Config>

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    // ui
    private lateinit var storeBookAdapter: StoreBookAdapter

    private val displayBookForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
            CoroutineScope(Dispatchers.IO).launch {
                storeBookList.clear()
                val documents = MainApplication.db.collection("book").get().await().documents
                for (document in documents){
                    val item = document.toObject(Config::class.java)
                    if (item != null) {
                        storeBookList.add(item)
                    }
                }
                storeBookAdapter.notifyDataSetChanged()
                withContext(Main){
                    _binding?.let {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        binding.toolbar.title = getString(R.string.frag_main_store)

        storeBookList = mutableListOf()
        context?.let { itContext ->
            storeBookAdapter = StoreBookAdapter(storeBookList, itContext)
            storeBookAdapter.setItemClickListener(object: StoreBookAdapter.OnItemClickListener{
                override fun onClick(v: View, config: Config) {
                    val intent = Intent(activity, DisplayBookActivity::class.java).apply {
                        putExtra(Const.INTENT_PUBLISH_CODE, config.publishCode)
                    }
                    displayBookForResult.launch(intent)
                }
            })

            binding.recyclerStoreBook.layoutManager = LinearLayoutManager(itContext)
            binding.recyclerStoreBook.adapter = storeBookAdapter
        }

        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        CoroutineScope(Dispatchers.IO).launch {
            val documents = MainApplication.db.collection("book").get().await().documents
            for (document in documents){
                val item = document.toObject(Config::class.java)
                if (item != null) {
                    storeBookList.add(item)
                }
            }
            withContext(Main){
                _binding?.let {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}