package com.cheesejuice.fancymansion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.databinding.ActivityDisplayBookBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DisplayBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDisplayBookBinding
    private lateinit var config: Config

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var fileUtil: FileUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_book)

        binding = ActivityDisplayBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val publishCode = intent.getStringExtra(Const.INTENT_PUBLISH_CODE)?: ""

        CoroutineScope(Dispatchers.IO).launch {
            var item:Config? = null
            if(publishCode != ""){
                val documents = MainApplication.db.collection("book").whereEqualTo("publishCode", publishCode).get().await().documents
                if(documents.size > 0){
                    item = documents[0].toObject(Config::class.java)
                }
            }
            withContext(Main) {
                showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                item?.also {
                    config = it
                    makeViewReadyScreen(config)
                }?:also {
                    util.getAlertDailog(this@DisplayBookActivity).show()
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

            binding.tvConfigPublishCode.text = "#$publishCode"
            binding.tvConfigTime.text = CommonUtil.longToTimeFormatss(updateTime)
            binding.tvConfigWriter.text = writer
            binding.tvConfigIllustrator.text = illustrator

            MainApplication.storage.reference.child("/book/$uid/$publishCode/$coverImage").downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    Glide.with(baseContext).load(it.result).into(binding.imageViewShowMain)
                }
            }

            if(!MainApplication.checkAuth() || conf.uid != MainApplication.auth.uid) {
                binding.toolbar.menu.findItem(R.id.menu_remove_store).isVisible = false
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
                    if(MainApplication.checkAuth() && config.uid == MainApplication.auth.uid) {
                        MainApplication.db.collection("book").document(config.publishCode).delete().await()

                        val deleteRef = MainApplication.storage.reference.child("/book/${config.uid}/${config.publishCode}")
                        try {
                            val list = deleteRef.listAll().await()
                            for(ref in list.items){
                                ref.delete().await()
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                    withContext(Main){
                        finish()
                    }
                }
            }

            R.id.menu_download -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(Dispatchers.IO).launch {
                    val bookRef = MainApplication.storage.reference.child("/book/${config.uid}/${config.publishCode}")
                    val list = bookRef.listAll().await()

                    val dir = File(fileUtil.readOnlyUserPath, Const.FILE_PREFIX_READ+config.bookId+"_${config.publishCode}")
                    if(!dir.exists()){
                        dir.mkdir()
                    }

                    for(file in list.items){
                        val subRef = bookRef.child(file.name)

                        val subFile = File(dir, file.name)
                        subRef.getFile(subFile).await()
                    }

                    fileUtil.extractBook(dir)

                    withContext(Main){
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}