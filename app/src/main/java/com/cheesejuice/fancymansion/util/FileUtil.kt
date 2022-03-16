package com.cheesejuice.fancymansion.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.model.Book
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.model.SlideBrief
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

class FileUtil @Inject constructor(@ActivityContext private val context: Context){
    private val path = context.getExternalFilesDir(null)

    fun initBook(bookId: Long): Boolean{
        val config = Config(id = bookId, title = "${context.getString(R.string.book_default_title)} $bookId")
        val slide = Slide(id = Const.FIRST_SLIDE, title = context.getString(R.string.name_slide_prefix)+1, question = context.getString(R.string.text_question_default))
        config.briefs.add(SlideBrief(slide.id, slide.title))

        return (makeBookFolder(config) && makeConfigFile(config) && makeSlideJson(bookId, slide))
    }

    fun makeBookFolder(config: Config): Boolean{
        try{
            val dir = File(path, Const.FILE_PREFIX_BOOK+config.id)
            if(dir.exists()){
                dir.deleteRecursively()
            }
            return dir.mkdirs()
        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
            return false
        }
    }

    fun deleteBookFolder(bookId: Long): Boolean{
        try{
            val dir = File(path, Const.FILE_PREFIX_BOOK+bookId)
            if(dir.exists()){
                return dir.deleteRecursively()
            }
        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
            return false
        }
        return true
    }

    fun makeConfigFile(config: Config): Boolean{
        try{
            val file = File(path, Const.FILE_PREFIX_BOOK+config.id+File.separator+Const.FILE_PREFIX_CONFIG+config.id+".json")
            if(file.exists()){
                file.delete()
            }
            FileOutputStream(file).use {
                it.write(Json.encodeToString(config).toByteArray())
            }
        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
            return false
        }
        return true
    }

    fun getConfigFromFile(bookId: Long): Config?{
        var config: Config? = null
        try{
            val file = File(path, Const.FILE_PREFIX_BOOK+bookId+File.separator+Const.FILE_PREFIX_CONFIG+bookId+".json")
            if(file.exists()){
                val configJson = FileInputStream(file).bufferedReader().use { it.readText() }
                config = Json.decodeFromString<Config>(configJson)
            }

        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
        }
        return config
    }

    fun makeSlideJson(bookId: Long, slide: Slide): Boolean{
        try{
            val file = File(path, Const.FILE_PREFIX_BOOK+bookId+File.separator+Const.FILE_PREFIX_SLIDE+slide.id+".json")
            if(file.exists()){
                file.delete()
            }
            FileOutputStream(file).use {
                it.write(Json.encodeToString(slide).toByteArray())
            }
        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
            return false
        }
        return true
    }

    fun getSlideFromJson(bookId: Long, slideId: Long): Slide?{
        var slide: Slide? = null
        try{
            val file = File(path, Const.FILE_PREFIX_BOOK+bookId+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
            if(file.exists()){
                val slideJson = FileInputStream(file).bufferedReader().use { it.readText() }
                slide = Json.decodeFromString<Slide>(slideJson)
            }

        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
        }
        return slide
    }

    fun saveImageFile(drawable: Drawable?, bookId: Long, imageName: String): String{
        if(drawable == null || imageName == ""){
            return ""
        }

        try{
            val file = File(path, Const.FILE_PREFIX_BOOK+bookId+ File.separator+imageName)
            val ext = imageName.split(".").last()

            val output = FileOutputStream(file)
            if(drawable is GifDrawable && ext == "gif"){
                val byteBuffer = drawable.buffer
                val bytes = ByteArray(byteBuffer.capacity())
                (byteBuffer.duplicate().clear() as ByteBuffer).get(bytes)
                output.write(bytes, 0 ,bytes.size)
            }else if(drawable is BitmapDrawable){
                if(ext == "jpg" || ext == "jpeg"){
                    drawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                }else if(ext == "png"){
                    drawable.bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                }else{
                    val name = imageName.split(".").first()
                    drawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(File(path, Const.FILE_PREFIX_BOOK+bookId+ File.separator+"$name.jpg")))
                    return "$name.jpg"
                }
            }
            output.close()
        }catch (e: Exception){
            e.printStackTrace()
            return ""
        }
        return imageName
    }

    fun getImageFile(bookId: Long, imageName: String): Any{
        Log.e(Const.TAG, "get $bookId >> $imageName")

        val file = File(path, Const.FILE_PREFIX_BOOK+ bookId + File.separator+imageName)
        if(imageName != "" && file.exists()){
            return file
        }else{
            return R.drawable.add_image
        }
    }
}