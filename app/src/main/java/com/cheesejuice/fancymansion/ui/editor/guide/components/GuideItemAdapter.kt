package com.cheesejuice.fancymansion.ui.editor.guide.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemPageGuideBinding
import com.cheesejuice.fancymansion.data.models.Guide

class GuideItemAdapter(val datas: MutableList<Guide>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return GuideItemViewHolder(ItemPageGuideBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GuideItemViewHolder){
            if(datas[position].image != -1){
                holder.binding.imageViewContent.setImageResource(datas[position].image)
            }

        }
    }

    override fun getItemCount(): Int{
        return datas.size
    }

    inner class GuideItemViewHolder(val binding: ItemPageGuideBinding): RecyclerView.ViewHolder(binding.root)
}