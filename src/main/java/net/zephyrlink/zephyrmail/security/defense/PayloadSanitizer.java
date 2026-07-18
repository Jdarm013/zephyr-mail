package net.zephyrlink.zephyrmail.security.defense;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class PayloadSanitizer extends OncePerRequestFilter {

    // MAX PAYLOAD
    private static final long MAX_PAYLOAD_BYTES = 25 * 1024 * 1024;

    // OWASP POLICY — allows basic formatting only, strips all scripts/events/unsafe attributes
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "b", "i", "u", "strong", "em", "ul", "ol", "li", "blockquote")
            .allowAttributes("href").onElements("a")
            .allowUrlProtocols("https", "mailto")
            .toFactory();

    // Only free-text content that is later rendered as real HTML (the outbound/draft email
    // body) needs HTML sanitization. Structured identifiers — email addresses, passwords,
    // aliases, recipients — must stay byte-exact, since Thymeleaf already auto-escapes
    // everything else it renders. Running those through an HTML sanitizer corrupts any value
    // containing @, &, <, >, ", ' (e.g. "user@host" becomes "user&#64;host"), which silently
    // breaks login, contacts, aliases, and password changes.
    private static final Set<String> HTML_SANITIZED_PARAMS = Set.of("body");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // PAYLOAD SIZE CHECK
        long contentLength = request.getContentLengthLong();
        if (contentLength > MAX_PAYLOAD_BYTES) {
            System.out.println("[PERIMETER DEFENSE] Payload rejected — exceeds 25MB limit.");
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "Payload exceeds maximum allowed size.");
            return;
        }

        // XSS SCRUB — parameters only, never headers (headers contain session/auth tokens)
        filterChain.doFilter(new XssRequestWrapper(request), response);
    }

    // XSS WRAPPER — sanitizes only known HTML content fields, leaves everything else untouched
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter);
            if (values == null || !HTML_SANITIZED_PARAMS.contains(parameter)) return values;
            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = sanitize(values[i]);
            }
            return sanitized;
        }

        @Override
        public String getParameter(String parameter) {
            String value = super.getParameter(parameter);
            if (!HTML_SANITIZED_PARAMS.contains(parameter)) return value;
            return sanitize(value);
        }

        private String sanitize(String value) {
            if (value == null) return null;
            return POLICY.sanitize(value);
        }
    }
}