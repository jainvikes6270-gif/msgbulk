# LathaBulk v3.15.1 – Recipient Search + Category Catalog

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
