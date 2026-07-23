# Business Dost v3.23.77 – Floating Mic Portrait Return Fix

## v3.23.77

- Fixed the floating mic disappearing after closing a landscape/full-screen
  media player or video.
- The mic position is clamped inside the visible screen after every orientation
  change and whenever Business Dost returns to the foreground.
- Dragged mic coordinates are corrected and saved safely for the next launch.

# Business Dost v3.23.76 – Customer WhatsApp Voice Note Router

## v3.23.76

- A WhatsApp customer voice note can now be long-pressed and shared to
  **Business Dost Voice Note** without using the WhatsApp Cloud API.
- Android 13+ decodes the shared OGG/Opus audio locally to mono 16 kHz speech
  audio and passes it to the phone's installed speech-recognition service.
- The editable transcript uses a strict route: quotation/estimate requests open
  Quotation Manager; price-list/rate-list/brand requests open Price List Manager.
- Hindi/Hinglish and English retry buttons are included. The user must review
  the transcript and confirm the existing WhatsApp share flow before anything
  is sent.
- Package name remains `com.lathaeps.lathabulk`; Recipient List/Ledger route
  isolation and manual-campaign priority from v3.23.74 are preserved.
- Version increased to `3.23.76` (`versionCode 133`).

## v3.23.74

- Recipient List and manual bulk campaigns clear old pending Ledger/Catalog attachments before starting.
- Auto Reply attachment routing pauses while a manual campaign or broadcast is running.
- Manual list sending has priority if a stale attachment task overlaps.
- Final navigation: Home, Quotation, Price List, Voice and Settings.

# Business Dost v3.23.73 – Premium Dashboard & Floating Mic Fix

## v3.23.73

- Renamed the app to **Business Dost** with caption **Har Business Ka Digital Dost**, while keeping package `com.lathaeps.lathabulk` unchanged.
- Added a premium dark card-based home dashboard with fixed Home, Auto Reply, Business Files and Settings navigation.
- Grouped Ledger, Payment Reminder, Price List Manager and Quotation Maker inside Business Files.
- Strengthened floating mic restoration and retained its draggable saved position.
- Updated signed APK/AAB artifact names to Business Dost v3.23.73.

# LathaBulk v3.23.72 – Price Manager Auto-Send Isolation Fix

## v3.23.72

- Removed the hidden WhatsApp auto-send route from Price List Manager. Its PDFs/images are now only available for in-app search, voice search and quotation workflows.
- Catalog auto-reply now sends only when a word explicitly saved in the Catalog editor matches the received message.
- Catalog category, filename and generic `catalog` text can no longer select a file automatically.
- Version increased to `3.23.72` (`versionCode 129`).

## v3.23.71

- Catalog **PHONE GALLERY** now opens the Android photo picker instead of the PDF/Documents screen.
- Gallery supports selecting one or multiple phone images.
- Catalog **PDF / FILES** now opens a separate PDF-only picker with multiple selection.
- Version increased to `3.23.71` (`versionCode 128`).

## v3.23.70

- Moved the Catalog Auto Reply ON/OFF switch out of the top title bar.
- Added a large Auto Reply-style `Enable Catalog Auto Reply` row below the Catalog header.
- Version increased to `3.23.70` (`versionCode 127`) and APK/AAB workflow names updated.

# LathaBulk v3.23.69 – Catalog Auto Reply ON/OFF

## v3.23.69

- Added a saved ON/OFF switch in the Catalog header.
- When OFF, Catalog keywords do not send any PDF or image automatically.
- Manual Catalog browsing and sending remain available.
- Version increased to `3.23.69` (`versionCode 126`) and APK/AAB workflow names updated.

# LathaBulk v3.23.67 – Auto Reply + Ledger Routing Fix

## v3.23.67

- Same WhatsApp notification is deduplicated by conversation and message, so a saved Auto Reply sends only once.
- The configured Ledger keyword has priority over saved Auto Reply rules and now opens the matched customer's Ledger PDF route.
- WhatsApp shortcut/JID identity is used when available for more reliable exact phone matching.
- Version increased to `3.23.67` (`versionCode 124`) and APK/AAB workflow names updated.

## v3.23.66

- WhatsApp automation now runs only when a user-saved Auto Reply rule matches.
- Every unmatched message is ignored; it cannot trigger Price List, Catalog or Ledger files.
- Business files remain available for manual sending from their own app screens.
- Each rule still follows its selected Contains, Exact, Starts with or Ends with match mode.
- Version increased to `3.23.66` (`versionCode 123`) and APK/AAB workflow names updated.

# LathaBulk v3.23.65 – Safe Price List Auto Routing

## v3.23.65

- Fixed normal WhatsApp messages such as `Sir bill` and `Transfer ready` incorrectly sending saved Price List PDFs.
- Extracted PDF text is now searched only when the customer explicitly asks for a Price List or Rate List.
- Requests without `price list` continue to work only when they match a saved Brand, Section or Price keyword.
- Saved Auto Reply rules still run first and send only their configured text/image.
- Version increased to `3.23.65` (`versionCode 122`) and APK/AAB workflow names updated.

# LathaBulk v3.23.64 – Separate WhatsApp Auto Reply Routing

## v3.23.64

- User-saved WhatsApp Auto Reply rules now run before Ledger, Catalog and Price List smart routing.
- A matched Auto Reply sends only its configured text/image instead of an unrelated business-module response.
- Catalog, Ledger and Price List continue to handle their own requests when no saved Auto Reply rule matches.
- Duplicate protection remains active per incoming message.
- Version increased to `3.23.64` (`versionCode 121`) and APK/AAB workflow names updated.

# LathaBulk v3.23.63 – Smart Price List WhatsApp Matching

## v3.23.63

- Fixed WhatsApp Price List matching for natural requests such as `anchor roma discount and price list`.
- Discount, conjunctions, and common Hinglish request words no longer block the correct brand/product match.
- Added typo-tolerant brand/product matching while still requiring brand + range/product for multi-word searches.
- Duplicate WhatsApp notification updates for the same Price List request now produce only one reply/share task.
- Version increased to `3.23.63` (`versionCode 120`) and signed APK/AAB artifact names updated.

# LathaBulk v3.23.62 – Voice Quotation, Favorites & Calculator

## v3.23.62

- Added Voice Quotation Maker with Hindi/Hinglish language choice, editable recognition review and automatic Price List rate matching.
- Added up to 12 Favorite Voice Commands with tap-to-run, manual add, recent-search long-press save and long-press delete.
- Added Voice Discount/GST Calculator with base amount, discount, taxable value, GST and grand-total breakup.
- Version increased to `3.23.62` (`versionCode 119`) and signed APK/AAB artifact names updated.

# LathaBulk v3.23.61 – Voice Commands & Recent Search

## v3.23.61

- Added `RECENT VOICE` with the last 8 unique searches and one-tap repeat.
- Added an in-app `VOICE HELP` screen with Hindi/Hinglish command examples.
- Voice commands can directly open Business Quotation, Price List, Catalog, Ledger Customers, Payment Reminder, Settings, Update Check and Backup/Restore.
- Improved spoken unit matching for `90 meter`, `1.5 sq mm`, coil/roll, pieces and Hindi digits.
- Version increased to `3.23.61` (`versionCode 118`) and signed APK/AAB artifact names updated.

# LathaBulk v3.23.60 – Proper Hindi Voice Recognition

## v3.23.60

- Voice Search now asks for `हिंदी` or `HINGLISH / ENGLISH` before listening.
- Hindi mode explicitly forces the recognizer to `hi-IN` and shows a Hindi listening prompt.
- Online recognition is preferred so Hindi works even when the offline Hindi language pack is missing.
- Up to 10 recognition alternatives are checked against saved Price List data.
- The floating mic uses the same Hindi/Hinglish language selector.
- Version increased to `3.23.60` (`versionCode 117`) and signed APK/AAB artifact names updated.

# LathaBulk v3.23.59 – Flexible Hindi/Hinglish Voice Search

## v3.23.59

- Hindi/Hinglish search now ignores harmless extra request words and word order.
- Two core words such as brand + product are enough in a longer spoken sentence.
- Added more Hindi pronunciations for Mylinc, Polycab, Finolex, Havells, Anchor, modular, RCCB and DB.
- Added common speech variants and misspellings such as `swich`, `swtich`, `थोड़ा`, `जरा`, `दे दो` and `भाव`.
- Version increased to `3.23.59` (`versionCode 116`) and signed APK/AAB artifact names updated.

# LathaBulk v3.23.58 – Hindi/Hinglish Smart Voice Search

## v3.23.58

- Voice Search now listens in Hindi/Hinglish and also understands English brand/product names.
- Added Hindi product words such as स्विच, तार/वायर, केबल, लाइट, सॉकेट, पाइप, पंखा, MCB and DB.
- Common request words such as “लिस्ट भेजो”, “दिखाओ”, “चाहिए”, “please”, “latest” and “rate” no longer block the real product match.
- Search tolerates word-order changes, extra words and small speech/typing mistakes.
- Multiple speech-recognition alternatives are checked against saved price data and the strongest result is used.
- Version increased to `3.23.58` (`versionCode 115`) and APK/AAB artifact names updated.

## v3.23.57

- Payment Reminder now uses the Auto Reply-style mint, white and green color design instead of a black background.
- Master Ledger no longer bulk-adds every party with one tap; a searchable multi-select picker imports only chosen parties.
- Untouched legacy bulk-imported Ledger rows are removed once on upgrade, while saved-list, due-date and previously sent parties remain protected.
- Added `LEDGER • ADD / CLEAR` with a safe clear option for imported Ledger parties.
- Payment customer rows, filters, checkboxes, saved lists and direct-message textbox are clearly visible.
- Version increased to `3.23.57` (`versionCode 114`) and APK/AAB artifact names updated.

## v3.23.56

- Every saved Payment Reminder list now has a dedicated `SEND MESSAGE` button.
- Type and confirm a custom list message without first opening the customer-selection screen.
- `{Name}` personalizes the message for every saved party; balance is never added.
- The dark message box now has visible white text, cursor and a blue focused/selected border.
- Version increased to `3.23.56` (`versionCode 113`) and APK/AAB artifact names updated.

## v3.23.55

- Payment Reminder customer names, phone numbers and due-date text are now clearly visible on the dark screen.
- `ADD NEW LIST` moved inside `SAVED LISTS`, with a required list-name field.
- Every saved payment list card now has visible `EDIT LIST` and `DELETE` buttons.
- Version increased to `3.23.55` (`versionCode 112`) and APK/AAB artifact names updated.

# LathaBulk v3.23.54 – Short Ledger Folder Name

## v3.23.54

- Business Files folder button and screen title shortened to `LEDGER & CONTACTS`.
- Version increased to `3.23.54` (`versionCode 111`) and APK/AAB artifact names updated.

## v3.23.53

- Business Files ke four Master Ledger tools ab ek `MASTER LEDGER & CONTACTS` folder button ke andar hain.
- Folder contains Master PDF to Phone/Balance Excel, Upload PDF, Import Contact Excel and View File History.
- `SAVE SETTINGS` and independent `AUTO REPLY ON/OFF` buttons added.
- Uploading a ledger no longer silently turns Auto Reply on; ON/OFF remains under user control.
- Version increased to `3.23.53` (`versionCode 110`) and APK/AAB artifact names updated.

## v3.23.52

- Subscription status now shows exact remaining validity in days and hours.
- Example: `Free trial • 11 Days 6 Hours Remaining`.
- Works for free trial, yearly plan and custom validity.
- Version increased to `3.23.52` (`versionCode 109`) and APK/AAB artifact names updated.

## v3.23.51

- Payment Reminder me selected parties ko named list ke roop me save aur dobara open kar sakte hain.
- Saved list me parties add/remove, list rename aur list delete options add kiye gaye.
- Payment Reminder screen aur saved list cards ko Recipient Lists jaisa dark background/button design diya gaya.
- Version increased to `3.23.51` (`versionCode 108`) and APK/AAB artifact names updated.

## v3.23.50

- Fixed the ₹800 UPI button so Android opens the installed UPI-app chooser directly.
- UPI payment QR and Copy UPI ID remain available as fallbacks.
- Added `SEND PAYMENT SCREENSHOT ON WHATSAPP`, opening WhatsApp number `9025156444` with the Device ID and confirmation message prefilled.
- Version increased to `3.23.50` (`versionCode 107`) and APK/AAB artifact names updated.

## v3.23.49

- The normal GitHub APK workflow now builds a release APK and signs it with the permanent LATHAEPS keystore.
- Removed the temporary GitHub-runner debug signing path that caused `ANDROID_ID`-based subscription IDs to change between builds.
- After the one-time move to this permanently signed APK, reinstalling APKs built with the same signing secrets keeps the same Device ID on the same phone.
- Version increased to `3.23.49` (`versionCode 106`) and APK/AAB artifact names updated.

> Important: always install the `RELEASE_SIGNED` APK. Do not distribute old `app-debug.apk` files.

## v3.23.48

- Ledger Party Lists now use an Auto Reply-style full-screen card layout.
- Create separate named lists and see customer names plus phone numbers before opening a list.
- Open/Edit, add or remove parties, rename, delete and send each list independently.
- Weekly or Monthly scheduling is available from every saved list.
- The older single saved ledger list is migrated automatically as `My Ledger List`.
- Version increased to `3.23.48` (`versionCode 105`) and signed APK/AAB workflow names updated.

# LathaBulk v3.23.46 – Saved Ledger Party List & Schedule

## v3.23.46

- Save a small reusable party list inside Ledger Customers.
- Open the saved list later with one tap and send every mapped Ledger.
- Added Weekly and Monthly recurring Ledger schedules with date and time selection.
- Scheduled runs use the latest mapped PDF for every saved party and continue after phone reboot/app update.
- Missing Ledger PDFs and Do Not Send parties are skipped safely.

# LathaBulk v3.23.45 – Multiple Party Ledger Send

## v3.23.45

- Ledger Customers now supports checkbox-based multiple party selection.
- Added Select All, Clear and Send Selected Ledgers actions with search-aware selection.
- Every selected party receives only its own mapped Ledger PDF in the automated WhatsApp queue.
- Parties with a missing PDF, invalid phone number or Do Not Send status are skipped before confirmation.
- Long-press a Ledger customer to edit or delete it; normal tap now selects/deselects it.

# LathaBulk v3.23.44 – Free-form Camera AI & Zoom Preview

## v3.23.44

- Camera AI now reads a customer's normal handwritten paper; no fixed six-column input is required.
- Free-form lines such as `1.0 - 10 coil`, `1.5 - 2 coil` and a separate `Discount 9%` note become clean Description/Qty rows.
- Electrical wire sizes written as bare numbers are presented as `1.0 sq`, `1.5 sq`, etc., and Coil quantities are totalled correctly.
- A detected brand/heading becomes the quotation title and the detected discount becomes the overall quotation discount after review.
- AI rows automatically match saved Price List Manager items when a compatible item/brand rate exists.
- If no rates are available, preview/image/PDF uses a clean screenshot-style `SL | DESCRIPTION | QTY` material quotation with quantity total.
- Quotation image preview now supports pinch zoom, drag, and double-tap zoom/reset.

# LathaBulk v3.23.43 – Business Assistant Camera AI & Totals

## v3.23.43

- Fixed Camera AI on camera apps that save the photo but do not return a reliable `RESULT_OK` value.
- Camera OCR review now supports `Item | Qty | Unit | Rate | Disc% | GST%`.
- Added automatic quantity × rate, item discount, additional overall discount, GST and grand-total calculations.
- Quotation cards and PDF/image output now show a clear subtotal, discount, GST and grand-total breakdown.
- Improved top and bottom action-button visibility with safe screen fitting, taller buttons and two-line labels.

## v3.23.42

- Fixed GitHub Actions `checkDebugDuplicateClasses` failure introduced by the OCR dependency.
- Kotlin stdlib, jdk7 and jdk8 transitive artifacts are now aligned through Kotlin BOM `1.8.22`.
- Camera AI quotation, small transparent mic and every existing feature remain unchanged.

## v3.23.41

- Floating voice mic reduced from 62dp to a compact 46dp bubble.
- Normal opacity is now 58%, so it does not cover app content behind it.
- The mic becomes clearer while pressed/dragged and automatically returns to transparent after release.
- Tap, drag position memory, voice search, foreground service and boot restore behavior remain unchanged.

## v3.23.40

- Business Quotation now has a direct **CAMERA • AI READ MATERIAL LIST** action.
- A photographed material list is saved as a separate quotation source and read on-device with bundled ML Kit OCR (no model download wait).
- Detected rows open in an editable review format: `Item name | Qty | Unit | Rate` before import.
- Imported AI rows remain separate, fully editable quotation items with the current default Discount and GST.
- Any saved source image can be read again from its menu with **AI read material list**.
- The quotation toolbar now clearly exposes **EDIT COMPANY HEADER** for changing the company name and letter-pad details.
- Final output buttons are clearly labeled **SHARE IMAGE** and **SHARE PDF** and open the Android/WhatsApp share chooser.

## v3.23.39

- Settings now includes optional **Fingerprint App Unlock**.
- When enabled, app launch displays Android's secure biometric prompt before showing business data.
- Existing 4-digit App PIN remains available as the fallback when fingerprint is cancelled, unavailable or fails.
- Enabling fingerprint automatically keeps App Login enabled and requires an existing App PIN plus an enrolled phone fingerprint.
- No fingerprint data is stored by LATHAEPS SMART; matching is handled by Android's system biometric prompt.

## v3.23.38

- Business Files now includes a complete **Business Quotation Manager**.
- Upload supplier price lists, material lists and discount sheets as multiple Gallery images or PDF/files.
- Source File Manager supports open, share, rename and delete while keeping each source separate.
- Add materials manually or choose an existing item from Price List Manager with rate, unit, discount and GST prefilled.
- Every quotation item keeps separate quantity, unit, rate, discount, GST and optional item details.
- Discount Structure saves defaults for new items and can update every existing item together.
- Customer name, customer details, quotation reference and subject are optional.
- Editable Letter Pad includes business name, address, phone/email, GST and quotation terms.
- Output options include Letter Pad preview, shareable PNG image and multi-page PDF.
- Quotation settings, material items and source files are included in app Backup/Restore.

## v3.23.37

- Settings now includes an optional **Floating Voice Mic** shortcut.
- The draggable mic stays available over other apps while enabled.
- Tapping it opens speech recognition and routes the result through the existing main Voice Search commands and Price List search.
- The first enable opens Android's **Display over other apps** permission screen.
- Turning the option OFF removes both the bubble and its foreground notification.
- The enabled shortcut is restored after device reboot or app update when Android overlay permission remains allowed.

## v3.23.36

- Secure Admin panel now includes `Custom Days • Extend current validity`.
- Admin can enter 1 to 365000 days for any valid 12-character Device ID.
- Days are added after the user's current expiry; an active user's remaining validity is preserved.
- Run `SUPABASE_CUSTOM_VALIDITY_MIGRATION.sql` once on an existing Supabase project. It does not rotate the admin password.

## v3.23.35

- Price List Manager home now shows compact brand folders instead of every saved file.
- New `+ BRAND` action creates an empty named brand folder.
- Tapping a folder opens that brand's image/PDF sections and price items.
- Adding a file or price item while a brand folder is open pre-fills that brand.

## v3.23.34

- Incoming WhatsApp requests such as `Polycab 90mtr price list` now search Price List Manager files before Ledger routing.
- Matching saved image/PDF files are attached; Ledger phone matching and its mismatch reply are skipped for price-list requests.
- `90mtr`, `90 mtr`, `90 meter`, and `90 metre` use the same search form.

## v3.23.33

- Price List Manager adds a dedicated **BRAND: ALL BRANDS** selector.
- Brand and Product Section are now separate fields for price items, images and PDFs.
- Polycab, Finolex, Mylinc and other brands render as separate colored brand groups.
- Each brand contains its own categories such as Wire, Switch and DB.
- Edit/Manage can move a saved file or complete section to another brand without deleting the file.
- Voice/search, WhatsApp text and generated PDF include both brand and product category.
- Existing price data remains available and can be reorganized with Edit.

## v3.23.32 – Individual WhatsApp Chats Only

## v3.23.32

- Auto Reply, Ledger and Catalog notification replies ignore WhatsApp group conversations.
- Group JIDs (`@g.us`), group-conversation notification metadata and group summaries are blocked before any reply is prepared.
- Bulk Auto Send continues to use validated mobile numbers and direct one-to-one WhatsApp links only.
- Send confirmation clearly shows **Individual chats only • WhatsApp groups blocked**.

## v3.23.31 – UPI QR and Copy Fallback

## v3.23.31

- Subscription payment adds **COPY UPI ID** and **SHOW PAYMENT QR** buttons.
- If no compatible UPI app is installed, the payment button automatically shows the QR/copy fallback instead of the Android error.
- QR contains the ₹800 amount, LATHAEPS UPI ID and the user's Device ID in the payment note.

## v3.23.30

- The 12-day trial is created and timed on Supabase, not in local app preferences.
- Reinstalling the APK or clearing app data does not reset a device's trial.
- Yearly, Lifetime Free and blocked status are verified online and cached for at most 48 hours.
- The old APK-embedded activation secret and offline activation-code generator were removed.
- The admin password is never stored in the APK. The secure Admin screen sends it over HTTPS to a protected Supabase function.
- Run `SUPABASE_SUBSCRIPTION_SETUP.sql` once before installing this build. The final SQL result displays the generated admin password; save it privately.

## v3.23.29

- Every media section has MANAGE: edit section, add more pictures/PDFs and delete the complete section.
- Every saved image/PDF has edit details, replace and delete controls.
- Add Price Item and Share Complete Price List are now connected on the Price List Manager screen.
- Build fix: updater reads the installed version without depending on a generated `BuildConfig` class.

## v3.23.28

- Main microphone now understands screen-opening, backup/restore, update-check, ledger-search and catalog-search commands.
- Brand share commands find the matching multiple-image/PDF section and require confirmation before Android sharing opens.
- Plain brand/product speech still searches Price List Manager.

## v3.23.27

- Business Files is now a full-screen colorful window matching the Auto Reply design.
- Settings no longer duplicates Local Backup and Restore; both remain on the main window.
- Daily GitHub Releases update check with `UPDATE NOW` and `LATER` notification actions.
- Settings includes App Updates notification control and manual `CHECK NOW`.
- Price List Manager keeps multiple Gallery image and PDF sections with Share All.

## v3.23.26

- Added a dedicated `PICTURES FROM PHONE GALLERY` option with multiple selection.
- Added a separate `PDF FROM FILES` option with multiple selection.
- Selected pictures/PDFs continue to use the same brand/category and search keywords.
- Ledger, Catalog and Auto Reply modules were not changed.

## v3.23.25

- Removed the top `+ ADD ITEM` and `SHARE PRICE LIST` buttons from Price List Manager.
- The screen now focuses on multiple image/PDF sections plus typed and voice search.
- Ledger, Catalog and Auto Reply modules were not changed.

## v3.23.24

- Price List Manager now accepts multiple images and PDFs in one selection.
- Every selected file is stored under the same brand/category section and uses the same search keywords.
- Section headers show separate image and PDF counts.
- `SHARE ALL` sends every image/PDF saved in the matching section through Android share/WhatsApp.
- Existing main-window voice search continues to filter these sections locally.
- Ledger, Catalog and Auto Reply modules were not changed.

## v3.23.23

- Price List Manager can store PDF and image source files inside Business Files.
- PDF text is indexed locally for smart search; images use their saved name, category and keywords.
- A large `VOICE SEARCH` microphone button is available directly on the main window.
- Voice results show matching manual price items and source PDF/images, which can be opened or shared.
- Source files and their search index are included automatically in Local/Drive backup and restore.
- Ledger, Catalog and Auto Reply code/workflows are unchanged.

# LathaBulk v3.23.22 – AI Smart + Voice Price Search

## v3.23.22

- Price List Manager includes one-tap microphone voice search.
- Speak a brand and product such as `Mylinc switch`; matching saved price-list items appear without typing.
- Smart multi-word matching understands common voice variations such as `my link` → `Mylinc` and singular/plural product words.
- Search uses only Price List Manager data; no business price data is uploaded to an AI server.

# LathaBulk v3.23.21 – Price List Manager

## v3.23.21

- Business Files now includes a category/brand-wise Price List Manager.
- Add, edit, search and delete items with rate, unit, discount and GST.
- Share the complete LATHA EPS price list as a professional PDF or WhatsApp-ready text.
- Price list data is included automatically in Local/Drive backup and restore.
- Existing Ledger, Catalog, Auto Reply and Payment Reminder workflows remain unchanged.

# LathaBulk v3.23.20 – Ledger settled-phone single-reply fix

## v3.23.20

- Ledger auto reply waits briefly for WhatsApp's settled sender identity before deciding the exact phone match.
- Repeated notification updates for one Ledger request produce only one final result.
- Prevents early false “mobile number not matched” replies followed by a later Ledger send.

## v3.23.19

- One incoming Ledger request is processed only once even when WhatsApp reposts or updates the same notification.
- When WhatsApp exposes only a saved contact name, all numbers under that exact phone contact are checked against the Ledger map; the PDF is sent only for an exact unique 10-digit Ledger match.
- A contact name alone never authorizes a Ledger send.

# LathaBulk v3.23.2 – Direct APK Share + Paid Plans + Admin Activation

## v3.23.2

- Settings me **Share App APK** option added. Installed APK direct share hota hai; GitHub username aur source code nahi dikhte.

## Subscription system

- 12-day free trial starts on first launch after install/update.
- ₹800 yearly plan opens the installed UPI apps and pays to `jainvikes6270@oksbi`.
- Payment is approved manually: user copies the 12-character Device ID and sends it to the admin.
- Separate Admin Panel generates a one-year code or Lifetime Free code for that Device ID.
- User pastes the code in Settings → Subscription & Payment.
- Admin Panel is hidden from normal users. Long-press the **LATHAEPS SMART** title on the Subscription screen, then enter the private Admin PIN.
- Offline activation: customer contacts, ledger, catalog, auto-reply and queue data are not uploaded.
- Existing Ledger, Catalog, Auto Reply, recipient lists, backups and sending workflows are unchanged.
- Payment Reminder alarms are restored automatically after phone restart or app update.
- When trial/plan expires, Auto Reply, scheduled reminders, Catalog sharing and background sending queues are paused until activation.

# LathaBulk v3.22.1 – Ledger + Catalog Fresh Workflow

## v3.22.1 verified fixes

- Ledger parser rebuilt for Mobile, Mob, Phone, Telephone, Tel, WhatsApp and Contact labels.
- Indian numbers normalize to strict last-10-digit matching before a customer PDF is sent.
- Catalog Phone Gallery and multi-PDF/image selections are copied into permanent app storage.
- Ledger and Catalog use one wake/unlock/send flow; tasks that began locked re-lock after completion.
- Accessibility and Notification Access show live green ON / red OFF buttons on the main screen.

- Main app header and Android app label now show **LATHAEPS SMART**.
- Version remains visible only inside Settings.

# LathaBulk v3.20.4 – Recurring Payment Reminder

- Schedule supports **One Time, Daily, Weekly and Monthly** repeat options.
- The app remembers the last repeat option selected.
- Schedule Status shows the repeat type and next send date/time.
- Daily, weekly and monthly reminders automatically schedule their next run.

# LathaBulk v3.20.3 – Simple Balance Payment Request

- Payment Reminder messages no longer show the customer's balance amount.
- The customer receives only a simple pending-balance payment request.
- Balance amounts are also hidden from the Payment Reminder customer list.

# LathaBulk v3.20.2 – Paid Broadcast Option Removed

- Removed the **WhatsApp Broadcast** option from the app menus.
- **Payment Reminder** remains available inside **Business Files** only.

# LathaBulk v3.20.1 – Business Files Menu Placement

- **WhatsApp Broadcast** and **Payment Reminder** are now available inside **Business Files** only.
- Both shortcut buttons were removed from the main screen for a cleaner layout.

# LathaBulk v3.20.0 – WhatsApp Broadcast + Scheduled Payment Reminder

## v3.20.0 changes

- **WHATSAPP BROADCAST** is available inside **Business Files**.
- Opens one already-created WhatsApp / WhatsApp Business Broadcast List by its exact name and sends the message once, instead of opening every customer chat.
- Supports one common text message with an optional Gallery image or PDF attachment.
- Main-screen **PHONE CONTACT LISTS** keeps phone-contact search, multiple selection and saved recipient lists easy to access.
- Payment Reminder now has a direct **PHONE CONTACTS** picker with name/number search, multi-select and automatic duplicate skipping.
- Payment Reminder includes **SCHEDULE TIME** with calendar + clock selection, a persistent Android alarm, scheduled notification, status and cancel option.
- Scheduled payment messages keep each customer’s own `{Name}`, `{Balance}` and `{DueDate}` values.
- WhatsApp Broadcast delivery still requires the customer to have saved the business number, as required by WhatsApp.

# LathaBulk v3.19.0 – Payment Reminder + Live Progress

## v3.19.0 changes

- **Business Files → Payment Reminder** is a separate full-screen reminder manager.
- Import customers and pending balances from the prepared Master Ledger or add/edit customers manually.
- Save due dates, filter Overdue/Today/Upcoming/No Due Date, select filtered customers and send reminders.
- Custom reminder template supports `{Name}`, `{Balance}` and `{DueDate}`.
- Test First, Send Selected, Do Not Send filtering and reminder history are included.
- Every recipient receives their own balance and due-date message through one Auto Send queue.
- Sending notification now updates live as **1/10, 2/10, 3/10...** with the current contact instead of remaining at 0/10.
- Main-screen mini progress also shows current/total and completion notification shows the final total.

## v3.18.0 changes

- Settings now includes one separate **Contact Settings** menu; the main screen stays uncluttered.
- **Pause / Resume / Skip Contact** controls preserve the current Auto Send queue.
- **Do Not Send List** supports manual numbers and all currently selected contacts; blocked numbers are automatically skipped in Test, Text and Image sends.
- **Recipient List Templates** saves different messages for every recipient list and shows them in that list's Send window.
- The **Catalog screen itself** now includes search and a type filter for filename, category and auto-reply words.
- Contact Settings data is included automatically in Local/Drive backup and restore.

## v3.17.2 changes

- Catalog Add screen now has separate **PHONE GALLERY** and **PDF / FILES** buttons.
- Multiple Catalog pictures can be selected directly from phone Gallery/Albums.
- Existing Catalog types offer the same Gallery or PDF/file choice through **Add More Files**.
- Gallery pictures keep the selected Catalog type, name and auto-reply keywords.

## v3.17.1 changes

- Recipient List image button now opens the phone Gallery/Albums directly.
- Selected gallery image continues into the built-in text and sticker editor.
- Auto Reply image selection uses the same phone Gallery-first picker.
- Android 13+ uses the secure system Photo Picker; older phones use the device Gallery with a file-picker fallback.

## v3.17.0 changes

- Main screen includes **TEST SEND • 1 CONTACT** beside the normal Auto Send button.
- Test Send uses only the first valid selected contact and never starts the full recipient queue.
- A confirmation shows the exact test recipient before WhatsApp opens.
- Existing message, header/footer and `{Name}` replacement are included in the test.

## v3.16.9 changes

- Main screen now shows **Review Selected (count)** after contacts are selected.
- Review screen lets you uncheck and remove individual contacts selected by mistake.
- A separate **Clear Selection** button cancels all selected contacts after confirmation.
- Cancel inside the review screen closes it without changing the current selection.
- Selected-contact changes are saved immediately and reflected in the contact list.
- Manual Auto Send now shows recipient count, the first selected contacts and a message preview before starting.
- Confirmation screen includes **Review Contacts** and **Cancel**, preventing accidental sends.
- Invalid or duplicate mobile numbers are skipped automatically and the skipped count is shown.

## v3.16.8 changes

- Recipient List cards now include a direct **SEND** button for Text or Image + Text.
- Gallery image selection opens a built-in editor before sending.
- Add overlay text, emoji/stickers, text color, text size and Top/Center/Bottom position.
- Edited image is saved safely inside the app and sent automatically to the selected recipient list.
- Existing lock-screen resume and notification Cancel Auto Send remain active for media queues.

## v3.16.7 changes

- Every Catalog category heading now has an **EDIT** button.
- Edit the category name and auto-reply keywords across all files in that type.
- Add more PDFs/pictures to an existing Catalog type without recreating it.
- Existing per-file View, WhatsApp Send, Rename and Delete controls remain available.

## v3.16.6 changes

- Running notification includes a prominent **CANCEL AUTO SEND** action.
- Cancel immediately stops the remaining recipient queue and releases the screen wake lock.
- The same cancel action also clears a pending Catalog attachment queue.
- Useful when recipients were selected by mistake, even after WhatsApp has opened.

## v3.16.5 changes

- Bulk and Catalog queues wake the display when the phone is locked.
- Swipe-only keyguard is dismissed and sending continues automatically.
- Secure PIN/pattern/fingerprint shows Android's unlock prompt; after successful unlock, the pending queue resumes automatically.
- Android security is respected: secure device credentials are never bypassed.

## v3.16.4 changes

- Catalog auto reply sends every matched PDF/picture as a real WhatsApp attachment, one-by-one.
- Avoids the WhatsApp multi-file share issue that sent only the `3 files` caption.
- Accessibility continues the saved category queue until every file is sent.

## v3.16.3 changes

- Fixed GitHub Actions Java compilation failure caused by duplicate `last10Digits()` method.
- Increased Android `versionCode` to 28 for clean update installation.

## v3.16.2 changes

- Schedule accepts Hours and Minutes with a live HH:MM:SS countdown.
- Auto Send starts automatically when the countdown finishes.
- Active schedule timer can be cancelled.
- Catalog auto reply now sends every saved PDF/picture from the matched category one-by-one, avoiding WhatsApp builds that drop attachments from multi-file shares.
- Catalog sharing grants file access correctly to both WhatsApp and WhatsApp Business.

## v3.16.1 changes

- Auto Send now acquires a timed screen wake lock before opening every WhatsApp contact.
- The app keeps the display awake throughout the active sending queue.
- Wake lock is released automatically when sending completes, fails or is stopped.
- Settings now includes **Screen-off Auto Send** help and display guidance.
- Secure PIN/pattern/fingerprint locks still cannot be bypassed; start while the phone is unlocked.

## v3.16.0 changes

- Main header now shows only **LATHAEPS**; version is available inside Settings.
- Master PDF → Phone + Balance Excel is now inside **Business Files** only.
- Dark/Light Theme is now available inside **Settings** only.
- Contacts button toggles the contact list: first tap shows it, next tap hides it.
- Catalog supports selecting and saving multiple pictures and PDFs in one action.
- Settings includes **Current Version** and **Contact Us: lathaeps@gmail.com**.

## New in v3.15.1

- My Recipient Lists is now a large, clearly visible home button.
- New Recipient List always starts empty; Add Contacts opens the phone contact selector.
- Recipient lists and contact picker support mobile-number search.
- Catalog files are saved list-wise by type such as Wires, Switch and DB.
- Each catalog type accepts PDF/pictures and its own auto-reply words.
- Removed duplicate top Saved list and Clear controls; Clear All remains in Settings.

## Included from v3.15.0
1. `My Groups` is now **My Recipient Lists**.
2. Recipient Lists have a premium dark card screen with contact counts.
3. Catalog is a completely separate section with its own PDF/image list.
4. Catalog screen supports View, WhatsApp Send, Rename and Delete.
5. Catalog auto reply uses the latest file saved in the separate Catalog section.
6. Catalog accepts `catalog`, `catalogue` and `catlog`.
7. Price List has been completely removed from the app and auto reply.
8. Saved Ledger filename is shown separately as **Master PDF** in a green highlight card.

## Preserved from v3.14.1
1. Screenshot-style premium **AutoReply to Messages** screen.
2. Global Auto Reply ON/OFF switch and scrollable rule cards.
3. Add, edit, delete and reorder keyword rules with the floating **+** button.
4. Contains, Exact, Starts With, Ends With and Case Sensitive matching preserved.
5. New user-friendly **Settings** screen with Dark/Light Theme.
6. Main window Local Backup/Restore, plus Settings Google Drive Backup.
7. Secure Forgot PIN flow using a saved recovery word.
8. Clear All removes app data but keeps the login PIN and recovery word safe.

All v3.13 message templates, header/footer, image auto reply and backup features are preserved.

## Update flow
1. Open **BUSINESS FILES**.
2. **UPLOAD & PREPARE MASTER LEDGER PDF** tap karke combined Tally PDF select karein.
3. App phone-number wale pages ko customer-wise PDF me automatically prepare karta hai. Bina phone number wale pages skip hote hain.
4. **MASTER PDF → PHONE + BALANCE EXCEL** se sirf do columns export hote hain: Phone Number, Closing Balance.
5. Customer apne registered WhatsApp number se **ledger** bheje to exact number ka private ledger PDF send hota hai.
6. Notification Access, Contacts permission aur Accessibility ON rakhein.

## Important
Text-based Tally PDF required hai. Ek phone number multiple ledger pages me ho to pages ek customer PDF me combine hote hain. Name-only matching disabled hai; exact 10-digit sender number match required hai.

## Build APK
Upload the extracted project to GitHub. GitHub Actions workflow is included under `.github/workflows/build-apk.yml`.
# LathaBulk v3.19.1

## Recipient image sending update

- Recipient List > Send Text / Image now opens the phone Gallery / Albums app first.
- Works with Android system Photo Picker and Files as automatic fallbacks.
- The chosen image opens in the built-in image editor and is saved as an app-safe copy before auto sending.
# LathaBulk v3.23.3 – Ledger Phone Match Repair

- Restores reliable WhatsApp sender-number detection from notification metadata and saved contacts.
- Ledger still sends only after strict last-10-digit matching with the prepared customer ledger map.
- Adds a visible Ledger failure status when the verified phone or customer PDF cannot be matched.
- Direct APK sharing, Catalog, Auto Reply, subscriptions and all other v3.23.2 features remain unchanged.

# LathaBulk v3.23.4 – Auto Reply Image Isolation

- Every Auto Reply rule now keeps its own optional image selection.
- A new text-only rule no longer reuses an old image selected for another rule.
- Image is sent only when **Send image with this rule** is enabled for that rule.

# LathaBulk v3.23.5 – Cleaner Subscription Settings

- Settings list no longer displays Lifetime Free or the active plan status.
- Plan status remains available only inside the Subscription & Payment screen.

# LathaBulk v3.23.6 – Ledger + Catalog Attachment Send Repair

- Ledger and Catalog attachment preview now detects WhatsApp's current Send button IDs.
- Accessibility can click the Send icon's clickable parent when the icon itself is not clickable.
- Text Auto Reply behavior remains unchanged.

# LathaBulk v3.23.9 – Crop + Manual Placement Image Editor

- Image ke upar 2-line text likhne aur dobara edit karne ka option
- Live preview: text, emoji/sticker, color, size aur position turant dikhega
- 12 emoji/sticker choices aur NONE/remove option
- 8 text colors: white, yellow, red, black, blue, green, pink aur orange
- Top, center aur bottom text position; small, medium aur large size
- Continuous auto reply / no-cooldown behavior v3.23.7 se preserved
- Signed-release build configuration removed; GitHub workflow builds debug APK only
- Crop presets: Original, Square 1:1, Portrait 4:5, Story 9:16 and Landscape 16:9
- Text/sticker can be dragged with a finger anywhere on the live image preview

- Removed the Auto Reply cooldown field and five-minute reply block.
- Repeated Ledger, Catalog and Auto Reply keywords can now trigger continuously.

# LathaBulk v3.23.10 – Task Completion Repair

- Ledger, Catalog and image Auto Reply no longer stop on the WhatsApp preview window.
- Accessibility retries while the preview loads and recognizes newer WhatsApp Send-button IDs.
- A bottom-right Send-button fallback covers WhatsApp screens that hide the icon ID or label.
- Auto Reply remains continuous with no cooldown option.
- Only bulk sending keeps its separate 3–7 second random delay.

# LathaBulk v3.23.11 – WhatsApp Recipient Window Repair

- Fixes Ledger, Catalog and image Auto Reply stopping on WhatsApp's `Send to…` window.
- If WhatsApp ignores the direct recipient ID, Accessibility searches the original sender, selects the recipient, continues to preview and presses the final Send button.
- The recipient-picker stage is saved across window redraws and cleared after completion/cancel.
- No-cooldown Auto Reply and the premium crop/manual-placement editor remain unchanged.

# LathaBulk v3.23.12 – Consolidated Ledger/Catalog/Auto Reply QA

- Ledger customer edits preserve the prepared customer PDF URI, filename, balance and page mapping.
- Valid individual WhatsApp notifications are no longer rejected only because their title contains a message count.
- Identical reposts of the same notification event are ignored without adding a reply cooldown; every new repeated keyword message still works.
- Recipient search prefers the verified last-10-digit phone number, with the WhatsApp contact title as fallback.
- Unlabelled green Next/Send buttons are supported in both recipient selection and final preview.
- Accessibility resumes an already-pending task when its service reconnects.
- Retry scheduling is single-chain to avoid stale handler events during long WhatsApp loads.
- The sender chat no longer becomes an active send target before the real PDF/image share queue is ready.
- Attachment requests arriving during another task wait for the active task instead of overwriting its Ledger/Catalog queue.
- Debug-only GitHub build, no signed-release configuration, no Auto Reply cooldown option.

# LathaBulk v3.23.18 – Ledger Exact Phone + Assistance Reply

- Ledger sends only when the incoming sender's verified last 10 digits exactly match the saved Ledger phone.
- WhatsApp notification conversation metadata is checked when the visible title does not expose the number.
- Customer/Tally name fallback is disabled; a missing or different number receives a LATHAEPS assistance message and no PDF.
- Catalog and normal Auto Reply flows are unchanged.

- Removed WhatsApp notification key/tag IDs from recipient phone detection; random phone-shaped IDs such as `7472764035` are no longer used.
- Ledger now matches a verified phone first, with exact unique customer-name fallback when WhatsApp does not expose the phone number.
- Catalog recipient picker now accepts only an exact full contact name or exact last 10 phone digits; partial matches are rejected.
- Existing text Auto Reply behavior is unchanged.

# LathaBulk v3.23.13 – Exact Recipient Safety

- Removed the unsafe first-search-result fallback that could select an unrelated contact.
- Catalog and image Auto Reply now select only an exact last-10-digit sender phone or exact normalized sender contact name.
- If the exact sender is not found, the task stops with `Recipient not matched • nothing sent`; no other contact is selected.
- Ledger phone extraction also reads Android MessagingStyle sender-person data before performing the strict saved-ledger phone match.
- Recipient search attempts have a bounded timeout and are cleared on completion, cancel or expired task cleanup.
