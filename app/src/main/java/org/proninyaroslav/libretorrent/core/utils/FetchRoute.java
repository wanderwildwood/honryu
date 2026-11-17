package org.proninyaroslav.libretorrent.core.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.proninyaroslav.libretorrent.core.settings.SessionSettings;

import java.net.InetSocketAddress;
import java.net.Proxy;

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
}
