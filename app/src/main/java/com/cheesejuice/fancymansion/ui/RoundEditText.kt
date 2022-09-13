package com.cheesejuice.fancymansion.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.cheesejuice.fancymansion.R
import com.google.android.material.textfield.TextInputEditText

class RoundEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle
): TextInputEditText(context, attrs, defStyleAttr){
    companion object{
        var onceFocus: Boolean = false
    }

    init {
        this.setPadding(context.resources.getDimensionPixelSize(R.dimen.padding_default_edit))
        typeface = ResourcesCompat.getFont(context, R.font.notosans_regular)
        includeFontPadding = false
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        val mgr: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        when(focused){
            true -> onceFocus = true
            false -> {
                mgr!!.hideSoftInputFromWindow(windowToken, 0)
            }
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }
}