package com.cheesejuice.fancymansion.ui.reader.start

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.FIRST_SLIDE
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.file.FileRepository
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import com.cheesejuice.fancymansion.databinding.ActivityReadStartBinding
import com.cheesejuice.fancymansion.extension.showDialogAndStart
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.extension.startReadSlideActivity
import com.cheesejuice.fancymansion.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ReadStartActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityReadStartBinding
    private lateinit var config: Config
    var mode: String = ""

    @Inject
    lateinit var util: Util
    @Inject
    lateinit var preferenceProvider: PreferenceProvider
    @Inject
    lateinit var fileRepository: FileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(R.string.loading_text_get_read_book))

        if(preferenceProvider.getEditPlay()) { mode = Const.EDIT_PLAY
        }

        binding.btnStartBook.setOnClickListener(this)
        binding.tvRemoveBook.setOnClickListener(this)

        val bookId = intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND)
        val publishCode = intent.getStringExtra(Const.INTENT_PUBLISH_CODE)?: ""

        CoroutineScope(Default).launch {
            val conf = fileRepository.getConfigFromFile(bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = publishCode)

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
        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive, "")
        binding.tvRemoveBook.visibility = if (mode == Const.EDIT_PLAY) { View.INVISIBLE } else { View.VISIBLE }
        with(conf){
            binding.tvConfigTitle.text = title
            binding.tvConfigDescription.text = description

            binding.tvConfigVersion.text = "v ${Util.versionToString(version)}"
            binding.tvConfigTime.text = Util.longToTimeFormatss(updateTime)
            binding.tvConfigUser.text = "$user ($email)"
            binding.tvConfigWriter.text = writer
            binding.tvConfigIllustrator.text = illustrator
            binding.tvConfigPub.text = getString(R.string.book_config_pub)+publishCode

        }
        fileRepository.getImageFile(conf.bookId, conf.coverImage, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = config.publishCode, isCover = true)
            ?.also {
                Glide.with(applicationContext).load(it).into(binding.imageViewShowMain)
            } ?: also {
            Glide.with(applicationContext).load(R.drawable.default_image)
                .into(binding.imageViewShowMain)
        }
        binding.btnStartBook.isEnabled = true
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btnStartBook -> {
                startBookWithSetting(mode, config)
            }
            R.id.tvRemoveBook -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive, getString(
                    R.string.loading_text_delete_read_book
                ))
                CoroutineScope(Dispatchers.IO).launch {
                    fileRepository.deleteBookFolder(config.bookId, isReadOnly = (mode != Const.EDIT_PLAY), publishCode = config.publishCode)
                    withContext(Main) {
                        finish()
                    }
                }
            }
        }
    }

    private fun startBookWithSetting(mod: String, con: Config){
        val saveSlide = preferenceProvider.getSaveSlideId(con.bookId, FirebaseRepository.auth.uid!!, config.publishCode)

        showDialogAndStart(isShow = (mod != Const.EDIT_PLAY && saveSlide != ID_NOT_FOUND),
            title = getString(R.string.record_dialog_title), message = getString(R.string.record_dialog_question),
            onlyOk = { startReadSlideActivity(con.bookId, config.publishCode, saveSlide) },  // Start Save Point
            onlyNo = { preferenceProvider.deleteBookPref(con.bookId, FirebaseRepository.auth.uid!!, config.publishCode, ""); startReadSlideActivity(con.bookId, config.publishCode, FIRST_SLIDE) },
            noShow = {
                if(mod == Const.EDIT_PLAY){
                    preferenceProvider.deleteBookPref(con.bookId, FirebaseRepository.auth.uid!!, config.publishCode,
                        Const.EDIT_PLAY
                    )
                }
                startReadSlideActivity(con.bookId, config.publishCode, FIRST_SLIDE)
            },
            loadingText = getString(R.string.loading_text_get_read_slide)
        )
    }
}