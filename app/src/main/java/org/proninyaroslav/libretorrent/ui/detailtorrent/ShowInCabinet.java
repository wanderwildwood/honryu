package org.proninyaroslav.libretorrent.ui.detailtorrent;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Pattern;

/*
 * "Show in Cabinet": asks Cabinet (com.wanderwildwood.tana) to open the folder that holds
 * a torrent, with the torrent's own file or folder found and selected. Cabinet takes a plain
 * path, so a torrent saved through a SAF folder is shown only when that folder names a place
 * on disk.
 */
public final class ShowInCabinet {
    public static final String ACTION = "com.wanderwildwood.tana.action.SHOW_FILE";
    public static final String PACKAGE = "com.wanderwildwood.tana";
    public static final String EXTRA_PATH = "path";

    static final String EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    static final String DOWNLOADS = "com.android.providers.downloads.documents";
    private static final String PRIMARY_STORAGE = "/storage/emulated/0";
    private static final Pattern CARD_VOLUME = Pattern.compile("[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}");

    private ShowInCabinet() {
    }

    @NonNull
    public static Intent intent(@NonNull String path) {
        return new Intent(ACTION).setPackage(PACKAGE).putExtra(EXTRA_PATH, path);
    }

    public static boolean isInstalled(@NonNull Context context) {
        return intent("/").resolveActivity(context.getPackageManager()) != null;
    }

    /*
     * The plain path of the torrent's top-level file or folder: the download folder plus
     * the first part of any file's path in the torrent. Null when either is unknown.
     */
    @Nullable
    public static String path(@NonNull Uri downloadDir, @Nullable String fileInTorrent) {
        String dir = plainDir(downloadDir);
        String top = topLevel(fileInTorrent);
        if (dir == null || top == null) {
            return null;
        }
        return join(dir, top);
    }

    @Nullable
    static String plainDir(@NonNull Uri dir) {
        String scheme = dir.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            String path = dir.getPath();
            return (path == null || !path.startsWith("/")) ? null : path;
        }
        if (!ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            return null;
        }
        String id;
        try {
            id = DocumentsContract.isTreeUri(dir)
                    ? DocumentsContract.getTreeDocumentId(dir)
                    : DocumentsContract.getDocumentId(dir);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return fromDocumentId(dir.getAuthority(), id);
    }

    /*
     * primary:Download/Torrents -> /storage/emulated/0/Download/Torrents,
     * 1A2B-3C4D:Torrents -> /storage/1A2B-3C4D/Torrents, the Downloads provider's
     * raw:/storage/... -> /storage/..., anything else -> null.
     */
    @Nullable
    static String fromDocumentId(@Nullable String authority, @Nullable String id) {
        if (authority == null || id == null) {
            return null;
        }
        if (EXTERNAL_STORAGE.equals(authority)) {
            int colon = id.indexOf(':');
            if (colon < 0) {
                return null;
            }
            String volume = id.substring(0, colon);
            String below = id.substring(colon + 1);
            String root;
            if (volume.equals("primary")) {
                root = PRIMARY_STORAGE;
            } else if (CARD_VOLUME.matcher(volume).matches()) {
                root = "/storage/" + volume;
            } else {
                return null;
            }
            return below.isEmpty() ? root : join(root, below);
        }
        if (DOWNLOADS.equals(authority)) {
            if (id.equals("downloads")) {
                return PRIMARY_STORAGE + "/Download";
            }
            if (id.startsWith("raw:/")) {
                return trimEnd(id.substring("raw:".length()));
            }
        }
        return null;
    }

    /* "Album/Disc 1/01.flac" -> "Album", "film.mkv" -> "film.mkv" */
    @Nullable
    static String topLevel(@Nullable String fileInTorrent) {
        if (fileInTorrent == null) {
            return null;
        }
        String path = fileInTorrent;
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        int slash = path.indexOf('/');
        String top = slash < 0 ? path : path.substring(0, slash);
        return top.isEmpty() ? null : top;
    }

    private static String join(String dir, String name) {
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        String parent = trimEnd(dir);
        return parent.equals("/") ? "/" + name : parent + "/" + name;
    }

    private static String trimEnd(String path) {
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
