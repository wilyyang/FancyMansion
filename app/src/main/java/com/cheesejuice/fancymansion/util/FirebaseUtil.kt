package com.cheesejuice.fancymansion.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.cheesejuice.fancymansion.Const
import com.cheesejuice.fancymansion.MainApplication
import com.cheesejuice.fancymansion.model.Config
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
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
        val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
        val storage: FirebaseStorage by lazy { Firebase.storage }
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
}