# LathaBulk v3.23.30 QA Checklist

## v3.23.30 Supabase subscription checks

- Run `SUPABASE_SUBSCRIPTION_SETUP.sql` and privately save the generated admin password.
- First launch with internet creates one `app_subscriptions` row and shows 12 trial days.
- Clear app data/reinstall; the same Device ID keeps the original trial start time.
- Admin panel activates Yearly and user sees it after **REFRESH ONLINE STATUS**.
- Admin panel activates Lifetime Free and can block/revoke the same device.
- Wrong admin password is rejected; no password is stored inside the APK.
- After 48 hours without verification, protected features require an online refresh.

## v3.23.29 Price List Manager checks

- Section MANAGE opens Edit, Add more pictures, Add more PDFs and Delete complete section.
- Individual file menu opens Edit details, Replace file and Delete file.
- Add Price Item is visible and saves a new manual price row.
- Existing manual price items keep Edit and Delete actions.
- Complete price-list share button is connected.
- MainActivity contains no `BuildConfig` reference; updater uses `appVersion()` so Java compilation does not depend on BuildConfig generation.

## v3.23.28 voice checks

- Main Voice Search opens Business Files, Catalog, Auto Reply, Payment Reminder, Recipient Lists and Settings.
- `backup`, `restore` and `check update` commands open the expected safe action.
- `ledger Ravi dhundo` opens Ledger Customers pre-filtered for Ravi.
- `catalog wires` opens Catalog pre-filtered for wires.
- `Polycab share karo` always asks for confirmation before opening Android share.

## v3.23.27 checks

- Main window still shows Local Backup and Restore Backup.
- Settings does not duplicate Local Backup or Restore Backup; Drive Backup remains.
- Business Files opens full-screen with the same green palette as Auto Reply.
- Price List Manager accepts multiple Gallery images and multiple PDFs per section.
- App Updates can be turned ON/OFF and CHECK NOW runs manually.
- A newer GitHub Release creates UPDATE NOW and LATER notification actions.

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
- `versionCode 86`, `versionName 3.23.29`.

## Multiple Price Media Section

- Confirm `+ ADD ITEM` and `SHARE PRICE LIST` are not shown at the top.
- Confirm `PICTURES FROM PHONE GALLERY` opens the phone gallery and accepts multiple pictures.
- Confirm `PDF FROM FILES` opens the file picker and accepts multiple PDFs.
- Add one brand/category and select multiple images and PDFs together.
- Confirm all selected files appear inside one section with correct image/PDF counts.
- Search the brand using typed search and main-window voice search.
- Tap `SHARE ALL` and confirm every section file is included in the Android/WhatsApp share sheet.
- Open, share and delete one individual source without affecting the other files.

## Price Sources + Main Voice Search

- Main window microphone starts Price List voice search without opening Business Files first.
- Price List Manager accepts PDF and image source files and stores safe app-owned copies.
- PDF text, file name, category and keywords participate in local smart search.
- Matching source files open/share correctly; delete removes only the selected price source.
- Ledger, Catalog and Auto Reply regression checks remain unchanged and pass independently.

## AI Smart + Voice Search

- Microphone button opens Android speech recognition from Price List Manager.
- Saying `Mylinc switch` filters the saved list without typing.
- `my link`, `mylink` and plural product words resolve through smart local matching.
- Closing the Price List screen clears the temporary voice-search field safely.

## Price List Manager

- Business Files → Price List Manager opens without changing Ledger settings.
- Add/edit/delete and search work for item, category, rate, unit, discount and GST.
- PDF and WhatsApp-text sharing include all saved categories and items.
- Local/Drive backup and restore preserve the saved price list.
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
