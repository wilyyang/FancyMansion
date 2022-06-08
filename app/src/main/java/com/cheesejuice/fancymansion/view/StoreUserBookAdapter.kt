package com.cheesejuice.fancymansion.view

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemStoreUserBookBinding
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil

class StoreUserBookAdapter(val datas: MutableList<Config>, val context: Context, val firebaseUtil: FirebaseUtil):
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
    {
        return StoreUserBookViewHolder(ItemStoreUserBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is StoreUserBookViewHolder){
            val binding=(holder).binding
            with(datas[position]){
                binding.tvStoreBookUpdate.text = CommonUtil.longToTimeFormatss(updateTime)
                binding.imageCover.clipToOutline = true
            }

            if(datas[position].report > Const.REPORT_BOOK){
                binding.tvStoreBookTitle.text = "${datas[position].title} ${context.getString(R.string.reported_book)}"
                binding.tvStoreBookTitle.setTypeface(null, Typeface.ITALIC)
                Glide.with(context).load(R.drawable.default_image).into(holder.binding.imageCover)
                return
            }else{
                binding.tvStoreBookTitle.text = datas[position].title
                if(datas[position].coverImage != ""){
                    firebaseUtil.returnImageToCallback("/book/${datas[position].uid}/${datas[position].publishCode}/${datas[position].coverImage}",
                        { result -> Glide.with(context).load(result).into(holder.binding.imageCover)},
                        { Glide.with(context).load(R.drawable.default_image).into(holder.binding.imageCover) }
                    )
                }else{
                    Glide.with(context).load(R.drawable.default_image).into(holder.binding.imageCover)
                }

                holder.apply {
                    itemView.setOnClickListener {
                        itemClickListener.onClick(it, datas[this.bindingAdapterPosition])
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int{
        return datas.size
    }

    // ViewHolder
    inner class StoreUserBookViewHolder(val binding: ItemStoreUserBookBinding): RecyclerView.ViewHolder(binding.root)
}