# LathaBulk v3.23.20 QA Checklist

## Ledger settled-phone regression

- [ ] From a phone present in the Ledger map, send `ledger` once and confirm only the PDF is sent.
- [ ] Confirm no early “mobile number not matched” text appears before the PDF.
- [ ] Confirm WhatsApp notification reposts/updates do not start a second reply.
- [ ] From a phone absent from the Ledger map, confirm exactly one LATHAEPS contact-help reply after identity verification.

## Automated static checks passed

- All 11 Java source files parse successfully.
- AndroidManifest and all XML resources parse successfully.
- GitHub Actions workflow YAML parses successfully.
- Package/namespace remains `com.lathaeps.lathabulk`.
- `versionCode 77`, `versionName 3.23.20`.
- No signing key or keystore is included; the Play AAB workflow reads signing values only from GitHub Secrets.
- No Auto Reply cooldown field/block remains.

## Flow checks completed

- Ledger phone match -> customer `ledger_uri` -> permanent FileProvider PDF -> WhatsApp queue.
- Editing a Ledger customer preserves PDF URI, filename, balance and pages.
- Catalog keyword/category -> every stored PDF/image -> sequential one-file queue.
- Auto Reply text uses notification RemoteInput; image rules use the attachment queue.
- Duplicate repost of the same notification event is ignored; a new repeated keyword event is accepted.
- Attachment queue becomes active only after the real share task is prepared.
- WhatsApp direct chat, `Send to…` recipient search, contact selection, preview and final Send are handled.
- Recipient selection requires an exact phone/name identity; arbitrary first-result selection is disabled.
- Accessibility reconnect, task timeout, cancel and queue cleanup paths are present.

## Required phone permissions/settings

- Notification Access: ON
- Latha Auto Send Accessibility: ON
- Contacts permission: Allow
- For a newly installed APK, toggle Accessibility OFF and ON once before testing.
# v3.23.18 Ledger exact-phone-only matching

- From a saved WhatsApp contact, send exactly `ledger`.
- Confirm the sender's verified last 10 digits exactly equal the mapped Ledger phone before its PDF opens.
- Confirm a missing or different phone sends only the LATHAEPS assistance reply; no PDF is attached.
- Confirm Catalog and normal Auto Reply still behave exactly as before.
