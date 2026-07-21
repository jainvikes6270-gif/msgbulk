# LATHAEPS SMART — Play Store upload

## Build details

- App name: LATHAEPS SMART
- Package: `com.lathaeps.lathabulk`
- Version: `3.23.72`
- Version code: `129`
- Target API: `35`
- Launcher logo: premium copper L-wire and plug icon
- Play Store 512px icon: `branding/LATHAEPS_SMART_Play_Store_Icon_512.png`

## Create the signed AAB in GitHub

Do not commit a keystore or its passwords to the repository.

In the repository, open **Settings → Secrets and variables → Actions** and add:

1. `LATHAEPS_KEYSTORE_BASE64` — Base64 text of the permanent keystore file.
2. `LATHAEPS_KEYSTORE_PASSWORD` — Keystore password.
3. `LATHAEPS_KEY_ALIAS` — Key alias.
4. `LATHAEPS_KEY_PASSWORD` — Key password.

Then open **Actions → Build Signed Play AAB → Run workflow**. When the job is green, download the `LATHAEPS-SMART-Play-AAB-v3.23.72` artifact and unzip it. Upload the `.aab` file to Play Console.

## Create the signed release APK for direct download or Uptodown

Use the same four permanent signing secrets listed above. Open **Actions → Build Signed Release APK → Run workflow**. When the job is green, download and unzip the `LATHAEPS-SMART-Signed-Release-APK-v3.23.72` artifact. Share or upload `LATHAEPS_SMART_v3.23.72_RELEASE_SIGNED.apk`.

Always keep the permanent keystore and its passwords safe. Every future APK update must be signed with the same key, otherwise Android will not install it over the existing app.

## First Play Console release

1. Complete the store listing, app access, ads, content rating, target audience, data safety, privacy policy and permissions declarations.
2. Open **Test and release → Closed testing** and create a release.
3. Configure Play App Signing carefully. If existing sideloaded installations must update without uninstalling, the Play-distributed build must use a compatible existing app-signing key. Do not let Google generate an unrelated app-signing key without understanding this consequence.
4. Upload `LATHAEPS_SMART_v3.23.72_Play_Signed.aab`.
5. Add release notes, save, review and start the closed test.
6. New personal developer accounts must meet Play Console's tester requirement before applying for production access.

## Policy items that need special attention

This project requests Contacts, Notification Listener, exact alarms, media access and Accessibility Service access. It also automates deterministic WhatsApp UI actions. Play Console may require declarations, prominent in-app disclosure, consent and a demonstration video. The store listing and declarations must describe the actual behavior accurately.

Target API 35 is accepted for new mobile apps until the Google Play deadline on August 31, 2026. Before that deadline, migrate and fully test target API 36 for later submissions.
