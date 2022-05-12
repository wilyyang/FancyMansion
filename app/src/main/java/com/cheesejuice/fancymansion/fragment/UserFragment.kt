package com.cheesejuice.fancymansion.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.AuthActivity
import com.cheesejuice.fancymansion.MainActivity
import com.cheesejuice.fancymansion.MainApplication
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.FragmentStoreBinding
import com.cheesejuice.fancymansion.databinding.FragmentUserBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil
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

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil
    @Inject
    lateinit var firebaseUtil: FirebaseUtil

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

        if(firebaseUtil.name != null && firebaseUtil.email != null && firebaseUtil.photoUrl != null) {
            binding.tvProfileName.text = firebaseUtil.name
            binding.tvProfileEmail.text = firebaseUtil.email

            Glide.with(this).load(firebaseUtil.photoUrl).circleCrop().into(binding.imageProfile)
        }else{
            util.getAlertDailog(activity as AppCompatActivity).show()
        }

        return binding.root
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