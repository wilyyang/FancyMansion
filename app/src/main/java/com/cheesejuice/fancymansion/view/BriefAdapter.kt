package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemBriefBinding
import com.cheesejuice.fancymansion.databinding.ItemChoiceBinding
import com.cheesejuice.fancymansion.model.ChoiceItem
import com.cheesejuice.fancymansion.model.SlideBrief

interface OnBriefItemClickListener{
    fun onItemClick(brief: SlideBrief)
}

class BriefAdapter(val datas: MutableList<SlideBrief>, val adapterListener: OnBriefItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = BriefViewHolder(ItemBriefBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as BriefViewHolder).binding
        binding.tvItemText.text= datas[position].slideTitle
        holder.brief = datas[position]
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