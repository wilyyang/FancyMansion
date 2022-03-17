package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemBriefBinding
import com.cheesejuice.fancymansion.model.SlideBrief
import java.util.*

class BriefAdapter(var datas: MutableList<SlideBrief>, val adapterListener: OnBriefItemListener):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = BriefViewHolder(ItemBriefBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as BriefViewHolder).binding
        binding.tvItemText.text= datas[position].slideTitle
        holder.brief = datas[position]
    }

    override fun getItemCount(): Int{
        return datas.size
    }

    interface OnBriefItemListener{
        fun onItemClick(brief: SlideBrief)
        fun onItemDrag(viewHolder: BriefViewHolder)
        fun onItemMove(fromPosition: Int, toPosition: Int)
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(datas, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun updateBriefTitle(id:Long, title:String){
        datas.find { it.slideId == id }!!.slideTitle = title
        notifyDataSetChanged()
    }

    inner class BriefViewHolder(val binding: ItemBriefBinding): RecyclerView.ViewHolder(binding.root){
        var brief : SlideBrief? = null
        init {
            binding.root.setOnClickListener {
                adapterListener.onItemClick(brief!!)
            }
        }
    }
}

class BriefItemCallback(private val itemMoveListener: BriefAdapter.OnBriefItemListener) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int
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