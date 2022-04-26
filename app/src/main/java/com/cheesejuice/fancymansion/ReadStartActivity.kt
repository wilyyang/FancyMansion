package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityReadStartBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.Const.Companion.FIRST_SLIDE
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.extension.showDialogAndStart
import com.cheesejuice.fancymansion.extension.startReadSlideActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

@AndroidEntryPoint
class ReadStartActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityReadStartBinding
    private lateinit var config: Config
    var mode: String = ""

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if(bookUtil.getEditPlay()) { mode = Const.EDIT_PLAY}

        binding.btnStartBook.setOnClickListener(this)

        val bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
        val publishCode = intent.getStringExtra(Const.INTENT_PUBLISH_CODE)?: ""

        CoroutineScope(Default).launch {
            val conf = fileUtil.getConfigFromFile(bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = publishCode)

            withContext(Main) {
                conf?.also{
                    config = it
                    makeViewReadyScreen(config)
                }?:also{
                    util.getAlertDailog(this@ReadStartActivity).show()
                }
            }
        }
    }

    private fun makeViewReadyScreen(conf: Config) {
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
        with(conf){
            binding.toolbar.title = title
            binding.tvConfigTitle.text = title
            binding.tvConfigDescription.text = description

            binding.tvConfigId.text = "#$bookId"
            binding.tvConfigTime.text = CommonUtil.longToTimeFormatss(updateTime)
            binding.tvConfigWriter.text = writer
            binding.tvConfigIllustrator.text = illustrator

        }
        Glide.with(applicationContext)
            .load(fileUtil.getImageFile(conf.bookId, conf.coverImage, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = config.publishCode, isCover = true))
            .into(binding.imageViewShowMain)
        binding.btnStartBook.isEnabled = true
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btnStartBook -> {
                startBookWithSetting(mode, config)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startBookWithSetting(mod: String, con: Config){
        val saveSlide = bookUtil.getSaveSlideId(con.bookId, config.publishCode)

        showDialogAndStart(isShow = (mod != Const.EDIT_PLAY && saveSlide != ID_NOT_FOUND),
            title = getString(R.string.record_dialog_title), message = getString(R.string.record_dialog_question),
            onlyOk = { startReadSlideActivity(con.bookId, config.publishCode, saveSlide) },  // Start Save Point
            onlyNo = { bookUtil.deleteBookPref(con.bookId, config.publishCode, ""); startReadSlideActivity(con.bookId, config.publishCode, FIRST_SLIDE) },
            noShow = {
                if(mod == Const.EDIT_PLAY){
                    bookUtil.deleteBookPref(con.bookId, config.publishCode, Const.EDIT_PLAY)
                }
                startReadSlideActivity(con.bookId, config.publishCode, FIRST_SLIDE)
            }
        )
    }
}