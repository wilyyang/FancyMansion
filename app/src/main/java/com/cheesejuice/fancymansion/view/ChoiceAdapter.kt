package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemChoiceBinding
import com.cheesejuice.fancymansion.model.ChoiceItem

interface OnChoiceItemClickListener{
    fun onItemClick(choiceId: Long)
}

class ChoiceAdapter(val datas: MutableList<ChoiceItem>, val adapterListener: OnChoiceItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = ChoiceViewHolder(ItemChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as ChoiceViewHolder).binding
        binding.tvItemText.text= datas[position].title
        holder.choiceId = datas.get(position).id
    }

    inner class ChoiceViewHolder(val binding: ItemChoiceBinding): RecyclerView.ViewHolder(binding.root){
        var choiceId : Long = 0
        init {
            binding.root.setOnClickListener {
                adapterListener.onItemClick(choiceId)
            }
        }
    }
}