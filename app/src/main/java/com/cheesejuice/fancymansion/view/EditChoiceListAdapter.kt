package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemEditChoiceBinding
import com.cheesejuice.fancymansion.model.ChoiceItem
import java.util.*

class EditChoiceListAdapter(var datas: MutableList<ChoiceItem> = mutableListOf()):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), EditChoiceListDragCallback.OnItemMoveListener{

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
            = EditChoiceViewHolder(ItemEditChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = datas.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as EditChoiceViewHolder).binding
        binding.tvChoiceId.text = "id : ${datas[position].id}"
        binding.tvChoiceText.text = "${datas[position].title}"
        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, this.bindingAdapterPosition)
            }
        }
    }

    // Custom
    fun notifyUpdateChoiceItem(choiceId: Long) {
        val idx = datas.indexOfFirst { it.id == choiceId }
        notifyItemChanged(idx)
    }

    fun notifyDeleteChoiceItem(position: Int) {
        notifyItemRemoved(position)
    }

    // ViewHolder
    inner class EditChoiceViewHolder(val binding: ItemEditChoiceBinding): RecyclerView.ViewHolder(binding.root)

    // EditChoiceListDragCallback
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        onceMove = true
        Collections.swap(datas, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }
}

class EditChoiceListDragCallback(
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