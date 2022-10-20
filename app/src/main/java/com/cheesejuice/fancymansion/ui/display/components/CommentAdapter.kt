package com.cheesejuice.fancymansion.ui.display.components

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Comment
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import com.cheesejuice.fancymansion.databinding.ItemCommentBinding
import com.cheesejuice.fancymansion.databinding.ItemCommentLoadingBinding
import com.cheesejuice.fancymansion.util.Formatter

class CommentAdapter(val datas: MutableList<Comment>, val context: Context, val bookUid:String):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_LOADING = 1
        const val TYPE_ME = 2
        const val TYPE_REPORT = 3
    }

    // OnItemClickListener
    private lateinit var itemClickListener : OnItemClickListener

    interface OnItemClickListener {
        fun onClick(v: View, comment: Comment, viewType: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // Override
    override fun getItemViewType(position: Int): Int {
        return if(datas[position].id == Const.VIEW_HOLDER_LOADING_COMMENT){
            TYPE_LOADING
        }else if(datas[position].report > Const.REPORT_COMMENT){
            TYPE_REPORT
        }else if(FirebaseRepository.auth.uid == datas[position].uid){
            TYPE_ME
        }else{
            TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        return if (viewType == TYPE_LOADING){
            LoadingViewHolder(ItemCommentLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }else{
            CommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommentViewHolder){
            val binding=(holder).binding
            binding.imageViewCommentUserIcon.visibility = if(datas[position].uid == bookUid) View.VISIBLE else View.GONE

            with(datas[position]){

                binding.tvCommentUserName.text = userName
                if(editCount == 0){
                    binding.tvCommentDate.text = Formatter.longToTimeUntilSecond(updateTime)
                }else{
                    binding.tvCommentDate.text = Formatter.longToTimeUntilSecond(editTime)+" "+context.getString(R.string.display_comment_is_edit)
                }

                binding.tvComment.text = if(holder.itemViewType == TYPE_REPORT){
                    binding.tvComment.setTypeface(null, Typeface.ITALIC)
                    context.getString(R.string.reported_comment)
                } else{
                    comment
                }

                Glide.with(context).load(R.drawable.default_image).circleCrop().into(holder.binding.imageProfilePhoto)
                if(photoUrl != ""){
                    val uri = Uri.parse(photoUrl)
                    Glide.with(context).load(uri).circleCrop().into(holder.binding.imageProfilePhoto)
                }
            }

            holder.apply {
                itemView.setOnClickListener {
                    itemClickListener.onClick(it, datas[this.bindingAdapterPosition], holder.itemViewType)
                }
            }

            if(holder.itemViewType == TYPE_ME){
                binding.imageViewCommentEdit.visibility = View.VISIBLE
                binding.imageViewCommentDelete.visibility = View.VISIBLE
            }else{
                binding.tvCommentReport.visibility = View.VISIBLE
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