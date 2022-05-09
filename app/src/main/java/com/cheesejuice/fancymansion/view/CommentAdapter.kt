package com.cheesejuice.fancymansion.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.MainApplication
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemCommentBinding
import com.cheesejuice.fancymansion.databinding.ItemLoadingBinding
import com.cheesejuice.fancymansion.databinding.ItemStoreBookBinding
import com.cheesejuice.fancymansion.model.Comment
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil

class CommentAdapter(val datas: MutableList<Comment>, val context: Context):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
    }

    // OnItemClickListener
    private lateinit var itemClickListener : OnItemClickListener

    interface OnItemClickListener {
        fun onClick(v: View, comment: Comment)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // Override
    override fun getItemViewType(position: Int): Int {
        return when (datas[position].id) {
            Const.VIEW_HOLDER_LOADING_COMMENT -> TYPE_LOADING
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return if (viewType == TYPE_ITEM){
            CommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }else{
            LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommentViewHolder){
            val binding=(holder).binding
            with(datas[position]){
                binding.tvCommentUserName.text = userName
                binding.tvCommentDate.text = CommonUtil.longToTimeFormatss(updateTime)

                binding.tvComment.text = comment

                Glide.with(context).load(R.drawable.add_image).into(holder.binding.imageProfilePhoto)
                if(photoUrl != ""){
                    val uri = Uri.parse(photoUrl)
                    Glide.with(context).load(uri).into(holder.binding.imageProfilePhoto)
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
    inner class CommentViewHolder(val binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)
}