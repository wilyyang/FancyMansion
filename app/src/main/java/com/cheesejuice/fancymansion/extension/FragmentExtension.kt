package com.cheesejuice.fancymansion.extension

import android.app.AlertDialog
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cheesejuice.fancymansion.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// UI
fun Fragment.showLoadingScreen(isLoading: Boolean, loading: View, main: View){
    if(isLoading){
        val view = this.requireActivity().currentFocus
        if (view != null) {
            view.clearFocus()
            val imm = this.requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        loading.visibility = View.VISIBLE
        main.visibility = View.GONE

    }else{
        loading.visibility = View.GONE
        main.visibility = View.VISIBLE

    }
}

fun Fragment.showDialogAndStart(isShow: Boolean, loading: View? = null, main: View? = null, title:String, message:String, onlyOkBackground:()->Unit = {}, onlyOk:()->Unit = {}, onlyNo:()->Unit = {}, noShow:() ->Unit = {}, always:() ->Unit = {}){
    if(isShow){
        this.requireActivity().currentFocus?.let { it.clearFocus() }

        AlertDialog.Builder(this.requireActivity()).also { builder ->
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton(this.getString(R.string.dialog_ok)) { _, _ ->
                if (loading != null && main != null) showLoadingScreen(true, loading, main)
                CoroutineScope(Dispatchers.IO).launch {
                    onlyOkBackground()
                    withContext(Dispatchers.Main) {
                        onlyOk()
                        if (loading != null && main != null) showLoadingScreen(false, loading, main)
                        always()
                    }
                }
            }
            builder.setNegativeButton(this.getString(R.string.dialog_no)) { _, _ ->
                onlyNo()
                always()
            }
        }.show()
    }else{
        noShow()
        always()
    }
}