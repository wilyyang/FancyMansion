package com.cheesejuice.fancymansion.ui.main.fragment.edit.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.databinding.ItemEditBookBinding
import com.cheesejuice.fancymansion.databinding.ItemLoadingBinding
import com.cheesejuice.fancymansion.util.Formatter

class EditBookAdapter(val datas: MutableList<Config>, val fileRepository: FileRepository, val context: Context):
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
            EditBookViewHolder(ItemEditBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }else{
            LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is EditBookViewHolder){
            val binding=holder.binding
            with(datas[position]){
                binding.tvEditBookId.text = "#${bookId} ${publishCode}"
                binding.tvEditBookUpdate.text = Formatter.longToTimeUntilSecond(updateTime)

                binding.tvEditBookTitle.text = title
                binding.tvEditBookWriter.text = writer
                binding.tvEditBookIllustrator.text = illustrator

                binding.imageCover.clipToOutline = true
                fileRepository.getImageFile(bookId, coverImage, isCover = true)?.also {
                    Glide.with(context).load(it).into(binding.imageCover)
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
    inner class EditBookViewHolder(val binding: ItemEditBookBinding): RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(var binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)
}