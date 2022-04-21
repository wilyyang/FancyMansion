package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemChoiceBinding
import com.cheesejuice.fancymansion.model.ChoiceItem
import com.cheesejuice.fancymansion.model.Config

class EditBookAdapter(val datas: MutableList<Config>):
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
            = EditBookViewHolder(ItemChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as EditBookViewHolder).binding
        binding.tvItemText.text = datas[position].title
        holder.apply {
//            itemView.setOnClickListener {
//                itemClickListener.onClick(it, datas[this.bindingAdapterPosition])
//            }
        }
    }

    // ViewHolder
    inner class EditBookViewHolder(val binding: ItemChoiceBinding): RecyclerView.ViewHolder(binding.root)
}