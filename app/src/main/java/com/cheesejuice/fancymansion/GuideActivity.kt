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
            add(Guide(R.drawable.guide_cover_basic, "1번째"))
            add(Guide(R.drawable.guide_cover_image, "2번째"))
            add(Guide(R.drawable.guide_slide_basic, "4번째"))
            add(Guide(R.drawable.guide_slide_image, "2번째"))
            add(Guide(R.drawable.guide_slide_choice, "2번째"))
            add(Guide(R.drawable.guide_slide_nav, "2번째"))

            add(Guide(R.drawable.guide_choice_basic, "4번째"))
            add(Guide(R.drawable.guide_choice_route, "2번째"))
            add(Guide(R.drawable.guide_choice_multi, "2번째"))
            add(Guide(R.drawable.guide_choice_show, "2번째"))
        }

        binding.viewPagerGuide.adapter = GuideItemAdapter(guideList)
        TabLayoutMediator(binding.dotIndicator, binding.viewPagerGuide){
            _, _ ->

        }.attach()
    }
}