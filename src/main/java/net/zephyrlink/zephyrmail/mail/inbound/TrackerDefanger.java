package net.zephyrlink.zephyrmail.mail.inbound;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TrackerDefanger {

    private static final List<String> KNOWN_TRACKER_DOMAINS = Arrays.asList(
            "track.", "pixel.", "open.", "click.", "analytics.",
            "mailchimp.com", "sendgrid.net", "mandrillapp.com",
            "constantcontact.com", "hubspot.com", "marketo.com"
    );

    public boolean containsTracker(String htmlBody) {
        if (htmlBody == null || htmlBody.isEmpty()) return false;

        Document doc = Jsoup.parse(htmlBody);
        Elements images = doc.select("img");

        for (Element img : images) {
            if (isTrackingPixel(img)) {
                return true;
            }
        }
        return false;
    }

    public String defang(String htmlBody) {
        if (htmlBody == null || htmlBody.isEmpty()) return htmlBody;

        Document doc = Jsoup.parse(htmlBody);
        Elements images = doc.select("img");

        for (Element img : images) {
            if (isTrackingPixel(img)) {
                img.remove();
            }
        }
        return doc.html();
    }

    private boolean isTrackingPixel(Element img) {
        String width = img.attr("width");
        String height = img.attr("height");
        String src = img.attr("src").toLowerCase();

        if (("1".equals(width) && "1".equals(height)) ||
                ("0".equals(width) && "0".equals(height))) {
            return true;
        }

        for (String tracker : KNOWN_TRACKER_DOMAINS) {
            if (src.contains(tracker)) {
                return true;
            }
        }
        return false;
    }
}