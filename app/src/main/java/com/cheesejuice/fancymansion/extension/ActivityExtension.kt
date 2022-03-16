package com.cheesejuice.fancymansion.extension

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.cheesejuice.fancymansion.util.Const
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.etc.Sample
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
    val tempConfig = Sample.extractConfigFromJson(-1)!!
    fileUtil.makeBookFolder(tempConfig)
    fileUtil.makeConfigFile(tempConfig)
    for(i in 1 .. 9){
        val slide = Sample.extractSlideFromJson(-1, i*100000000L)
        fileUtil.makeSlideJson(tempConfig.id, slide!!)
    }

    val array = arrayOf("image_1.gif", "image_2.gif", "image_3.gif", "image_4.gif", "image_5.gif", "image_6.gif", "fish_cat.jpg", "game_end.jpg")
    for (fileName in array){
        val file = File(getExternalFilesDir(null), Const.FILE_PREFIX_BOOK+ tempConfig.id + File.separator+fileName)
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