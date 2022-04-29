package com.cheesejuice.fancymansion

import android.app.Activity
import android.net.Uri
import androidx.multidex.MultiDexApplication
import com.cheesejuice.fancymansion.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.HiltAndroidApp
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.storage.ktx.storage

@HiltAndroidApp
class MainApplication: MultiDexApplication() {
    var logic: Logic? = null
    var slideLogic: SlideLogic? = null
    var choice: ChoiceItem? = null
    var enter: EnterItem? = null
    var condition: Condition? = null

    companion object{
        lateinit var auth: FirebaseAuth
        var email: String? = null
        var name: String? = null
        var photoUrl: Uri? = null

        lateinit var db: FirebaseFirestore
        lateinit var storage: FirebaseStorage

        fun checkAuth(): Boolean{
            val currentUser = auth.currentUser
            return currentUser?.let {
                name = currentUser.displayName
                email = currentUser.email
                photoUrl = currentUser.photoUrl

                currentUser.isEmailVerified
            }?: let{
                false
            }
        }

        fun signOut(activity: Activity){
            auth.signOut()
            email = null
            GoogleSignIn.getClient(
                activity,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut()
        }
    }

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth

        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage
    }
}