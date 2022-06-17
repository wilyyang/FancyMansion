package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.cheesejuice.fancymansion.databinding.ActivityAuthBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.cheesejuice.fancymansion.model.UserInfo
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject
import android.view.animation.TranslateAnimation
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.cheesejuice.fancymansion.Const.Companion.TAG

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var firebaseUtil: FirebaseUtil

    private val googleLoginForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if(result.resultCode == -1){
                showLoadingScreen(true, binding.layoutLoading.root, binding.layoutBody, getString(R.string.loading_text_login))
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseUtil.auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                CoroutineScope(Dispatchers.Default).launch {
                                    firebaseUtil.initUserInfo()
                                    withContext(Main){
                                        val intent = Intent(this@AuthActivity, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                }
                            } else {
                                showLoadingScreen(false, binding.layoutLoading.root, binding.layoutBody, "")
                                Toast.makeText(this, "Failed login", Toast.LENGTH_SHORT).show()
                            }
                        }
                } catch (e: ApiException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logoutExtra = intent.getBooleanExtra(Const.INTENT_LOGOUT, false)
        FirebaseUtil.userInfo = null
        CoroutineScope(Dispatchers.Default).launch {
            if (firebaseUtil.checkAuth()) {
                firebaseUtil.initUserInfo()
            }
            withContext(Main) {
                val constraintSet = ConstraintSet().apply {
                    clone(binding.layoutImage)
                    clear(binding.imageViewMid.id, ConstraintSet.TOP)
                    connect(binding.imageViewMid.id,
                        ConstraintSet.BOTTOM,
                        binding.imageViewBottom.id,
                        ConstraintSet.TOP
                    )
                }

                if(!logoutExtra){
                    delay(500L)
                    val autoTransition = AutoTransition().apply {
                        duration = 1200
                    }
                    TransitionManager.beginDelayedTransition(binding.layoutImage, autoTransition)
                }

                constraintSet.applyTo(binding.layoutImage)

                if(!logoutExtra){
                    delay(1500L)
                }

                FirebaseUtil.userInfo?.also {
                    val intent = Intent(this@AuthActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }?:also {
                    if(!logoutExtra){
                        TransitionManager.beginDelayedTransition(binding.layoutBody)
                    }
                    binding.googleLoginBtn.visibility = View.VISIBLE
                }
            }
        }

        binding.googleLoginBtn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            googleLoginForResult.launch(signInIntent)
        }
    }
}