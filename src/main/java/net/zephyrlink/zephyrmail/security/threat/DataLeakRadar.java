package net.zephyrlink.zephyrmail.security.threat;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataLeakRadar {

    private static final Pattern SSN_PATTERN =
            Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    private static final Pattern AWS_KEY_PATTERN =
            Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b");

    private static final Pattern PRIVATE_KEY_PATTERN =
            Pattern.compile("-----BEGIN (RSA |EC )?PRIVATE KEY-----");

    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b(?:\\d{4}[\\s-]?){3}\\d{4}\\b");

    public List<String> scan(String content) {
        List<String> findings = new ArrayList<>();

        if (matches(SSN_PATTERN, content)) {
            findings.add("Social Security Number detected");
        }
        if (matches(AWS_KEY_PATTERN, content)) {
            findings.add("AWS Access Key detected");
        }
        if (matches(PRIVATE_KEY_PATTERN, content)) {
            findings.add("Private key detected");
        }
        if (matches(CREDIT_CARD_PATTERN, content)) {
            findings.add("Credit card number detected");
        }

        return findings;
    }

    public boolean isSafe(String content) {
        return scan(content).isEmpty();
    }

    private boolean matches(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }
}