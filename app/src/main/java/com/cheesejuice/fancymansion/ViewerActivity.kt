package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.cheesejuice.fancymansion.databinding.ActivityViewerBinding

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }
}