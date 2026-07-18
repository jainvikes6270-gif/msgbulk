package com.lathaeps.lathabulk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class SubscriptionManager {
    static final String PREFS = "latha_subscription_online_v1";
    static final String UPI_ID = "jainvikes6270@oksbi";
    static final int YEARLY_PRICE = 800;
    static final int TRIAL_DAYS = 12;

    private static final String ALLOWED = "allowed";
    private static final String PLAN = "plan";
    private static final String ACCESS_UNTIL = "access_until_ms";
    private static final String SERVER_TIME = "server_time_ms";
    private static final String DEVICE_TIME = "device_time_ms";
    private static final String LAST_MESSAGE = "last_message";
    private static final long CACHE_TTL = 48L * 60L * 60L * 1000L;
    private static final ExecutorService NETWORK = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    interface Callback { void done(boolean ok, String message); }

    private SubscriptionManager() {}

    static void ensureTrial(Context c) {
        // Trial creation is intentionally server-side. This method remains so older callers compile.
        deviceId(c);
    }

    static String deviceId(Context c) {
        String raw = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (raw == null || raw.trim().isEmpty()) raw = "unknown-device";
        return sha256(raw + "|" + c.getPackageName()).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    static boolean hasAccess(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!p.getBoolean(ALLOWED, false)) return false;
        long serverAtFetch = p.getLong(SERVER_TIME, 0);
        long deviceAtFetch = p.getLong(DEVICE_TIME, 0);
        long now = System.currentTimeMillis();
        if (serverAtFetch <= 0 || deviceAtFetch <= 0) return false;
        if (now + 5L * 60L * 1000L < deviceAtFetch) return false; // device clock moved backwards
        long estimatedServerNow = serverAtFetch + Math.max(0, now - deviceAtFetch);
        if (estimatedServerNow - serverAtFetch > CACHE_TTL) return false;
        long until = p.getLong(ACCESS_UNTIL, 0);
        return until == Long.MAX_VALUE || estimatedServerNow < until;
    }

    static String statusText(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long serverTime = p.getLong(SERVER_TIME, 0);
        if (serverTime == 0) return "Online subscription check required";
        if (!hasFreshCache(p)) return "Status expired • Internet se REFRESH karein";
        String plan = p.getString(PLAN, "trial");
        if (!p.getBoolean(ALLOWED, false)) return p.getString(LAST_MESSAGE, "Plan expired • Payment required");
        if ("lifetime".equals(plan)) return "Lifetime Free • Online verified";
        long leftMs = p.getLong(ACCESS_UNTIL, 0) - estimatedServerNow(p);
        int days = leftMs <= 0 ? 0 : (int)Math.ceil(leftMs / 86400000d);
        if ("yearly".equals(plan)) return "₹800/year • Active • " + days + " day(s) left";
        if ("custom".equals(plan)) return "Custom validity • Active • " + days + " day(s) left";
        return "Free trial • " + days + " day(s) left";
    }

    static void refresh(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        rpc("register_or_get_subscription", new JSONObjectBuilder()
                .put("p_device_id", deviceId(app)).build(), (ok, json, message) -> {
            if (ok && json != null) saveStatus(app, json);
            MAIN.post(() -> callback.done(ok, ok ? statusText(app) : message));
        });
    }

    static void adminSetPlan(Context context, String targetDeviceId, String plan,
                             String adminPassword, Callback callback) {
        String device = targetDeviceId == null ? "" : targetDeviceId.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (device.length() != 12) { callback.done(false, "Valid 12-character Device ID required"); return; }
        if (adminPassword == null || adminPassword.length() < 12) { callback.done(false, "Admin password kam se kam 12 characters ka hona chahiye"); return; }
        rpc("admin_set_subscription", new JSONObjectBuilder()
                .put("p_device_id", device)
                .put("p_plan", plan)
                .put("p_admin_password", adminPassword).build(), (ok, json, message) ->
                MAIN.post(() -> callback.done(ok, ok && json != null ? json.optString("message", "Subscription updated") : message)));
    }

    static void adminExtendDays(Context context,String targetDeviceId,int days,
                                String adminPassword,Callback callback){
        String device=targetDeviceId==null?"":targetDeviceId.replaceAll("[^A-Za-z0-9]","").toUpperCase(Locale.ROOT);
        if(device.length()!=12){callback.done(false,"Valid 12-character Device ID required");return;}
        if(days<1||days>365000){callback.done(false,"Days 1 se 365000 ke beech hone chahiye");return;}
        if(adminPassword==null||adminPassword.length()<12){callback.done(false,"Admin password kam se kam 12 characters ka hona chahiye");return;}
        rpc("admin_extend_subscription",new JSONObjectBuilder()
                .put("p_device_id",device)
                .put("p_days",days)
                .put("p_admin_password",adminPassword).build(),(ok,json,message)->
                MAIN.post(()->callback.done(ok,ok&&json!=null?json.optString("message",days+" days validity added"):message)));
    }

    private static void saveStatus(Context c, JSONObject j) {
        boolean allowed = j.optBoolean("allowed", false);
        String plan = j.optString("plan", "trial");
        long until = j.optLong("access_until_ms", 0);
        if ("lifetime".equals(plan) && allowed) until = Long.MAX_VALUE;
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(ALLOWED, allowed)
                .putString(PLAN, plan)
                .putLong(ACCESS_UNTIL, until)
                .putLong(SERVER_TIME, j.optLong("server_time_ms", System.currentTimeMillis()))
                .putLong(DEVICE_TIME, System.currentTimeMillis())
                .putString(LAST_MESSAGE, j.optString("message", "Plan expired • Payment required"))
                .apply();
    }

    private interface RpcCallback { void done(boolean ok, JSONObject json, String message); }

    private static void rpc(String function, JSONObject body, RpcCallback callback) {
        NETWORK.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(trimSlash(BuildConfig.SUPABASE_URL) + "/rest/v1/rpc/" + function);
                connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY);
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.getOutputStream().write(bytes);
                int code = connection.getResponseCode();
                String response = read(code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream());
                if (code >= 200 && code < 300) callback.done(true, new JSONObject(response), "OK");
                else callback.done(false, null, friendlyError(code, response));
            } catch (Exception e) {
                callback.done(false, null, "Internet/Supabase connection failed. Dobara REFRESH karein.");
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static boolean hasFreshCache(SharedPreferences p) {
        long fetched = p.getLong(DEVICE_TIME, 0);
        long now = System.currentTimeMillis();
        return fetched > 0 && now + 300000L >= fetched && now - fetched <= CACHE_TTL;
    }

    private static long estimatedServerNow(SharedPreferences p) {
        return p.getLong(SERVER_TIME, 0) + Math.max(0, System.currentTimeMillis() - p.getLong(DEVICE_TIME, 0));
    }

    private static String friendlyError(int code, String response) {
        try {
            String msg = new JSONObject(response).optString("message", "");
            if (!msg.isEmpty()) return msg;
        } catch (Exception ignored) {}
        return code == 404 ? "Supabase SQL setup abhi complete nahi hua" : "Server error " + code;
    }

    private static String read(InputStream input) throws Exception {
        if (input == null) return "{}";
        BufferedReader r = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder b = new StringBuilder(); String line;
        while ((line = r.readLine()) != null) b.append(line);
        r.close(); return b.toString();
    }

    private static String trimSlash(String s) { return s.endsWith("/") ? s.substring(0, s.length() - 1) : s; }

    private static String sha256(String value) {
        try {
            byte[] out = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder b = new StringBuilder(); for (byte x : out) b.append(String.format(Locale.ROOT, "%02x", x)); return b.toString();
        } catch (Exception e) { return "00000000000000000000000000000000"; }
    }

    private static final class JSONObjectBuilder {
        private final JSONObject object = new JSONObject();
        JSONObjectBuilder put(String key, Object value) { try { object.put(key, value); } catch (Exception ignored) {} return this; }
        JSONObject build() { return object; }
    }
}
