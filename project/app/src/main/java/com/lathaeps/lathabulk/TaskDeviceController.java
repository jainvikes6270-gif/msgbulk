package com.lathaeps.lathabulk;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;

/** Owns the wake -> user/Android unlock -> send -> re-lock lifecycle for every queue. */
final class TaskDeviceController {
    static final String PREFS = "latha_task_device";
    static final String AUTO_UNLOCK = "auto_unlock_for_task";
    static final String AUTO_RELOCK = "auto_relock_after_task";
    static final String STARTED_LOCKED = "task_started_while_locked";
    static final String ACTIVE = "task_device_cycle_active";
    static final String STARTED_AT = "task_device_cycle_started_at";
    static final String LAST_STATUS = "task_device_last_status";

    private TaskDeviceController() {}

    static boolean autoUnlockEnabled(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(AUTO_UNLOCK, true);
    }

    static boolean autoRelockEnabled(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(AUTO_RELOCK, true);
    }

    static void setAutomaticCycle(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(AUTO_UNLOCK, enabled)
                .putBoolean(AUTO_RELOCK, enabled)
                .putString(LAST_STATUS, enabled ? "Automatic unlock + re-lock ON" : "Automatic unlock + re-lock OFF")
                .apply();
    }

    /** Call once when a send task starts, before checking/opening the keyguard activity. */
    static boolean begin(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean alreadyActive = p.getBoolean(ACTIVE, false)
                && System.currentTimeMillis()-p.getLong(STARTED_AT,0L)<10*60_000L;
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        if (!alreadyActive) {
            p.edit().putBoolean(ACTIVE, true).putBoolean(STARTED_LOCKED, locked).putLong(STARTED_AT,System.currentTimeMillis())
                    .putString(LAST_STATUS, locked ? "Screen locked • opening for task" : "Task started while unlocked")
                    .apply();
        }
        wake(context);
        return locked;
    }

    static boolean shouldRequestUnlock(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return autoUnlockEnabled(context) && km != null && km.isKeyguardLocked();
    }

    static boolean consumeRelockRequest(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean requested = p.getBoolean(ACTIVE, false)
                && p.getBoolean(STARTED_LOCKED, false)
                && p.getBoolean(AUTO_RELOCK, true);
        p.edit().putBoolean(ACTIVE, false).putBoolean(STARTED_LOCKED, false).remove(STARTED_AT)
                .putString(LAST_STATUS, requested ? "Task completed • locking again" : "Task completed")
                .apply();
        return requested;
    }

    static void cancel(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(ACTIVE, false).putBoolean(STARTED_LOCKED, false).remove(STARTED_AT)
                .putString(LAST_STATUS, "Task stopped")
                .apply();
    }

    @SuppressWarnings("deprecation")
    private static void wake(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm == null) return;
            PowerManager.WakeLock lock = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE, "LathaBulk:TaskWake");
            lock.setReferenceCounted(false);
            lock.acquire(45_000L);
        } catch (Exception ignored) {}
    }
}
