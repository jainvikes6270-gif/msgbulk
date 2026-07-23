package com.lathaeps.lathabulk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

public class FloatingMicService extends Service {
    public static final String PREF_ENABLED = "floating_voice_mic_enabled";
    public static final String ACTION_STOP = "com.lathaeps.lathabulk.STOP_FLOATING_MIC";
    private static final String CHANNEL = "business_dost_floating_voice";
    private static final int NOTIFICATION_ID = 539;
    private static final int BUBBLE_SIZE_DP = 46;
    private static final float BUBBLE_ALPHA = 0.58f;

    private WindowManager windowManager;
    private View bubble;
    private WindowManager.LayoutParams params;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void start(Context context) {
        Intent intent = new Intent(context, FloatingMicService.class);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
        else context.startService(intent);
    }

    public static void stop(Context context) {
        context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).edit().putBoolean(PREF_ENABLED, false).apply();
        context.stopService(new Intent(context, FloatingMicService.class));
    }

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        Intent open = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(this, 539, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("Business Dost Voice Mic")
                .setContentText("Mic shortcut active • Settings se OFF kar sakte hain")
                .setContentIntent(content)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build());
        showBubble();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stop(this);
            return START_NOT_STICKY;
        }
        if (bubble == null) showBubble();
        else ensureBubbleVisible();
        return START_STICKY;
    }

    private void showBubble() {
        if (bubble != null || !Settings.canDrawOverlays(this)) {
            if (!Settings.canDrawOverlays(this)) stopSelf();
            return;
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        TextView mic = new TextView(this);
        mic.setText("🎤");
        mic.setTextSize(20);
        mic.setGravity(Gravity.CENTER);
        mic.setContentDescription("Business Dost floating voice search");
        mic.setElevation(dp(4));
        mic.setAlpha(BUBBLE_ALPHA);
        GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.argb(185, 22, 105, 190), Color.argb(185, 95, 45, 170)});
        background.setShape(GradientDrawable.OVAL);
        background.setStroke(dp(1), Color.argb(175, 255, 255, 255));
        mic.setBackground(background);
        bubble = mic;

        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(dp(BUBBLE_SIZE_DP), dp(BUBBLE_SIZE_DP), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getInt("floating_mic_x", dp(12));
        params.y = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getInt("floating_mic_y", dp(220));
        clampBubblePosition();
        mic.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY;
            float downX, downY;
            boolean moved;
            @Override public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setAlpha(0.90f);
                    initialX = params.x; initialY = params.y; downX = event.getRawX(); downY = event.getRawY(); moved = false; return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx = (int) (event.getRawX() - downX), dy = (int) (event.getRawY() - downY);
                    if (Math.abs(dx) > dp(5) || Math.abs(dy) > dp(5)) moved = true;
                    params.x = initialX + dx; params.y = initialY + dy;
                    if (windowManager != null) windowManager.updateViewLayout(bubble, params);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.setAlpha(BUBBLE_ALPHA);
                    clampBubblePosition();
                    try { windowManager.updateViewLayout(bubble, params); } catch (Exception ignored) { }
                    saveBubblePosition();
                    if (!moved) openVoiceSearch();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_CANCEL) { view.setAlpha(BUBBLE_ALPHA); return true; }
                return false;
            }
        });
        windowManager.addView(bubble, params);
    }

    private void ensureBubbleVisible() {
        if (bubble == null || params == null || windowManager == null) return;
        clampBubblePosition();
        bubble.setVisibility(View.VISIBLE);
        bubble.setAlpha(BUBBLE_ALPHA);
        try { windowManager.updateViewLayout(bubble, params); } catch (Exception ignored) { }
        saveBubblePosition();
    }

    private void clampBubblePosition() {
        if (params == null) return;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int bubbleSize = dp(BUBBLE_SIZE_DP);
        int edge = dp(6);
        int topSafe = dp(28);
        int bottomSafe = dp(72);
        int maxX = Math.max(edge, screenWidth - bubbleSize - edge);
        int maxY = Math.max(topSafe, screenHeight - bubbleSize - bottomSafe);
        params.x = Math.max(edge, Math.min(params.x, maxX));
        params.y = Math.max(topSafe, Math.min(params.y, maxY));
    }

    private void saveBubblePosition() {
        if (params == null) return;
        getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit()
                .putInt("floating_mic_x", params.x)
                .putInt("floating_mic_y", params.y)
                .apply();
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mainHandler.removeCallbacksAndMessages(null);
        mainHandler.postDelayed(this::ensureBubbleVisible, 250L);
    }

    private void openVoiceSearch() {
        try {
            startActivity(new Intent(this, FloatingVoiceSearchActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
        } catch (Exception ignored) { }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "Floating Voice Mic", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Keeps the Business Dost floating voice shortcut available");
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override public void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (bubble != null && windowManager != null) {
            try { windowManager.removeView(bubble); } catch (Exception ignored) { }
        }
        bubble = null;
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
