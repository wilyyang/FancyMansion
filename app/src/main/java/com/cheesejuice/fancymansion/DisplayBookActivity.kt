package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
class DisplayBookActivity : AppCompatActivity(), View.OnClickListener  {
    private lateinit var binding: ActivityDisplayBookBinding
    private lateinit var config: Config

    private var isClickGood:Boolean = false

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
            var downloads = 0
            var good = 0
            if(publishCode != ""){
                val colRef = MainApplication.db.collection(Const.FB_DB_KEY_BOOK)
                val documents = colRef.whereEqualTo(Const.FB_DB_KEY_PUBLISH, publishCode).get().await().documents
                if(documents.size > 0){
                    item = documents[0].toObject(Config::class.java)?.also {
                        with(colRef.document(it.publishCode)){
                            downloads = collection(Const.FB_DB_KEY_DOWNLOADS).get().await().size()
                            good = collection(Const.FB_DB_KEY_GOOD).get().await().size()

                            isClickGood = (collection(Const.FB_DB_KEY_GOOD).whereEqualTo(Const.FB_DB_KEY_UID, MainApplication.auth.uid).get().await().size() > 0)
                        }
                    }
                }
            }
            withContext(Main) {
                showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                item?.also {
                    config = it
                    makeViewReadyScreen(config, downloads, good, isClickGood)
                }?:also {
                    util.getAlertDailog(this@DisplayBookActivity).show()
                }
            }
        }
    }

    private fun makeViewReadyScreen(conf: Config, downloads:Int, good:Int, isGood:Boolean) {
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

            MainApplication.storage.reference.child("/${Const.FB_STORAGE_BOOK}/$uid/$publishCode/$coverImage").downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    Glide.with(baseContext).load(it.result).into(binding.imageViewShowMain)
                }
            }

            if(!MainApplication.checkAuth() || conf.uid != MainApplication.auth.uid) {
                binding.toolbar.menu.findItem(R.id.menu_remove_store).isVisible = false
            }
        }

        if(isGood){
            Glide.with(baseContext).load(android.R.drawable.btn_star_big_on).into(binding.imageViewGood)
        }else{
            Glide.with(baseContext).load(android.R.drawable.btn_star_big_off).into(binding.imageViewGood)
        }

        binding.imageViewGood.setOnClickListener(this)
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
                        MainApplication.db.collection(Const.FB_DB_KEY_BOOK).document(config.publishCode).delete().await()

                        val deleteRef = MainApplication.storage.reference.child("/${Const.FB_STORAGE_BOOK}/${config.uid}/${config.publishCode}")
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
                        setResult(Const.RESULT_DELETE)
                        finish()
                    }
                }
            }

            R.id.menu_download -> {
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutActive)
                CoroutineScope(Dispatchers.IO).launch {
                    val bookRef = MainApplication.storage.reference.child("/${Const.FB_STORAGE_BOOK}/${config.uid}/${config.publishCode}")
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

                    var downloads: Int
                    with(MainApplication.db.collection(Const.FB_DB_KEY_BOOK).document(config.publishCode).collection(Const.FB_DB_KEY_DOWNLOADS)){
                        if(whereEqualTo(Const.FB_DB_KEY_UID, MainApplication.auth.uid).get().await().size() < 1){
                            add( hashMapOf(Const.FB_DB_KEY_UID to MainApplication.auth.uid) ).await()
                        }
                        downloads = get().await().size()
                    }

                    fileUtil.extractBook(dir)

                    withContext(Main){
                        binding.tvConfigDownloads.text = "${getString(R.string.display_downloads)} : $downloads"
                        showLoadingScreen(false, binding.layoutLoading.root, binding.layoutActive)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.imageViewGood -> {
                CoroutineScope(Dispatchers.IO).launch {
                    var good = 0
                    with(MainApplication.db.collection(Const.FB_DB_KEY_BOOK).document(config.publishCode).collection(Const.FB_DB_KEY_GOOD)){
                        whereEqualTo(Const.FB_DB_KEY_UID, MainApplication.auth.uid).get().await().let {  result ->
                            if(result.size() < 1){
                                add( hashMapOf(Const.FB_DB_KEY_UID to MainApplication.auth.uid) ).await()
                            }else{
                                document(result.documents[0].id).delete().await()
                            }
                        }
                        isClickGood = (whereEqualTo(Const.FB_DB_KEY_UID, MainApplication.auth.uid).get().await().size() > 0)
                        good = get().await().size()


                    }

                    withContext(Main){
                        if(isClickGood){
                            Glide.with(baseContext).load(android.R.drawable.btn_star_big_on).into(binding.imageViewGood)
                        }else{
                            Glide.with(baseContext).load(android.R.drawable.btn_star_big_off).into(binding.imageViewGood)
                        }
                        binding.tvConfigGood.text = "$good"
                    }
                }
            }
        }
    }
}