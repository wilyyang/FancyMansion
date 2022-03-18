package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemChoiceBinding
import com.cheesejuice.fancymansion.model.ChoiceItem

class ChoiceAdapter(val datas: MutableList<ChoiceItem>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // OnItemClickListener
    private lateinit var itemClickListener : OnItemClickListener

    interface OnItemClickListener {
        fun onClick(v: View, choiceItem: ChoiceItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // Override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = ChoiceViewHolder(ItemChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as ChoiceViewHolder).binding
        binding.tvItemText.text = datas[position].title
        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, datas[this.bindingAdapterPosition])
            }
        }
    }

    // ViewHolder
    inner class ChoiceViewHolder(val binding: ItemChoiceBinding): RecyclerView.ViewHolder(binding.root)
}