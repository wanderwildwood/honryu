package org.proninyaroslav.libretorrent.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.proninyaroslav.libretorrent.core.settings.SessionSettings.ProxyType;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class FetchRouteTest {

    @Test
    public void noProxyFetchesDirectly() {
        FetchRoute r = FetchRoute.of(ProxyType.NONE, null, 0, true);
        assertEquals(FetchRoute.Kind.DIRECT, r.kind);
        assertNull(r.proxy);
    }

    @Test
    public void socks5GoesThroughTheProxyAndLeavesTheNameUnresolved() {
        FetchRoute r = FetchRoute.of(ProxyType.SOCKS5, " proxy.invalid ", 9050, false);
        assertEquals(FetchRoute.Kind.PROXY, r.kind);
        assertEquals(Proxy.Type.SOCKS, r.proxy.type());
        InetSocketAddress at = (InetSocketAddress) r.proxy.address();
        assertEquals("proxy.invalid", at.getHostString());
        assertEquals(true, at.isUnresolved());
        assertEquals(9050, at.getPort());
    }

    @Test
    public void httpGoesThroughTheProxy() {
        FetchRoute r = FetchRoute.of(ProxyType.HTTP, "10.0.0.2", 8080, true);
        assertEquals(FetchRoute.Kind.PROXY, r.kind);
        assertEquals(Proxy.Type.HTTP, r.proxy.type());
    }

    @Test
    public void socks4IsRefusedUnderTheKillSwitchAndDirectWithout() {
        assertEquals(FetchRoute.Kind.REFUSE, FetchRoute.of(ProxyType.SOCKS4, "10.0.0.2", 1080, true).kind);
        assertEquals(FetchRoute.Kind.DIRECT, FetchRoute.of(ProxyType.SOCKS4, "10.0.0.2", 1080, false).kind);
    }

    @Test
    public void aProxyWithoutAnAddressOrPortIsRefusedUnderTheKillSwitch() {
        assertEquals(FetchRoute.Kind.REFUSE, FetchRoute.of(ProxyType.SOCKS5, "", 1080, true).kind);
        assertEquals(FetchRoute.Kind.REFUSE, FetchRoute.of(ProxyType.HTTP, "10.0.0.2", 0, true).kind);
        assertEquals(FetchRoute.Kind.DIRECT, FetchRoute.of(ProxyType.HTTP, null, 8080, false).kind);
    }
}
