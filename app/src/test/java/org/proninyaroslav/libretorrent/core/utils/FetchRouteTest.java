package org.proninyaroslav.libretorrent.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    public void socks5IsRefusedUnderTheKillSwitch() {
        FetchRoute r = FetchRoute.of(ProxyType.SOCKS5, "10.0.0.2", 9050, true);
        assertEquals(FetchRoute.Kind.REFUSE, r.kind);
        assertNull(r.proxy);
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

    @Test
    public void throughAProxyLocalNamesAreRefusedByTheirForm() {
        assertTrue(FetchRoute.isLocalName("localhost"));
        assertTrue(FetchRoute.isLocalName("LOCALHOST."));
        assertTrue(FetchRoute.isLocalName("printer"));
        assertTrue(FetchRoute.isLocalName("feeds.localhost"));
        assertTrue(FetchRoute.isLocalName("nas.local"));
        assertTrue(FetchRoute.isLocalName("router.lan"));
        assertTrue(FetchRoute.isLocalName("box.home.arpa"));
        assertTrue(FetchRoute.isLocalName("wiki.corp.internal"));
    }

    @Test
    public void throughAProxyOddAddressFormsAreRefused() {
        assertTrue(FetchRoute.isLocalName("2130706433"));
        assertTrue(FetchRoute.isLocalName("0x7f000001"));
        assertTrue(FetchRoute.isLocalName("0177.0.0.1"));
        assertTrue(FetchRoute.isLocalName("127.1"));
        assertTrue(FetchRoute.isLocalName("0x7f.0.0.1"));
        assertFalse(FetchRoute.isDottedQuad("0177.0.0.1"));
        assertTrue(FetchRoute.isDottedQuad("127.0.0.1"));
        assertTrue(FetchRoute.isDottedQuad("203.0.113.7"));
    }

    @Test
    public void anAddressWithATrailingDotIsCheckedAsAnAddress() {
        assertEquals("127.0.0.1", FetchRoute.bareHost("127.0.0.1."));
        assertEquals("10.0.0.1", FetchRoute.bareHost("10.0.0.1.."));
        assertTrue(FetchRoute.isDottedQuad(FetchRoute.bareHost("127.0.0.1.")));
        assertTrue(FetchRoute.isDottedQuad(FetchRoute.bareHost("10.0.0.1.")));
        assertEquals("::1", FetchRoute.bareHost("[::1]"));
        assertEquals("feeds.example.org", FetchRoute.bareHost("feeds.example.org."));
        assertTrue(FetchRoute.isLocalName(FetchRoute.bareHost(".")));
    }

    @Test
    public void throughAProxyPublicNamesPass() {
        assertFalse(FetchRoute.isLocalName("feeds.example.org"));
        assertFalse(FetchRoute.isLocalName("example.plan"));
        assertFalse(FetchRoute.isLocalName("203.0.113.7"));
        assertFalse(FetchRoute.isLocalName("1.example.com"));
    }
}
