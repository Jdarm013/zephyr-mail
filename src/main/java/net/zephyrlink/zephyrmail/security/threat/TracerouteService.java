package net.zephyrlink.zephyrmail.security.threat;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class TracerouteService {

    public List<String> trace(String ipAddress) {
        List<String> hops = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "traceroute", "-m", "15", "-w", "2", ipAddress
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    hops.add(trimmed);
                }
            }

            process.waitFor();

        } catch (Exception e) {
            hops.add("Traceroute failed: " + e.getMessage());
        }

        return hops;
    }

    public boolean isReachable(String ipAddress) {
        return !trace(ipAddress).isEmpty();
    }
}