/*
 * This file is part of Torrents, a fork of LibreTorrent, and is under the GNU General Public
 * License, version 3 or later.
 */

package org.proninyaroslav.libretorrent.ui.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.theme.MaterialComponentsViewInflater

/**
 * A text cursor that does not blink. The platform one is repainted twice a second, which on an
 * E Ink panel is a refresh for nothing, so the platform cursor is switched off and a steady
 * 2dp bar is drawn in its place while the field has focus and nothing is selected.
 */
class SteadyCaret(private val view: EditText) {
    private val paint = Paint().apply { color = Color.BLACK }
    private val bounds = Rect()
    private val width = 2f * view.resources.displayMetrics.density

    init {
        view.isCursorVisible = false
    }

    fun draw(canvas: Canvas) {
        val layout = view.layout ?: return
        if (!view.isFocused || !view.isEnabled) return
        val start = view.selectionStart
        if (start < 0 || start != view.selectionEnd) return
        val line = layout.getLineForOffset(start)
        view.getLineBounds(line, bounds)
        val x = (view.compoundPaddingLeft + layout.getPrimaryHorizontal(start)).coerceAtLeast(0f)
        canvas.drawRect(x, bounds.top.toFloat(), x + width, bounds.bottom.toFloat(), paint)
    }
}

class SteadyEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    private val caret = SteadyCaret(this)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        caret.draw(canvas)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        invalidate()
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        invalidate()
    }
}

class SteadyTextInputEditText(context: Context, attrs: AttributeSet?) :
    TextInputEditText(context, attrs) {
    private val caret = SteadyCaret(this)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        caret.draw(canvas)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        invalidate()
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        invalidate()
    }
}

/** Every EditText and TextInputEditText inflated from a layout gets the steady cursor. */
class SteadyViewInflater : MaterialComponentsViewInflater() {
    override fun createEditText(context: Context, attrs: AttributeSet): AppCompatEditText =
        SteadyEditText(context, attrs)

    override fun createView(context: Context, name: String, attrs: AttributeSet): View? =
        if (name == "com.google.android.material.textfield.TextInputEditText") {
            SteadyTextInputEditText(context, attrs)
        } else {
            null
        }
}
