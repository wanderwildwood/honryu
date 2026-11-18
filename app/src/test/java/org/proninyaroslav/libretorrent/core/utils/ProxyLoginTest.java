package org.proninyaroslav.libretorrent.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

public class ProxyLoginTest {

    private static PasswordAuthentication ask(String host, String protocol,
                                              Authenticator.RequestorType type) throws MalformedURLException {
        return Authenticator.requestPasswordAuthentication(host, null, 8080, protocol,
                "realm", "Basic", new URL("https://feeds.example.org/rss"), type);
    }

    @Test
    public void theProxyGetsItsLoginAsSocksOrAsAnHttpProxy() throws MalformedURLException {
        ProxyLogin.set("proxy.example.net", "ada", "not-a-real-password");

        PasswordAuthentication socks = ask("proxy.example.net", "SOCKS5", Authenticator.RequestorType.SERVER);
        assertEquals("ada", socks.getUserName());

        PasswordAuthentication http = ask("PROXY.example.net", "https", Authenticator.RequestorType.PROXY);
        assertEquals("ada", http.getUserName());
        assertEquals("not-a-real-password", new String(http.getPassword()));
    }

    @Test
    public void nobodyElseGetsIt() throws MalformedURLException {
        ProxyLogin.set("proxy.example.net", "ada", "not-a-real-password");

        assertNull(ask("feeds.example.org", "https", Authenticator.RequestorType.PROXY));
        assertNull(ask("feeds.example.org", "SOCKS5", Authenticator.RequestorType.SERVER));
        /* The site itself asking, even from the proxy's name, is not the proxy asking */
        assertNull(ask("proxy.example.net", "https", Authenticator.RequestorType.SERVER));
    }
}
