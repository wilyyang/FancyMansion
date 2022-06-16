package com.cheesejuice.fancymansion

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cheesejuice.fancymansion.databinding.ActivityMainBinding
import com.cheesejuice.fancymansion.extension.createEditSampleFiles
import com.cheesejuice.fancymansion.fragment.EditListFragment
import com.cheesejuice.fancymansion.fragment.ReadListFragment
import com.cheesejuice.fancymansion.fragment.StoreFragment
import com.cheesejuice.fancymansion.fragment.UserFragment
import com.cheesejuice.fancymansion.util.BookUtil
import com.cheesejuice.fancymansion.util.CommonUtil
import com.cheesejuice.fancymansion.util.FileUtil
import com.cheesejuice.fancymansion.util.FirebaseUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var util: CommonUtil
    @Inject
    lateinit var bookUtil: BookUtil
    @Inject
    lateinit var fileUtil: FileUtil
    @Inject
    lateinit var firebaseUtil: FirebaseUtil

    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    companion object {
        private const val PERMISSION_REQUEST_CODE = 20
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(firebaseUtil.checkAuth()){
            fileUtil.initRootFolder()
            if(!bookUtil.isSampleMake()){
                createEditSampleFiles(FirebaseUtil.auth.uid!!)
            }
        }

        binding.bottomMenuMain.apply {
            setOnItemSelectedListener { item ->
                replaceFragment(
                    when (item.itemId) {
                        R.id.menu_store -> {
                            StoreFragment()
                        }
                        R.id.menu_book -> {
                            ReadListFragment()
                        }
                        R.id.menu_make -> {
                            EditListFragment()
                        }
                        R.id.menu_user -> {
                            UserFragment()
                        }
                        else -> {
                            EditListFragment()
                        }
                    }
                )
                true
            }
            selectedItemId = R.id.menu_store
        }
        requestPermissions()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.containerMain.id, fragment)
            .commit()
    }

    private fun checkPermissions() :Boolean{
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity@this, permission) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    private fun requestPermissions(): Boolean{
        if(!checkPermissions()){
            ActivityCompat.requestPermissions(
                MainActivity@this as Activity,
                permissions, PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // PERMISSION_GRANTED
                }else{
                    showDialogToGetPermission()
                }
            }
        }
    }

    private fun showDialogToGetPermission() {

        util.getAlertDailog(
            context = this@MainActivity,
            title = getString(R.string.permission_title), message = getString(R.string.permission_message),
            click = { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        ).setNegativeButton(getString(R.string.dialog_no)) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }
}