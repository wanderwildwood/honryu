package org.proninyaroslav.libretorrent.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import org.proninyaroslav.libretorrent.ui.compose.DarkMode
import java.util.WeakHashMap

/**
 * The app's short menus as the house list: a white panel with a thin black rim and no shadow,
 * one row of words per thing to do, a dotted rule between the rows (STYLE.md). It stands in for
 * the platform's popup menus, which are windows of their own that dark mode does not reach and
 * so opened white on a black page. This one is handed to the dark filter as it opens.
 *
 * The bars and their buttons stay as they are; only what the three dots and the "+" open changes.
 * The rows are the menu's own items, and a press is passed on to the menu, so each item still
 * does what it did.
 */
object HouseMenu {

    private val sources = WeakHashMap<ViewGroup, () -> Menu?>()

    /** The toolbar's three dots open the house list from now on. */
    @JvmStatic
    fun overflow(toolbar: Toolbar) = hook(toolbar) { toolbar.menu }

    /** The same for the bar of a selection [mode], which is made anew each time the mode starts. */
    @JvmStatic
    fun overflow(activity: Activity, mode: ActionMode) {
        // The bar is put in place after the mode's menu is made.
        activity.window.decorView.post {
            // Its id depends on whether the activity has an action bar of its own.
            val bar = activity.findViewById<ViewGroup>(androidx.appcompat.R.id.action_mode_bar)
                ?: activity.findViewById(androidx.appcompat.R.id.action_context_bar)
                ?: return@post
            hook(bar) { mode.menu }
        }
    }

    /** The items of [menuRes] as a house list by [anchor]; [onItem] hears the one pressed. */
    @JvmStatic
    fun show(anchor: View, @MenuRes menuRes: Int, onItem: MenuItem.OnMenuItemClickListener) {
        // Never shown: it only reads the menu file.
        val menu = PopupMenu(anchor.context, anchor).apply { inflate(menuRes) }.menu
        show(anchor, items(menu)) { onItem.onMenuItemClick(it) }
    }

    private fun hook(bar: ViewGroup, menu: () -> Menu?) {
        val known = sources.containsKey(bar)
        sources[bar] = menu
        if (!known) bar.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> takeOver(bar) }
        takeOver(bar)
    }

    /**
     * The three-dots button of each row of actions in [bar] opens the house list. The button is
     * the one picture among a row's children (an action shown in the bar is a text view), and it
     * opens its own popup only when nothing else listens for its press.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun takeOver(bar: ViewGroup) {
        for (row in actionRows(bar)) {
            for (i in 0 until row.childCount) {
                val dots = row.getChildAt(i)
                if (dots !is ImageView) continue
                dots.setOnTouchListener(null) // its own listener opens the popup on a drag
                dots.setOnClickListener {
                    val menu = sources[bar]?.invoke() ?: return@setOnClickListener
                    val rest = items(menu).filter { bar.findViewById<View>(it.itemId) == null }
                    show(dots, rest) { menu.performIdentifierAction(it.itemId, 0) }
                }
            }
        }
    }

    private fun actionRows(group: ViewGroup): List<ActionMenuView> {
        val found = ArrayList<ActionMenuView>()
        for (i in 0 until group.childCount) {
            when (val child = group.getChildAt(i)) {
                is ActionMenuView -> found.add(child)
                is ViewGroup -> found.addAll(actionRows(child))
            }
        }
        return found
    }

    private fun items(menu: Menu): List<MenuItem> =
        (0 until menu.size()).map { menu.getItem(it) }.filter { it.isVisible }

    private fun show(anchor: View, items: List<MenuItem>, press: (MenuItem) -> Unit) {
        if (items.isEmpty() || !anchor.isAttachedToWindow) return
        val context = anchor.context
        val dp = context.resources.displayMetrics.density
        fun px(v: Float) = (v * dp + 0.5f).toInt()
        lateinit var popup: PopupWindow
        val list = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            minimumWidth = px(196f)
            setPadding(0, px(4f), 0, px(4f))
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    addView(Rule(context), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, px(1f).coerceAtLeast(1)).apply {
                        marginStart = px(16f)
                        marginEnd = px(16f)
                    })
                }
                addView(TextView(context).apply {
                    text = item.title
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    setTextColor(Color.BLACK)
                    alpha = if (item.isEnabled) 1f else 0.38f
                    gravity = Gravity.CENTER_VERTICAL
                    minHeight = px(48f)
                    setPadding(px(16f), px(10f), px(16f), px(10f))
                    if (item.isEnabled) setOnClickListener { popup.dismiss(); press(item) }
                }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }
        // The panel and its rim are the scrolling box, so they stay put when a long list scrolls.
        val box = ScrollView(context).apply {
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(px(1f).coerceAtLeast(1), Color.BLACK)
                cornerRadius = 12 * dp
            }
            isVerticalScrollBarEnabled = true
            isScrollbarFadingEnabled = false
            addView(list)
        }
        val root = anchor.rootView
        val edge = px(8f)
        val wide = (root.width - 2 * edge).coerceAtLeast(0)
        list.measure(View.MeasureSpec.makeMeasureSpec(wide, View.MeasureSpec.AT_MOST), View.MeasureSpec.UNSPECIFIED)
        val width = list.measuredWidth
        val at = IntArray(2).also { anchor.getLocationInWindow(it) }
        val seen = Rect().also { root.getWindowVisibleDisplayFrame(it) }
        val origin = IntArray(2).also { root.getLocationOnScreen(it) }
        val place = place(
            height = list.measuredHeight,
            anchorTop = at[1],
            anchorBottom = at[1] + anchor.height,
            top = seen.top - origin[1] + edge,
            bottom = seen.bottom - origin[1] - edge,
        )
        // Its right edge under the button's right edge, and never off the screen.
        val x = (at[0] + anchor.width - width).coerceIn(edge, (root.width - width - edge).coerceAtLeast(edge))
        popup = PopupWindow(box, width, place.second, true).apply {
            isOutsideTouchable = true
            elevation = 0f
            animationStyle = 0
        }
        popup.showAtLocation(root, Gravity.TOP or Gravity.START, x, place.first)
        // The list is a window of its own: dark in dark mode only when handed to the dark filter.
        DarkMode.window(box)
    }

    /**
     * Where the list goes, as (top, height), between [top] and [bottom] of the window: under the
     * button when it fits there, else above it, else on the roomier side and only as tall as
     * that room (it scrolls then).
     */
    @JvmStatic
    fun place(height: Int, anchorTop: Int, anchorBottom: Int, top: Int, bottom: Int): Pair<Int, Int> {
        val below = bottom - anchorBottom
        val above = anchorTop - top
        return when {
            height <= below -> anchorBottom to height
            height <= above -> (anchorTop - height) to height
            below >= above -> anchorBottom to below.coerceAtLeast(0)
            else -> top to above.coerceAtLeast(0)
        }
    }

    /** The dotted rule between rows: the dashes of the house lists (2.3 dp of ink, 1.5 dp of gap, a hairline). */
    private class Rule(context: Context) : View(context) {
        private val dp = resources.displayMetrics.density
        private val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = dp.coerceAtMost(1.5f)
            pathEffect = DashPathEffect(floatArrayOf(2.3f * dp, 1.5f * dp), 0f)
        }
        private val line = Path()

        // No width of its own: the widest row of words sets the list's width, and the rule follows it.
        override fun onMeasure(widthSpec: Int, heightSpec: Int) {
            val exact = MeasureSpec.getMode(widthSpec) == MeasureSpec.EXACTLY
            setMeasuredDimension(if (exact) MeasureSpec.getSize(widthSpec) else 0, getDefaultSize(0, heightSpec))
        }

        override fun onDraw(canvas: Canvas) {
            line.rewind()
            line.moveTo(0f, height / 2f)
            line.lineTo(width.toFloat(), height / 2f)
            canvas.drawPath(line, paint)
        }
    }
}
