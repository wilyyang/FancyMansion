package com.cheesejuice.fancymansion.ui.editor.guide

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ActivityGuideBinding
import com.cheesejuice.fancymansion.data.models.Guide
import com.cheesejuice.fancymansion.ui.editor.guide.components.GuideItemAdapter
import com.google.android.material.tabs.TabLayoutMediator

class GuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuideBinding
    private lateinit var guideList:MutableList<Guide>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val guideType = intent.getIntExtra(Const.INTENT_GUIDE,0)
        guideList = mutableListOf()
        guideList.apply {
            when(guideType){
                Const.GUIDE_COVER -> {
                    add(Guide(R.drawable.guide_cover_basic, "guide"))
                    add(Guide(R.drawable.guide_cover_image, "guide"))
                }

                Const.GUIDE_SLIDE -> {
                    add(Guide(R.drawable.guide_slide_basic, "guide"))
                    add(Guide(R.drawable.guide_slide_image, "guide"))
                    add(Guide(R.drawable.guide_slide_choice, "guide"))
                    add(Guide(R.drawable.guide_slide_nav, "guide"))
                }

                Const.GUIDE_CHOICE -> {
                    add(Guide(R.drawable.guide_choice_basic, "guide"))
                    add(Guide(R.drawable.guide_choice_route, "guide"))
                    add(Guide(R.drawable.guide_choice_multi, "guide"))
                    add(Guide(R.drawable.guide_choice_show, "guide"))
                }

                Const.GUIDE_ENTER -> {
                    add(Guide(R.drawable.guide_enter_basic, "guide"))
                    add(Guide(R.drawable.guide_enter_condition, "guide"))
                }

                Const.GUIDE_CONDITION -> {
                    add(Guide(R.drawable.guide_condition_basic, "guide"))
                    add(Guide(R.drawable.guide_condition_id, "guide"))
                    add(Guide(R.drawable.guide_condition_op, "guide"))
                    add(Guide(R.drawable.guide_condition_next, "guide"))
                }
            }
        }

        binding.viewPagerGuide.adapter = GuideItemAdapter(guideList)
        TabLayoutMediator(binding.dotIndicator, binding.viewPagerGuide){
            _, _ ->

        }.attach()
    }
}