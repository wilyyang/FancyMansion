package com.cheesejuice.fancymansion.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemCommentBinding
import com.cheesejuice.fancymansion.databinding.ItemCommentLoadingBinding
import com.cheesejuice.fancymansion.model.Comment
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil

class CommentAdapter(val datas: MutableList<Comment>, val context: Context, val bookUid:String):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
        private const val TYPE_ME = 2
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
            else -> {
                if(FirebaseUtil.auth.uid == datas[position].uid) {
                    TYPE_ME
                }else{
                    TYPE_ITEM
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return if (viewType == TYPE_ITEM || viewType == TYPE_ME){
            CommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }else{
            LoadingViewHolder(ItemCommentLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommentViewHolder){
            val binding=(holder).binding
            binding.imageViewCommentUserIcon.visibility = if(datas[position].uid == bookUid) View.VISIBLE else View.GONE

            with(datas[position]){

                binding.tvCommentUserName.text = userName
                if(editCount == 0){
                    binding.tvCommentDate.text = CommonUtil.longToTimeFormatss(updateTime)
                }else{
                    binding.tvCommentDate.text = CommonUtil.longToTimeFormatss(editTime)+" "+context.getString(R.string.display_comment_is_edit)
                }

                binding.tvComment.text = comment

                Glide.with(context).load(R.drawable.default_image).circleCrop().into(holder.binding.imageProfilePhoto)
                if(photoUrl != ""){
                    val uri = Uri.parse(photoUrl)
                    Glide.with(context).load(uri).circleCrop().into(holder.binding.imageProfilePhoto)
                }
            }

            holder.apply {
                itemView.setOnClickListener {
                    itemClickListener.onClick(it, datas[this.bindingAdapterPosition])
                }
            }

            if(holder.itemViewType == TYPE_ME){
                binding.imageViewCommentEdit.visibility = View.VISIBLE
                binding.imageViewCommentDelete.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int{
        return datas.size
    }

    // ViewHolder
    inner class CommentViewHolder(val binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(var binding: ItemCommentLoadingBinding) : RecyclerView.ViewHolder(binding.root)
}