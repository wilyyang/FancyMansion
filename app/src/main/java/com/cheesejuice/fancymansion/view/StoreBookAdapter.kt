package com.cheesejuice.fancymansion.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.MainApplication
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemStoreBookBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import kotlinx.coroutines.tasks.await

class StoreBookAdapter(val datas: MutableList<Config>, val context: Context):
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
            = StoreBookViewHolder(ItemStoreBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as StoreBookViewHolder).binding
        with(datas[position]){
            binding.tvStoreBookId.text = "#${bookId}"
            binding.tvStoreBookUpdate.text = CommonUtil.longToTimeFormatss(updateTime)

            binding.tvStoreBookTitle.text = title
            binding.tvStoreBookWriter.text = writer
            binding.tvStoreBookIllustrator.text = illustrator
            Glide.with(context).load(R.drawable.add_image).into(holder.binding.imageCover)
            if(coverImage != ""){
                MainApplication.storage.reference.child("/book/$uid/$publishCode/$coverImage").downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Glide.with(context).load(it.result).into(holder.binding.imageCover)
                    }
                }
            }
        }

        holder.apply {
            itemView.setOnClickListener {
                itemClickListener.onClick(it, datas[this.bindingAdapterPosition])
            }
        }
    }

    // ViewHolder
    inner class StoreBookViewHolder(val binding: ItemStoreBookBinding): RecyclerView.ViewHolder(binding.root)
}