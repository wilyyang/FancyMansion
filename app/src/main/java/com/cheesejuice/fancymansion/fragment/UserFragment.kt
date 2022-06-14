package com.cheesejuice.fancymansion.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.*
import com.cheesejuice.fancymansion.databinding.FragmentUserBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil
import com.cheesejuice.fancymansion.view.StoreUserBookAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private var userUploadBookList : MutableList<Config> = mutableListOf()

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil
    @Inject
    lateinit var firebaseUtil: FirebaseUtil

    private lateinit var storeBookAdapter: StoreUserBookAdapter

    private val displayBookForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_frag_store_book))
            CoroutineScope(Dispatchers.Default).launch {
                userUploadBookList.clear()
                val addList = firebaseUtil.getUserBookList(FirebaseUtil.auth.uid!!)
                userUploadBookList.addAll(addList)
                withContext(Dispatchers.Main) {
                    _binding?.let {
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
                        binding.tvUserBooks.text = "${userUploadBookList.size}"
                        binding.tvUserGood.text = "${userUploadBookList.map { it.good }.sum()}"
                        storeBookAdapter.notifyDataSetChanged()
                        updateEmptyBook()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        binding.toolbar.title = getString(R.string.frag_main_user)

        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_frag_store_book))
        CoroutineScope(Dispatchers.Default).launch {
            userUploadBookList.clear()
            val addList = firebaseUtil.getUserBookList(FirebaseUtil.auth.uid!!)
            userUploadBookList.addAll(addList)
            withContext(Dispatchers.Main) {
                _binding?.let {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
                    if(firebaseUtil.name != null && firebaseUtil.email != null && firebaseUtil.photoUrl != null) {
                        binding.tvProfileName.text = firebaseUtil.name
                        binding.tvProfileEmail.text = firebaseUtil.email
                        binding.tvUserBooks.text = "${userUploadBookList.size}"
                        binding.tvUserGood.text = "${userUploadBookList.map { it.good }.sum()}"
                        Glide.with(this@UserFragment).load(firebaseUtil.photoUrl).circleCrop().into(binding.imageProfile)

                        storeBookAdapter = StoreUserBookAdapter(userUploadBookList, requireActivity(), firebaseUtil)
                        storeBookAdapter.setItemClickListener(object: StoreUserBookAdapter.OnItemClickListener{
                            override fun onClick(v: View, config: Config) {
                                val intent = Intent(activity, DisplayBookActivity::class.java).apply {
                                    putExtra(Const.INTENT_PUBLISH_CODE, config.publishCode)
                                }
                                displayBookForResult.launch(intent)
                            }
                        })

                        binding.recyclerUserUploadBook.layoutManager = LinearLayoutManager(context)
                        binding.recyclerUserUploadBook.adapter = storeBookAdapter
                        updateEmptyBook()
                    }else{
                        util.getAlertDailog(activity as AppCompatActivity).show()
                    }
                }
            }
        }
        return binding.root
    }

    private fun updateEmptyBook(){
        if(userUploadBookList.size < 1 && _binding != null)
        {
            binding.layoutEmptyBook.visibility = View.VISIBLE
            binding.recyclerUserUploadBook.visibility = View.INVISIBLE
        }else{
            binding.layoutEmptyBook.visibility = View.INVISIBLE
            binding.recyclerUserUploadBook.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_frag_user, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            R.id.menu_logout -> {
                firebaseUtil.signOut(requireActivity())

                val intent = Intent(activity, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when(view?.id) {

        }
    }
}