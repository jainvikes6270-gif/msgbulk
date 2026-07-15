package com.lathaeps.lathabulk;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

final class LicenseManager {
    static final int TRIAL_DAYS = 7;
    static final int YEARLY_PRICE = 700;
    static final String OWNER_PIN = "LATHA700";
    private static final String PREFS = "lathaeps_licence";
    private static final String INSTALL_AT = "trial_started_at";
    private static final String ACTIVATION = "activation_code";
    private static final String LAST_CHECK = "last_licence_check";
    private static final String SECRET = "LATHAEPS-SMART-2026-OWNER-LICENCE";
    private static final long DAY = 24L * 60L * 60L * 1000L;

    private LicenseManager() {}

    static void ensureTrialStarted(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (p.getLong(INSTALL_AT, 0L) == 0L) p.edit().putLong(INSTALL_AT, System.currentTimeMillis()).apply();
    }

    static String deviceId(Context context) {
        String raw = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (raw == null || raw.trim().isEmpty()) raw = "unknown-device";
        String h = sha256("DEVICE|" + raw + "|" + context.getPackageName()).substring(0, 12).toUpperCase(Locale.ROOT);
        return h.substring(0, 4) + "-" + h.substring(4, 8) + "-" + h.substring(8, 12);
    }

    static boolean isEntitled(Context context) {
        ensureTrialStarted(context);
        long now = System.currentTimeMillis();
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long last = p.getLong(LAST_CHECK, 0L);
        if (last > 0L && now + DAY < last) return false;
        if (last == 0L || now > last + 6L * 60L * 60L * 1000L)
            p.edit().putLong(LAST_CHECK, Math.max(last, now)).apply();
        if (isActivationValid(context, p.getString(ACTIVATION, ""))) return true;
        return now < p.getLong(INSTALL_AT, now) + TRIAL_DAYS * DAY;
    }

    static boolean activate(Context context, String code) {
        String clean = code == null ? "" : code.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        if (!isActivationValid(context, clean)) return false;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(ACTIVATION, clean).apply();
        return true;
    }

    static String status(Context context) {
        ensureTrialStarted(context);
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String code = p.getString(ACTIVATION, "");
        if (isActivationValid(context, code)) {
            String[] part = code.split("-");
            if (part.length == 3 && "F".equals(part[0])) return "Free Lifetime • Active";
            if (part.length == 3) {
                long expiryDay = parseLong(part[1]);
                long days = Math.max(0L, expiryDay - epochDay());
                return "Yearly ₹" + YEARLY_PRICE + " • Active • " + days + " days left";
            }
        }
        long left = p.getLong(INSTALL_AT, System.currentTimeMillis()) + TRIAL_DAYS * DAY - System.currentTimeMillis();
        if (left > 0L) return "Free Trial • " + Math.max(1L, (left + DAY - 1L) / DAY) + " days left";
        return "Expired • Activate ₹" + YEARLY_PRICE + "/year";
    }

    static String generateCode(String targetDeviceId, boolean freeLifetime) {
        String device = normalizeDevice(targetDeviceId);
        if (device.length() != 12) return "";
        String type = freeLifetime ? "F" : "Y";
        long expiryDay = freeLifetime ? 0L : epochDay() + 365L;
        String signature = signature(device, type, expiryDay);
        return type + "-" + expiryDay + "-" + signature;
    }

    private static boolean isActivationValid(Context context, String code) {
        if (code == null || code.trim().isEmpty()) return false;
        String[] part = code.trim().toUpperCase(Locale.ROOT).split("-");
        if (part.length != 3 || !("F".equals(part[0]) || "Y".equals(part[0]))) return false;
        long expiryDay = parseLong(part[1]);
        if ("F".equals(part[0]) && expiryDay != 0L) return false;
        if ("Y".equals(part[0]) && expiryDay < epochDay()) return false;
        String device = normalizeDevice(deviceId(context));
        return signature(device, part[0], expiryDay).equals(part[2]);
    }

    private static String signature(String device, String type, long expiryDay) {
        return sha256(SECRET + "|" + device + "|" + type + "|" + expiryDay).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String normalizeDevice(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private static long epochDay() { return System.currentTimeMillis() / DAY; }
    private static long parseLong(String value) { try { return Long.parseLong(value); } catch (Exception e) { return -1L; } }
    private static String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            return out.toString();
        } catch (Exception e) { return "00000000000000000000000000000000"; }
    }
}
