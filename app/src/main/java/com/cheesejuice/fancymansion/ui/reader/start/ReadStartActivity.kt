package com.cheesejuice.fancymansion.ui.reader.start

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.FIRST_SLIDE
import com.cheesejuice.fancymansion.Const.Companion.ID_NOT_FOUND
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.databinding.ActivityReadStartBinding
import com.cheesejuice.fancymansion.extension.getAlertDialog
import com.cheesejuice.fancymansion.ui.reader.slide.ReadSlideActivity
import com.cheesejuice.fancymansion.util.Util
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReadStartActivity : AppCompatActivity(){
    private lateinit var binding: ActivityReadStartBinding

    private val viewModel: ReadStartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.loading.observe(this) { isLoading ->
            binding.layoutLoading.root.findViewById<TextView>(R.id.tvLoading).text = viewModel.loadingText
            binding.layoutLoading.root.visibility = if(isLoading) View.VISIBLE else View.GONE
            binding.layoutActive.visibility = if(isLoading) View.GONE else View.VISIBLE
        }

        viewModel.init.observe(this) { isInit ->
            if(isInit){
                viewModel.config?.also{
                    makeViewReadyScreen(it)
                }?:also{
                    getAlertDialog().show()
                }
            }
        }

        viewModel.delete.observe(this) { isDelete ->
            if(isDelete){ finish() }
        }

        viewModel.initConfig(intent.getLongExtra(Const.INTENT_BOOK_ID, ID_NOT_FOUND),intent.getStringExtra(Const.INTENT_PUBLISH_CODE)?: "")

        binding.btnStartBook.setOnClickListener {
            startBookWithSetting(viewModel.mode, viewModel.config!!)
        }
        binding.tvRemoveBook.setOnClickListener {
            viewModel.deleteBookFolder()
        }
    }

    @SuppressLint("SetTextI18n")
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

        Glide.with(applicationContext).load(viewModel.coverImage?:R.drawable.default_image
        ).into(binding.imageViewShowMain)
        binding.btnStartBook.isEnabled = true
    }

    private fun startBookWithSetting(mod: String, con: Config){
        if((mod != Const.EDIT_PLAY && viewModel.saveSlideId != ID_NOT_FOUND)){
            getAlertDialog(getString(R.string.record_dialog_title),
                getString(R.string.record_dialog_question),
                {startReadSlideActivity(con.bookId, viewModel.config!!.publishCode, viewModel.saveSlideId)},
            ) {
                viewModel.deleteBookPref()
                startReadSlideActivity(con.bookId, viewModel.config!!.publishCode, FIRST_SLIDE)
            }.show()
            AlertDialog.Builder(this).also { builder ->
                builder.setTitle(getString(R.string.record_dialog_title))
                builder.setMessage(getString(R.string.record_dialog_question))
                builder.setPositiveButton(this.getString(R.string.dialog_ok)) { _, _ ->
                    startReadSlideActivity(con.bookId, viewModel.config!!.publishCode, viewModel.saveSlideId)
                }
                builder.setNegativeButton(this.getString(R.string.dialog_no)) { _, _ ->
                    viewModel.deleteBookPref()
                    startReadSlideActivity(con.bookId, viewModel.config!!.publishCode, FIRST_SLIDE)
                }
            }.show()
        }else{
            if(mod == Const.EDIT_PLAY){
                viewModel.deleteBookPref()
            }
            startReadSlideActivity(con.bookId, viewModel.config!!.publishCode, FIRST_SLIDE)
        }
    }

    private fun startReadSlideActivity(bookId:Long, publishCode:String, slideId: Long){
        val intent = Intent(this, ReadSlideActivity::class.java).apply {
            putExtra(Const.INTENT_BOOK_ID, bookId)
            putExtra(Const.INTENT_PUBLISH_CODE, publishCode)
            putExtra(Const.INTENT_SLIDE_ID, slideId)
        }
        startActivity(intent)
    }
}