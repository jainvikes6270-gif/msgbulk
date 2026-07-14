# LathaBulk v3.16.8 – Recipient List Image Composer

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
6. Separate Local Backup and Google Drive Backup options, plus Restore.
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
