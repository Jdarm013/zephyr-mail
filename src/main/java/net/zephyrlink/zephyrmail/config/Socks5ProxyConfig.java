package net.zephyrlink.zephyrmail.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Socks5ProxyConfig {

    private final String proxyHost;
    private final int proxyPort;

    public Socks5ProxyConfig(
            @org.springframework.beans.factory.annotation.Value("${socks5.proxy.host}") String proxyHost,
            @org.springframework.beans.factory.annotation.Value("${socks5.proxy.port}") int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public String getProxyHost() { return proxyHost; }
    public int getProxyPort() { return proxyPort; }
}