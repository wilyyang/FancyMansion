package com.cheesejuice.fancymansion.ui.main.fragment.read.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemLoadingBinding
import com.cheesejuice.fancymansion.databinding.ItemReadBookBinding
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.util.Util
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository

class ReadBookAdapter(val datas: MutableList<Config>, val fileRepository: FileRepository, val context: Context):
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
            ReadBookViewHolder(ItemReadBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }else{
            LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ReadBookViewHolder){
            val binding=(holder).binding
            with(datas[position]){
                binding.tvReadBookId.text = "#${bookId} ${publishCode}"
                binding.tvReadBookUpdate.text = Util.longToTimeFormatss(updateTime)

                binding.tvReadBookTitle.text = title
                binding.tvReadBookUser.text = user
                binding.tvReadBookEmail.text = email

                binding.imageCover.clipToOutline = true
                fileRepository.getImageFile(bookId, coverImage, isCover = true, isReadOnly = true, publishCode = publishCode)?.also {
                    Glide.with(context).load(it)
                        .into(binding.imageCover)
                }?:also {
                    Glide.with(context).load(R.drawable.default_image).into(binding.imageCover)
                }
            }

            holder.apply {
                itemView.setOnClickListener {
                    itemClickListener.onClick(it, datas[this.bindingAdapterPosition])
                }
            }
        }else if (holder is LoadingViewHolder){

        }
    }

    override fun getItemCount(): Int{
        return datas.size
    }

    // ViewHolder
    inner class ReadBookViewHolder(val binding: ItemReadBookBinding): RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)
}