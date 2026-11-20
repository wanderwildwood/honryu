/*
 * This file is part of Torrents, a fork of LibreTorrent, and is under the GNU General Public
 * License, version 3 or later.
 */

package org.proninyaroslav.libretorrent.ui.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.theme.MaterialComponentsViewInflater

/**
 * A plain EditText with SteadyCaretEditText's caret, for the fields libraries inflate as
 * EditText (the search bar's field among them). The same drawing, on AppCompatEditText so such
 * a field stays what it was.
 */
class SteadyEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val caret = Paint()
    private val caretWidth = 2f * resources.displayMetrics.density
    private var caretWanted = true

    init {
        caretWanted = context.obtainStyledAttributes(
            attrs, intArrayOf(android.R.attr.cursorVisible), defStyleAttr, 0
        ).run { getBoolean(0, true).also { recycle() } }
        super.setCursorVisible(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val layout = layout ?: return
        if (!caretWanted || !isFocused || selectionStart < 0 || selectionStart != selectionEnd) return
        val offset = selectionStart
        val line = layout.getLineForOffset(offset)
        val x = totalPaddingLeft + layout.getPrimaryHorizontal(offset)
        val top = totalPaddingTop + layout.getLineTop(line)
        val bottom = totalPaddingTop + layout.getLineBottom(line)
        caret.color = currentTextColor
        canvas.drawRect(x, top.toFloat(), x + caretWidth, bottom.toFloat(), caret)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        invalidate()
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        invalidate()
    }

    override fun setCursorVisible(visible: Boolean) {
        super.setCursorVisible(false)
        if (caretWanted != visible) {
            caretWanted = visible
            invalidate()
        }
    }
}

/** Every EditText and TextInputEditText inflated from a layout gets the steady caret. */
class SteadyViewInflater : MaterialComponentsViewInflater() {
    override fun createEditText(context: Context, attrs: AttributeSet): AppCompatEditText =
        SteadyEditText(context, attrs)

    override fun createView(context: Context, name: String, attrs: AttributeSet): View? =
        if (name == "com.google.android.material.textfield.TextInputEditText") {
            SteadyCaretEditText(context, attrs)
        } else {
            null
        }
}
