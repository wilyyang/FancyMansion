package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cheesejuice.fancymansion.databinding.ActivityGuideBinding
import com.cheesejuice.fancymansion.model.Guide
import com.cheesejuice.fancymansion.view.GuideItemAdapter
import com.google.android.material.tabs.TabLayoutMediator

class GuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuideBinding
    private lateinit var guideList:MutableList<Guide>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        guideList = mutableListOf()
        guideList.apply {
            add(Guide(R.drawable.guide_cover_basic, "guide"))
            add(Guide(R.drawable.guide_cover_image, "guide"))

            add(Guide(R.drawable.guide_slide_basic, "guide"))
            add(Guide(R.drawable.guide_slide_image, "guide"))
            add(Guide(R.drawable.guide_slide_choice, "guide"))
            add(Guide(R.drawable.guide_slide_nav, "guide"))

            add(Guide(R.drawable.guide_choice_basic, "guide"))
            add(Guide(R.drawable.guide_choice_route, "guide"))
            add(Guide(R.drawable.guide_choice_multi, "guide"))
            add(Guide(R.drawable.guide_choice_show, "guide"))

            add(Guide(R.drawable.guide_enter_basic, "guide"))
            add(Guide(R.drawable.guide_enter_condition, "guide"))

            add(Guide(R.drawable.guide_condition_basic, "guide"))
            add(Guide(R.drawable.guide_condition_id, "guide"))
            add(Guide(R.drawable.guide_condition_op, "guide"))
            add(Guide(R.drawable.guide_condition_next, "guide"))
        }

        binding.viewPagerGuide.adapter = GuideItemAdapter(guideList)
        TabLayoutMediator(binding.dotIndicator, binding.viewPagerGuide){
            _, _ ->

        }.attach()
    }
}