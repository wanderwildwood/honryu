package org.proninyaroslav.libretorrent.ui.compose

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mudita.mmd.ThemeMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import kotlinx.coroutines.flow.first

/**
 * The rows of one list, handed over from the fragment that loads them, and where the list is.
 * It also keeps the fragment's "nothing here" and "loading" placeholders in step, as the
 * RecyclerView it replaces did.
 */
class ComposeList<T : Any> {
    // By reference: the row classes count two rows as equal when they are the same torrent or
    // file, however far it has come on, and an equal list would never be drawn again.
    var items: List<T> by mutableStateOf(emptyList(), referentialEqualityPolicy())
        private set

    /** Letters down the edge while this is on and the rows really are in name order. */
    var byName: Boolean by mutableStateOf(false)

    val state = LazyListState()

    /**
     * Keep to the last page as rows come in, as a log does. Paging back turns it off and paging
     * on to the last page turns it on again.
     */
    var follow: Boolean by mutableStateOf(false)

    private var emptyView: View? = null
    private var loadingView: View? = null
    private var listView: View? = null
    private var loading = false

    fun submitList(list: List<T>?) {
        items = list?.toList() ?: emptyList()
        checkEmpty()
    }

    fun size(): Int = items.size

    fun setEmptyView(list: View, empty: View?) {
        listView = list
        emptyView = empty
        checkEmpty()
    }

    fun setLoadingView(view: View) {
        loadingView = view
        view.visibility = View.GONE
    }

    fun setLoading(isLoading: Boolean) {
        loading = isLoading
        loadingView?.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) emptyView?.visibility = View.GONE else checkEmpty()
    }

    private fun checkEmpty() {
        val empty = emptyView ?: return
        if (loading) return
        val none = items.isEmpty()
        empty.visibility = if (none) View.VISIBLE else View.GONE
        listView?.visibility = if (none) View.GONE else View.VISIBLE
    }
}

/**
 * The rows picked out for an action, in the order they were picked. A press and hold picks a
 * row; while any is picked a tap picks or drops one; with none picked a tap opens the row. That
 * is how the RecyclerView selection this replaces behaved.
 */
class RowSelection<K : Any> @JvmOverloads constructor(private val canSelect: (K) -> Boolean = { true }) {
    private val keys = mutableStateListOf<K>()
    private val observers = mutableListOf<Runnable>()

    fun hasSelection(): Boolean = keys.isNotEmpty()

    fun isSelected(key: K): Boolean = keys.contains(key)

    /** A copy: the fragment may act on it after the picks have been cleared. */
    fun getSelection(): List<K> = keys.toList()

    fun select(key: K) {
        if (!canSelect(key) || keys.contains(key)) return
        keys.add(key)
        changed()
    }

    fun toggle(key: K) {
        if (keys.contains(key)) {
            keys.remove(key)
            changed()
        } else {
            select(key)
        }
    }

    fun selectAll(all: List<K>) {
        val add = all.filter { canSelect(it) && !keys.contains(it) }
        if (add.isEmpty()) return
        keys.addAll(add)
        changed()
    }

    fun clearSelection() {
        if (keys.isEmpty()) return
        keys.clear()
        changed()
    }

    fun addObserver(observer: Runnable) {
        observers.add(observer)
    }

    private fun changed() = observers.toList().forEach { it.run() }
}

/** Puts a list on a ComposeView, in the house theme. */
internal fun ComposeView.showList(content: @Composable () -> Unit) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent { ThemeMMD(colorScheme = monochrome) { content() } }
}

/**
 * A list of rows split by thin rules, full width, paged on whole rows.
 */
@Composable
internal fun <T : Any> RowList(
    model: ComposeList<T>,
    key: ((T) -> Any)?,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    row: @Composable (T) -> Unit,
) {
    val items = model.items
    PagedColumn(
        Modifier.fillMaxSize(),
        state = model.state,
        contentPadding = contentPadding,
        isScrollbarVisible = false,
    ) {
        itemsIndexed(items, key = key?.let { k -> { _: Int, item: T -> k(item) } }) { i, item ->
            Column(Modifier.fillMaxWidth()) {
                row(item)
                if (i < items.lastIndex) {
                    HorizontalDividerMMD(Modifier.padding(horizontal = 16.dp), thickness = 1.dp)
                }
            }
        }
    }
}

/**
 * A row that opens on a tap and is picked by a press and hold, and drawn inside a ring while
 * picked. Without a [selection] a press and hold calls [onLongClick] instead.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun <T : Any> Modifier.rowClicks(
    item: T,
    selection: RowSelection<T>?,
    onClick: ((T) -> Unit)?,
    onLongClick: ((T) -> Unit)? = null,
): Modifier {
    val picked = selection?.isSelected(item) == true
    val source = remember { MutableInteractionSource() }
    return this
        .padding(horizontal = 4.dp)
        .then(
            if (picked) Modifier.border(2.dp, Color.Black, RoundedCornerShape(12.dp)) else Modifier
        )
        .combinedClickable(
            interactionSource = source,
            // No ripple: on this panel it is a grey smear drawn and undrawn.
            indication = null,
            onClick = {
                if (selection != null && selection.hasSelection()) selection.toggle(item)
                else onClick?.invoke(item)
            },
            onLongClick = when {
                selection != null -> ({ selection.select(item) })
                onLongClick != null -> ({ onLongClick(item) })
                else -> null
            },
        )
}

/**
 * Brings the last page of [rows] rows to the screen: the last row whole at the foot, with as many
 * whole rows above it as fit. PagedColumn itself brings the last row to the top, which suits a
 * jump but not a log that is read from its newest end.
 */
internal suspend fun LazyListState.lastPage(rows: Int) {
    if (rows <= 0) return
    // Measured with the rows in it first: before that there is nothing to fit them against.
    snapshotFlow { layoutInfo.viewportEndOffset > 0 && layoutInfo.totalItemsCount >= rows }.first { it }
    val last = rows - 1
    var start = last
    scrollToItem(start)
    while (start > 0) {
        scrollToItem(start - 1)
        val end = layoutInfo.visibleItemsInfo.firstOrNull { it.index == last }
        if (end == null || end.offset + end.size > layoutInfo.viewportEndOffset) {
            scrollToItem(start)
            return
        }
        start--
    }
}

/** Whether the last of [rows] rows is on screen, whole. */
internal fun LazyListState.atLastRow(rows: Int): Boolean {
    if (rows <= 0) return true
    val end = layoutInfo.visibleItemsInfo.firstOrNull { it.index == rows - 1 } ?: return false
    return end.offset + end.size <= layoutInfo.viewportEndOffset
}
