package com.cheesejuice.fancymansion.extension

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cheesejuice.fancymansion.*
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.etc.Sample
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.view.RoundEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

// startActivity
fun Activity.startReadSlideActivity(bookId:Long, slideId: Long){
    val intent = Intent(this, ReadSlideActivity::class.java).apply {
        putExtra(Const.INTENT_BOOK_ID, bookId)
        putExtra(Const.INTENT_SLIDE_ID, slideId)
    }
    startActivity(intent)
}

fun Activity.startEditSlideActivity(bookId: Long, slideId:Long = Const.FIRST_SLIDE){
    val intent = Intent(this, EditSlideActivity::class.java).apply {
        putExtra(Const.INTENT_BOOK_ID, bookId)
        putExtra(Const.INTENT_SLIDE_ID, slideId)
    }

    startActivity(intent)
}

fun Activity.registerGallaryResultName(imageView: ImageView? = null, afterResult:(String)->Unit) =
    (this as ComponentActivity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            if(imageView != null) Glide.with(applicationContext).load(result.data!!.data).into(imageView)

            result.data!!.data?.let { returnUri ->
                contentResolver.query(returnUri, null, null, null, null)
            }?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()

                cursor.getString(nameIndex)?.let {
                    afterResult(it)
                }
            }
        }
    }



// UI
fun Activity.showLoadingScreen(isLoading: Boolean, loading: View, main: View){
    if(isLoading){
        val view = this.currentFocus
        if (view != null) {
            view.clearFocus()
            val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        loading.visibility = View.VISIBLE
        main.visibility = View.GONE

    }else{
        loading.visibility = View.GONE
        main.visibility = View.VISIBLE

    }
}

fun Activity.showDialogAndStart(isShow: Boolean, loading: View? = null, main: View? = null, title:String, message:String, onlyOkBackground:()->Unit = {}, onlyOk:()->Unit = {}, onlyNo:()->Unit = {}, noShow:() ->Unit = {}, always:() ->Unit = {}){
    if(isShow){
        this.currentFocus?.let { it.clearFocus() }

        AlertDialog.Builder(this).also { builder ->
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton(this.getString(R.string.dialog_ok)) { _, _ ->
                if (loading != null && main != null) showLoadingScreen(true, loading, main)
                CoroutineScope(Dispatchers.IO).launch {
                    onlyOkBackground()
                    withContext(Dispatchers.Main) {
                        onlyOk()
                        if (loading != null && main != null) showLoadingScreen(false, loading, main)
                        always()
                    }
                }
            }
            builder.setNegativeButton(this.getString(R.string.dialog_no)) { _, _ ->
                onlyNo()
                always()
            }
        }.show()
    }else{
        noShow()
        always()
    }
}

// Create Sample
fun Activity.createSampleFiles(){
    val fileUtil = FileUtil(this)
    val config = Json.decodeFromString<Config>(Sample.getConfigSample(12345))
    val logic = Json.decodeFromString<Logic>(Sample.getLogicSample(12345))

    fileUtil.makeBookFolder(config.bookId)
    fileUtil.makeConfigFile(config)

    for(i in 1 .. 9){
        val slide = Json.decodeFromString<Slide>(Sample.getSlideSample(i * 1_00_00_00_00L))
        fileUtil.makeSlideFile(config.bookId, slide!!)
    }

//    for(i in 10 .. 99){
//        val slide = Json.decodeFromString<Slide>(Sample.getLogicSample(i * 1_00_00_00_00L))
//        fileUtil.makeSlideJson(config.bookId, slide!!)
//        logic.logics.add(SlideLogic(slide.slideId, slide.slideTitle))
//    }

    fileUtil.makeLogicFile(logic)
    val bookPath = File(getExternalFilesDir(null), Const.FILE_DIR_BOOK)
    val array = arrayOf("image_1.gif", "image_2.gif", "image_3.gif", "image_4.gif", "image_5.gif", "image_6.gif", "fish_cat.jpg", "game_end.jpg")
    for (fileName in array){
        val file = File(bookPath, Const.FILE_PREFIX_BOOK+ config.bookId + File.separator+ Const.FILE_DIR_MEDIA + File.separator+ fileName)
        val input: InputStream = resources.openRawResource(Sample.getSampleImageId(fileName))
        val out = FileOutputStream(file)
        val buff = ByteArray(1024)
        var read = 0
        try {
            while (input.read(buff).also { read = it } > 0) {
                out.write(buff, 0, read)
            }
        } finally {
            input.close()
            out.close()
        }
    }
}