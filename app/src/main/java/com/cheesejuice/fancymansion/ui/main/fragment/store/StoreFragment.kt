package com.cheesejuice.fancymansion.ui.main.fragment.store

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
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.*
import com.cheesejuice.fancymansion.databinding.FragmentStoreBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.ui.display.DisplayBookActivity
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.util.Util
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import com.cheesejuice.fancymansion.ui.main.fragment.store.components.StoreBookAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

@AndroidEntryPoint
class StoreFragment : Fragment() {
    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private var storeBookList : MutableList<Config> = mutableListOf()

    private var isListLoading = false

    private var orderKey = Const.ORDER_LATEST_IDX
    private lateinit var spinnerOrder: AppCompatSpinner
    private lateinit var sortItem:MenuItem

    private var searchKeyword = ""
    private lateinit var searchView : SearchView

    @Inject
    lateinit var util: Util
    @Inject
    lateinit var preferenceProvider: PreferenceProvider
    @Inject
    lateinit var fileRepository: FileRepository
    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    // ui
    private lateinit var storeBookAdapter: StoreBookAdapter

    private val displayBookForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            bookClearLoad()
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

        MobileAds.initialize(requireContext()) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        return binding.root
    }

    private fun updateEmptyBook(){
        if(storeBookList.size < 1 && _binding != null)
        {
            binding.layoutEmptyBook.visibility = View.VISIBLE
            binding.recyclerStoreBook.visibility = View.INVISIBLE
        }else{
            binding.layoutEmptyBook.visibility = View.INVISIBLE
            binding.recyclerStoreBook.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun makeStoreList(_storeList : MutableList<Config>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")

        storeBookAdapter = StoreBookAdapter(_storeList, requireActivity(), firebaseRepository)
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

                if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == storeBookList.size - 1){
                    addMoreStoreBook()
                }
            }
        })

        updateEmptyBook()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_frag_store, menu)

        // Search Item
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                searchKeyword = p0.orEmpty()
                bookClearLoad()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean { return true }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                sortItem.isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchKeyword = ""
                sortItem.isVisible = true
                bookClearLoad()

                return true
            }
        })

        // Sort Item
        sortItem = menu.findItem(R.id.action_sort)
        spinnerOrder = (sortItem)?.actionView as AppCompatSpinner
        spinnerOrder.apply {
            layoutParams = ActionBar.LayoutParams(450, ActionBar.LayoutParams.WRAP_CONTENT)
            val sortOrders = resources.getStringArray(R.array.sort_store)
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOrders)

            onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    orderKey = spinnerOrder.selectedItemPosition
                    bookClearLoad()
                    spinnerOrder.setSelection(orderKey)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun bookClearLoad(){
        if(!isListLoading){
            isListLoading = true
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_frag_store_book))
            CoroutineScope(Dispatchers.Default).launch {
                storeBookList.clear()
                val addList = firebaseRepository.getBookList(limit = Const.PAGE_COUNT_LONG, orderKey = orderKey, searchKeyword = searchKeyword)
                storeBookList.addAll(addList)
                withContext(Main){
                    _binding?.let {
                        makeStoreList(storeBookList)
                        isListLoading = false
                    }
                }
            }
        }
    }

    private fun addMoreStoreBook(){
        if(!isListLoading){
            isListLoading = true

            val lastConfig = storeBookList.lastOrNull()
            storeBookList.add(Config(bookId = Const.VIEW_HOLDER_LOADING))
            storeBookAdapter.notifyItemInserted(storeBookList.size -1)

            CoroutineScope(Dispatchers.Default).launch {
                val addList = firebaseRepository.getBookList(limit = Const.PAGE_COUNT_LONG, startConfig = lastConfig, orderKey = orderKey, searchKeyword = searchKeyword)

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
}