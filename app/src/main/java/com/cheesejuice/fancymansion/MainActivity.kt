package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.cheesejuice.fancymansion.databinding.ActivityMainBinding
import com.cheesejuice.fancymansion.fragment.EditListFragment
import com.cheesejuice.fancymansion.fragment.ReadListFragment
import com.cheesejuice.fancymansion.fragment.StoreFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomMenuMain.apply {
            setOnItemSelectedListener { item ->
                replaceFragment(
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            EditListFragment()
                        }
                        R.id.menu_store -> {
                            StoreFragment()
                        }
                        R.id.menu_read -> {
                            ReadListFragment()
                        }
                        else -> {
                            EditListFragment()
                        }
                    }
                )
                true
            }
            selectedItemId = R.id.menu_edit
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.containerMain.id, fragment)
            .commit()
    }
}