package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemEditEnterBinding
import com.cheesejuice.fancymansion.model.EnterItem
import com.cheesejuice.fancymansion.model.Logic
import java.util.*

class EditEnterListAdapter(var datas: MutableList<EnterItem> = mutableListOf(), var logic: Logic? = null):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), EditEnterListDragCallback.OnItemMoveListener{

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
            = EditEnterViewHolder(ItemEditEnterBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = datas.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as EditEnterViewHolder).binding
        binding.tvEnterId.text = "id : ${datas[position].id}"
        binding.tvEnterSlideId.text = "${datas[position].enterSlideId}"

        binding.tvEnterSlideTitle.text = logic?.logics?.first { it.slideId == datas[position].enterSlideId }?.slideTitle
        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, this.bindingAdapterPosition)
            }
        }
    }

    // Custom
    fun notifyUpdateEnterItem(enterId: Long) {
        val idx = datas.indexOfFirst { it.id == enterId }
        notifyItemChanged(idx)
    }

    fun notifyDeleteEnterItem(position: Int) {
        notifyItemRemoved(position)
    }

    // ViewHolder
    inner class EditEnterViewHolder(val binding: ItemEditEnterBinding): RecyclerView.ViewHolder(binding.root)

    // EditEnterListDragCallback
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        onceMove = true
        Collections.swap(datas, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }
}

class EditEnterListDragCallback(
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