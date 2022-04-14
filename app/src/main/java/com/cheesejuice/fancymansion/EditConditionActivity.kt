package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.cheesejuice.fancymansion.databinding.ActivityEditConditionBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.EnterItem
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.util.BookUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditConditionActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditConditionBinding
    private lateinit var logic: Logic
    private lateinit var enterItem: EnterItem
    private var slideId: Long = Const.ID_NOT_FOUND
    private var choiceId: Long = Const.ID_NOT_FOUND
    private var enterId: Long = Const.ID_NOT_FOUND
    private var conditionId: Long = Const.ID_NOT_FOUND
    var isMenuItemEnabled = true
    var isShowCondition = false

    @Inject
    lateinit var bookUtil: BookUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditConditionBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    private fun makeNotHaveCondition() {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE

        isMenuItemEnabled = false
    }

    override fun onClick(view: View?) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_edit_choice, menu)
        return true
    }
}