package com.cheesejuice.fancymansion.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.MainApplication
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemLoadingBinding
import com.cheesejuice.fancymansion.databinding.ItemReadBookBinding
import com.cheesejuice.fancymansion.databinding.ItemStoreBookBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import kotlinx.coroutines.tasks.await

class StoreBookAdapter(val datas: MutableList<Config>, val context: Context):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    // OnItemClickListener
    private lateinit var itemClickListener : OnItemClickListener

    interface OnItemClickListener {
        fun onClick(v: View, config: Config)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // Override
    override fun getItemViewType(position: Int): Int {
        return when (datas[position].bookId) {
            Const.VIEW_HOLDER_LOADING -> TYPE_LOADING
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return if (viewType == TYPE_ITEM){
            StoreBookViewHolder(ItemStoreBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }else{
            LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is StoreBookViewHolder){
            val binding=(holder).binding
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
        }else if (holder is ReadBookAdapter.LoadingViewHolder){

        }
    }

    override fun getItemCount(): Int{
        return datas.size
    }

    // ViewHolder
    inner class StoreBookViewHolder(val binding: ItemStoreBookBinding): RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)
}