package com.cheesejuice.fancymansion

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.cheesejuice.fancymansion.databinding.ActivityMainBinding
import com.cheesejuice.fancymansion.extension.createSampleFiles
import com.cheesejuice.fancymansion.fragment.EditListFragment
import com.cheesejuice.fancymansion.fragment.ReadListFragment
import com.cheesejuice.fancymansion.fragment.StoreFragment
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
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

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // [start] temp code
        util.checkRequestPermissions()
        fileUtil.initRootFolder()

        if(!bookUtil.isSampleMake()){
            createSampleFiles()
        }
        // [end] temp code

        binding.bottomMenuMain.apply {
            setOnItemSelectedListener { item ->
                replaceFragment(
                    when (item.itemId) {
                        R.id.menu_make -> {
                            EditListFragment()
                        }
                        R.id.menu_store -> {
                            StoreFragment()
                        }
                        R.id.menu_book -> {
                            ReadListFragment()
                        }
                        else -> {
                            EditListFragment()
                        }
                    }
                )
                true
            }
            selectedItemId = R.id.menu_make
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.containerMain.id, fragment)
            .commit()
    }
}