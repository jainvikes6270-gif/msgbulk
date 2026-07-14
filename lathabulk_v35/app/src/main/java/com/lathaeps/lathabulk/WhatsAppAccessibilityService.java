package com.lathaeps.lathabulk;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WhatsAppAccessibilityService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean clickLocked = false;
    private boolean pendingScanScheduled = false;

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")) return;
        SharedPreferences autoReply = getSharedPreferences(AutoReplyNotificationService.PREFS, MODE_PRIVATE);
        boolean pendingShare = autoReply.getBoolean(AutoReplyNotificationService.PENDING_SHARE, false);
        long pendingAt = autoReply.getLong(AutoReplyNotificationService.PENDING_SHARE_AT, 0L);
        if (pendingShare && System.currentTimeMillis()-pendingAt > 30000L) {
            autoReply.edit().putBoolean(AutoReplyNotificationService.PENDING_SHARE,false).apply();
            pendingShare=false;
        }
        SharedPreferences p = getSharedPreferences(MainActivity.AUTO_PREFS, MODE_PRIVATE);
        boolean bulkRunning=p.getBoolean(MainActivity.AUTO_RUNNING, false);
        if ((!bulkRunning && !pendingShare) || clickLocked) return;
        if(pendingShare && System.currentTimeMillis()-pendingAt < 1100L){
            schedulePendingShareScan(1200L-(System.currentTimeMillis()-pendingAt));
            return;
        }
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        AccessibilityNodeInfo send = findSendButton(root, pkg);
        if (send != null && send.isEnabled() && send.isClickable()) {
            clickLocked = true;
            send.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if(pendingShare){
                autoReply.edit().putBoolean(AutoReplyNotificationService.PENDING_SHARE,false).apply();
                handler.postDelayed(()->clickLocked=false,1200);
                return;
            }
            int min=p.getInt(MainActivity.AUTO_MIN_DELAY,3);int max=p.getInt(MainActivity.AUTO_MAX_DELAY,7);if(max<min)max=min;
            int delay=(min+new Random().nextInt(max-min+1))*1000;
            handler.postDelayed(this::advanceQueue, delay);
        }
    }

    private void schedulePendingShareScan(long delay){
        if(pendingScanScheduled)return;
        pendingScanScheduled=true;
        handler.postDelayed(()->{
            pendingScanScheduled=false;
            SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
            if(!p.getBoolean(AutoReplyNotificationService.PENDING_SHARE,false)||clickLocked)return;
            AccessibilityNodeInfo root=getRootInActiveWindow();
            if(root==null)return;
            String pkg=root.getPackageName()==null?"com.whatsapp":root.getPackageName().toString();
            AccessibilityNodeInfo send=findSendButton(root,pkg);
            if(send!=null&&send.isEnabled()&&send.isClickable()){
                clickLocked=true;send.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                p.edit().putBoolean(AutoReplyNotificationService.PENDING_SHARE,false).apply();
                handler.postDelayed(()->clickLocked=false,1200);
            }
        },Math.max(100L,delay));
    }

    private AccessibilityNodeInfo findSendButton(AccessibilityNodeInfo root, String pkg) {
        List<AccessibilityNodeInfo> byId = root.findAccessibilityNodeInfosByViewId(pkg + ":id/send");
        if (byId != null) for (AccessibilityNodeInfo n : byId) if (n != null && n.isClickable()) return n;
        AccessibilityNodeInfo found = findByDescription(root);
        if (found != null) return found;
        return null;
    }

    private AccessibilityNodeInfo findByDescription(AccessibilityNodeInfo node) {
        if (node == null) return null;
        CharSequence d = node.getContentDescription();
        if (d != null) {
            String x = d.toString().trim().toLowerCase();
            if ((x.equals("send") || x.equals("भेजें") || x.contains("send")) && node.isClickable()) return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo r = findByDescription(node.getChild(i));
            if (r != null) return r;
        }
        return null;
    }

    private void advanceQueue() {
        SharedPreferences p = getSharedPreferences(MainActivity.AUTO_PREFS, MODE_PRIVATE);
        int next = p.getInt(MainActivity.AUTO_INDEX, 0) + 1;
        p.edit().putInt(MainActivity.AUTO_INDEX, next).apply();
        try {
            JSONArray a = new JSONArray(p.getString(MainActivity.AUTO_NUMBERS, "[]"));
            if (next >= a.length()) {
                String old=p.getString(MainActivity.AUTO_HISTORY,"");
                String stamp=new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new Date());
                String entry=stamp+" • Completed "+a.length()+" contacts";
                p.edit().putBoolean(MainActivity.AUTO_RUNNING, false).putString(MainActivity.AUTO_HISTORY,entry+(old.isEmpty()?"":"\n"+old)).apply();
                clickLocked = false;
                Intent done = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(done);
                return;
            }
        } catch (Exception e) {
            p.edit().putBoolean(MainActivity.AUTO_RUNNING, false).apply();
            clickLocked = false;
            return;
        }
        clickLocked = false;
        openCurrentChat(this);
    }

    static void openCurrentChat(Context context) {
        SharedPreferences p = context.getSharedPreferences(MainActivity.AUTO_PREFS, Context.MODE_PRIVATE);
        if (!p.getBoolean(MainActivity.AUTO_RUNNING, false)) return;
        try {
            JSONArray a = new JSONArray(p.getString(MainActivity.AUTO_NUMBERS, "[]"));
            int index = p.getInt(MainActivity.AUTO_INDEX, 0);
            if (index < 0 || index >= a.length()) {
                p.edit().putBoolean(MainActivity.AUTO_RUNNING, false).apply();
                return;
            }
            String number = a.getString(index);
            String message = p.getString(MainActivity.AUTO_MESSAGE, "");
            try { JSONArray names=new JSONArray(p.getString(MainActivity.AUTO_NAMES,"[]")); String name=index<names.length()?names.getString(index):""; message=message.replace("{Name}",name).replace("{name}",name); } catch(Exception ignored) {}
            String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.name()).replace("+", "%20");
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + number + "?text=" + encoded));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                i.setPackage("com.whatsapp");
                context.startActivity(i);
            } catch (Exception first) {
                i.setPackage("com.whatsapp.w4b");
                context.startActivity(i);
            }
        } catch (Exception e) {
            try { JSONArray failed=new JSONArray(p.getString(MainActivity.AUTO_FAILED,"[]")); JSONArray nums=new JSONArray(p.getString(MainActivity.AUTO_NUMBERS,"[]")); int idx=p.getInt(MainActivity.AUTO_INDEX,0); if(idx<nums.length())failed.put(nums.getString(idx)); p.edit().putString(MainActivity.AUTO_FAILED,failed.toString()).putBoolean(MainActivity.AUTO_RUNNING,false).apply(); } catch(Exception ignored) { p.edit().putBoolean(MainActivity.AUTO_RUNNING,false).apply(); }
        }
    }

    @Override public void onInterrupt() { }
}
