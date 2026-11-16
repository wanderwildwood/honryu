package org.proninyaroslav.libretorrent.ui.base

import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ListAdapter
import org.proninyaroslav.libretorrent.ui.compose.DarkMode

/**
 * The list a drop-down field opens (the storage chooser, piece size, torrent version) is a
 * window of its own, which dark mode does not reach unless it is handed over: it opened white
 * on the black page. The field's rows come from an adapter, and the first row drawn is the
 * first moment the app hears of that window, so the adapter is wrapped and hands it over there.
 */
object DarkDropDown {

    /** Call after the field has its rows (and again after they are replaced). */
    @JvmStatic
    fun on(field: AutoCompleteTextView) {
        val rows = field.adapter ?: return
        if (rows is Rows || rows !is Filterable) return
        field.setAdapter(Rows(rows, rows))
    }

    private class Rows(private val inner: ListAdapter, private val filterable: Filterable) : BaseAdapter(), Filterable {
        override fun getCount() = inner.count
        override fun getItem(position: Int): Any? = inner.getItem(position)
        override fun getItemId(position: Int) = inner.getItemId(position)
        override fun hasStableIds() = inner.hasStableIds()
        override fun getItemViewType(position: Int) = inner.getItemViewType(position)
        override fun getViewTypeCount() = inner.viewTypeCount
        override fun isEmpty() = inner.isEmpty
        override fun areAllItemsEnabled() = inner.areAllItemsEnabled()
        override fun isEnabled(position: Int) = inner.isEnabled(position)
        override fun getFilter(): Filter = filterable.filter
        override fun registerDataSetObserver(observer: DataSetObserver) = inner.registerDataSetObserver(observer)
        override fun unregisterDataSetObserver(observer: DataSetObserver) = inner.unregisterDataSetObserver(observer)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Rows are also made to measure the list before it is on the screen; only the list
            // in its window is handed over, so the window is turned once and not twice.
            if (parent.isAttachedToWindow) DarkMode.window(parent)
            else parent.post { if (parent.isAttachedToWindow) DarkMode.window(parent) }
            return inner.getView(position, convertView, parent)
        }
    }
}
