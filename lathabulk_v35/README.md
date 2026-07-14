# Latha Bulk v3.6 Fixed Groups

GitHub-ready Android project.

## Fixed / added
- Login PIN: change, test, enable/disable and reset
- WhatsApp notification auto reply reads standard and messaging-style notification text
- Reply image is copied into app storage and shared using FileProvider
- Direct WhatsApp image/file target uses resolved contact phone when available
- Ledger phone matching supports +91, 0-prefix, spaces, dashes and contact-name-to-phone lookup
- Saved contacts preserve name + phone
- Saved list can be loaded or converted directly into a group
- Groups: save, open, add/remove contacts, rename, duplicate and delete
- Backup/restore includes settings, groups, rules and copied images/files

## Required phone settings
1. Allow Contacts permission.
2. Enable Notification Access for **Latha Auto Reply**.
3. Enable Accessibility for **Latha Bulk** for auto-send queue.
4. Keep WhatsApp notifications enabled and message preview visible.

Build with GitHub Actions using `.github/workflows/build-apk.yml`.
