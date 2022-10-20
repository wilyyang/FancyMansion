package com.cheesejuice.fancymansion.ui.main.fragment.store.components

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ItemLoadingBinding
import com.cheesejuice.fancymansion.databinding.ItemStoreBookBinding
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.ui.main.fragment.read.components.ReadBookAdapter
import com.cheesejuice.fancymansion.util.Util
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import com.cheesejuice.fancymansion.util.Formatter

class StoreBookAdapter(val datas: MutableList<Config>, val context: Context, val firebaseRepository: FirebaseRepository):
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
                binding.tvStoreBookId.text = "#${bookId} ${publishCode}"
                binding.tvStoreBookUpdate.text = Formatter.longToTimeUntilSecond(updateTime)

                binding.tvStoreBookUser.text = user
                binding.tvStoreBookEmail.text = email

                binding.tvStoreBookDownloads.text = "$downloads"
                binding.tvStoreBookGood.text = "$good"

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
                    firebaseRepository.returnImageToCallback("/book/${datas[position].uid}/${datas[position].publishCode}/${datas[position].coverImage}",
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