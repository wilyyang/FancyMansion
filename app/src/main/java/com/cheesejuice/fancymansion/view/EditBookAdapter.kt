package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemEditBookBinding
import com.cheesejuice.fancymansion.model.Config

class EditBookAdapter(val datas: MutableList<Config>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    // OnItemClickListener
    private lateinit var itemClickListener : OnItemClickListener

    interface OnItemClickListener {
        fun onClick(v: View, config: Config)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // Override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = EditBookViewHolder(ItemEditBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as EditBookViewHolder).binding
        binding.tvEditBookId.text = "${datas[position].bookId}"
        binding.tvEditBookTitle.text = "${datas[position].title}"
        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, datas[this.bindingAdapterPosition])
            }
        }
    }

    // ViewHolder
    inner class EditBookViewHolder(val binding: ItemEditBookBinding): RecyclerView.ViewHolder(binding.root)
}