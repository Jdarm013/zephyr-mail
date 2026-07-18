package net.zephyrlink.zephyrmail.security.threat;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ThreatGrader {

    private static final List<String> TRUSTED_DOMAINS = Arrays.asList(
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
            "paypal.com", "apple.com", "amazon.com", "microsoft.com",
            "google.com", "facebook.com", "twitter.com", "instagram.com",
            "netflix.com", "bank.com", "chase.com", "wellsfargo.com"
    );

    private static final int SUSPICIOUS_THRESHOLD = 2;

    public boolean isSuspicious(String senderEmail) {
        String domain = extractDomain(senderEmail);
        if (domain == null) return false;

        for (String trusted : TRUSTED_DOMAINS) {
            int distance = levenshtein(domain, trusted);
            if (distance > 0 && distance <= SUSPICIOUS_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    public String getMatchedDomain(String senderEmail) {
        String domain = extractDomain(senderEmail);
        if (domain == null) return null;

        for (String trusted : TRUSTED_DOMAINS) {
            int distance = levenshtein(domain, trusted);
            if (distance > 0 && distance <= SUSPICIOUS_THRESHOLD) {
                return trusted;
            }
        }
        return null;
    }

    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) return null;
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}