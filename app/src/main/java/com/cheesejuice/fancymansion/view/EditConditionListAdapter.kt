package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.databinding.ItemEditConditionBinding
import com.cheesejuice.fancymansion.model.Condition
import com.cheesejuice.fancymansion.model.Logic
import java.util.*

class EditConditionListAdapter(var datas: MutableList<Condition> = mutableListOf(), var logic: Logic? = null):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), EditConditionListDragCallback.OnItemMoveListener{

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
            = EditConditionViewHolder(ItemEditConditionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = datas.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as EditConditionViewHolder).binding

        with(binding){
            tvConditionId.text  = "condition id : ${datas[position].id}"

            tvCondition1Id.text = "slide : ${datas[position].conditionId1}"
            tvCondition1Title.text = logic?.logics?.first { it.slideId == datas[position].conditionId1 }?.slideTitle

            tvOperator.text = datas[position].conditionOp

            if(datas[position].conditionId2 != Const.ID_NOT_FOUND){
                tvCondition2Id.text = "slide : ${datas[position].conditionId2}"
                tvCondition2Title.text = logic?.logics?.first { it.slideId == datas[position].conditionId2 }?.slideTitle
            }else{
                tvCondition2Title.text = "${datas[position].conditionCount}"
            }

            tvNext.text = datas[position].conditionNext
        }

        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, this.bindingAdapterPosition)
            }
        }
    }

    // Custom
    fun notifyUpdateConditionItem(enterId: Long) {
        val idx = datas.indexOfFirst { it.id == enterId }
        notifyItemChanged(idx)
    }

    fun notifyDeleteConditionItem(position: Int) {
        notifyItemRemoved(position)
    }

    // ViewHolder
    inner class EditConditionViewHolder(val binding: ItemEditConditionBinding): RecyclerView.ViewHolder(binding.root)

    // EditConditionListDragCallback
    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        onceMove = true
        Collections.swap(datas, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }
}

class EditConditionListDragCallback(
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