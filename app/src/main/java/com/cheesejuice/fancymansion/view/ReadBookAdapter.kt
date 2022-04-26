package com.cheesejuice.fancymansion.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemReadBookBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil

class ReadBookAdapter(val datas: MutableList<Config>, val fileUtil: FileUtil, val context: Context):
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
            = ReadBookViewHolder(ItemReadBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as ReadBookViewHolder).binding
        with(datas[position]){
            binding.tvEditBookId.text = "#${bookId}"
            binding.tvEditBookUpdate.text = CommonUtil.longToTimeFormatss(updateTime)

            binding.tvEditBookTitle.text = title
            binding.tvEditBookWriter.text = writer
            binding.tvEditBookIllustrator.text = illustrator

            fileUtil.getImageFile(bookId, coverImage, isCover = true, isReadOnly = true, publishCode = publishCode)?.also {
                Glide.with(context).load(it).into(binding.imageCover)
            }?:also {
                Glide.with(context).load(R.drawable.add_image).into(binding.imageCover)
            }
        }

        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, datas[this.bindingAdapterPosition])
            }
        }
    }

    // ViewHolder
    inner class ReadBookViewHolder(val binding: ItemReadBookBinding): RecyclerView.ViewHolder(binding.root)
}