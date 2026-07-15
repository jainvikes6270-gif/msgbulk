# LATHAEPS SMART — Signing & Auto Update

The app now checks `latest-version.json` in `jainvikes6270-gif/lathabulk`, downloads `LathaEPS-Smart.apk` from the latest GitHub Release, and opens Android's update confirmation.

## One-time GitHub signing setup

Create one permanent `lathaeps-release.jks` file and keep it private. Add these repository secrets under **Settings → Secrets and variables → Actions**:

- `LATHAEPS_KEYSTORE_BASE64` — Base64 content of the JKS file
- `LATHAEPS_KEYSTORE_PASSWORD` — Keystore password
- `LATHAEPS_KEY_ALIAS` — Key alias
- `LATHAEPS_KEY_PASSWORD` — Key password

Never upload the JKS file or passwords as normal repository files.

## Publish an update

1. Increase `versionCode` and `versionName` in `app/build.gradle`.
2. Put the same values in `latest-version.json`.
3. Push the project, then create a Git tag such as `v3.21.4`.
4. GitHub Actions builds the signed APK and attaches it to the Release as `LathaEPS-Smart.apk`.

Every future APK must use the same JKS key. Otherwise Android will reject the update over the installed app.
