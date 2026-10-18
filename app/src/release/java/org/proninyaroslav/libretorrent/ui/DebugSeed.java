package org.proninyaroslav.libretorrent.ui;

import android.content.Context;

import org.proninyaroslav.libretorrent.core.model.data.TorrentInfo;
import org.proninyaroslav.libretorrent.ui.detailtorrent.pages.peers.PeerItem;
import org.proninyaroslav.libretorrent.ui.feeditems.FeedItemsListItem;
import org.proninyaroslav.libretorrent.ui.feeds.FeedChannelItem;
import org.proninyaroslav.libretorrent.ui.home.TorrentListItem;

import java.util.Comparator;
import java.util.List;

/* Made-up rows for trying long lists in a debug build; a release shows only what is there. */
public final class DebugSeed {
    private DebugSeed() {
    }

    public static List<TorrentListItem> torrents(Context context, List<TorrentListItem> list,
                                                 Comparator<TorrentInfo> sorting) {
        return list;
    }

    public static List<FeedChannelItem> feeds(Context context, List<FeedChannelItem> list) {
        return list;
    }

    public static List<FeedItemsListItem> feedItems(Context context, List<FeedItemsListItem> list) {
        return list;
    }

    public static List<PeerItem> peers(Context context, List<PeerItem> list) {
        return list;
    }
}
