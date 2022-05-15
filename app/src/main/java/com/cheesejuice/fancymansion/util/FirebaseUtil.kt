package com.cheesejuice.fancymansion.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import com.cheesejuice.fancymansion.BookOrderBy
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.Const.Companion.FB_ALL_BOOK
import com.cheesejuice.fancymansion.Const.Companion.FB_ALL_COMMENT
import com.cheesejuice.fancymansion.Const.Companion.TAG
import com.cheesejuice.fancymansion.MainApplication
import com.cheesejuice.fancymansion.model.Comment
import com.cheesejuice.fancymansion.model.Config
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
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class FirebaseUtil @Inject constructor(@ActivityContext private val context: Context){
    companion object{
        val auth: FirebaseAuth by lazy { Firebase.auth }
        private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
        private val storage: FirebaseStorage by lazy { Firebase.storage }
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

    fun returnImageToCallback(filePath:String, successCallback:(Uri?)->Unit, failCallback:()->Unit = {}){
        storage.reference.child(filePath).downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                successCallback(it.result)
            }else{
                failCallback()
            }
        }
    }

    suspend fun isBookUpload(publishCode:String, uid:String? = auth.uid ):Boolean{
        val uploadConfig = db.collection(Const.FB_DB_KEY_BOOK).whereEqualTo(Const.FB_DB_KEY_PUBLISH, publishCode)
            .whereEqualTo(Const.FB_DB_KEY_UID, uid).get().await()
        return uploadConfig.documents.size > 0
    }

    suspend fun getBookConfig(publishCode: String):Config?{
        val documents = db.collection(Const.FB_DB_KEY_BOOK).whereEqualTo(Const.FB_DB_KEY_PUBLISH, publishCode).get().await().documents
        return if(documents.size > 0) {
            documents[0].toObject(Config::class.java)
        }else{ null }
    }

    suspend fun uploadBookConfig(config: Config):String{
        return db.collection(Const.FB_DB_KEY_BOOK).add(config).await().id
    }

    suspend fun updateBookConfig(config: Config){
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

    suspend fun downloadBook(config: Config, dir:File){
        val bookRef = storage.reference.child("/${Const.FB_STORAGE_BOOK}/${config.uid}/${config.publishCode}")
        val list = bookRef.listAll().await()

        if(!dir.exists()){
            dir.mkdir()
        }

        for(file in list.items){
            val subRef = bookRef.child(file.name)
            val subFile = File(dir, file.name)
            subRef.getFile(subFile).await()
        }
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
    }

    suspend fun getBookList(order: BookOrderBy = BookOrderBy.TIME, isDescending:Boolean = false, limit:Long = FB_ALL_BOOK, startConfig:Config? = null, orderKey:Int, searchKeyword:String?):MutableList<Config>{
        val configList = mutableListOf<Config>()
        val documents = db.collection(Const.FB_DB_KEY_BOOK)
            .let {
                if(searchKeyword != null && searchKeyword != ""){
                    it.whereArrayContains(Const.FB_DB_KEY_TITLE, searchKeyword)
                }else{
                    it
                }
            }
            .orderBy(order.keyName,
            if(isDescending){ Query.Direction.DESCENDING } else { Query.Direction.ASCENDING })
            .orderBy(Const.FB_DB_KEY_PUBLISH)
            .let {
                if(startConfig != null){
                    it.startAfter(
                        if (order == BookOrderBy.TITLE) { startConfig.title } else { startConfig.updateTime },
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

    suspend fun getBookGoodCount(publishCode: String):Int{
        return db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_GOOD).get().await().size()
    }

    suspend fun isBookGoodUser(publishCode: String):Boolean{
        return (db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_GOOD)
            .whereEqualTo(Const.FB_DB_KEY_UID, auth.uid).get().await().size() > 0)
    }

    suspend fun setBookGoodUser(publishCode: String, setGood:Boolean){
        with(db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_GOOD)){
            whereEqualTo(Const.FB_DB_KEY_UID, auth.uid).get().await().let { result ->
                if (result.size() > 0) {
                    // isBookGood true  & setGood false -> delete
                    if(!setGood){
                        document(result.documents[0].id).delete().await()
                    }
                } else {
                    // isBookGood false & setGood true  -> add
                    if(setGood){
                        add(hashMapOf(Const.FB_DB_KEY_UID to auth.uid)).await()
                    }
                }
            }
        }
    }

    suspend fun getDownloads(publishCode: String):Int{
        return db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_DOWNLOADS).get().await().size()
    }

    suspend fun incrementBookDownloads(publishCode: String){
        with(db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_DOWNLOADS)){
            if(whereEqualTo(Const.FB_DB_KEY_UID, auth.uid).get().await().size() <1){
                add( hashMapOf(Const.FB_DB_KEY_UID to auth.uid) ).await()
            }
        }
    }

    // Comment
    suspend fun addComment(comment: Comment):String{
        return db.collection(Const.FB_DB_KEY_BOOK).document(comment.bookPublishCode).collection(Const.FB_DB_KEY_COMMENT).add(comment).await().id
    }

    suspend fun editComment(comment:Comment){
        db.collection(Const.FB_DB_KEY_BOOK).document(comment.bookPublishCode).collection(Const.FB_DB_KEY_COMMENT)
            .document(comment.id).set(comment).await()
    }

    suspend fun deleteComment(comment:Comment){
        db.collection(Const.FB_DB_KEY_BOOK).document(comment.bookPublishCode).collection(Const.FB_DB_KEY_COMMENT)
            .document(comment.id).delete().await()
    }

    suspend fun getCommentList(publishCode:String, limit:Long = FB_ALL_COMMENT, startComment: Comment? = null):MutableList<Comment>{
        val commentList = mutableListOf<Comment>()

        val documents = db.collection(Const.FB_DB_KEY_BOOK).document(publishCode).collection(Const.FB_DB_KEY_COMMENT)
            .orderBy(Const.FB_DB_KEY_COMMENT_TIME).orderBy(Const.FB_DB_KEY_COMMENT_ID)
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