package com.cheesejuice.fancymansion.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.model.SlideLogic
import com.cheesejuice.fancymansion.model.Logic
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
    private val bookPath = File(path, Const.FILE_DIR_BOOK)
    private val readOnlyPath = File(path, Const.FILE_DIR_READONLY)

    fun initRootFolder():Boolean{
        try{
            if(!bookPath.exists()){
                bookPath.mkdirs()
            }
            if(!readOnlyPath.exists()){
                readOnlyPath.mkdirs()
            }
            return true
        }catch (e: Exception){
            return false
        }
    }

    fun getNewEditBookId():Long{
        val pref = context.getSharedPreferences(Const.PREF_SETTING, Context.MODE_PRIVATE)
        val count = pref.getLong(Const.PREF_BOOK_COUNT, 0L)
        var id = count + 1L

        while(id != count){
            if(id == Long.MAX_VALUE ){
                id = 0L
            }

            //file id equal to candidate id
            val sameId = bookPath.listFiles { _, name -> name.startsWith(Const.FILE_PREFIX_BOOK) }
                ?.map { it.name.substring(Const.FILE_PREFIX_BOOK.length).toLongOrNull() }
                ?.firstOrNull { it == id }

            //id count increment if present
            sameId?.also {
                ++id
            } ?: also {
                pref.edit().apply {
                    putLong(Const.PREF_BOOK_COUNT, id)
                    commit()
                }
                return id
            }
        }
        return -1L
    }

    fun getConfigList(isReadOnly:Boolean = false): MutableList<Config>?{
        try {
            return if (isReadOnly) { readOnlyPath } else { bookPath }
                .listFiles { _, name -> name.startsWith(Const.FILE_PREFIX_BOOK) }
                ?.map { File(it.absolutePath, Const.FILE_PREFIX_CONFIG + ".json") }!!
                .filter { it.exists() }
                .map { file ->
                    Json.decodeFromString<Config>(FileInputStream(file).bufferedReader().use { it.readText() })
                }.toMutableList()
        } catch (e: Exception) {
            Log.d(TAG, "" + e.printStackTrace())
        }
        return null
    }

    fun makeEmptyBook(bookId: Long): Boolean{
        val config = Config(bookId = bookId, title = "${context.getString(R.string.book_default_title)} $bookId")
        val slide = Slide(slideId = Const.ID_1_SLIDE, slideTitle = context.getString(R.string.name_slide_prefix)+1, question = context.getString(R.string.text_question_default))
        val logic = Logic(bookId = bookId)
        logic.logics.add(SlideLogic(slide.slideId, slide.slideTitle))

        return (makeBookFolder(bookId) && makeConfigFile(config) && makeLogicFile(logic) && makeSlideFile(bookId, slide))
    }

    // Book Folder
    fun makeBookFolder(bookId: Long): Boolean{
        try{
            val dir = File(bookPath, Const.FILE_PREFIX_BOOK+bookId)
            if(dir.exists()){
                dir.deleteRecursively()
            }
            dir.mkdirs()
            val media = File(dir, Const.FILE_DIR_MEDIA)
            media.mkdirs()
            val slide = File(dir, Const.FILE_DIR_SLIDE)
            slide.mkdirs()
            return true
        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
            return false
        }
    }

    fun deleteBookFolder(bookId: Long, isReadOnly:Boolean = false, publishCode:String = ""): Boolean{
        try{
            val dir = if(isReadOnly){
                File(readOnlyPath, Const.FILE_PREFIX_BOOK+bookId + "_$publishCode")
            }else{
                File(bookPath, Const.FILE_PREFIX_BOOK+bookId)
            }
            if(dir.exists()){
                return dir.deleteRecursively()
            }
        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
            return false
        }
        return true
    }

    // Config File
    fun makeConfigFile(config: Config): Boolean{
        try{
            val file = File(bookPath, Const.FILE_PREFIX_BOOK+config.bookId+ File.separator +Const.FILE_PREFIX_CONFIG+".json")
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

    fun getConfigFromFile(bookId: Long, isReadOnly:Boolean = false, publishCode:String = ""): Config?{
        var config: Config? = null
        try{
            val file = if(isReadOnly){
                File(readOnlyPath, Const.FILE_PREFIX_BOOK+bookId + "_$publishCode" +File.separator+ Const.FILE_PREFIX_CONFIG+".json")
            }else{
                File(bookPath, Const.FILE_PREFIX_BOOK+bookId +File.separator+ Const.FILE_PREFIX_CONFIG+".json")
            }
            if(file.exists()){
                val configJson = FileInputStream(file).bufferedReader().use { it.readText() }
                config = Json.decodeFromString<Config>(configJson)
            }

        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
        }
        return config
    }

    // Logic File
    fun makeLogicFile(logic: Logic): Boolean{
        try{
            val file = File(bookPath, Const.FILE_PREFIX_BOOK+logic.bookId +File.separator+ Const.FILE_PREFIX_LOGIC+".json")
            if(file.exists()){
                file.delete()
            }
            FileOutputStream(file).use {
                it.write(Json.encodeToString(logic).toByteArray())
            }
        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
            return false
        }
        return true
    }

    fun getLogicFromFile(bookId: Long, isReadOnly:Boolean = false, publishCode:String = ""): Logic?{
        var logic: Logic? = null
        try{
            val file = if(isReadOnly){
                File(readOnlyPath, Const.FILE_PREFIX_BOOK+bookId + "_$publishCode" +File.separator+ Const.FILE_PREFIX_LOGIC+".json")
            }else{
                File(bookPath, Const.FILE_PREFIX_BOOK+bookId +File.separator+ Const.FILE_PREFIX_LOGIC+".json")
            }

            if(file.exists()){
                val logicJson = FileInputStream(file).bufferedReader().use { it.readText() }
                logic = Json.decodeFromString<Logic>(logicJson)
            }

        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
        }
        return logic
    }

    // Slide File
    fun makeSlideFile(bookId: Long, slide: Slide): Boolean{
        try{
            val file = File(bookPath, Const.FILE_PREFIX_BOOK+bookId +File.separator + Const.FILE_DIR_SLIDE + File.separator+Const.FILE_PREFIX_SLIDE+slide.slideId+".json")
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

    fun getSlideFromFile(bookId: Long, slideId: Long, isReadOnly:Boolean = false, publishCode:String = ""): Slide?{
        var slide: Slide? = null
        try{
            val file = if(isReadOnly){
                File(readOnlyPath, Const.FILE_PREFIX_BOOK +bookId + "_$publishCode" + File.separator+ Const.FILE_DIR_SLIDE+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
            }else{
                File(bookPath, Const.FILE_PREFIX_BOOK +bookId+File.separator+ Const.FILE_DIR_SLIDE+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
            }
            if(file.exists()){
                val slideJson = FileInputStream(file).bufferedReader().use { it.readText() }
                slide = Json.decodeFromString<Slide>(slideJson)
            }

        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
        }
        return slide
    }

    fun deleteSlideFile(bookId: Long, slideId: Long, isReadOnly:Boolean = false, publishCode:String = ""): Boolean{
        try{
            val file = if(isReadOnly){
                File(readOnlyPath, Const.FILE_PREFIX_BOOK+bookId + "_$publishCode" + File.separator+ Const.FILE_DIR_SLIDE+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
            }else{
                File(bookPath, Const.FILE_PREFIX_BOOK+bookId +File.separator+ Const.FILE_DIR_SLIDE+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
            }
            if(file.exists()){
                file.delete()
            }
        }catch (e: Exception){
            Log.d(Const.TAG, ""+e.printStackTrace())
            return false
        }
        return true
    }

    // Image File
    fun makeImageFile(drawable: Drawable?, bookId: Long, imageName: String): String{
        Log.d(Const.TAG, "makeImageFile >> $bookId $imageName $drawable")
        if(drawable == null || imageName == ""){
            return ""
        }

        try{
            val file = File(bookPath, Const.FILE_PREFIX_BOOK+bookId+ File.separator + Const.FILE_DIR_MEDIA + File.separator+ imageName)
            val ext = imageName.split(".").last()

            val output = FileOutputStream(file)
            if(drawable is GifDrawable){
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
                    drawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(File(bookPath, Const.FILE_PREFIX_BOOK+bookId+ File.separator+ Const.FILE_DIR_MEDIA + File.separator+ "$name.jpg")))
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

    fun getImageFile(bookId: Long, imageName: String, isReadOnly:Boolean = false, publishCode:String = ""): File?{
        Log.e(TAG, "getImageFile $bookId >> $imageName")

        val file = if(isReadOnly){
            File(readOnlyPath, Const.FILE_PREFIX_BOOK+ bookId + "_$publishCode" + File.separator + Const.FILE_DIR_MEDIA + File.separator+ imageName)
        }else{
            File(bookPath, Const.FILE_PREFIX_BOOK+ bookId + File.separator + Const.FILE_DIR_MEDIA + File.separator+ imageName)
        }
        return if(imageName != "" && file.exists()){
            file
        }else{
            null
        }
    }
}