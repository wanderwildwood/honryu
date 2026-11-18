package org.proninyaroslav.libretorrent.core.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.proninyaroslav.libretorrent.core.settings.SessionSettings;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Locale;
import java.util.regex.Pattern;

/*
 * How a feed or a .torrent link is fetched, by the same proxy settings the torrents use:
 * through the proxy when one is set and Java can speak to it (HTTP, SOCKS5), directly when none
 * is set, and not at all when the proxy is required for all connections but cannot carry this
 * one (SOCKS4, or an address or port that is missing). Through a proxy the site's name is left
 * for the proxy to look up, so no DNS query leaves the phone.
 */
public final class FetchRoute {

    public enum Kind { DIRECT, PROXY, REFUSE }

    @NonNull
    public final Kind kind;
    @Nullable
    public final Proxy proxy;

    private FetchRoute(@NonNull Kind kind, @Nullable Proxy proxy) {
        this.kind = kind;
        this.proxy = proxy;
    }

    @NonNull
    public static FetchRoute of(@NonNull SessionSettings.ProxyType type,
                                @Nullable String address,
                                int port,
                                boolean requireAll) {
        if (type == SessionSettings.ProxyType.NONE) {
            return new FetchRoute(Kind.DIRECT, null);
        }
        Proxy.Type javaType = switch (type) {
            case HTTP -> Proxy.Type.HTTP;
            case SOCKS5 -> Proxy.Type.SOCKS;
            default -> null;
        };
        boolean usable = javaType != null
                && address != null && !address.trim().isEmpty()
                && port > 0 && port <= 65535;
        if (!usable) {
            return new FetchRoute(requireAll ? Kind.REFUSE : Kind.DIRECT, null);
        }
        Proxy proxy = new Proxy(javaType, InetSocketAddress.createUnresolved(address.trim(), port));
        return new FetchRoute(Kind.PROXY, proxy);
    }

    private static final Pattern DOTTED_QUAD =
            Pattern.compile("(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])(\\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}");
    private static final Pattern NUMERIC_LABEL = Pattern.compile("[0-9]+|0x[0-9a-f]*");
    private static final String[] LOCAL_SUFFIXES = {
            ".localhost", ".local", ".lan", ".home.arpa", ".internal"
    };

    /* An IPv4 address in the plain dotted form, which is checked as an address */
    public static boolean isDottedQuad(@NonNull String host) {
        return DOTTED_QUAD.matcher(host).matches();
    }

    /*
     * Through a proxy the name is not looked up here, so it is judged by its form alone: a
     * name with no dot (localhost, a LAN machine, an address written as one number), a name
     * under a suffix kept for local networks, and an address written in any form but the
     * plain dotted one (0x7f000001, 0177.0.0.1, 127.1) are refused. A public name that points
     * at a private address cannot be told from here; the proxy looks it up.
     */
    public static boolean isLocalName(@NonNull String host) {
        String name = host.toLowerCase(Locale.ROOT);
        while (name.endsWith(".")) {
            name = name.substring(0, name.length() - 1);
        }
        if (name.isEmpty() || !name.contains(".")) {
            return true;
        }
        for (String suffix : LOCAL_SUFFIXES) {
            if (name.endsWith(suffix)) {
                return true;
            }
        }
        if (isDottedQuad(name)) {
            return false;
        }
        String last = name.substring(name.lastIndexOf('.') + 1);
        return NUMERIC_LABEL.matcher(last).matches();
    }
}
