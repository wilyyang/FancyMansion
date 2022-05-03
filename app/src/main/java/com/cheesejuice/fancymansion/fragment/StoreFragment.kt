package com.cheesejuice.fancymansion.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.MainApplication
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.ReadStartActivity
import com.cheesejuice.fancymansion.databinding.FragmentStoreBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.view.ReadBookAdapter
import com.cheesejuice.fancymansion.view.StoreBookAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

                }
            })

            binding.recyclerStoreBook.layoutManager = LinearLayoutManager(itContext)
            binding.recyclerStoreBook.adapter = storeBookAdapter
        }

        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
        MainApplication.db.collection("book").get()
            .addOnSuccessListener { result ->
                for (document in result.documents){
                    val item = document.toObject(Config::class.java)
                    if (item != null) {
                        storeBookList.add(item)
                    }
                }
                _binding?.let {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                }

            }
            .addOnFailureListener {
                _binding?.let {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                }
            }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}