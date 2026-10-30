/*
 * This file is part of Torrents, a fork of LibreTorrent, and is under the GNU General Public
 * License, version 3 or later.
 */

package org.proninyaroslav.libretorrent.ui.customviews

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import org.proninyaroslav.libretorrent.R
import org.proninyaroslav.libretorrent.ui.compose.openLink

/** A web link that opens in a real browser, or says there is none. Never the system's WebView shell. */
class GuardedUrlSpan(private val url: String) : ClickableSpan() {
    override fun onClick(widget: View) {
        openGuarded(widget.context, url)
    }
}

/** Opens [url] through the shared guarded opener, with the "no browser" message carrying the address. */
fun openGuarded(context: Context, url: String) {
    openLink(context, url, context.getString(R.string.honryu_about_no_browser_address, url))
}

/** Swaps every [URLSpan] (what Linkify and HTML make) for a [GuardedUrlSpan]. */
fun CharSequence.guardLinks(): CharSequence {
    val out = SpannableString(this)
    for (span in out.getSpans(0, out.length, URLSpan::class.java)) {
        val start = out.getSpanStart(span)
        val end = out.getSpanEnd(span)
        val flags = out.getSpanFlags(span)
        out.removeSpan(span)
        out.setSpan(GuardedUrlSpan(span.url), start, end, flags or Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return out
}

/**
 * A text view that turns web addresses in its text into links, tapped through the guarded opener.
 * Used where a torrent's own comment or a dialog message can hold addresses.
 */
class GuardedLinkTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text.isNullOrEmpty()) {
            super.setText(text, type)
            return
        }
        val spannable = SpannableString(text)
        Linkify.addLinks(spannable, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
        super.setText(spannable.guardLinks(), BufferType.SPANNABLE)
        if (movementMethod !is LinkMovementMethod) {
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
