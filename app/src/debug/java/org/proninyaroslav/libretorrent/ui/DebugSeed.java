package org.proninyaroslav.libretorrent.ui;

import android.content.Context;

import org.proninyaroslav.libretorrent.core.model.data.PeerInfo;
import org.proninyaroslav.libretorrent.core.model.data.TorrentInfo;
import org.proninyaroslav.libretorrent.core.model.data.TorrentStateCode;
import org.proninyaroslav.libretorrent.core.model.data.entity.FeedChannel;
import org.proninyaroslav.libretorrent.core.model.data.entity.FeedItem;
import org.proninyaroslav.libretorrent.ui.detailtorrent.pages.peers.PeerItem;
import org.proninyaroslav.libretorrent.ui.feeditems.FeedItemsListItem;
import org.proninyaroslav.libretorrent.ui.feeds.FeedChannelItem;
import org.proninyaroslav.libretorrent.ui.home.TorrentListItem;
import org.proninyaroslav.libretorrent.core.model.data.Priority;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * Made-up rows for trying long lists in a debug build. Off until a file named "seed" is put in
 * the app's files folder: adb shell run-as <package> touch files/seed
 */
public final class DebugSeed {
    private static final String[] WORDS = {
            "Apple", "Birch", "Cedar", "Dune", "Ember", "Fern", "Glacier", "Heron", "Iris",
            "Juniper", "Kestrel", "Lichen", "Moss", "Nettle", "Otter", "Pine", "Quartz", "Reed",
            "Sorrel", "Thistle", "Umber", "Vale", "Willow", "Xylem", "Yarrow", "Zephyr",
    };
    private static final long DAY = 24L * 60 * 60 * 1000;

    private DebugSeed() {
    }

    private static boolean on(Context context) {
        return new File(context.getFilesDir(), "seed").exists();
    }

    private static String name(int i) {
        return WORDS[i % WORDS.length] + " " + (1 + i / WORDS.length) + " field recordings";
    }

    public static List<TorrentListItem> torrents(Context context, List<TorrentListItem> list,
                                                 Comparator<TorrentInfo> sorting) {
        if (!on(context)) {
            return list;
        }
        var out = new ArrayList<>(list);
        var states = new TorrentStateCode[]{
                TorrentStateCode.DOWNLOADING, TorrentStateCode.SEEDING,
                TorrentStateCode.PAUSED, TorrentStateCode.FINISHED,
        };
        for (int i = 0; i < 60; i++) {
            long total = (i + 1) * 37_000_000L;
            int progress = (i * 17) % 101;
            var info = new TorrentInfo("seed" + i, name(i), states[i % states.length], progress,
                    total * progress / 100, 0, total, i % 3 == 0 ? 120_000 : 0, 4_000,
                    i % 3 == 0 ? 3600 + i * 60L : 0, 1_700_000_000_000L - i * DAY,
                    40 + i, i % 9, null, false, new Priority[0], new ArrayList<>(), false);
            out.add(new TorrentListItem(info));
        }
        out.sort(sorting);
        return out;
    }

    public static List<FeedChannelItem> feeds(Context context, List<FeedChannelItem> list) {
        if (!on(context)) {
            return list;
        }
        var out = new ArrayList<>(list);
        for (int i = 0; i < 50; i++) {
            var channel = new FeedChannel("https://example.org/" + i + "/feed.xml", name(i),
                    1_700_000_000_000L - i * DAY, false, null, false, null);
            channel.id = 100_000 + i;
            out.add(new FeedChannelItem(channel, i % 4 == 0 ? i % 7 + 1 : 0));
        }
        Collections.sort(out);
        return out;
    }

    public static List<FeedItemsListItem> feedItems(Context context, List<FeedItemsListItem> list) {
        if (!on(context)) {
            return list;
        }
        var out = new ArrayList<>(list);
        for (int i = 0; i < 70; i++) {
            var item = new FeedItem("seed" + i, 100_000, "magnet:?xt=urn:btih:seed" + i,
                    "https://example.org/" + i, name(i), 1_700_000_000_000L - i * DAY / 3);
            item.read = i % 3 != 0;
            out.add(new FeedItemsListItem(item));
        }
        Collections.sort(out);
        return out;
    }

    public static List<PeerItem> peers(Context context, List<PeerItem> list) {
        if (!on(context)) {
            return list;
        }
        var out = new ArrayList<>(list);
        for (int i = 0; i < 45; i++) {
            out.add(new PeerItem(new PeerInfo("192.0.2." + (i + 1), "Transmission 4." + (i % 7),
                    i * 3_000_000L, i * 900_000L, (i % 10) / 10.0, i % 3, 51413 + i,
                    (i * 23) % 101, i * 1200, i * 300)));
        }
        Collections.sort(out);
        return out;
    }
}
