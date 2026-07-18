# Supabase setup — LATHAEPS SMART v3.23.30

1. Open your Supabase project.
2. Open **SQL Editor** and paste the complete `SUPABASE_SUBSCRIPTION_SETUP.sql` file.
3. Press **Run** once.
4. The bottom result contains `SAVE_THIS_ADMIN_PASSWORD`. Copy it and keep it private. Do not send it to users.
5. Build/install v3.23.30. On first launch, keep internet on and press **REFRESH ONLINE STATUS** if needed.

## Activate a paying user

1. Ask the user to open **Settings → Subscription & Payment** and send you their 12-character Device ID.
2. On your admin phone, long-press **LATHAEPS SMART** on the Subscription screen.
3. Enter the saved Supabase admin password and the user's Device ID.
4. Select **₹800 • Activate 1 Year** or **Lifetime Free**, then tap **UPDATE ONLINE SUBSCRIPTION**.
5. The user taps **REFRESH ONLINE STATUS**.

The admin password is not stored in the APK. Running the complete SQL setup again rotates the password, so save the newly displayed value each time.
