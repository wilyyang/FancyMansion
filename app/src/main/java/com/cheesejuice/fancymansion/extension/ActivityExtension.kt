package com.cheesejuice.fancymansion.extension

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.etc.Sample
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Logic
import com.cheesejuice.fancymansion.model.Slide
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

fun Activity.createSampleFiles(){
    val fileUtil = FileUtil(this)
    val config = Json.decodeFromString<Config>(Sample.getConfigSample(12345))
    val logic = Json.decodeFromString<Logic>(Sample.getLogicSample(12345))

    fileUtil.makeBookFolder(config.bookId)
    fileUtil.makeConfigFile(config)

    for(i in 1 .. 9){
        val slide = Json.decodeFromString<Slide>(Sample.getLogicSample(i * 1_00_00_00_00L))
        fileUtil.makeSlideFile(config.bookId, slide!!)
    }

//    for(i in 10 .. 99){
//        val slide = Json.decodeFromString<Slide>(Sample.getLogicSample(i * 1_00_00_00_00L))
//        fileUtil.makeSlideJson(config.bookId, slide!!)
//        logic.logics.add(SlideLogic(slide.slideId, slide.slideTitle))
//    }

    fileUtil.makeLogicFile(logic)

    val array = arrayOf("image_1.gif", "image_2.gif", "image_3.gif", "image_4.gif", "image_5.gif", "image_6.gif", "fish_cat.jpg", "game_end.jpg")
    for (fileName in array){
        val file = File(getExternalFilesDir(null), Const.FILE_PREFIX_BOOK+ config.bookId + File.separator+fileName)
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