package org.proninyaroslav.libretorrent.ui.compose

import android.text.TextUtils
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.checkbox.CheckboxMMD
import com.mudita.mmd.components.menus.DropdownMenuItemMMD
import com.mudita.mmd.components.menus.DropdownMenuMMD
import com.mudita.mmd.components.progress_indicator.LinearProgressIndicatorMMD
import com.mudita.mmd.components.text.TextMMD
import org.proninyaroslav.libretorrent.R
import org.proninyaroslav.libretorrent.core.logger.LogEntry
import org.proninyaroslav.libretorrent.core.model.data.PeerInfo
import org.proninyaroslav.libretorrent.core.model.data.TorrentInfo
import org.proninyaroslav.libretorrent.core.model.data.TorrentStateCode
import org.proninyaroslav.libretorrent.core.model.filetree.FilePriority
import org.proninyaroslav.libretorrent.core.model.filetree.TorrentContentFileTree
import org.proninyaroslav.libretorrent.core.utils.Utils
import org.proninyaroslav.libretorrent.ui.detailtorrent.pages.files.TorrentContentFileItem
import org.proninyaroslav.libretorrent.ui.detailtorrent.pages.peers.PeerItem
import org.proninyaroslav.libretorrent.ui.feeditems.FeedItemsListItem
import org.proninyaroslav.libretorrent.ui.feeds.FeedChannelItem
import org.proninyaroslav.libretorrent.ui.home.TorrentListItem
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// The rows of Torrents' lists, drawn as the XML rows they replace were: the same lines in the
// same places, in MMD's type.

fun interface OnRow<T> {
    fun onRow(item: T)
}

fun interface OnRowChecked<T> {
    fun onRowChecked(item: T, checked: Boolean)
}

fun interface OnRowMenu<T> {
    fun onRowMenu(menuId: Int, item: T)
}

object Lists {
    /** The torrents on Home, and the same rows under search. */
    @JvmStatic
    fun torrents(
        view: ComposeView,
        model: ComposeList<TorrentListItem>,
        selection: RowSelection<TorrentListItem>,
        onClick: OnRow<TorrentListItem>,
        onPause: OnRow<TorrentListItem>,
        bottomPadding: Int,
    ) = view.showList {
        val bottom = pxToDp(bottomPadding)
        RowList(
            model,
            key = { it.torrentId },
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = bottom),
        ) { TorrentRow(it, selection, onClick, onPause) }
    }

    @JvmStatic
    fun feeds(
        view: ComposeView,
        model: ComposeList<FeedChannelItem>,
        selection: RowSelection<FeedChannelItem>,
        onClick: OnRow<FeedChannelItem>,
        bottomPadding: Int,
    ) = view.showList {
        RowList(
            model,
            key = { it.id },
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = pxToDp(bottomPadding)),
        ) { FeedRow(it, selection, onClick) }
    }

    @JvmStatic
    fun feedItems(
        view: ComposeView,
        model: ComposeList<FeedItemsListItem>,
        onClick: OnRow<FeedItemsListItem>,
        onMenu: OnRowMenu<FeedItemsListItem>,
    ) = view.showList {
        RowList(model, key = { it.id }) { FeedItemRow(it, onClick, onMenu) }
    }

    @JvmStatic
    fun files(
        view: ComposeView,
        model: ComposeList<TorrentContentFileItem>,
        selection: RowSelection<TorrentContentFileItem>,
        onClick: OnRow<TorrentContentFileItem>,
        onChecked: OnRowChecked<TorrentContentFileItem>,
    ) = view.showList {
        val items = model.items
        RowList(model, key = null) {
            FileRow(it, selection, onClick, onChecked)
        }
    }

    @JvmStatic
    fun peers(view: ComposeView, model: ComposeList<PeerItem>, onLongClick: OnRow<PeerItem>) =
        view.showList {
            RowList(model, key = null) { PeerRow(it, onLongClick) }
        }

    /** The log, newest at the foot, kept to its last page while [ComposeList.follow] is on. */
    @JvmStatic
    fun log(view: ComposeView, model: ComposeList<LogEntry>, onClick: OnRow<LogEntry>) =
        view.showList {
            val count = model.items.size
            // Set while the list moves itself, so that only a page turned by hand, or a jump,
            // decides whether it goes on following.
            val moving = remember { booleanArrayOf(false) }
            LaunchedEffect(count, model.follow) {
                if (!model.follow) return@LaunchedEffect
                moving[0] = true
                try {
                    model.state.lastPage(count)
                } finally {
                    moving[0] = false
                }
            }
            LaunchedEffect(model) {
                snapshotFlow { model.state.firstVisibleItemIndex }.collect {
                    if (!moving[0]) model.follow = model.state.atLastRow(model.items.size)
                }
            }
            RowList(model, key = { it.id }) { LogRow(it, onClick) }
        }
}

@Composable
private fun pxToDp(px: Int): Dp = with(androidx.compose.ui.platform.LocalDensity.current) { px.toDp() }

private val label: TextStyle @Composable get() = MaterialTheme.typography.bodyLarge
private val supporting: TextStyle @Composable get() = MaterialTheme.typography.bodyMedium

@Composable
private fun Line(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = supporting,
    align: TextAlign? = null,
    weight: FontWeight? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = 1,
) = TextMMD(
    text,
    modifier = modifier,
    color = Color.Black,
    fontWeight = weight,
    textAlign = align,
    overflow = overflow,
    maxLines = maxLines,
    style = style,
)

/** Two short lines side by side, the second set against the right edge. */
@Composable
private fun TwoLines(start: String, end: String, modifier: Modifier = Modifier, startWeight: Float = 1f, endWeight: Float = 1f) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Line(start, Modifier.weight(startWeight).padding(end = 8.dp))
        Line(end, Modifier.weight(endWeight), align = TextAlign.End)
    }
}

@Composable
private fun Bar(percent: Int, modifier: Modifier = Modifier) =
    LinearProgressIndicatorMMD(progress = { percent / 100f }, modifier = modifier.fillMaxWidth())

/** An error under a row, boxed so it reads as apart from the row's own lines. */
@Composable
private fun ErrorBox(text: String) {
    Box(
        Modifier
            .padding(top = 8.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
            .padding(8.dp),
    ) { Line(text, maxLines = Int.MAX_VALUE) }
}

@Composable
private fun TorrentRow(
    item: TorrentListItem,
    selection: RowSelection<TorrentListItem>,
    onClick: OnRow<TorrentListItem>,
    onPause: OnRow<TorrentListItem>,
) {
    val context = LocalContext.current
    val paused = item.stateCode == TorrentStateCode.PAUSED
    Row(
        Modifier
            .fillMaxWidth()
            .rowClicks(item, selection, { onClick.onRow(it) })
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .defaultMinSize(minHeight = 72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onPause.onRow(item) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(if (paused) R.drawable.ic_play_arrow_24px else R.drawable.ic_pause_24px),
                contentDescription = stringResource(if (paused) R.string.resume_torrent else R.string.pause_torrent),
                tint = Color.Black,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Line(item.name ?: "", style = label, overflow = TextOverflow.MiddleEllipsis)
            val showBar = !(item.progress == 100 ||
                item.stateCode == TorrentStateCode.UNKNOWN ||
                item.progress == 0 && item.stateCode == TorrentStateCode.STOPPED)
            if (showBar) {
                // Fetching metadata has no amount to show: the bar stays empty rather than
                // running an animation the panel would draw as a smear.
                Bar(if (item.stateCode == TorrentStateCode.DOWNLOADING_METADATA) 0 else item.progress, Modifier.padding(top = 8.dp))
            }
            val status = when (item.stateCode) {
                TorrentStateCode.DOWNLOADING -> stringResource(R.string.torrent_status_downloading)
                TorrentStateCode.SEEDING -> stringResource(R.string.torrent_status_seeding)
                TorrentStateCode.PAUSED -> stringResource(R.string.torrent_status_paused)
                TorrentStateCode.STOPPED -> stringResource(R.string.torrent_status_stopped)
                TorrentStateCode.FINISHED -> stringResource(R.string.torrent_status_finished)
                TorrentStateCode.CHECKING -> stringResource(R.string.torrent_status_checking)
                TorrentStateCode.DOWNLOADING_METADATA -> stringResource(R.string.torrent_status_downloading_metadata)
                else -> ""
            }
            val speeds = context.getString(
                R.string.download_upload_speed_template,
                Formatter.formatFileSize(context, item.downloadSpeed),
                Formatter.formatFileSize(context, item.uploadSpeed),
            )
            TwoLines(status, speeds, Modifier.padding(top = 4.dp))

            val total = Formatter.formatFileSize(context, item.totalBytes)
            val received = if (item.progress == 100) total else Formatter.formatFileSize(context, item.receivedBytes)
            val eta = when {
                item.ETA >= TorrentInfo.MAX_ETA -> "• " + Utils.INFINITY_SYMBOL
                item.ETA == 0L -> ""
                else -> "• " + DateUtils.formatElapsedTime(item.ETA)
            }
            val counter = context.getString(
                R.string.download_counter_ETA_template,
                received, total, if (item.totalBytes == 0L) 0 else item.progress, eta,
            )
            val peers = context.getString(R.string.peers_template, item.peers, item.totalPeers)
            TwoLines(counter, peers, Modifier.padding(top = 4.dp), startWeight = 0.8f, endWeight = 0.2f)

            item.error?.let { ErrorBox(context.getString(R.string.error_template, it)) }
        }
    }
}

@Composable
private fun FeedRow(item: FeedChannelItem, selection: RowSelection<FeedChannelItem>, onClick: OnRow<FeedChannelItem>) {
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxWidth()
            .rowClicks(item, selection, { onClick.onRow(it) })
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .defaultMinSize(minHeight = 72.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            val unread = item.unreadCount > 0
            val noName = TextUtils.isEmpty(item.name)
            Line(
                if (noName) item.url else item.name,
                Modifier.padding(end = if (unread) 48.dp else 16.dp),
                style = label,
                weight = if (unread) FontWeight.Bold else null,
                maxLines = Int.MAX_VALUE,
            )
            if (!noName) Line(item.url, Modifier.padding(top = 4.dp))
            val date = if (item.lastUpdate == 0L) {
                context.getString(R.string.feed_last_update_never)
            } else {
                SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.lastUpdate))
            }
            Line(
                context.getString(R.string.feed_last_update_template, date),
                Modifier.fillMaxWidth().padding(top = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                align = TextAlign.End,
            )
            item.fetchError?.let { ErrorBox(it) }
        }
        if (item.unreadCount > 0) {
            // The unread count, where the badge sat.
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black, RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp),
            ) {
                TextMMD(item.unreadCount.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun FeedItemRow(item: FeedItemsListItem, onClick: OnRow<FeedItemsListItem>, onMenu: OnRowMenu<FeedItemsListItem>) {
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .rowClicks(item, null, { onClick.onRow(it) })
            .padding(start = 16.dp)
            .defaultMinSize(minHeight = 72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(vertical = 12.dp).padding(end = 16.dp)) {
            Line(
                item.title ?: "",
                Modifier.padding(bottom = 8.dp),
                style = label,
                weight = if (item.read) null else FontWeight.Bold,
                maxLines = Int.MAX_VALUE,
            )
            Line(SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.pubDate)))
        }
        Box {
            Box(
                Modifier
                    .size(48.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { menu = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_more_vert_24px), contentDescription = stringResource(R.string.menu), tint = Color.Black)
            }
            DropdownMenuMMD(expanded = menu, onDismissRequest = { menu = false }) {
                val entries = buildList {
                    add(R.id.open_article_menu to R.string.feed_item_open_article_url)
                    if (item.read) add(R.id.mark_as_unread_menu to R.string.mark_as_unread)
                    else add(R.id.mark_as_read_menu to R.string.mark_as_read)
                }
                entries.forEach { (id, text) ->
                    DropdownMenuItemMMD(
                        text = { TextMMD(stringResource(text), style = label) },
                        onClick = {
                            menu = false
                            onMenu.onRowMenu(id, item)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FileRow(
    item: TorrentContentFileItem,
    selection: RowSelection<TorrentContentFileItem>,
    onClick: OnRow<TorrentContentFileItem>,
    onChecked: OnRowChecked<TorrentContentFileItem>,
) {
    val context = LocalContext.current
    val parent = item.name == TorrentContentFileTree.PARENT_DIR
    Row(
        Modifier
            .fillMaxWidth()
            .rowClicks(item, selection, { onClick.onRow(it) })
            .padding(horizontal = 16.dp)
            .defaultMinSize(minHeight = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (icon, what) = when {
            item.isFile -> R.drawable.ic_file_24px to R.string.file
            parent -> R.drawable.ic_arrow_upward_alt_24px to R.string.parent_folder
            else -> R.drawable.ic_folder_24px to R.string.folder
        }
        Icon(painterResource(icon), contentDescription = stringResource(what), tint = Color.Black)
        Column(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Line(item.name, Modifier.padding(bottom = 4.dp), style = label, overflow = TextOverflow.MiddleEllipsis)
            if (!item.path.isNullOrEmpty()) {
                Line(item.path, Modifier.padding(bottom = 8.dp), overflow = TextOverflow.MiddleEllipsis)
            }
            if (!parent) {
                val total = item.size
                val received = item.receivedBytes
                val progress = if (received == total) 100 else (received * 100.0f / total).toInt()
                val priority = when (item.priority.type) {
                    FilePriority.Type.NORMAL -> context.getString(R.string.file_priority_normal)
                    FilePriority.Type.IGNORE -> context.getString(R.string.file_priority_low)
                    FilePriority.Type.MIXED -> context.getString(R.string.file_priority_mixed)
                    FilePriority.Type.HIGH -> context.getString(R.string.file_priority_high)
                    else -> ""
                }
                val avail = item.availability
                val availability = if (avail < 0) {
                    context.getString(R.string.not_available)
                } else {
                    String.format(Locale.getDefault(), "%.1f%%", if (avail >= 1) 100.0 else avail * 100)
                }
                if (progress != 100) Bar(progress, Modifier.padding(bottom = 8.dp))
                Line(
                    context.getString(
                        R.string.file_downloading_status_template,
                        priority,
                        Formatter.formatFileSize(context, received),
                        Formatter.formatFileSize(context, total),
                        progress,
                        availability,
                    ),
                    maxLines = Int.MAX_VALUE,
                )
            }
        }
        if (!parent) {
            Box(Modifier.align(Alignment.Top).padding(top = 8.dp)) {
                CheckboxMMD(
                    checked = item.priority.type != FilePriority.Type.IGNORE,
                    onCheckedChange = { onChecked.onRowChecked(item, it) },
                )
            }
        }
    }
}

@Composable
private fun PeerRow(item: PeerItem, onLongClick: OnRow<PeerItem>) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .rowClicks(item, null, null, { onLongClick.onRow(it) })
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .defaultMinSize(minHeight = 72.dp),
    ) {
        Line(item.ip ?: "", Modifier.padding(bottom = 8.dp), style = label)
        Bar(item.progress)
        TwoLines(
            context.getString(R.string.peer_port, item.port),
            context.getString(R.string.peer_relevance, item.relevance),
            Modifier.padding(top = 8.dp),
        )
        val type = when (item.connectionType) {
            PeerInfo.ConnectionType.BITTORRENT -> context.getString(R.string.peer_connection_type_bittorrent)
            PeerInfo.ConnectionType.WEB -> context.getString(R.string.peer_connection_type_web)
            PeerInfo.ConnectionType.UTP -> context.getString(R.string.peer_connection_type_utp)
            else -> ""
        }
        TwoLines(
            context.getString(R.string.peer_connection_type, type),
            context.getString(
                R.string.download_upload_speed_template,
                Formatter.formatFileSize(context, item.downSpeed.toLong()),
                Formatter.formatFileSize(context, item.upSpeed.toLong()),
            ),
            Modifier.padding(top = 4.dp),
        )
        TwoLines(
            context.getString(R.string.peer_client, item.client),
            context.getString(
                R.string.peer_total_download_upload,
                Formatter.formatFileSize(context, item.totalDownload),
                Formatter.formatFileSize(context, item.totalUpload),
            ),
            Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun LogRow(entry: LogEntry, onClick: OnRow<LogEntry>) {
    Column(
        Modifier
            .fillMaxWidth()
            .rowClicks(entry, null, { onClick.onRow(it) })
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Line(entry.tag ?: "", Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.titleSmall, weight = FontWeight.Bold)
        Line(entry.msg ?: "", Modifier.padding(bottom = 8.dp), maxLines = Int.MAX_VALUE)
        Line(entry.timeStampAsString ?: "", style = MaterialTheme.typography.bodySmall)
    }
}
