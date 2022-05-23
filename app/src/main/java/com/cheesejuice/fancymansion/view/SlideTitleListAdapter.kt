package com.cheesejuice.fancymansion.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemSlideTitleBinding
import com.cheesejuice.fancymansion.model.SlideLogic
import java.util.*

class SlideTitleListAdapter(var datas: MutableList<SlideLogic> = mutableListOf(), val context: Context):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), SlideTitleListDragCallback.OnItemMoveListener{

    var onceMove = false

    // OnItemClickListener
    private lateinit var itemClickListener : OnItemClickListener

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // Override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = SlideTitleViewHolder(ItemSlideTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = datas.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as SlideTitleViewHolder).binding
        binding.tvItemId.text = datas[position].slideId.toString()
        binding.tvItemText.text = datas[position].slideTitle

        when(datas[position].type){
            Const.SLIDE_TYPE_NORMAL -> {
                binding.tvNavSlideType.apply {
                    visibility = View.INVISIBLE
                }
            }
            Const.SLIDE_TYPE_START -> {
                binding.tvNavSlideType.apply {
                    visibility = View.VISIBLE
                    text = context.getString(R.string.slide_type_start)
                    background = context.getDrawable(R.drawable.bg_type_start)
                }
            }
            Const.SLIDE_TYPE_END -> {
                binding.tvNavSlideType.apply {
                    visibility = View.VISIBLE
                    text = context.getString(R.string.slide_type_ending)
                    background = context.getDrawable(R.drawable.bg_type_ending)
                }
            }
        }
        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, this.bindingAdapterPosition)
            }
        }
    }

    // Custom
    fun notifyUpdateSlideTitle(slideId: Long) {
        val idx = datas.indexOfFirst { it.slideId == slideId }
        notifyItemChanged(idx)
    }

    fun notifyDeleteSlideTitle(position: Int) {
        notifyItemRemoved(position)
    }

    // ViewHolder
    inner class SlideTitleViewHolder(val binding: ItemSlideTitleBinding): RecyclerView.ViewHolder(binding.root)

    // SlideTitleListDragCallback
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        onceMove = true
        if(toPosition == 0){
            datas[toPosition].type = Const.SLIDE_TYPE_NORMAL
            datas[fromPosition].type = Const.SLIDE_TYPE_START
        }else if(fromPosition == 0){
            datas[toPosition].type = Const.SLIDE_TYPE_START
            datas[fromPosition].type = Const.SLIDE_TYPE_NORMAL
        }
        Collections.swap(datas, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        notifyItemChanged(toPosition)
        notifyItemChanged(fromPosition)
    }
}

class SlideTitleListDragCallback(
    private val itemMoveListener: OnItemMoveListener
) : ItemTouchHelper.Callback() {

    interface OnItemMoveListener {
        fun onItemMove(fromPosition: Int, toPosition: Int)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder)
    = makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        itemMoveListener.onItemMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}