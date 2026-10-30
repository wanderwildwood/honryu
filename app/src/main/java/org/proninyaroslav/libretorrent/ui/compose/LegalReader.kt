/*
 * This file is part of Torrents, a fork of LibreTorrent, and is under the GNU General Public
 * License, version 3 or later.
 */

package org.proninyaroslav.libretorrent.ui.compose

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.annotation.RawRes
import android.view.ViewGroup
import com.mudita.mmd.components.text.TextMMD
import org.proninyaroslav.libretorrent.R
import org.proninyaroslav.libretorrent.ui.base.EInkDialogBuilder
import org.proninyaroslav.libretorrent.ui.customviews.openGuarded

/** One row of a legal page: a paragraph, a heading, or a link. */
data class LegalRow(val text: String, val link: String? = null, val heading: Boolean = false)

/** Splits a licence or policy text into paragraphs, one row each, so the reader turns on whole rows. */
fun paragraphs(text: String): List<LegalRow> =
    text.replace("\r\n", "\n")
        .split(Regex("\n\\s*\n"))
        .map { block -> block.lines().joinToString("\n") { it.trimEnd() }.trim('\n') }
        .filter { it.isNotBlank() }
        .map { LegalRow(it) }

/**
 * Turns a small Markdown document (headings, bullets, bold, code, a table) into plain rows: a heading
 * is a bold row, a bullet starts with a dot, a table row reads "first: second". No markup shows.
 */
fun markdownRows(text: String): List<LegalRow> {
    val rows = mutableListOf<LegalRow>()
    val block = StringBuilder()
    fun flush() {
        if (block.isNotBlank()) rows.add(LegalRow(block.toString().trim()))
        block.clear()
    }
    var inCode = false
    for (raw in text.replace("\r\n", "\n").lines()) {
        val line = raw.trimEnd()
        if (line.startsWith("```")) { inCode = !inCode; flush(); continue }
        if (inCode) continue
        when {
            line.isBlank() -> flush()
            line.startsWith("#") -> { flush(); rows.add(LegalRow(plainInline(line.trimStart('#').trim()), heading = true)) }
            line.startsWith("|") -> {
                flush()
                if (!line.matches(Regex("\\|[\\s|:-]+\\|?"))) {
                    val cells = line.trim('|').split('|').map { plainInline(it.trim()) }
                    if (!(cells.size > 1 && cells[0] == "permission")) rows.add(LegalRow(cells.joinToString(": ")))
                }
            }
            line.startsWith("- ") || line.startsWith("* ") -> { flush(); block.append("\u2022 ").append(plainInline(line.drop(2))) }
            line.startsWith("  ") && block.isNotEmpty() -> block.append(' ').append(plainInline(line.trim()))
            else -> { if (block.isNotEmpty()) block.append(' '); block.append(plainInline(line)) }
        }
    }
    flush()
    return rows
}

/** Strips bold, emphasis, code and link markup from one line. */
fun plainInline(line: String): String =
    line.replace(Regex("\\[([^\\]]*)]\\(([^)]*)\\)"), "$1 ($2)")
        .replace("**", "")
        .replace("`", "")
        .replace(Regex("(?<![A-Za-z0-9])_([^_]+)_(?![A-Za-z0-9])"), "$1")
        .replace(Regex("(?<![A-Za-z0-9*])\\*([^*]+)\\*(?![A-Za-z0-9*])"), "$1")

fun readRaw(context: Context, @RawRes id: Int): String =
    context.resources.openRawResource(id).use { it.bufferedReader().readText() }

/**
 * Shows [rows] in a dialog that pages on whole rows and draws no scrollbar. A row with a [LegalRow.link]
 * opens that address through the guarded opener.
 */
fun showLegalReader(activity: Activity, title: String, rows: List<LegalRow>): AlertDialog {
    val height = (activity.resources.displayMetrics.heightPixels * 0.7f).toInt()
    val view = ComposeView(activity).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        showList {
            PagedColumn(Modifier.fillMaxSize(), isScrollbarVisible = false) {
                itemsIndexed(rows) { _, row ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .then(
                                if (row.link != null) Modifier.clickable { openGuarded(activity, row.link) }
                                else Modifier
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        TextMMD(
                            text = row.text,
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = if (row.heading) FontWeight.Bold else FontWeight.Normal,
                                textDecoration = if (row.link != null) TextDecoration.Underline else null,
                            ),
                        )
                    }
                }
            }
        }
    }
    return EInkDialogBuilder(activity)
        .setTitle(title)
        .setView(view)
        .setPositiveButton(R.string.honryu_about_close, null)
        .show()
}
