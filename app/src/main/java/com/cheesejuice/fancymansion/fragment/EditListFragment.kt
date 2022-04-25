package com.cheesejuice.fancymansion.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cheesejuice.fancymansion.*
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.databinding.FragmentEditListBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.EnterItem
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.view.EditBookAdapter
import com.cheesejuice.fancymansion.view.EditConditionListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class EditListFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentEditListBinding? = null
    private val binding get() = _binding!!

    private lateinit var editList : MutableList<Config>

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
            CoroutineScope(Dispatchers.Default).launch {
                val init = loadData()
                withContext(Dispatchers.Main) {
                    if(init) {
                        makeEditList(editList)
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

        CoroutineScope(Dispatchers.Default).launch {
            val init = loadData()
            withContext(Dispatchers.Main) {
                if(init) {
                    makeEditList(editList)
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
        val list = fileUtil.getConfigList()
        return if(list != null){
            editList = list
            editList.sortBy { it.title }
            true
        }else{
            false
        }
    }

    private fun makeEditList(editList : MutableList<Config>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

        editBookAdapter = EditBookAdapter(editList, fileUtil, requireActivity())
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
}