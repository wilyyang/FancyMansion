package com.cheesejuice.fancymansion.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.CondNext
import com.cheesejuice.fancymansion.CondOp
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemEditConditionBinding
import com.cheesejuice.fancymansion.data.models.Condition
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import java.util.*

class EditConditionListAdapter(val preferenceProvider: PreferenceProvider, var datas: MutableList<Condition> = mutableListOf(), val context: Context):
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
            tvConditionId.text  = "id : ${datas[position].id}"

            tvCondition1Id.text = "#${datas[position].conditionId1}"
            tvOperator.text = context.resources.getStringArray(R.array.cond_operator)[CondOp.values().indexOfFirst { it.opName == datas[position].conditionOp }]

            if(datas[position].conditionId2 != Const.ID_NOT_FOUND){
                tvCondition2Id.text = "#${datas[position].conditionId2}"
                tvCondition2label.text = context.getString(R.string.cond_text_entries_to)
            }else{
                tvCondition2Id.text = "${datas[position].conditionCount}"
                tvCondition2label.text = context.getString(R.string.cond_text_count)
            }
            tvNext.text = context.resources.getStringArray(R.array.cond_next)[CondNext.values().indexOfFirst { it.relName == datas[position].conditionNext }]
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