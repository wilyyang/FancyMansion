package com.cheesejuice.fancymansion.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cheesejuice.fancymansion.databinding.ItemReportBinding

class ReportItemAdapter(val datas: List<String>, val itemClickListener:OnItemClickListener):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return ReportItemViewHolder(ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ReportItemAdapter.ReportItemViewHolder){
            holder.binding.tvReportText.text = datas[position]
            holder.itemView.setOnClickListener {
                itemClickListener.onClick(holder.bindingAdapterPosition)
            }
        }
    }

    override fun getItemCount(): Int{
        return datas.size
    }

    inner class ReportItemViewHolder(val binding: ItemReportBinding): RecyclerView.ViewHolder(binding.root)
}