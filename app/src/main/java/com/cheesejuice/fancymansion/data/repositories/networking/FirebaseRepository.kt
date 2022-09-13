package com.cheesejuice.fancymansion.data.repositories.networking

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.FB_ALL_BOOK
import com.cheesejuice.fancymansion.Const.Companion.FB_ALL_COMMENT
import com.cheesejuice.fancymansion.R
import com.cheesejuice.fancymansion.data.models.Comment
import com.cheesejuice.fancymansion.data.models.Config
import com.cheesejuice.fancymansion.data.models.UserInfo
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class FirebaseRepository @Inject constructor(@ActivityContext private val context: Context){
    companion object{
        val auth: FirebaseAuth by lazy { Firebase.auth }
        private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
        private val storage: FirebaseStorage by lazy { Firebase.storage }
        var userInfo: UserInfo? = null
    }

    val email: String?
        get() = auth.currentUser?.email
    val name: String?
        get() = auth.currentUser?.displayName
    val photoUrl: Uri?
        get() = auth.currentUser?.photoUrl

    fun checkAuth(): Boolean = auth.currentUser?.isEmailVerified ?:let { false }

    fun signOut(activity: Activity){
        auth.signOut()
        GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut()
    }

    // User
    suspend fun initUserInfo(){
        userInfo =
            getUserInfo(uid = auth.uid!!) ?: let {
                addUserInfo(
                    UserInfo(
                        uid = auth.uid!!,
                        email = email!!,
                        userName = name!!,
                        photoUrl = photoUrl.toString()
                    )
                )
            }
    }

    suspend fun getUserInfo(uid: String): UserInfo?{
        val documents = db.collection(Const.FB_DB_KEY_USER).whereEqualTo(Const.FB_DB_KEY_UID, uid).get().await().documents
        return if(documents.size > 0) {
            documents[0].toObject(UserInfo::class.java)
        }else{ null }
    }

    suspend fun addUserInfo(userInfo: UserInfo): UserInfo? {
        userInfo.id = db.collection(Const.FB_DB_KEY_USER).add(userInfo).await().id
        db.collection(Const.FB_DB_KEY_USER).document(userInfo.id).set(userInfo).await()

        val documents = db.collection(Const.FB_DB_KEY_USER).whereEqualTo(Const.FB_DB_KEY_UID, userInfo.uid).get().await().documents
        return if(documents.size > 0) {
            documents[0].toObject(UserInfo::class.java)
        }else{ null }
    }

    private suspend fun registerBookInUserInfo(publishCode: String): Boolean {
        var result = false
        initUserInfo()
        userInfo!!.apply {
            uploadBookTime = System.currentTimeMillis()
            uploadBookIds.add(publishCode)
            db.collection(Const.FB_DB_KEY_USER).document(id).set(this).addOnSuccessListener {
                result = true
            }.await()
        }
        return result
    }

    private suspend fun unregisterBookInUserInfo(publishCode: String): Boolean {
        var result = false
        initUserInfo()
        userInfo!!.apply {
            uploadBookIds.remove(publishCode)
            db.collection(Const.FB_DB_KEY_USER).document(id).set(this).addOnSuccessListener {
                result = true
            }.await()
        }
        return result
    }

    suspend fun updateCommentInUserInfo(): Boolean {
        var result = false
        initUserInfo()
        userInfo!!.apply {
            addCommentTime = System.currentTimeMillis()
            db.collection(Const.FB_DB_KEY_USER).document(id).set(this).addOnSuccessListener {
                result = true
            }.await()
        }
        return result
    }

    // Book
    fun returnImageToCallback(filePath:String, successCallback:(Uri?)->Unit, failCallback:()->Unit = {}){
        storage.reference.child(filePath).downloadUrl.addOnCompleteListener {
            try{
                if (it.isSuccessful) {
                    successCallback(it.result)
                }else{
                    failCallback()
                }
            }catch (exception: IllegalArgumentException){}
        }.addOnFailureListener {
            try{
                failCallback()
            }catch (exception: IllegalArgumentException){}
        }
    }

    suspend fun isBookUpload(publishCode:String, uid:String? = auth.uid ):Boolean{
        val uploadConfig = db.collection(Const.FB_DB_KEY_BOOK).whereEqualTo(Const.FB_DB_KEY_PUBLISH, publishCode)
            .whereEqualTo(Const.FB_DB_KEY_UID, uid).get().await()
        return uploadConfig.documents.size > 0
    }

    suspend fun getBookConfig(publishCode: String): Config?{
        val documents = db.collection(Const.FB_DB_KEY_BOOK).whereEqualTo(Const.FB_DB_KEY_PUBLISH, publishCode).get().await().documents
        return if(documents.size > 0) {
            documents[0].toObject(Config::class.java)
        }else{ null }
    }

    suspend fun uploadBookConfig(config: Config):String{
        val publishCode = db.collection(Const.FB_DB_KEY_BOOK).add(config).await().id
        registerBookInUserInfo(publishCode)
        return publishCode
    }

    suspend fun updateBookConfig(config: Config){
        config.email = email!!
        config.user = name!!
        config.downloads = getDownloads(config.publishCode)
        config.good = getBookGoodCount(config.publishCode)
        db.collection(Const.FB_DB_KEY_BOOK).document(config.publishCode).set(config).await()
    }

    suspend fun uploadBookFile(storagePath: String, file: File):Boolean{
        var result = true
        val subFileRef: StorageReference = storage.reference.child(storagePath)
        subFileRef.putFile(Uri.fromFile(file))
            .addOnFailureListener{
                result = false
            }.await()
        return result
    }

    suspend fun downloadBook(config: Config, dir:File, channel: Channel<Pair<String, Int>>){
        val bookRef = storage.reference.child("/${Const.FB_STORAGE_BOOK}/${config.uid}/${config.publishCode}")
        val list = bookRef.listAll().await()

        if(!dir.exists()){
            dir.mkdir()
        }

        val total = list.items.sumOf { item -> item.metadata.await().sizeBytes }
        var current = 0L

        for(file in list.items){
            val subRef = bookRef.child(file.name)
            val subFile = File(dir, file.name)
            subRef.getFile(subFile).await()

            current += subFile.length()
            val data = Pair(context.getString(R.string.loading_text_download_file_percent)+subFile.name, ((current.toFloat() / total)*100).toInt())
            channel.send(data)
        }

        channel.close()
    }

    suspend fun deleteBook(config: Config){
        db.collection(Const.FB_DB_KEY_BOOK).document(config.publishCode).delete().await()

        val deleteRef = storage.reference.child("/${Const.FB_STORAGE_BOOK}/${config.uid}/${config.publishCode}")
        try {
            val list = deleteRef.listAll().await()
            for(ref in list.items){
                ref.delete().await()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        unregisterBookInUserInfo(config.publishCode)
    }
    suspend fun getUserBookList(uid:String):MutableList<Config> {
        val configList = mutableListOf<Config>()
        val documents = db.collection(Const.FB_DB_KEY_BOOK).whereEqualTo(Const.FB_DB_KEY_UID, uid)
            .orderBy(Const.FB_DB_KEY_TIME).get().await().documents

        for (document in documents){
            val item = document.toObject(Config::class.java)
            if (item != null) {
                configList.add(item)
            }
        }
        return configList
    }

    suspend fun getBookList(limit:Long = FB_ALL_BOOK, startConfig: Config? = null, orderKey:Int, searchKeyword:String?):MutableList<Config>{
        val (sortKeyword, isAscending) = if(searchKeyword != null && searchKeyword != ""){
            Pair(Const.FB_DB_KEY_TITLE, true)
        }else{
            when(orderKey){
                Const.ORDER_LATEST_IDX -> Pair(Const.FB_DB_KEY_TIME, false)
                Const.ORDER_OLDEST_IDX -> Pair(Const.FB_DB_KEY_TIME, true)
                Const.ORDER_TITLE_ASC_IDX -> Pair(Const.FB_DB_KEY_TITLE, true)
                Const.ORDER_TITLE_DESC_IDX -> Pair(Const.FB_DB_KEY_TITLE, false)
                Const.ORDER_DOWNLOADS_ASC_IDX -> Pair(Const.FB_DB_KEY_DOWNLOADS, true)
                Const.ORDER_DOWNLOADS_DESC_IDX -> Pair(Const.FB_DB_KEY_DOWNLOADS, false)
                Const.ORDER_GOOD_ASC_IDX -> Pair(Const.FB_DB_KEY_GOOD, true)
                Const.ORDER_GOOD_DESC_IDX -> Pair(Const.FB_DB_KEY_GOOD, false)
                else -> Pair(Const.FB_DB_KEY_TITLE, true)
            }
        }

        val configList = mutableListOf<Config>()
        val documents = db.collection(Const.FB_DB_KEY_BOOK)
            .let {
                if(searchKeyword != null && searchKeyword != ""){
                    it.whereGreaterThanOrEqualTo(Const.FB_DB_KEY_TITLE, searchKeyword)
                        .whereLessThanOrEqualTo(Const.FB_DB_KEY_TITLE, searchKeyword+ '\uf8ff')
                }else{
                    it
                }
            }
            .orderBy(sortKeyword, if(isAscending){ Query.Direction.ASCENDING } else { Query.Direction.DESCENDING })
            .orderBy(Const.FB_DB_KEY_PUBLISH)
            .let {
                if(startConfig != null){
                    it.startAfter(
                        when(sortKeyword){
                            Const.FB_DB_KEY_TIME -> { startConfig.updateTime }
                            Const.FB_DB_KEY_TITLE -> { startConfig.title }
                            Const.FB_DB_KEY_DOWNLOADS -> { startConfig.downloads }
                            Const.FB_DB_KEY_GOOD -> { startConfig.good }
                            else -> { startConfig.title}
                        },
                        startConfig.publishCode
                    )
                }else{
                    it
                }
            }.let {
                if(limit == FB_ALL_BOOK){
                    it
                }else{
                    it.limit(limit)
                }
            }.get().await().documents

        for (document in documents){
            val item = document.toObject(Config::class.java)
            if (item != null) {
                configList.add(item)
            }
        }
        return configList
    }

    // Good
    private suspend fun getBookGoodCount(publishCode: String):Int{
        return db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_GOOD).get().await().size()
    }

    suspend fun isBookGoodUser(publishCode: String):Boolean{
        return (db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_GOOD)
            .whereEqualTo(Const.FB_DB_KEY_UID, auth.uid).get().await().size() > 0)
    }

    suspend fun setBookGoodUser(publishCode: String, setGood:Boolean):Int{
        with(db.collection(Const.FB_DB_KEY_BOOK).document(publishCode)){
            collection(Const.FB_DB_KEY_GOOD).whereEqualTo(Const.FB_DB_KEY_UID, auth.uid).get().await().let { result ->
                if (result.size() > 0) {
                    // isBookGood true  & setGood false -> delete
                    if(!setGood){
                        collection(Const.FB_DB_KEY_GOOD).document(result.documents[0].id).delete().await()
                        val good = getBookGoodCount(publishCode)
                        update(Const.FB_DB_KEY_GOOD, good).await()
                        return good
                    }
                } else {
                    // isBookGood false & setGood true  -> add
                    if(setGood){
                        collection(Const.FB_DB_KEY_GOOD).add(hashMapOf(Const.FB_DB_KEY_UID to auth.uid)).await()
                        val good = getBookGoodCount(publishCode)
                        update(Const.FB_DB_KEY_GOOD, good).await()
                        return good
                    }
                }
            }
        }
        return 0
    }

    // Downloads
    private suspend fun getDownloads(publishCode: String):Int{
        return db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_DOWNLOADS).get().await().size()
    }

    suspend fun incrementBookDownloads(publishCode: String):Int{
        with(db.collection(Const.FB_DB_KEY_BOOK).document(publishCode)){
            if(collection(Const.FB_DB_KEY_DOWNLOADS).whereEqualTo(Const.FB_DB_KEY_UID, auth.uid).get().await().size() <1){
                collection(Const.FB_DB_KEY_DOWNLOADS).add( hashMapOf(Const.FB_DB_KEY_UID to auth.uid) ).await()
                val downloads = getDownloads(publishCode)
                update(Const.FB_DB_KEY_DOWNLOADS, downloads).await()
                return downloads
            }
        }
        return 0
    }

    // Book Report
    suspend fun incrementBookReport(config: Config, type:Int):Int{
        with(db.collection(Const.FB_DB_KEY_BOOK).document(config.publishCode)){
            if(collection(Const.FB_DB_KEY_REPORT).whereEqualTo(Const.FB_DB_KEY_UID, auth.uid).get().await().size() <1){
                collection(Const.FB_DB_KEY_REPORT).add( hashMapOf(Const.FB_DB_KEY_UID to auth.uid,
                    Const.FB_DB_KEY_REPORT_TYPE to type)).await()
                val reports = getBookReports(config)
                update(Const.FB_DB_KEY_REPORT, reports).await()
                return reports
            }
        }
        return 0
    }

    private suspend fun getBookReports(config: Config):Int{
        return db.collection(Const.FB_DB_KEY_BOOK).document(config.publishCode)
            .collection(Const.FB_DB_KEY_REPORT).get().await().size()
    }

    // Comment
    suspend fun addComment(comment: Comment):String{
        return db.collection(Const.FB_DB_KEY_BOOK).document(comment.bookPublishCode).collection(Const.FB_DB_KEY_COMMENT).add(comment).await().id
    }

    suspend fun editComment(comment: Comment){
        db.collection(Const.FB_DB_KEY_BOOK).document(comment.bookPublishCode).collection(Const.FB_DB_KEY_COMMENT)
            .document(comment.id).set(comment).await()
    }

    suspend fun deleteComment(comment: Comment){
        db.collection(Const.FB_DB_KEY_BOOK).document(comment.bookPublishCode).collection(Const.FB_DB_KEY_COMMENT)
            .document(comment.id).delete().await()
    }

    suspend fun incrementCommentReport(comment: Comment, type:Int):Int{
        with(db.collection(Const.FB_DB_KEY_BOOK).document(comment.bookPublishCode).collection(Const.FB_DB_KEY_COMMENT).document(comment.id)){
            if(collection(Const.FB_DB_KEY_REPORT).whereEqualTo(Const.FB_DB_KEY_UID, auth.uid).get().await().size() <1){
                collection(Const.FB_DB_KEY_REPORT).add( hashMapOf(Const.FB_DB_KEY_UID to auth.uid,
                    Const.FB_DB_KEY_REPORT_TYPE to type)).await()
                val reports = getCommentReports(comment)
                update(Const.FB_DB_KEY_REPORT, reports).await()
                return reports
            }
        }
        return 0
    }

    private suspend fun getCommentReports(comment: Comment):Int{
        return db.collection(Const.FB_DB_KEY_BOOK).document(comment.bookPublishCode).collection(Const.FB_DB_KEY_COMMENT).document(comment.id)
            .collection(Const.FB_DB_KEY_REPORT).get().await().size()
    }

    suspend fun getCommentList(publishCode:String, limit:Long = FB_ALL_COMMENT, startComment: Comment? = null, isOrderRecent:Boolean):MutableList<Comment>{
        val commentList = mutableListOf<Comment>()

        val documents = db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_COMMENT)
            .orderBy(Const.FB_DB_KEY_COMMENT_TIME, if(!isOrderRecent){ Query.Direction.ASCENDING } else { Query.Direction.DESCENDING })
            .orderBy(Const.FB_DB_KEY_COMMENT_ID)
            .let {
                if(startComment != null){
                    it.startAfter(startComment.updateTime, startComment.id)
                }else{
                    it
                }
            }.let {
                if(limit == FB_ALL_COMMENT){
                    it
                }else{
                    it.limit(limit)
                }
            }.get().await().documents

        for (document in documents){
            val item = document.toObject(Comment::class.java)
            if (item != null) {
                commentList.add(item)
            }
        }
        return commentList
    }
}