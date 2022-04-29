package com.cheesejuice.fancymansion.fragment

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class StoreFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var storeBookList : MutableList<Config>

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

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

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
    }

    private fun makeStoreBookList(readList : MutableList<Config>) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when(view?.id) {

        }
    }
}