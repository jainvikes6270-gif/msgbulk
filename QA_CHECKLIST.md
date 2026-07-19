# LathaBulk v3.23.46 QA Checklist

## v3.23.46 Saved Ledger List & Schedule QA

- Select a small set of Ledger parties and tap **SAVE SMALL LIST**.
- Clear selection, tap **OPEN SAVED LIST**, and confirm the same parties are selected.
- Choose **WEEKLY / MONTHLY**, set a future date/time, and confirm the scheduled notification.
- Verify the scheduled run sends each saved party its latest mapped Ledger PDF.
- Verify the next Weekly/Monthly alarm is created and restored after reboot/app update.
- Verify missing PDFs and Do Not Send parties are skipped.

## v3.23.45 Multiple Party Ledger Send QA

- Import/prepare a Master Ledger and open **Business Files → Manage Ledger Customers**.
- Search parties, select multiple checkboxes, and verify the selected-party count.
- Check **SELECT ALL**, **CLEAR**, and selection persistence while changing the search text.
- Tap **SEND SELECTED LEDGERS**, confirm the count, and verify each WhatsApp recipient receives only their own mapped PDF.
- Verify missing-PDF, invalid-phone and Do Not Send parties are skipped and reported in confirmation.
- Long-press a party and verify Edit/Delete still works.

## v3.23.44 Camera AI Free-form Note + Zoom QA

- Photograph a normal handwritten note containing `1.0 - 10 coil`, `1.5 - 2 coil`, `2.5 - 2 coil`, and `Discount 9%`.
- Confirm AI Review shows three rows in `Description | Qty | Unit` format without requiring six input columns.
- Confirm the rows become `1.0 sq | 10 | Coils`, `1.5 sq | 2 | Coils`, and `2.5 sq | 2 | Coils`.
- Confirm the output title uses a detected heading/brand when present, discount shows 9%, and total shows 14 Coils.
- Without saved rates, confirm the preview/image/PDF uses the compact SL/DESCRIPTION/QTY layout.
- With matching Price List Manager rates, confirm rate, discount, GST and grand total calculations remain available.
- In Preview, pinch to zoom, drag the enlarged image, and double-tap to zoom/reset.

## v3.23.43 Camera AI, calculation and layout checks

- Take a camera photo and confirm Business Assistant AI review opens even on OEM camera apps.
- Verify each reviewed line supports Item, Qty, Unit, Rate, Disc% and GST%.
- Import items and verify Qty × Rate, item discount, overall discount, GST and grand total.
- Preview/share PDF and image; verify subtotal, discount, GST and grand total are shown.
- Verify top source/header buttons and bottom Preview/Share buttons remain fully visible above system bars.

## v3.23.42 dependency/build checks

- Push the project to GitHub and confirm `gradle assembleDebug --stacktrace` passes `:app:checkDebugDuplicateClasses`.
- Confirm build output no longer reports mixed `kotlin-stdlib 1.8.22` with `kotlin-stdlib-jdk7/jdk8 1.6.21` duplicate classes.
- Install the APK and recheck Camera AI material reading and the small transparent floating mic.

## v3.23.41 small transparent floating-mic checks

- Enable Floating Voice Mic and confirm the bubble is visibly smaller (46dp) and semi-transparent.
- Confirm content behind the floating mic remains visible.
- Press/drag the mic and confirm it becomes clearer while touched, then returns to 58% opacity after release.
- Tap without dragging and confirm Voice Search still opens.
- Restart the service/app and confirm the small transparent appearance remains.

## v3.23.40 camera-AI quotation checks

- Open Business Files → Business Quotation Manager and tap CAMERA • AI READ MATERIAL LIST.
- Photograph a clear printed material list and confirm the camera image is retained as a separate source file.
- Confirm the offline AI/OCR review opens with rows in `Item name | Qty | Unit | Rate` format.
- Correct one detected name/quantity/rate, import it, and confirm every row becomes a separately editable quotation item.
- Open a previously saved source image → menu → AI read material list and confirm it can be imported again.
- Tap EDIT COMPANY HEADER, change the company name, and confirm Preview/Image/PDF show the new header.
- Tap SHARE IMAGE and SHARE PDF and confirm the Android chooser (including WhatsApp when installed) opens with the quotation attached.
- Take a blurred/empty photo and confirm the app asks for a clearer retry instead of importing blank items.

## v3.23.39 fingerprint-unlock checks

- Confirm a 4-digit App PIN exists and at least one phone fingerprint is enrolled.
- Open Settings → Fingerprint App Unlock and switch it ON.
- Close the app completely and reopen it; confirm Android's fingerprint prompt appears before app data.
- Use the correct fingerprint and confirm the main screen opens.
- Use a wrong fingerprint and confirm the prompt stays available for retry.
- Tap USE APP PIN and confirm the existing 4-digit App PIN unlocks the app.
- Remove all enrolled phone fingerprints and confirm the app safely falls back to App PIN.
- Switch Fingerprint App Unlock OFF and confirm the next launch uses only App PIN.

## v3.23.38 business-quotation checks

- Open Business Files → Business Quotation Manager.
- Upload multiple Gallery images, then a PDF; open, share, rename and delete individual source files.
- Add a manual material item with quantity, unit, rate, discount and GST.
- Add another item from Price List Manager and confirm its saved fields are prefilled but independently editable.
- Set default Discount/GST, add a new item, and confirm defaults are applied.
- Use Apply to All and confirm existing separate item discounts update.
- Save quotation/customer details with customer name blank and confirm output still works.
- Edit Letter Pad business details and confirm preview uses them.
- Generate IMAGE and PDF; confirm WhatsApp/Android share opens and the PDF includes every item.
- Create Backup, clear/restore it, and confirm quotation items and source files return.

## v3.23.37 floating-voice checks

- Open Settings → Floating Voice Mic, switch it ON, and allow **Display over other apps**.
- Confirm a draggable blue/purple mic appears outside the app and remains after the app is closed.
- Drag it, release it, and confirm its last position is remembered.
- Tap the mic, say `Mylinc switch`, and confirm the Price List search opens with matching content.
- With Login PIN enabled, confirm the spoken result waits until the correct PIN is entered.
- Switch Floating Voice Mic OFF and confirm both the bubble and ongoing notification disappear.
- Restart the phone and confirm an enabled mic returns after boot.

## v3.23.36 custom-validity checks

- Run `SUPABASE_CUSTOM_VALIDITY_MIGRATION.sql` once in the existing Supabase project.
- Open Secure Online Admin, select Custom Days, and confirm the days box becomes enabled.
- Enter a valid Device ID and 30 days; confirm the success message says 30 days were extended.
- Refresh subscription on that user's phone and confirm `Custom validity` with the correct days left.
- Extend the same active user again and confirm new days are added after the existing expiry.

## v3.23.35 brand-folder checks

- Tap `+ BRAND`, create `Polycab`, and confirm one compact Polycab folder appears.
- Confirm files are not shown outside while `ALL BRANDS` and an empty search are active.
- Tap the Polycab folder and confirm only its sections, files, and price items appear.
- Add an image/PDF inside the selected folder and confirm Brand is pre-filled as Polycab.
- Use search and confirm matching files/items can still appear directly.

## v3.23.34 price-list auto-reply routing checks

- Save a Polycab / 90mtr PDF or image in Price List Manager with matching brand, section, name, or keywords.
- From an individual WhatsApp chat, send `Polycab 90mtr price list`.
- Confirm the matching price-list attachment is sent and the Ledger mismatch contact message is not sent.
- Confirm `Polycab 90 mtr price list` matches the same file.
- Send `ledger` and confirm normal exact-phone Ledger routing still runs.

## v3.23.33 brand-wise Price Manager checks

- Brand selector lists ALL BRANDS plus every saved brand.
- Selecting one brand hides other brands without deleting their data.
- Add Image/PDF requires separate Brand and Product Section fields.
- Add/Edit Price Item requires separate Brand and Product Category fields.
- Manage section edits only the selected Brand + Section combination.
- Brand-wise PDF and WhatsApp text show brand headers and category subheaders.
- Existing saved sources and items remain visible and editable.

## v3.23.32 individual-only safety checks

- Group message containing an Auto Reply keyword receives no response.
- Group message containing Ledger/Catalog keywords receives no PDF, image or text response.
- Direct contact with the same keyword still receives the configured reply.
- Bulk confirmation displays **Individual chats only • WhatsApp groups blocked**.
- Bulk sending targets normalized phone numbers through direct one-to-one WhatsApp links.

## v3.23.31 UPI fallback checks

- PAY ₹800 opens an installed UPI app when available.
- Without a UPI handler, PAY ₹800 automatically shows a scannable QR.
- COPY UPI ID copies `jainvikes6270@oksbi`.
- QR payment payload includes ₹800 and the current Device ID note.

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
