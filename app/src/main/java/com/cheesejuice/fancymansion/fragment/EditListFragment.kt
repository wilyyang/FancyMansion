package com.cheesejuice.fancymansion.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.databinding.FragmentEditListBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.view.EditBookAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class EditListFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentEditListBinding? = null
    private val binding get() = _binding!!

    private val editList : MutableList<Config> = mutableListOf()

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
    private lateinit var editBookAdapter: EditBookAdapter

    private val editStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

            isListLoading = true
            CoroutineScope(Dispatchers.Default).launch {
                editList.clear()
                page = 1

                val list = fileUtil.getConfigListRange(0, page * Const.PAGE_COUNT -1, isLatest = isLatest)
                withContext(Dispatchers.Main) {
                    if(list != null) {
                        editList.addAll(list)
                        _binding?.let {
                            makeEditList(editList)
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
        _binding = FragmentEditListBinding.inflate(inflater, container, false)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        binding.fabMakeBook.setOnClickListener(this)

        binding.toolbar.title = getString(R.string.frag_main_make)

        isListLoading = true
        CoroutineScope(Dispatchers.Default).launch {
            val list = fileUtil.getConfigListRange(0, page * Const.PAGE_COUNT -1, isLatest = isLatest)
            withContext(Dispatchers.Main) {
                if(list != null) {
                    editList.addAll(list)
                    _binding?.let {
                        makeEditList(editList)
                        isListLoading = false
                    }
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

    private fun makeEditList(_editList : MutableList<Config>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

        editBookAdapter = EditBookAdapter(_editList, fileUtil, requireActivity())
        editBookAdapter.setItemClickListener(object: EditBookAdapter.OnItemClickListener{
            override fun onClick(v: View, config: Config) {
                val intent = Intent(activity, EditStartActivity::class.java).apply {
                    putExtra(Const.INTENT_BOOK_CREATE, false)
                    putExtra(Const.INTENT_BOOK_ID, config.bookId)
                }
                editStartForResult.launch(intent)
            }

        })

        binding.recyclerEditBook.layoutManager = LinearLayoutManager(context)
        binding.recyclerEditBook.adapter = editBookAdapter

        binding.recyclerEditBook.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if(!isListLoading){
                    if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == editList.size - 1){
                        addMoreEditBook()
                    }
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.fabMakeBook -> {
                val intent = Intent(activity, EditStartActivity::class.java).apply {
                    putExtra(Const.INTENT_BOOK_CREATE, true)
                    putExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
                }
                editStartForResult.launch(intent)
            }
        }
    }

    private fun addMoreEditBook(){
        isListLoading = true

        editList.add(Config(bookId = Const.VIEW_HOLDER_LOADING))
        editBookAdapter.notifyItemInserted(editList.size -1)

        CoroutineScope(Dispatchers.Default).launch {
            delay(500L)
            val list = fileUtil.getConfigListRange(page * Const.PAGE_COUNT, ++page * Const.PAGE_COUNT -1, isLatest = isLatest)
            withContext(Dispatchers.Main) {
                if(list != null) {
                    val beforeSize = editList.size
                    editList.removeAt(beforeSize - 1)
                    editBookAdapter.notifyItemRemoved(beforeSize - 1)
                    editList.addAll(list)
                    editBookAdapter.notifyItemRangeInserted(beforeSize, list.size)
                }else{
                    util.getAlertDailog(activity as AppCompatActivity).show()
                }
                isListLoading = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_frag_edit, menu)

        val sortItem = menu.findItem(R.id.action_sort)
        spinnerOrder = (sortItem)?.actionView as AppCompatSpinner
        spinnerOrder.apply {
            layoutParams = ActionBar.LayoutParams(400, ActionBar.LayoutParams.WRAP_CONTENT)
            val sortOrders = resources.getStringArray(R.array.sort_edit)
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOrders)

            onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if(!isListLoading){
                        isListLoading = true
                        isLatest = (spinnerOrder.selectedItemPosition == 0)
                        CoroutineScope(Dispatchers.Default).launch {
                            editList.clear()
                            page = 1

                            val list = fileUtil.getConfigListRange(0, page * Const.PAGE_COUNT -1, isLatest = isLatest)
                            withContext(Dispatchers.Main) {
                                if(list != null) {
                                    editList.addAll(list)
                                    _binding?.let {
                                        makeEditList(editList)
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