package com.cheesejuice.fancymansion

import androidx.multidex.MultiDexApplication
import com.cheesejuice.fancymansion.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.HiltAndroidApp

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

        fun checkAuth(): Boolean{
            val currentUser = auth.currentUser
            return currentUser?.let {
                email = currentUser.email

                currentUser.isEmailVerified
            }?: let{
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        auth = Firebase.auth
    }
}