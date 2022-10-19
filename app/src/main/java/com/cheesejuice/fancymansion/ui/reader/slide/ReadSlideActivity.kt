package com.cheesejuice.fancymansion.ui.reader.slide

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.databinding.ActivityReadSlideBinding
import com.cheesejuice.fancymansion.data.models.ChoiceItem
import com.cheesejuice.fancymansion.data.models.Slide
import com.cheesejuice.fancymansion.ui.ChoiceAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReadSlideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadSlideBinding
    private val viewModel: ReadSlideViewModel by viewModels()
    private lateinit var adapter: ChoiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadSlideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.loading.observe(this) { isLoading ->
            binding.layoutLoading.root.visibility = if(isLoading) View.VISIBLE else View.GONE
            binding.recyclerChoice.isEnabled = !isLoading

            if(!isLoading){
                viewModel.slide?.also{
                    makeSlideScreen(it)
                } ?:also{
                    makeNotHaveSlide()
                }
            }
        }

        viewModel.initLogicSlide(
            bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, Const.ID_NOT_FOUND),
            slideId = intent.getLongExtra(Const.INTENT_SLIDE_ID, Const.ID_NOT_FOUND),
            publishCode = intent.getStringExtra(Const.INTENT_PUBLISH_CODE).orEmpty())
    }

    private fun makeSlideScreen(slide: Slide) {
        with(slide){
            // Make Main Content
            viewModel.coverImage?.also {
                binding.layoutShow.visibility = View.VISIBLE
                Glide.with(baseContext).load(it).into(binding.imageViewShowMain)
            } ?: also {
                binding.layoutShow.visibility = View.GONE
            }

            binding.tvSlideTitle.text = slideTitle
            binding.tvSlideDescription.text = description
            binding.tvSlideQuestion.text = question

            if(viewModel.slideLogic!!.type == Const.SLIDE_TYPE_END){
                binding.tvEndingType.visibility = View.VISIBLE
            }else{
                binding.tvEndingType.visibility = View.INVISIBLE
            }

            binding.recyclerChoice.layoutManager = LinearLayoutManager(baseContext)
            adapter = ChoiceAdapter(viewModel.passChoiceItems)
            adapter.setItemClickListener(object : ChoiceAdapter.OnItemClickListener {
                override fun onClick(v: View, choiceItem: ChoiceItem) {
                    adapter.setDisabled(disabled = true)

                    viewModel.moveToNextSlide(choiceItem)
                }
            })
            binding.recyclerChoice.adapter = adapter
        }
    }

    private fun makeNotHaveSlide() {
        binding.layoutEmpty.root.visibility = View.VISIBLE
        binding.layoutContain.visibility = View.GONE
    }
}