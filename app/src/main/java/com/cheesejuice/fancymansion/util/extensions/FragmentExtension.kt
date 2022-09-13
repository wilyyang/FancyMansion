package com.cheesejuice.fancymansion.extension

import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cheesejuice.fancymansion.R

// UI
fun Fragment.showLoadingScreen(isLoading: Boolean, loading: View, main: View, loadingText:String){
    if(isLoading){
        val view = this.requireActivity().currentFocus
        if (view != null) {
            view.clearFocus()
            val imm = this.requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        loading.findViewById<TextView>(R.id.tvLoading).text = loadingText
        loading.visibility = View.VISIBLE
        main.visibility = View.GONE

    }else{
        loading.visibility = View.GONE
        main.visibility = View.VISIBLE

    }
}