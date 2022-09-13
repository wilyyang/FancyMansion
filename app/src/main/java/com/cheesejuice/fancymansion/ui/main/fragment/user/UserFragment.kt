package com.cheesejuice.fancymansion.ui.main.fragment.user

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.ui.auth.AuthActivity
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.ui.display.DisplayBookActivity
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.FragmentUserBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.util.Util
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import com.cheesejuice.fancymansion.ui.main.fragment.user.components.StoreUserBookAdapter
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
    lateinit var util: Util
    @Inject
    lateinit var preferenceProvider: PreferenceProvider
    @Inject
    lateinit var fileRepository: FileRepository
    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    private var isInit = false
    private lateinit var storeBookAdapter: StoreUserBookAdapter

    private val displayBookForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_frag_store_book))
            CoroutineScope(Dispatchers.Default).launch {
                userUploadBookList.clear()
                val addList = firebaseRepository.getUserBookList(FirebaseRepository.auth.uid!!)
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
            val addList = firebaseRepository.getUserBookList(FirebaseRepository.auth.uid!!)
            userUploadBookList.addAll(addList)
            withContext(Dispatchers.Main) {
                _binding?.let {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
                    if(firebaseRepository.name != null && firebaseRepository.email != null && firebaseRepository.photoUrl != null) {
                        binding.tvProfileName.text = firebaseRepository.name
                        binding.tvProfileEmail.text = firebaseRepository.email
                        binding.tvUserBooks.text = "${userUploadBookList.size}"
                        binding.tvUserGood.text = "${userUploadBookList.map { it.good }.sum()}"
                        Glide.with(this@UserFragment).load(firebaseRepository.photoUrl).circleCrop().into(binding.imageProfile)

                        storeBookAdapter = StoreUserBookAdapter(userUploadBookList, requireActivity(), firebaseRepository)
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
                isInit = true
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
        if(!isInit) return true
        when(item.itemId) {
            R.id.menu_logout -> {
                firebaseRepository.signOut(requireActivity())

                val intent = Intent(activity, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(Const.INTENT_LOGOUT, true)
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