package com.cheesejuice.fancymansion.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cheesejuice.fancymansion.*
import com.cheesejuice.fancymansion.databinding.FragmentReadListBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.view.EditBookAdapter
import com.cheesejuice.fancymansion.view.ReadBookAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ReadListFragment : Fragment() {
    private var _binding: FragmentReadListBinding? = null
    private val binding get() = _binding!!

    private lateinit var readList : MutableList<Config>

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    // ui
    private lateinit var readBookAdapter: ReadBookAdapter

    private val readStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
            CoroutineScope(Dispatchers.Default).launch {
                val init = loadData()
                withContext(Dispatchers.Main) {
                    if(init) {
                        makeReadList(readList)
                    }else{
                        util.getAlertDailog(activity as AppCompatActivity).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadListBinding.inflate(inflater, container, false)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        binding.toolbar.title = getString(R.string.frag_main_book)

        CoroutineScope(Dispatchers.Default).launch {
            val init = loadData()
            withContext(Dispatchers.Main) {
                if(init) {
                    makeReadList(readList)
                }else{
                    util.getAlertDailog(activity as AppCompatActivity).show()
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadData(): Boolean{
        val list = fileUtil.getConfigList(isReadOnly = true)
        return if(list != null){
            readList = list
            readList.sortBy { it.title }
            true
        }else{
            false
        }
    }

    private fun makeReadList(readList : MutableList<Config>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

        readBookAdapter = ReadBookAdapter(readList, fileUtil, requireActivity())
        readBookAdapter.setItemClickListener(object: ReadBookAdapter.OnItemClickListener{
            override fun onClick(v: View, config: Config) {
                bookUtil.setEditPlay(false)
                val intent = Intent(activity, ReadStartActivity::class.java).apply {
                    putExtra(Const.INTENT_BOOK_ID, config.bookId)
                    putExtra(Const.INTENT_PUBLISH_CODE, config.publishCode)
                }
                readStartForResult.launch(intent)
            }

        })

        binding.recyclerReadBook.layoutManager = LinearLayoutManager(context)
        binding.recyclerReadBook.adapter = readBookAdapter
    }
}