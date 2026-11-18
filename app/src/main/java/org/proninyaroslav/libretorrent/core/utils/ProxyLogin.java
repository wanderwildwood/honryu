package org.proninyaroslav.libretorrent.core.utils;

import androidx.annotation.NonNull;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/*
 * The proxy's login. Java asks for it through the process-wide Authenticator: a SOCKS5 proxy
 * when the connection opens, an HTTP proxy when it answers 407 (for an https address that is
 * the tunnel's CONNECT, which carries no header set on the request). This one answers only the
 * proxy's own host, asking as a SOCKS server or as a proxy, and nothing else.
 */
final class ProxyLogin extends Authenticator {
    private static volatile String host;
    private static volatile PasswordAuthentication login;
    private static boolean installed;

    private ProxyLogin() {
    }

    static synchronized void set(@NonNull String proxyHost, @NonNull String user, @NonNull String password) {
        host = proxyHost;
        login = new PasswordAuthentication(user, password.toCharArray());
        if (!installed) {
            Authenticator.setDefault(new ProxyLogin());
            installed = true;
        }
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String protocol = getRequestingProtocol();
        String asking = getRequestingHost();
        if (asking == null || !asking.equalsIgnoreCase(host)) {
            return null;
        }
        boolean socks = protocol != null && protocol.toUpperCase().startsWith("SOCKS");
        boolean proxy = getRequestorType() == RequestorType.PROXY;
        return socks || proxy ? login : null;
    }
}
