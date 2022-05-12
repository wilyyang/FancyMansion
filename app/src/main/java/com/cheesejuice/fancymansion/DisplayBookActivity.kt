package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.databinding.ActivityDisplayBookBinding
import com.cheesejuice.fancymansion.databinding.LayoutEditCommentBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Comment
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil
import com.cheesejuice.fancymansion.view.CommentAdapter
import com.cheesejuice.fancymansion.view.StoreBookAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DisplayBookActivity : AppCompatActivity(), View.OnClickListener  {
    private lateinit var binding: ActivityDisplayBookBinding
    private lateinit var config: Config

    private var isClickGood:Boolean = false

    private var commentList : MutableList<Comment> = mutableListOf()

    private var isListLoading = false

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var fileUtil: FileUtil
    @Inject
    lateinit var firebaseUtil: FirebaseUtil

    // ui
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val publishCode = intent.getStringExtra(Const.INTENT_PUBLISH_CODE)?: ""

        CoroutineScope(Dispatchers.IO).launch {
            var item : Config? = null
            var downloads = 0
            var good = 0

            if(publishCode != ""){
                item = firebaseUtil.getBookConfig(publishCode)?.also {
                    downloads = firebaseUtil.getDownloads(it.publishCode)
                    good = firebaseUtil.getBookGoodCount(it.publishCode)
                    isClickGood = firebaseUtil.isBookGoodUser(it.publishCode)
                    val addList = firebaseUtil.getCommentList(publishCode = it.publishCode, limit = Const.COMMENT_COUNT)
                    commentList.addAll(addList)
                }
            }

            withContext(Main) {
                item?.also {
                    showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                    config = it
                    makeBookDisplayScreen(config, downloads, good, isClickGood)
                    makeCommentList(commentList)
                }?:also {
                    util.getAlertDailog(this@DisplayBookActivity).show()
                }
            }
        }
    }

    private fun makeBookDisplayScreen(conf: Config, downloads:Int, good:Int, isGood:Boolean) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        with(conf){
            binding.toolbar.title = title
            binding.tvConfigTitle.text = title
            binding.tvConfigDescription.text = description

            binding.tvConfigDownloads.text = "${getString(R.string.display_downloads)} : $downloads"
            binding.tvConfigGood.text = "$good"
            binding.tvConfigTime.text = CommonUtil.longToTimeFormatss(updateTime)
            binding.tvConfigWriter.text = writer
            binding.tvConfigIllustrator.text = illustrator

            firebaseUtil.returnImageToCallback("/${Const.FB_STORAGE_BOOK}/$uid/$publishCode/$coverImage", { result ->
                Glide.with(baseContext).load(result).into(binding.imageViewShowMain)
            })

            if(!firebaseUtil.checkAuth() || conf.uid != FirebaseUtil.auth.uid) {
                binding.toolbar.menu.findItem(R.id.menu_remove_store).isVisible = false
            }
        }

        if(isGood){
            Glide.with(baseContext).load(R.drawable.ic_thumbs_up_check).into(binding.imageViewGood)
        }else{
            Glide.with(baseContext).load(R.drawable.ic_thumbs_up).into(binding.imageViewGood)
        }

        binding.imageViewGood.setOnClickListener(this)
        binding.btnAddComment.setOnClickListener(this)
    }

    private fun makeCommentList(_commentList : MutableList<Comment>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

        commentAdapter = CommentAdapter(_commentList, baseContext, config.uid)
        commentAdapter.setItemClickListener(object: CommentAdapter.OnItemClickListener{
            override fun onClick(v: View, comment: Comment) {
                binding.etAddComment.clearFocus()
                if(firebaseUtil.checkAuth()){
                    if(comment.uid == FirebaseUtil.auth.uid){
                        BottomSheetDialog(this@DisplayBookActivity).also {  dialog ->
                            val dialogView = LayoutEditCommentBinding.inflate(layoutInflater).apply {
                                etAddComment.setText(comment.comment)
                                tvCommentEdit.setOnClickListener {
                                    progressbarComment.visibility = View.VISIBLE
                                    layoutCommentUpdate.visibility = View.GONE
                                    comment.comment = etAddComment.text.toString()
                                    CoroutineScope(Dispatchers.IO).launch {
                                        firebaseUtil.editComment(comment)
                                        val addList = firebaseUtil.getCommentList(publishCode = config.publishCode, limit = commentList.size.toLong())
                                        commentList.clear()
                                        commentList.addAll(addList)

                                        withContext(Main){
                                            commentAdapter.notifyDataSetChanged()
                                            binding.layoutBody.fullScroll(View.FOCUS_DOWN)
                                            dialog.dismiss()
                                        }
                                    }
                                }
                                tvCommentDelete.setOnClickListener {
                                    progressbarComment.visibility = View.VISIBLE
                                    layoutCommentUpdate.visibility = View.GONE
                                    CoroutineScope(Dispatchers.IO).launch {
                                        firebaseUtil.deleteComment(comment)
                                        val addList = firebaseUtil.getCommentList(publishCode = config.publishCode, limit = commentList.size.toLong())
                                        commentList.clear()
                                        commentList.addAll(addList)

                                        withContext(Main){
                                            commentAdapter.notifyDataSetChanged()
                                            binding.layoutBody.fullScroll(View.FOCUS_DOWN)
                                            dialog.dismiss()
                                        }
                                    }
                                }
                            }
                            dialog.setContentView(dialogView.root)
                        }.show()
                    }
                }
            }
        })

        binding.recyclerComment.layoutManager = LinearLayoutManager(baseContext)
        binding.recyclerComment.adapter = commentAdapter

        binding.layoutBody.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            if(!isListLoading){
                if(!v.canScrollVertically(1)){
                    addMoreComment()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_display_config, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            R.id.menu_remove_store -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(Dispatchers.IO).launch {
                    if(firebaseUtil.checkAuth() && config.uid == FirebaseUtil.auth.uid) {
                        firebaseUtil.deleteBook(config)
                    }

                    withContext(Main){
                        setResult(Const.RESULT_DELETE)
                        finish()
                    }
                }
            }

            R.id.menu_download -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(Dispatchers.IO).launch {
                    val dir = File(fileUtil.readOnlyUserPath, Const.FILE_PREFIX_READ+config.bookId+"_${config.publishCode}")
                    firebaseUtil.downloadBook(config, dir)

                    firebaseUtil.incrementBookDownloads(config.publishCode)
                    val downloads: Int = firebaseUtil.getDownloads(config.publishCode)
                    val isSuccess = fileUtil.extractBook(dir)

                    withContext(Main){
                        binding.tvConfigDownloads.text = "${getString(R.string.display_downloads)} : $downloads"
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)

                        Toast.makeText(this@DisplayBookActivity,
                            if (isSuccess) { getString(R.string.toast_download_success) }
                            else { getString(R.string.toast_download_fail) },
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.imageViewGood -> clickBookIsGood()
            R.id.btnAddComment -> clickAddComment()
        }
    }

    private fun clickBookIsGood(){
        CoroutineScope(Dispatchers.IO).launch {
            firebaseUtil.setBookGoodUser(config.publishCode, !isClickGood)
            isClickGood = firebaseUtil.isBookGoodUser(config.publishCode)
            val good:Int = firebaseUtil.getBookGoodCount(config.publishCode)
            withContext(Main){
                if(isClickGood){
                    Glide.with(baseContext).load(R.drawable.ic_thumbs_up_check).into(binding.imageViewGood)
                }else{
                    Glide.with(baseContext).load(R.drawable.ic_thumbs_up).into(binding.imageViewGood)
                }
                binding.tvConfigGood.text = "$good"
            }
        }
    }

    private fun clickAddComment(){
        CoroutineScope(Dispatchers.IO).launch {
            if(firebaseUtil.checkAuth()){
                val comment = Comment(id = "", uid = FirebaseUtil.auth.uid!!, email = firebaseUtil.email!!, userName = firebaseUtil.name!!,
                    photoUrl = firebaseUtil.photoUrl.toString(), comment = binding.etAddComment.text.toString(),
                    updateTime = System.currentTimeMillis(), bookPublishCode = config.publishCode)

                comment.id = firebaseUtil.addComment(comment)
                firebaseUtil.editComment(comment)

                commentList.clear()
                val addList = firebaseUtil.getCommentList(publishCode = config.publishCode)
                commentList.addAll(addList)

                withContext(Main) {
                    commentAdapter.notifyDataSetChanged()

                    binding.etAddComment.let {
                        it.text?.clear()
                        it.clearFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(it.windowToken, 0)
                    }
                    binding.layoutBody.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    private fun addMoreComment(){
        isListLoading = true

        val lastComment = commentList.lastOrNull()

        commentList.add(Comment(id = Const.VIEW_HOLDER_LOADING_COMMENT))
        commentAdapter.notifyItemInserted(commentList.size -1)

        CoroutineScope(Dispatchers.Default).launch {
            delay(500L)
            val addList = firebaseUtil.getCommentList(publishCode = config.publishCode, limit = Const.COMMENT_COUNT, startComment = lastComment)

            withContext(Main) {
                val beforeSize = commentList.size
                commentList.removeAt(beforeSize - 1)
                commentAdapter.notifyItemRemoved(beforeSize - 1)
                commentList.addAll(addList)
                commentAdapter.notifyItemRangeInserted(beforeSize, addList.size)

                isListLoading = false
            }
        }
    }
}