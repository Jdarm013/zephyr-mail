# ZephyrMail

A self-hosted mail server I designed, built, and now run in production for my own domain, **zephyrlink.net** — real internet email, sent and received, not a localhost demo. Built solo with Spring Boot, and hardened through a deliberate security self-audit rather than assumed to be safe.

## What it actually does

- Runs its own embedded SMTP server (SubEthaSMTP) and a full webmail client — inbox, compose, contacts, folders, attachments.
- Sends and receives real internet mail: outbound through a Resend relay, inbound through a signature-verified Resend webhook, with SPF/DKIM/DMARC configured on the domain's real DNS.
- Deployed on physical hardware I administer myself — systemd-managed, exposed to the internet through a Cloudflare Tunnel (no port-forwarding, no exposed home IP), TLS terminated at the Cloudflare edge.

## Security features

- **Argon2id** password hashing.
- **Proof-of-work gated login** — the client has to compute a SHA-256 hash meeting a difficulty prefix (via the Web Crypto API) before a login POST is even accepted, raising the cost of automated credential-stuffing.
- **Cloudflare Turnstile** as a second bot-resistance layer on top of the PoW challenge.
- **Dual-vector brute-force lockout** — tracked per-account and per-IP independently, so an attacker can't spread guesses across accounts from one IP or hammer one account from many.
- **CSRF protection** via Spring Security's cookie-based SPA mode.
- **Payload sanitization** on the message body to prevent stored XSS, without mangling legitimate `@`/`&`/`<`/`>` characters in addresses or passwords elsewhere in the app.
- **Outbound data-leak scanning** — every message body is checked for SSNs, AWS access keys, PEM private keys, and credit card numbers before it's allowed to send, with a visible warning if something matches.
- **Phishing/typosquat detection** — incoming sender domains are checked against a trusted-domain list using Levenshtein distance, catching lookalikes like `paypa1.com`.
- **Panic button** — instantly invalidates every active session across the app and writes a `BREACH_ATTEMPT` audit entry, for a "something is wrong, kill everything now" scenario.
- **Break-glass emergency access** — a one-time token, printed once at startup, that can restore admin access if normal login is unavailable.
- **Full audit log** and an admin **SOC (Security Operations Center) dashboard** for reviewing login attempts, threat scores, and flagged activity.

## Mail features

- **Shadow-route aliases** — disposable/masked addresses that forward to your real inbox, resolved through a cached hot path for SMTP accept/deliver.
- **Mail flow rules** — auto-tag, auto-forward, mark-as-spam, or delete incoming mail by rule.
- **Outbox queue with a 10-second undo window** before anything actually sends, bounded retry with a terminal failed state, and a visible in-inbox banner if a message ultimately can't be delivered.
- **Attachment vault**, contacts with autocomplete, and a PWA-installable client with its own icon set.

## Architecture

Domain-driven package layout rather than a generic MVC dump:

```
config/            – app-wide configuration, proxy/cache setup
user/              – accounts and authentication identity
contact/           – contacts and autocomplete
alias/             – shadow-route aliases
mail/
  inbound/         – Resend webhook ingestion
  outbound/        – SMTP dispatch, outbox queue
  flow/            – mail flow rule engine
  web/             – compose/inbox/draft controllers
security/
  auth/            – login, lockout, session events
  defense/         – PoW, Turnstile, sanitizer, panic button, emergency access
  threat/          – phishing detection, data-leak radar, chaos/probe tooling
  audit/           – audit log
admin/             – admin dashboard, SOC dashboard
web/               – top-level web controllers
```

## Built by auditing my own work, honestly

Before I called this "done," I went through the app looking for real problems instead of assuming it was secure because I wrote it. That audit found and fixed, among others:

- A **hardcoded live production password** committed to source control — replaced with a randomly generated credential that's printed once at first boot and never stored in code.
- A **silent CORS misconfiguration** that was dropping every real-browser login attempt with zero server-side log trace, tracked down via Spring Security TRACE-level filter-chain logging.
- An **IP-lockout bypass** — any successful login from an IP was incorrectly resetting the failed-attempt counter for that entire IP, undermining the brute-force protection.
- A **filter-ordering bug** that let the proof-of-work and Turnstile checks be skipped entirely, because they were registered later in the chain than Spring Security's own internal filter position.
- An **unbounded retry loop** in outbound mail dispatch — a single mistyped address was silently retried every 5 seconds for over an hour before I traced it through the logs and added a bounded-retry, dead-letter design.
- An **IDOR** in the send-queue cancel endpoint that let any authenticated user cancel *any other user's* pending outbound mail, just by guessing a numeric ID.
- A **design flaw** where the autosave-while-typing feature was quietly leaking a permanent row into the real send queue on every tick, scheduled a year in the future as a workaround — since fixed to a proper draft state that upserts a single row and cleans up on send.

## Tech stack

Spring Boot 4 · Spring Security 7 · Spring Data JPA / Hibernate · H2 (file-mode) · Thymeleaf · SubEthaSMTP · Argon2id · Caffeine cache · Cloudflare (Turnstile, Tunnel, DNS) · Resend · systemd

## Known limitations

Being direct about what isn't here yet:

- No automated test suite beyond the default Spring Boot context-load smoke test. Verification so far has been manual code review, live log inspection, and running as my actual production mail server — not CI-enforced coverage.
- H2 file-based storage — appropriate for a personal-scale deployment, not a multi-tenant one.
- Single-instance deployment with no high-availability failover.

## Why I built this

I wanted to understand what a real mail server actually has to get right — SMTP handling, deliverability (SPF/DKIM/DMARC), and the kind of security surface a login form, a compose box, and an inbound webhook all quietly expose — by building and running one for real, not by following a tutorial. The audit list above isn't a hypothetical exercise; those were real bugs in my own running code that I found and fixed.
