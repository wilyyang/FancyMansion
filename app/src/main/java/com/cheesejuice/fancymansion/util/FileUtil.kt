package com.cheesejuice.fancymansion.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.MainApplication
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.model.Config
import com.cheesejuice.fancymansion.model.Slide
import com.cheesejuice.fancymansion.model.SlideLogic
import com.cheesejuice.fancymansion.model.Logic
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.*
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class FileUtil @Inject constructor(@ActivityContext private val context: Context){
    private val path = context.getExternalFilesDir(null)
    private val bookPath = File(path, Const.FILE_DIR_BOOK)
    private val readOnlyPath = File(path, Const.FILE_DIR_READONLY)
    val bookUserPath: File
        get() {
            return File(bookPath, FirebaseUtil.auth.uid!!)
        }

    val readOnlyUserPath: File
        get() {
            return File(readOnlyPath, FirebaseUtil.auth.uid!!)
        }

    fun initRootFolder():Boolean{
        try{
            if(!bookPath.exists()){
                bookPath.mkdirs()
            }
            if(!readOnlyPath.exists()){
                readOnlyPath.mkdirs()
            }

            if(!bookUserPath.exists()){
                bookUserPath.mkdirs()
            }
            if(!readOnlyUserPath.exists()){
                readOnlyUserPath.mkdirs()
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
            val sameId = bookUserPath.listFiles { _, name -> name.startsWith(Const.FILE_PREFIX_BOOK) }
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
            return if (isReadOnly) { readOnlyUserPath.listFiles { _, name -> name.startsWith(Const.FILE_PREFIX_READ) } }
            else { bookUserPath.listFiles { _, name -> name.startsWith(Const.FILE_PREFIX_BOOK) } }
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

    fun getConfigListRange(start:Int, end:Int, isReadOnly:Boolean = false, isLatest:Boolean = false): MutableList<Config>?{
        try {
            val addList = mutableListOf<Config>()
            val allList = if (isReadOnly) { readOnlyUserPath.listFiles { _, name -> name.startsWith(Const.FILE_PREFIX_READ) } }
            else { bookUserPath.listFiles { _, name -> name.startsWith(Const.FILE_PREFIX_BOOK) } }
                ?.map { File(it.absolutePath, Const.FILE_PREFIX_CONFIG + ".json") }!!
                .filter { it.exists() }.sortedBy { it.lastModified() }.let {
                    if(isLatest){
                        it.reversed()
                    }else{
                        it
                    }
                }

            if(allList.size > start){
                val limit = if(allList.size > end) end else (allList.size-1)
                for(i in start .. limit){
                    addList.add(Json.decodeFromString(FileInputStream(allList[i]).bufferedReader().use { it.readText() }))
                }
            }
            return addList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun makeEmptyBook(bookId: Long): Boolean{
        val config = Config(bookId = bookId, title = "${context.getString(R.string.book_default_title)} $bookId")
        val slide = Slide(slideId = Const.ID_1_SLIDE, slideTitle = context.getString(R.string.name_slide_prefix)+1, question = context.getString(R.string.text_question_default))
        val logic = Logic(bookId = bookId)
        logic.logics.add(SlideLogic(slide.slideId, slide.slideTitle, Const.SLIDE_TYPE_START))

        return (makeBookFolder(bookId) && makeConfigFile(config) && makeLogicFile(logic) && makeSlideFile(bookId, slide))
    }

    // ReadOnly File
    fun compressBook(bookId: Long):File?{
        val dir = File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId)
        if(!dir.exists()){
            return null
        }

        // get config
        val configFile = File(dir, Const.FILE_PREFIX_CONFIG+".json")
        val config = Json.decodeFromString<Config>(FileInputStream(configFile).bufferedReader().use { it.readText() })

        // copy origin folder
        val target = File(bookUserPath, Const.FILE_PREFIX_READ+bookId+"_${config.publishCode}")
        if(target.exists()){
            target.deleteRecursively()
        }
        dir.copyRecursively(target, overwrite = true)

        val file = File(target, Const.FILE_PREFIX_CONFIG+".json")
        if(file.exists()){
            file.delete()
        }
        FileOutputStream(file).use {
            it.write(Json.encodeToString(config).toByteArray())
        }

        // encrypt logic
        val logicFile = File(target, Const.FILE_DIR_CONTENT + File.separator +Const.FILE_PREFIX_LOGIC+".json")
        if(logicFile.exists()){
            val logic = Json.decodeFromString<Logic>(FileInputStream(logicFile).bufferedReader().use { it.readText() })
            logicFile.delete()
            FileOutputStream(logicFile).use {
                it.write(encryptCBC(Json.encodeToString(logic)).toByteArray())
            }
        }else{
            target.deleteRecursively()
            return null
        }

        // compress folder
        zipFolder(target.absolutePath + File.separator + Const.FILE_DIR_CONTENT, target.absolutePath + File.separator+ Const.FILE_DIR_CONTENT+".zip")
        File(target, Const.FILE_DIR_CONTENT).deleteRecursively()

        return target
    }

    fun deleteTempFile(bookId: Long, publishCode: String){
        val target = File(bookUserPath, Const.FILE_PREFIX_READ+bookId+"_$publishCode")
        target.deleteRecursively()
    }

    fun extractBook(target:File):Boolean{
        if(!target.exists()){
            return false
        }
        val content = File(target, Const.FILE_DIR_CONTENT)
        if(!content.exists()){
            content.mkdir()
        }
        unzip(target.absolutePath+File.separator+Const.FILE_DIR_CONTENT+".zip",
            target.absolutePath)
        File(target, Const.FILE_DIR_CONTENT+".zip").delete()
        return true
    }

    // encrypt / decrypt
    private val SECRET_KEY = "kvE4bhrMPqadsfer345dv39fmbAnyuwO"
    private val SECRET_IV = SECRET_KEY.substring(0, 16)

    private fun encryptCBC(target: String): String {
        val iv = IvParameterSpec(SECRET_IV.toByteArray())
        val keySpec = SecretKeySpec(
            SECRET_KEY.toByteArray(),
            "AES"
        )
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING").apply {
            init(Cipher.ENCRYPT_MODE, keySpec, iv)
        }

        val crypted = cipher.doFinal(target.toByteArray())
        val encodedByte = Base64.encode(crypted, Base64.DEFAULT)
        return String(encodedByte)
    }

    private fun decryptCBC(target: String): String {
        val decodedByte: ByteArray = Base64.decode(target, Base64.DEFAULT)
        val iv = IvParameterSpec(SECRET_IV.toByteArray())
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING").apply {
            init(Cipher.DECRYPT_MODE, keySpec, iv)
        }
        val output = cipher.doFinal(decodedByte)
        return String(output)
    }

    // compress / uncompress
    private fun zipFolder(folderPath: String, zipPath: String) {
        val zipFile = ZipFile(zipPath)
        val parameters = ZipParameters().apply {
            compressionMethod = CompressionMethod.getCompressionMethodFromCode(CompressionMethod.DEFLATE.code)
            compressionLevel = CompressionLevel.NORMAL
        }
        zipFile.addFolder(File(folderPath), parameters)
    }

    fun unzip(zipPath: String, dirPath: String) {
        val zipFile = ZipFile(zipPath)
        zipFile.extractAll(dirPath)
    }

    // Book Folder
    fun makeBookFolder(bookId: Long): Boolean{
        try{
            val dir = File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId)
            if(dir.exists()){
                dir.deleteRecursively()
            }
            dir.mkdirs()
            val content = File(dir, Const.FILE_DIR_CONTENT)
            content.mkdirs()
            val media = File(content, Const.FILE_DIR_MEDIA)
            media.mkdirs()
            val slide = File(content, Const.FILE_DIR_SLIDE)
            slide.mkdirs()
            return true
        }catch (e: Exception){
            Log.d(TAG, ""+e.printStackTrace())
            return false
        }
    }

    fun deleteBookFolder(bookId: Long, isReadOnly:Boolean = false, publishCode:String = ""): Boolean{
        try{
            val dir = if(isReadOnly){
                File(readOnlyUserPath, Const.FILE_PREFIX_READ+bookId + "_$publishCode")
            }else{
                File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId)
            }
            if(dir.exists()){
                return dir.deleteRecursively()
            }
        }catch (e: Exception){
            Log.d(TAG, ""+e.printStackTrace())
            return false
        }
        return true
    }

    // Config File
    fun makeConfigFile(config: Config): Boolean{
        try{
            val file = File(bookUserPath, Const.FILE_PREFIX_BOOK+config.bookId+ File.separator +Const.FILE_PREFIX_CONFIG+".json")
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
                File(readOnlyUserPath, Const.FILE_PREFIX_READ+bookId + "_$publishCode" +File.separator+ Const.FILE_PREFIX_CONFIG+".json")
            }else{
                File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId +File.separator+ Const.FILE_PREFIX_CONFIG+".json")
            }
            if(file.exists()){
                val configJson = FileInputStream(file).bufferedReader().use { it.readText() }
                config = Json.decodeFromString<Config>(configJson)
            }

        }catch (e: Exception){
            Log.d(TAG, ""+e.printStackTrace())
        }
        return config
    }

    // Logic File
    fun makeLogicFile(logic: Logic): Boolean{
        try{
            val file = File(bookUserPath, Const.FILE_PREFIX_BOOK+logic.bookId +File.separator+ Const.FILE_DIR_CONTENT + File.separator + Const.FILE_PREFIX_LOGIC+".json")
            if(file.exists()){
                file.delete()
            }
            FileOutputStream(file).use {
                it.write(Json.encodeToString(logic).toByteArray())
            }
        }catch (e: Exception){
            Log.d(TAG, ""+e.printStackTrace())
            return false
        }
        return true
    }

    fun getLogicFromFile(bookId: Long, isReadOnly:Boolean = false, publishCode:String = ""): Logic?{
        var logic: Logic? = null
        try{
            val file = if(isReadOnly){
                File(readOnlyUserPath, Const.FILE_PREFIX_READ+bookId + "_$publishCode" +File.separator + Const.FILE_DIR_CONTENT+File.separator+ Const.FILE_PREFIX_LOGIC+".json")
            }else{
                File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId +File.separator+ Const.FILE_DIR_CONTENT + File.separator+Const.FILE_PREFIX_LOGIC+".json")
            }

            if(file.exists()){
                val logicJson = if(isReadOnly){
                    decryptCBC(FileInputStream(file).bufferedReader().use { it.readText() })
                }else{
                    FileInputStream(file).bufferedReader().use { it.readText() }
                }
                logic = Json.decodeFromString<Logic>(logicJson)
            }

        }catch (e: Exception){
            Log.d(TAG, ""+e.printStackTrace())
        }
        return logic
    }

    // Slide File
    fun makeSlideFile(bookId: Long, slide: Slide): Boolean{
        try{
            val file = File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId +File.separator + Const.FILE_DIR_CONTENT +File.separator + Const.FILE_DIR_SLIDE + File.separator+Const.FILE_PREFIX_SLIDE+slide.slideId+".json")
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
                File(readOnlyUserPath, Const.FILE_PREFIX_READ +bookId + "_$publishCode" + File.separator+ Const.FILE_DIR_CONTENT + File.separator+ Const.FILE_DIR_SLIDE+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
            }else{
                File(bookUserPath, Const.FILE_PREFIX_BOOK +bookId + File.separator+ Const.FILE_DIR_CONTENT+File.separator+ Const.FILE_DIR_SLIDE+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
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
                File(readOnlyUserPath, Const.FILE_PREFIX_READ+bookId + "_$publishCode" + File.separator+ Const.FILE_DIR_CONTENT + File.separator+ Const.FILE_DIR_SLIDE+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
            }else{
                File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId + File.separator+ Const.FILE_DIR_CONTENT +File.separator+ Const.FILE_DIR_SLIDE+File.separator+Const.FILE_PREFIX_SLIDE+slideId+".json")
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
    fun makeImageFile(drawable: Drawable?, bookId: Long, imageName: String, isCover: Boolean = false): String{
        if(drawable == null || imageName == ""){
            return ""
        }

        try{
            val file = if(isCover){
                File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId + File.separator + imageName)
            }else{
                File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId + File.separator + Const.FILE_DIR_CONTENT + File.separator + Const.FILE_DIR_MEDIA + File.separator+ imageName)
            }
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
                    if(isCover){
                        drawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId + File.separator + "$name.jpg")))
                    }else{
                        drawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(File(bookUserPath, Const.FILE_PREFIX_BOOK+bookId + File.separator + Const.FILE_DIR_CONTENT + File.separator+ Const.FILE_DIR_MEDIA + File.separator+ "$name.jpg")))
                    }
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

    fun getImageFile(bookId: Long, imageName: String, isReadOnly:Boolean = false, publishCode:String = "", isCover: Boolean = false): File?{
        val file = File( if(isReadOnly){ readOnlyUserPath } else { bookUserPath },
            if(isReadOnly){ Const.FILE_PREFIX_READ } else { Const.FILE_PREFIX_BOOK } + bookId
                    + if(isReadOnly){ "_$publishCode" } else { "" } + File.separator
                    + if(isCover){ "" } else {Const.FILE_DIR_CONTENT + File.separator + Const.FILE_DIR_MEDIA + File.separator }
                    + imageName)

        return if(imageName != "" && file.exists()){
            file
        }else{
            null
        }
    }
}