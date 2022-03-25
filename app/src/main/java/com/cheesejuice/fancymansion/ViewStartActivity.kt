package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.databinding.ActivityViewStartBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.*
import com.cheesejuice.fancymansion.Const.Companion.FIRST_SLIDE
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.extension.showDialogAndStart
import com.cheesejuice.fancymansion.extension.startViewStartActivity
import com.cheesejuice.fancymansion.extension.startViewerActivity
import com.cheesejuice.fancymansion.view.RoundEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

@AndroidEntryPoint
class ViewStartActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityViewStartBinding
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
        binding = ActivityViewStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if(bookUtil.getOnlyPlay()) { mode = Const.MODE_PLAY}

        binding.btnStartBook.setOnClickListener(this)

        val bookId = 12345L//intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
        CoroutineScope(Default).launch {
            val conf = fileUtil.getConfigFromFile(bookId)

            withContext(Main) {
                conf?.also{
                    config = it
                    makeViewReadyScreen(config)
                }?:also{
                    util.getAlertDailog(this@ViewStartActivity).show()
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
            binding.tvConfigTime.text = util.longToTimeFormatss(updateTime)
            binding.tvConfigWriter.text = writer
            binding.tvConfigIllustrator.text = illustrator

        }
        Glide.with(applicationContext).load(fileUtil.getImageFile(conf.bookId, conf.coverImage)).into(binding.imageViewShowMain)
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
        val saveSlide = bookUtil.getSaveSlideId(con.bookId)

        showDialogAndStart(isShow = (mod != Const.MODE_PLAY && saveSlide != ID_NOT_FOUND),
            title = getString(R.string.record_dialog_title), message = getString(R.string.record_dialog_question),
            onlyOk = { startViewerActivity(con.bookId, saveSlide) },  // Start Save Point
            onlyNo = { bookUtil.deleteBookPref(con.bookId, ""); startViewerActivity(con.bookId, FIRST_SLIDE) },
            noShow = { bookUtil.deleteBookPref(con.bookId, Const.MODE_PLAY); startViewerActivity(con.bookId, FIRST_SLIDE)}
        )
    }
}