package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemBriefBinding
import com.cheesejuice.fancymansion.model.SlideLogic
import java.util.*

class BriefAdapter(var data: MutableList<SlideLogic>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), BriefDragCallback.OnItemMoveListener{

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
            = BriefViewHolder(ItemBriefBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as BriefViewHolder).binding
        binding.tvItemId.text = data[position].slideId.toString()
        binding.tvItemText.text = data[position].slideTitle
        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, this.bindingAdapterPosition)
            }
        }
    }

    // Custom
    fun notifyUpdateBrief(id: Long, title: String) {
        val idx = data.indexOfFirst { slideBrief -> slideBrief.slideId == id }
        notifyItemChanged(idx)
    }

    fun notifyDeleteBrief(position: Int) {
        notifyItemRemoved(position)
    }

    // ViewHolder
    inner class BriefViewHolder(val binding: ItemBriefBinding): RecyclerView.ViewHolder(binding.root)

    // BriefDragCallback
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        onceMove = true
        Collections.swap(data, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }
}

class BriefDragCallback(
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