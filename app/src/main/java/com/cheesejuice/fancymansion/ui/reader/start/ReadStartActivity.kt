package com.cheesejuice.fancymansion.ui.reader.start

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.FIRST_SLIDE
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.repositories.PreferenceProvider
import com.cheesejuice.fancymansion.data.repositories.networking.FirebaseRepository
import com.cheesejuice.fancymansion.databinding.ActivityReadStartBinding
import com.cheesejuice.fancymansion.ui.reader.slide.ReadSlideActivity
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
class ReadStartActivity : AppCompatActivity(){
    private lateinit var binding: ActivityReadStartBinding

    private val viewModel: ReadStartViewModel by viewModels()

    @Inject
    lateinit var util: Util
    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.loading.observe(this) { isLoading ->
            binding.layoutLoading.root.findViewById<TextView>(R.id.tvLoading).text = viewModel.loadingText
            binding.layoutLoading.root.visibility = if(isLoading) View.VISIBLE else View.GONE
            binding.layoutActive.visibility = if(isLoading) View.GONE else View.VISIBLE

            viewModel.config?.also{
                makeViewReadyScreen(viewModel.config!!)
            }?:also{
                util.getAlertDailog(this@ReadStartActivity).show()
            }
        }
        viewModel.setLoading(true, getString(R.string.loading_text_get_read_book))

        viewModel.initConfig(intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND),
            intent.getStringExtra(Const.INTENT_PUBLISH_CODE)?: "")


        binding.btnStartBook.setOnClickListener {
            startBookWithSetting(viewModel.mode, viewModel.config!!)
        }
        binding.tvRemoveBook.setOnClickListener {
            viewModel.setLoading(true, getString(R.string.loading_text_delete_read_book))
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.deleteBookFolder()
                withContext(Main) {
                    finish()
                }
            }
        }
    }

    private fun makeViewReadyScreen(conf: Config) {
        binding.tvRemoveBook.visibility = if (viewModel.mode == Const.EDIT_PLAY) { View.INVISIBLE } else { View.VISIBLE }
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

        viewModel.coverImage?.also {
            Glide.with(applicationContext).load(it).into(binding.imageViewShowMain)
        } ?: also {
            Glide.with(applicationContext).load(R.drawable.default_image)
                .into(binding.imageViewShowMain)
        }
        binding.btnStartBook.isEnabled = true
    }

    private fun startBookWithSetting(mod: String, con: Config){
        val saveSlide = preferenceProvider.getSaveSlideId(con.bookId, FirebaseRepository.auth.uid!!, viewModel.config!!.publishCode)
        if((mod != Const.EDIT_PLAY && saveSlide != ID_NOT_FOUND)){
            AlertDialog.Builder(this).also { builder ->
                builder.setTitle(getString(R.string.record_dialog_title))
                builder.setMessage(getString(R.string.record_dialog_question))
                builder.setPositiveButton(this.getString(R.string.dialog_ok)) { _, _ ->
                    viewModel.setLoading(true, getString(R.string.loading_text_get_read_slide))
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) {
                            startReadSlideActivity(con.bookId, viewModel.config!!.publishCode, saveSlide)
                            viewModel.setLoading(false)
                        }
                    }
                }
                builder.setNegativeButton(this.getString(R.string.dialog_no)) { _, _ ->
                    preferenceProvider.deleteBookPref(con.bookId, FirebaseRepository.auth.uid!!, viewModel.config!!.publishCode, "");
                    startReadSlideActivity(con.bookId, viewModel.config!!.publishCode, FIRST_SLIDE)
                }
            }.show()
        }else{
            if(mod == Const.EDIT_PLAY){
                preferenceProvider.deleteBookPref(con.bookId, FirebaseRepository.auth.uid!!, viewModel.config!!.publishCode,
                    Const.EDIT_PLAY
                )
            }
            startReadSlideActivity(con.bookId, viewModel.config!!.publishCode, FIRST_SLIDE)
        }
    }

    fun startReadSlideActivity(bookId:Long, publishCode:String, slideId: Long){
        val intent = Intent(this, ReadSlideActivity::class.java).apply {
            putExtra(Const.INTENT_BOOK_ID, bookId)
            putExtra(Const.INTENT_PUBLISH_CODE, publishCode)
            putExtra(Const.INTENT_SLIDE_ID, slideId)
        }
        startActivity(intent)
    }
}