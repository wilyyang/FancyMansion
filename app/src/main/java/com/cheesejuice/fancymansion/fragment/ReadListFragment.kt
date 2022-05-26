package com.cheesejuice.fancymansion.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ReadListFragment : Fragment() {
    private var _binding: FragmentReadListBinding? = null
    private val binding get() = _binding!!

    private var readList : MutableList<Config> = mutableListOf()

    private var page = 1
    private var isListLoading = false

    private var isLatest = true
    private lateinit var spinnerOrder: AppCompatSpinner

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
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_frag_my_book))

            isListLoading = true
            CoroutineScope(Dispatchers.Default).launch {
                readList.clear()
                page = 1

                val list = fileUtil.getConfigListRange(0, page * Const.PAGE_COUNT -1, isReadOnly = true, isLatest = isLatest)
                withContext(Dispatchers.Main) {
                    if(list != null) {
                        readList.addAll(list)
                        _binding?.let {
                            makeReadList(readList)
                            isListLoading = false
                        }
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
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_frag_my_book))

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        binding.toolbar.title = getString(R.string.frag_main_book)

        isListLoading = true
        CoroutineScope(Dispatchers.Default).launch {
            val list = fileUtil.getConfigListRange(0, page * Const.PAGE_COUNT -1, isReadOnly = true, isLatest = isLatest)
            withContext(Dispatchers.Main) {
                if(list != null) {
                    readList.addAll(list)
                    _binding?.let {
                        makeReadList(readList)
                        isListLoading = false
                    }
                }else{
                    util.getAlertDailog(activity as AppCompatActivity).show()
                }
            }
        }
        return binding.root
    }

    private fun updateEmptyBook(){
        if(readList.size < 1 && _binding != null)
        {
            binding.layoutEmptyBook.visibility = View.VISIBLE
            binding.recyclerReadBook.visibility = View.INVISIBLE
        }else{
            binding.layoutEmptyBook.visibility = View.INVISIBLE
            binding.recyclerReadBook.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun makeReadList(_readList : MutableList<Config>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")

        readBookAdapter = ReadBookAdapter(_readList, fileUtil, requireActivity())
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

        binding.recyclerReadBook.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if(!isListLoading){
                    if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == readList.size - 1){
                        addMoreReadBook()
                    }
                }
            }
        })
        updateEmptyBook()
    }

    private fun addMoreReadBook(){
        isListLoading = true

        readList.add(Config(bookId = Const.VIEW_HOLDER_LOADING))
        readBookAdapter.notifyItemInserted(readList.size -1)

        CoroutineScope(Dispatchers.Default).launch {
            delay(500L)
            val list = fileUtil.getConfigListRange(page * Const.PAGE_COUNT, ++page * Const.PAGE_COUNT -1, isReadOnly = true, isLatest = isLatest)
            withContext(Dispatchers.Main) {
                if(list != null) {
                    val beforeSize = readList.size
                    readList.removeAt(beforeSize - 1)
                    readBookAdapter.notifyItemRemoved(beforeSize - 1)
                    readList.addAll(list)
                    readBookAdapter.notifyItemRangeInserted(beforeSize, list.size)
                }else{
                    util.getAlertDailog(activity as AppCompatActivity).show()
                }
                isListLoading = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_frag_read, menu)

        val sortItem = menu.findItem(R.id.action_sort)
        spinnerOrder = (sortItem)?.actionView as AppCompatSpinner
        spinnerOrder.apply {
            layoutParams = ActionBar.LayoutParams(450, ActionBar.LayoutParams.WRAP_CONTENT)
            val sortOrders = resources.getStringArray(R.array.sort_read)
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOrders)

            onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if(!isListLoading){
                        isListLoading = true
                        isLatest = (spinnerOrder.selectedItemPosition == 0)
                        CoroutineScope(Dispatchers.Default).launch {
                            readList.clear()
                            page = 1

                            val list = fileUtil.getConfigListRange(0, page * Const.PAGE_COUNT -1, isReadOnly = true, isLatest = isLatest)
                            withContext(Dispatchers.Main) {
                                if(list != null) {
                                    readList.addAll(list)
                                    _binding?.let {
                                        makeReadList(readList)
                                        isListLoading = false
                                    }
                                }else{
                                    util.getAlertDailog(activity as AppCompatActivity).show()
                                }
                            }
                        }
                    }
                    spinnerOrder.setSelection(if(isLatest){ 0 }else{ 1 })
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}