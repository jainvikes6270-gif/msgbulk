# LathaBulk v3.14 – Recipient Lists + WhatsApp Image Composer

## New in v3.14
1. Full-screen **Recipient Lists** page with dark cards, contact count, 3-dot management menu and blue `+` button.
2. Tap a recipient list to select the whole group for sending.
3. WhatsApp-style image composer: Gallery image preview, Change/Remove controls and caption/message in the same compose card.
4. Bulk queue supports image + caption or image-only messages.
5. Selected bulk image is included in Drive Backup/Restore.

## Included from v3.13
1. Rounded two-line message box with emoji button inside.
2. Header and footer can be saved and automatically added to every bulk message.
3. Save and reuse message templates, including their header and footer.
4. Message draft is remembered automatically.
5. **Drive Backup** includes saved templates, headers/footers, keyword rules, selected auto-reply images, business files, groups and settings.
6. **Restore Backup** restores all of the above.

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
