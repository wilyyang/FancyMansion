package com.cheesejuice.fancymansion

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.cheesejuice.fancymansion.databinding.ActivityMainBinding
import com.cheesejuice.fancymansion.extension.createEditSampleFiles
import com.cheesejuice.fancymansion.fragment.EditListFragment
import com.cheesejuice.fancymansion.fragment.ReadListFragment
import com.cheesejuice.fancymansion.fragment.StoreFragment
import com.cheesejuice.fancymansion.fragment.UserFragment
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil
    @Inject
    lateinit var firebaseUtil: FirebaseUtil

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(firebaseUtil.checkAuth()){
            fileUtil.initRootFolder()
            if(!bookUtil.isSampleMake()){
                createEditSampleFiles(FirebaseUtil.auth.uid!!)
            }
        }

        binding.bottomMenuMain.apply {
            setOnItemSelectedListener { item ->
                replaceFragment(
                    when (item.itemId) {
                        R.id.menu_store -> {
                            StoreFragment()
                        }
                        R.id.menu_book -> {
                            ReadListFragment()
                        }
                        R.id.menu_make -> {
                            EditListFragment()
                        }
                        R.id.menu_user -> {
                            UserFragment()
                        }
                        else -> {
                            EditListFragment()
                        }
                    }
                )
                true
            }
            selectedItemId = R.id.menu_store
        }
        util.checkRequestPermissions()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.containerMain.id, fragment)
            .commit()
    }
}