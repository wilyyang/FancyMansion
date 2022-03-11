package com.cheesejuice.fancymansion.view

import android.content.Context
import android.graphics.Rect
import android.text.InputType.*
import android.text.TextUtils
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.core.view.setPadding
import com.cheesejuice.fancymansion.R
import com.google.android.material.textfield.TextInputEditText

class RoundEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle
): TextInputEditText(context, attrs, defStyleAttr){
    init {
        this.setPadding(context.resources.getDimensionPixelSize(R.dimen.padding_default_edit))
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        val mgr: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        when(focused){
            false -> {
                mgr!!.hideSoftInputFromWindow(windowToken, 0)
            }
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }
}