package com.cheesejuice.fancymansion.ui.display

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.databinding.ActivityDisplayBookBinding
import com.cheesejuice.fancymansion.databinding.LayoutEditCommentBinding
import com.cheesejuice.fancymansion.databinding.LayoutReportBinding
import com.cheesejuice.fancymansion.extension.showLoadingPercent
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Comment
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil
import com.cheesejuice.fancymansion.view.CommentAdapter
import com.cheesejuice.fancymansion.view.ReportItemAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DisplayBookActivity : AppCompatActivity(), View.OnClickListener  {
    private lateinit var binding: ActivityDisplayBookBinding
    private lateinit var config: Config

    private var isClickGood:Boolean = false

    private var commentList : MutableList<Comment> = mutableListOf()

    private var isListLoading = false

    private var isCommentOrderRecent = false

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
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_load_store_book))

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val publishCode = intent.getStringExtra(Const.INTENT_PUBLISH_CODE)?: ""

        isListLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            var item : Config? = null
            if(publishCode != ""){
                item = firebaseUtil.getBookConfig(publishCode)?.also {
                    isClickGood = firebaseUtil.isBookGoodUser(it.publishCode)
                    val addList = firebaseUtil.getCommentList(publishCode = it.publishCode, limit = Const.COMMENT_COUNT, isOrderRecent = isCommentOrderRecent)
                    commentList.addAll(addList)
                }
            }

            withContext(Main) {
                item?.also {
                    config = it
                    makeBookDisplayScreen(config, isClickGood)
                    makeCommentList(commentList)
                    isListLoading = false
                    updateEmptyComment()
                }?:also {
                    util.getAlertDailog(this@DisplayBookActivity).show()
                }
            }
        }
    }

    private fun makeBookDisplayScreen(conf: Config, isGood:Boolean) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
        with(conf){
            binding.toolbar.title = title
            if (description != "") {
                binding.tvConfigDescription.text = description
            } else {
                binding.tvConfigDescription.visibility = View.GONE
            }

            binding.tvConfigVersion.text = "v ${CommonUtil.versionToString(version)}"
            binding.tvConfigTime.text = CommonUtil.longToTimeFormatss(updateTime)
            binding.tvConfigDownloads.text = "$downloads"
            binding.tvConfigGood.text = "$good"

            binding.tvConfigUser.text = "$user ($email)"
            binding.tvConfigWriter.text = writer
            binding.tvConfigIllustrator.text = illustrator
            binding.tvConfigPub.text = getString(R.string.book_config_pub)+publishCode

            firebaseUtil.returnImageToCallback("/${Const.FB_STORAGE_BOOK}/$uid/$publishCode/$coverImage", { result ->
                Glide.with(baseContext).load(result).into(binding.imageViewShowMain)
            }, {
                Glide.with(baseContext).load(R.drawable.default_image).into(binding.imageViewShowMain)
            })

            if(!firebaseUtil.checkAuth() || conf.uid != FirebaseUtil.auth.uid) {
                binding.toolbar.menu.findItem(R.id.menu_remove_store).isVisible = false
            }else{
                binding.toolbar.menu.findItem(R.id.menu_report).isVisible = false
            }
        }

        if(isGood){
            Glide.with(baseContext).load(R.drawable.ic_thumbs_up_check).into(binding.imageViewGood)
        }else{
            Glide.with(baseContext).load(R.drawable.ic_thumbs_up).into(binding.imageViewGood)
        }

        binding.layoutGood.setOnClickListener(this)
        binding.btnAddComment.setOnClickListener(this)
        binding.tvOrderRegistration.setOnClickListener(this)
        binding.tvOrderRecent.setOnClickListener(this)
    }

    private fun makeCommentList(_commentList : MutableList<Comment>) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")

        commentAdapter = CommentAdapter(_commentList, baseContext, config.uid).apply {
            setItemClickListener(commentListener)
        }

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

    private val commentListener = object : CommentAdapter.OnItemClickListener {
        override fun onClick(v: View, comment: Comment, viewType: Int) {
            if(viewType == CommentAdapter.TYPE_REPORT) return
            binding.etAddComment.clearFocus()
            BottomSheetDialog(this@DisplayBookActivity).also { dialog ->
                val dialogView =
                    if (firebaseUtil.checkAuth() && comment.uid == FirebaseUtil.auth.uid) {
                        LayoutEditCommentBinding.inflate(layoutInflater).apply {
                            etAddComment.setText(comment.comment)
                            tvCommentEdit.setOnClickListener {
                                comment.apply {
                                    this.comment = etAddComment.text.toString()
                                    editTime = System.currentTimeMillis()
                                    editCount += 1
                                }

                                bottomSheetCommonProcess(dialog, progressbarComment, layoutCommentUpdate) {
                                    firebaseUtil.editComment(comment)
                                }
                            }
                            tvCommentDelete.setOnClickListener {
                                bottomSheetCommonProcess(dialog, progressbarComment, layoutCommentUpdate) {
                                    firebaseUtil.deleteComment(comment)
                                }
                            }
                        }
                    } else {
                        LayoutReportBinding.inflate(layoutInflater).apply {
                            val adapter = ReportItemAdapter(
                                resources.getStringArray(R.array.report_comment).toList(),
                                object : ReportItemAdapter.OnItemClickListener {
                                    override fun onClick(position: Int) {
                                        util.getAlertDailog(
                                            context = this@DisplayBookActivity,
                                            title = getString(R.string.report_title), message = getString(
                                                R.string.report_question
                                            ),
                                            click = { _, _ ->
                                                bottomSheetCommonProcess(dialog, progressbarReport, layoutReport) {
                                                    firebaseUtil.incrementCommentReport(comment, position)
                                                }
                                            }
                                        ).setNegativeButton(getString(R.string.dialog_no)) { _, _ -> dialog.dismiss()}
                                            .setOnCancelListener { dialog.dismiss() }
                                            .show()
                                    }
                                }
                            )
                            recyclerReport.layoutManager = LinearLayoutManager(this@DisplayBookActivity)
                            recyclerReport.adapter = adapter
                        }
                    }
                dialog.setContentView(dialogView.root)
            }.show()
        }
    }

    fun bottomSheetCommonProcess(dialog:BottomSheetDialog, progressbar:View, content:View, logic:suspend()->Unit = {}){
        isListLoading = true
        dialog.setCancelable(false)
        progressbar.visibility = View.VISIBLE
        content.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            /**firebaseUtil.editComment(comment)*/
            /**firebaseUtil.deleteComment(comment)*/
            /**firebaseUtil.incrementCommentReport(comment,position)*/
            logic()
            val addList = firebaseUtil.getCommentList(
                publishCode = config.publishCode,
                limit = commentList.size.toLong(),
                isOrderRecent = isCommentOrderRecent
            )
            commentList.clear()
            commentList.addAll(addList)

            withContext(Main) {
                commentAdapter.notifyDataSetChanged()
                binding.layoutBody.fullScroll(View.FOCUS_DOWN)
                dialog.dismiss()
                isListLoading = false
                updateEmptyComment()
            }
        }
    }

    fun updateEmptyComment(){
        if(commentList.size < 1)
        {
            binding.layoutEmptyComment.visibility = View.VISIBLE
        }else{
            binding.layoutEmptyComment.visibility = View.GONE
        }
    }

    private fun updateCommentOrderText(isRecent:Boolean){
        binding.dotOrderRecent.setImageResource(if (isRecent) {
            R.drawable.ic_dot
        } else {
            R.drawable.ic_dot_gray
        })
        binding.tvOrderRecent.setTextColor(
            getColor(
                if (isRecent) {
                    R.color.black_8
                } else {
                    R.color.black_4
                }
            )
        )
        binding.dotOrderRegistration.setImageResource(if (!isRecent) {
            R.drawable.ic_dot
        } else {
            R.drawable.ic_dot_gray
        })
        binding.tvOrderRegistration.setTextColor(
            getColor(
                if (!isRecent) {
                    R.color.black_8
                } else {
                    R.color.black_4
                }
            )
        )
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
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(
                    R.string.loading_text_remove_store_book
                ))
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
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(
                    R.string.loading_text_download_store_book
                ))
                CoroutineScope(Dispatchers.IO).launch {
                    val dir = File(fileUtil.readOnlyUserPath, Const.FILE_PREFIX_READ +config.bookId+"_${config.publishCode}")

                    val channel : Channel<Pair<String, Int>> = Channel()
                    launch {
                        firebaseUtil.downloadBook(config, dir, channel)
                    }

                    for(data in channel ){
                        withContext(Main){
                            showLoadingPercent(binding.layoutLoading.root, data.first, data.second)
                        }
                    }

                    val downloads: Int = firebaseUtil.incrementBookDownloads(config.publishCode)
                    val isSuccess = fileUtil.extractBook(dir)

                    withContext(Main){
                        binding.tvConfigDownloads.text = "$downloads"
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")

                        Toast.makeText(this@DisplayBookActivity,
                            if (isSuccess) { getString(R.string.toast_download_success) }
                            else { getString(R.string.toast_download_fail) },
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.menu_report -> {
                BottomSheetDialog(this@DisplayBookActivity).also { dialog ->
                    val dialogView =
                        LayoutReportBinding.inflate(layoutInflater).apply {
                            val adapter = ReportItemAdapter(
                                resources.getStringArray(R.array.report_book).toList(),
                                object : ReportItemAdapter.OnItemClickListener {
                                    override fun onClick(position: Int) {
                                        util.getAlertDailog(
                                            context = this@DisplayBookActivity,
                                            title = getString(R.string.report_title), message = getString(
                                                R.string.report_question
                                            ),
                                            click = { _, _ ->
                                                dialog.setCancelable(false)
                                                progressbarReport.visibility = View.VISIBLE
                                                layoutReport.visibility = View.GONE

                                                CoroutineScope(Dispatchers.IO).launch {
                                                    firebaseUtil.incrementBookReport(config, position)
                                                    withContext(Main) {
                                                        dialog.dismiss()
                                                    }
                                                }
                                            }
                                        ).setNegativeButton(getString(R.string.dialog_no)) { _, _ -> dialog.dismiss()}
                                            .setOnCancelListener { dialog.dismiss() }
                                            .show()
                                    }
                                }
                            )
                            recyclerReport.layoutManager = LinearLayoutManager(this@DisplayBookActivity)
                            recyclerReport.adapter = adapter
                        }
                    dialog.setContentView(dialogView.root)
                }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.layoutGood -> clickBookIsGood()
            R.id.btnAddComment -> clickAddComment()

            R.id.tvOrderRegistration -> {
                if(isCommentOrderRecent){
                    CoroutineScope(Dispatchers.IO).launch {
                        isListLoading = true
                        isCommentOrderRecent = false
                        val addList = firebaseUtil.getCommentList(publishCode = config.publishCode, limit = Const.COMMENT_COUNT, isOrderRecent = isCommentOrderRecent)
                        commentList.clear()
                        commentList.addAll(addList)

                        withContext(Main){
                            updateCommentOrderText(isCommentOrderRecent)

                            commentAdapter.notifyDataSetChanged()
                            isListLoading = false
                            updateEmptyComment()
                        }
                    }
                }
            }
            R.id.tvOrderRecent -> {
                if(!isCommentOrderRecent){
                    CoroutineScope(Dispatchers.IO).launch {
                        isListLoading = true
                        isCommentOrderRecent = true
                        val addList = firebaseUtil.getCommentList(publishCode = config.publishCode, limit = Const.COMMENT_COUNT, isOrderRecent = isCommentOrderRecent)
                        commentList.clear()
                        commentList.addAll(addList)

                        withContext(Main){
                            updateCommentOrderText(isCommentOrderRecent)

                            commentAdapter.notifyDataSetChanged()
                            isListLoading = false
                            updateEmptyComment()
                        }
                    }
                }
            }
        }
    }

    private fun clickBookIsGood(){
        CoroutineScope(Dispatchers.IO).launch {
            val good:Int = firebaseUtil.setBookGoodUser(config.publishCode, !isClickGood)
            isClickGood = firebaseUtil.isBookGoodUser(config.publishCode)
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
                isListLoading = true
                firebaseUtil.initUserInfo()
                val current = System.currentTimeMillis()
                if(FirebaseUtil.userInfo!!.addCommentTime+ Const.CONST_TIME_LIMIT_COMMENT > current){
                    val leftTime = (FirebaseUtil.userInfo!!.addCommentTime+ Const.CONST_TIME_LIMIT_COMMENT - current) / 1000
                    withContext(Main){
                        isListLoading = false
                        util.getAlertDailog(
                            context = this@DisplayBookActivity,
                            title = getString(R.string.dialog_time_limit_title),
                            message = String.format(getString(R.string.dialog_time_limit_comment), leftTime),
                            click = { _, _ -> }
                        ).show()
                    }
                }else{
                    val comment = Comment(id = "", uid = FirebaseUtil.auth.uid!!, email = firebaseUtil.email!!, userName = firebaseUtil.name!!,
                        photoUrl = firebaseUtil.photoUrl.toString(), comment = binding.etAddComment.text.toString(),
                        updateTime = System.currentTimeMillis(), bookPublishCode = config.publishCode)

                    comment.id = firebaseUtil.addComment(comment)
                    firebaseUtil.editComment(comment)
                    firebaseUtil.updateCommentInUserInfo()

                    commentList.clear()
                    val addList = firebaseUtil.getCommentList(publishCode = config.publishCode, isOrderRecent = isCommentOrderRecent)
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
                        isListLoading = false
                        updateEmptyComment()
                    }
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
            val addList = firebaseUtil.getCommentList(publishCode = config.publishCode, limit = Const.COMMENT_COUNT, startComment = lastComment, isOrderRecent = isCommentOrderRecent)

            withContext(Main) {
                val beforeSize = commentList.size
                commentList.removeAt(beforeSize - 1)
                commentAdapter.notifyItemRemoved(beforeSize - 1)
                commentList.addAll(addList)
                commentAdapter.notifyItemRangeInserted(beforeSize, addList.size)

                isListLoading = false
                updateEmptyComment()
            }
        }
    }
}