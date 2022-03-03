package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemChoiceBinding
import com.cheesejuice.fancymansion.model.SlideItem

class ChoiceViewHolder(val binding: ItemChoiceBinding): RecyclerView.ViewHolder(binding.root)

class ChoiceAdapter(val datas: MutableList<SlideItem>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = ChoiceViewHolder(ItemChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as ChoiceViewHolder).binding
        binding.tvItemText.text= datas[position].title
    }
}