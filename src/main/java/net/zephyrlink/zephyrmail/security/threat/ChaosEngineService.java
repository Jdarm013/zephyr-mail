package net.zephyrlink.zephyrmail.security.threat;

import net.zephyrlink.zephyrmail.config.Socks5ProxyConfig;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChaosEngineService {

    private final TracerouteService tracerouteService;
    private final ThreatGrader threatGrader;
    private final Socks5ProxyConfig proxyConfig;

    public ChaosEngineService(TracerouteService tracerouteService,
                              ThreatGrader threatGrader,
                              Socks5ProxyConfig proxyConfig) {
        this.tracerouteService = tracerouteService;
        this.threatGrader = threatGrader;
        this.proxyConfig = proxyConfig;
    }

    public Map<String, Object> investigate(String senderEmail, String senderIp) {
        Map<String, Object> report = new HashMap<>();

        report.put("senderEmail", senderEmail);
        report.put("senderIp", senderIp);
        report.put("isSuspiciousDomain", threatGrader.isSuspicious(senderEmail));
        report.put("matchedTrustedDomain", threatGrader.getMatchedDomain(senderEmail));

        List<String> hops = tracerouteService.trace(senderIp);
        report.put("tracerouteHops", hops);
        report.put("hopCount", hops.size());

        return report;
    }

    public boolean probeViaSocks5(String targetUrl) {
        try {
            Proxy proxy = new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(proxyConfig.getProxyHost(), proxyConfig.getProxyPort())
            );
            URL url = new URL(targetUrl);
            URLConnection connection = url.openConnection(proxy);
            connection.setConnectTimeout(5000);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}