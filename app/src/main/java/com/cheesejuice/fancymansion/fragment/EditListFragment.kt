package com.cheesejuice.fancymansion.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.FragmentEditListBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditListFragment : Fragment() {
    private var _binding: FragmentEditListBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentEditListBinding.inflate(inflater, container, false)

        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        binding.toolbar.title = "Edit List Fragment"

        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit_config, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            R.id.menu_save -> {
                Toast.makeText(context, "save clicked", Toast.LENGTH_SHORT).show()
            }

            R.id.menu_play -> {
                Toast.makeText(context, "play clicked", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}