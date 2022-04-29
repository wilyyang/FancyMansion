package com.cheesejuice.fancymansion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.cheesejuice.fancymansion.databinding.ActivityAuthBinding
import com.cheesejuice.fancymansion.extension.showLoadingScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding

    private val googleLoginForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            showLoadingScreen(true, binding.layoutLoading.root, binding.layoutBody)
            val task = GoogleSignIn.getSignedInAccountFromIntent(result?.data)
            try {
                val account = task.getResult(ApiException::class.java)!!

                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                MainApplication.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            MainApplication.name = account.displayName
                            MainApplication.email = account.email
                            MainApplication.photoUrl = account.photoUrl
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            showLoadingScreen(false, binding.layoutLoading.root, binding.layoutBody)
                            Toast.makeText(this, "Failed login", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                Toast.makeText(this, "Failed ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        binding= ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(MainApplication.checkAuth()){
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
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