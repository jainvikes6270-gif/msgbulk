package com.lathaeps.lathabulk;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
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
    private static PowerManager.WakeLock queueWakeLock;

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if(!SubscriptionManager.hasAccess(this)){stopExpiredQueues(this);return;}
        String pkg = event.getPackageName().toString();
        if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")) return;
        SharedPreferences autoReply = getSharedPreferences(AutoReplyNotificationService.PREFS, MODE_PRIVATE);
        boolean pendingShare = autoReply.getBoolean(AutoReplyNotificationService.PENDING_SHARE, false);
        long pendingAt = autoReply.getLong(AutoReplyNotificationService.PENDING_SHARE_AT, 0L);
        if (pendingShare && System.currentTimeMillis()-pendingAt > 120000L) {
            autoReply.edit().putBoolean(AutoReplyNotificationService.PENDING_SHARE,false).apply();
            TaskDeviceController.cancel(this);
            pendingShare=false;
        }
        SharedPreferences p = getSharedPreferences(MainActivity.AUTO_PREFS, MODE_PRIVATE);
        boolean broadcastRunning=p.getBoolean(MainActivity.BROADCAST_RUNNING,false);
        if(broadcastRunning){
            if(clickLocked)return;
            AccessibilityNodeInfo broadcastRoot=getRootInActiveWindow();
            if(broadcastRoot!=null)handleBroadcast(broadcastRoot,p,pkg);
            return;
        }
        boolean bulkRunning=p.getBoolean(MainActivity.AUTO_RUNNING, false);
        if ((!bulkRunning && !pendingShare) || clickLocked) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        AccessibilityNodeInfo send = findSendButton(root, pkg);
        if (send != null && send.isEnabled() && send.isClickable()) {
            clickLocked = true;
            send.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if(pendingShare){
                boolean more=AutoReplyNotificationService.advanceCatalogQueue(this);
                if(more){
                    handler.postDelayed(()->{clickLocked=false;AutoReplyNotificationService.shareNextCatalogFile(this);},1500);
                }else handler.postDelayed(()->{clickLocked=false;completeDeviceCycle();},1200);
                return;
            }
            int min=p.getInt(MainActivity.AUTO_MIN_DELAY,3);int max=p.getInt(MainActivity.AUTO_MAX_DELAY,7);if(max<min)max=min;
            int delay=(min+new Random().nextInt(max-min+1))*1000;
            int sentIndex=p.getInt(MainActivity.AUTO_INDEX,0);
            String queueToken=p.getString(MainActivity.AUTO_QUEUE_TOKEN,"");
            handler.postDelayed(()->advanceQueue(sentIndex,queueToken), delay);
        }
    }

    private AccessibilityNodeInfo findSendButton(AccessibilityNodeInfo root, String pkg) {
        List<AccessibilityNodeInfo> byId = root.findAccessibilityNodeInfosByViewId(pkg + ":id/send");
        if (byId != null) for (AccessibilityNodeInfo n : byId) if (n != null && n.isClickable()) return n;
        AccessibilityNodeInfo found = findByDescription(root);
        if (found != null) return found;
        return null;
    }

    private void handleBroadcast(AccessibilityNodeInfo root,SharedPreferences p,String pkg){
        int stage=p.getInt(MainActivity.BROADCAST_STAGE,0);String mode=p.getString(MainActivity.BROADCAST_MODE,"text");String listName=p.getString(MainActivity.BROADCAST_LIST_NAME,"").trim();
        if(listName.isEmpty()){finishBroadcast(false,"Broadcast list name missing");return;}
        if(stage==0){AccessibilityNodeInfo editable="share".equals(mode)?findFirstEditable(root):null;if(editable!=null){setNodeText(editable,listName);lockBroadcastStep(p,2,650);return;}AccessibilityNodeInfo search=findActionNode(root,new String[]{"search","खोज","தேடு"});if(search!=null){clickLocked=true;search.performAction(AccessibilityNodeInfo.ACTION_CLICK);p.edit().putInt(MainActivity.BROADCAST_STAGE,1).apply();handler.postDelayed(()->clickLocked=false,650);}return;}
        if(stage==1){AccessibilityNodeInfo editable=findFirstEditable(root);if(editable!=null){setNodeText(editable,listName);lockBroadcastStep(p,2,800);}return;}
        if(stage==2){AccessibilityNodeInfo result=findExactResult(root,listName);if(result!=null){clickLocked=true;clickNodeOrParent(result);p.edit().putInt(MainActivity.BROADCAST_STAGE,3).apply();handler.postDelayed(()->clickLocked=false,900);}return;}
        if(stage==3){
            if("text".equals(mode)){AccessibilityNodeInfo input=findFirstEditable(root);if(input!=null){String message=p.getString(MainActivity.BROADCAST_MESSAGE,"");setNodeText(input,message);lockBroadcastStep(p,4,600);}}
            else{AccessibilityNodeInfo next=findActionNode(root,new String[]{"next","continue","आगे","send","भेजें"});if(next!=null){clickLocked=true;clickNodeOrParent(next);p.edit().putInt(MainActivity.BROADCAST_STAGE,4).apply();handler.postDelayed(()->clickLocked=false,1000);}}
            return;
        }
        if(stage==4){AccessibilityNodeInfo send=findSendButton(root,pkg);if(send==null)send=findActionNode(root,new String[]{"send","भेजें"});if(send!=null&&send.isEnabled()){clickLocked=true;clickNodeOrParent(send);handler.postDelayed(()->finishBroadcast(true,"Broadcast sent once"),900);}}
    }

    private void lockBroadcastStep(SharedPreferences p,int nextStage,long delay){clickLocked=true;p.edit().putInt(MainActivity.BROADCAST_STAGE,nextStage).apply();handler.postDelayed(()->clickLocked=false,delay);}
    private void setNodeText(AccessibilityNodeInfo node,String value){if(node==null)return;android.os.Bundle args=new android.os.Bundle();args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value);node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args);}
    private AccessibilityNodeInfo findFirstEditable(AccessibilityNodeInfo node){if(node==null)return null;if(node.isEditable()&&node.isEnabled())return node;for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo r=findFirstEditable(node.getChild(i));if(r!=null)return r;}return null;}
    private AccessibilityNodeInfo findExactResult(AccessibilityNodeInfo root,String value){List<AccessibilityNodeInfo> nodes=root.findAccessibilityNodeInfosByText(value);if(nodes==null)return null;for(AccessibilityNodeInfo n:nodes){if(n==null||n.isEditable())continue;CharSequence text=n.getText();if(text!=null&&text.toString().trim().equalsIgnoreCase(value))return n;}return null;}
    private AccessibilityNodeInfo findActionNode(AccessibilityNodeInfo node,String[] words){if(node==null)return null;String text=node.getText()==null?"":node.getText().toString().trim().toLowerCase(Locale.ROOT);String desc=node.getContentDescription()==null?"":node.getContentDescription().toString().trim().toLowerCase(Locale.ROOT);for(String word:words){String w=word.toLowerCase(Locale.ROOT);if((text.equals(w)||desc.equals(w)||desc.contains(w))&&(node.isClickable()||clickableParent(node)!=null))return node;}for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo r=findActionNode(node.getChild(i),words);if(r!=null)return r;}return null;}
    private AccessibilityNodeInfo clickableParent(AccessibilityNodeInfo node){AccessibilityNodeInfo p=node==null?null:node.getParent();for(int i=0;p!=null&&i<4;i++){if(p.isClickable())return p;p=p.getParent();}return null;}
    private void clickNodeOrParent(AccessibilityNodeInfo node){if(node==null)return;if(node.isClickable())node.performAction(AccessibilityNodeInfo.ACTION_CLICK);else{AccessibilityNodeInfo p=clickableParent(node);if(p!=null)p.performAction(AccessibilityNodeInfo.ACTION_CLICK);}}
    private void finishBroadcast(boolean success,String label){SharedPreferences p=getSharedPreferences(MainActivity.AUTO_PREFS,MODE_PRIVATE);p.edit().putBoolean(MainActivity.BROADCAST_RUNNING,false).remove(MainActivity.BROADCAST_STAGE).remove(MainActivity.BROADCAST_FILE_URI).remove(MainActivity.BROADCAST_FILE_TYPE).apply();MainActivity.updateProgressNotification(this,success?1:0,1,label,success);clickLocked=false;if(success){Intent done=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);startActivity(done);}}

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

    private void advanceQueue(int sentIndex,String queueToken) {
        SharedPreferences p = getSharedPreferences(MainActivity.AUTO_PREFS, MODE_PRIVATE);
        if(!queueToken.equals(p.getString(MainActivity.AUTO_QUEUE_TOKEN,""))){clickLocked=false;return;}
        int stored=p.getInt(MainActivity.AUTO_INDEX,0);
        if(stored!=sentIndex){clickLocked=false;return;}
        int next=sentIndex+1;
        p.edit().putInt(MainActivity.AUTO_INDEX, next).apply();
        try {
            JSONArray a = new JSONArray(p.getString(MainActivity.AUTO_NUMBERS, "[]"));
            if (next >= a.length()) {
                String old=p.getString(MainActivity.AUTO_HISTORY,"");
                String stamp=new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new Date());
                String entry=stamp+" • Completed "+a.length()+" contacts";
                p.edit().putBoolean(MainActivity.AUTO_RUNNING, false).remove(MainActivity.AUTO_IMAGE_URI).remove(MainActivity.AUTO_IMAGE_TYPE).remove(MainActivity.AUTO_MESSAGES).remove(MainActivity.AUTO_QUEUE_TOKEN).putString(MainActivity.AUTO_HISTORY,entry+(old.isEmpty()?"":"\n"+old)).apply();
                MainActivity.updateProgressNotification(this,a.length(),a.length(),"All contacts completed",true);
                releaseQueueWakeLock();
                clickLocked = false;
                Intent done = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(done);
                handler.postDelayed(this::completeDeviceCycle,1000L);
                return;
            }
        } catch (Exception e) {
            p.edit().putBoolean(MainActivity.AUTO_RUNNING, false).apply();
            releaseQueueWakeLock();
            TaskDeviceController.cancel(this);
            clickLocked = false;
            return;
        }
        clickLocked = false;
        openCurrentChat(this);
    }

    static void openCurrentChat(Context context) {
        if(!SubscriptionManager.hasAccess(context)){stopExpiredQueues(context);return;}
        SharedPreferences p = context.getSharedPreferences(MainActivity.AUTO_PREFS, Context.MODE_PRIVATE);
        if (!p.getBoolean(MainActivity.AUTO_RUNNING, false)) return;
        TaskDeviceController.begin(context);wakeForQueue(context);
        android.app.KeyguardManager keyguard=(android.app.KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        if(keyguard!=null&&keyguard.isKeyguardLocked()){
            if(TaskDeviceController.shouldRequestUnlock(context))LockScreenSendActivity.open(context,LockScreenSendActivity.MODE_BULK);
            return;
        }
        openCurrentChatAfterUnlock(context);
    }

    static void openCurrentChatAfterUnlock(Context context) {
        if(!SubscriptionManager.hasAccess(context)){stopExpiredQueues(context);return;}
        SharedPreferences p = context.getSharedPreferences(MainActivity.AUTO_PREFS, Context.MODE_PRIVATE);
        if (!p.getBoolean(MainActivity.AUTO_RUNNING, false)) return;
        try {
            JSONArray a = new JSONArray(p.getString(MainActivity.AUTO_NUMBERS, "[]"));
            int index = p.getInt(MainActivity.AUTO_INDEX, 0);
            if (index < 0 || index >= a.length()) {
                p.edit().putBoolean(MainActivity.AUTO_RUNNING, false).apply();
                releaseQueueWakeLock();
                TaskDeviceController.cancel(context);
                return;
            }
            String number = a.getString(index);
            String message = p.getString(MainActivity.AUTO_MESSAGE, "");
            String progressName=number;
            try {JSONArray messages=new JSONArray(p.getString(MainActivity.AUTO_MESSAGES,"[]"));if(index<messages.length()&&!messages.optString(index,"").isEmpty())message=messages.optString(index,message);}catch(Exception ignored){}
            try { JSONArray names=new JSONArray(p.getString(MainActivity.AUTO_NAMES,"[]")); String name=index<names.length()?names.getString(index):"";progressName=name.isEmpty()?number:name; message=message.replace("{Name}",name).replace("{name}",name); } catch(Exception ignored) {}
            MainActivity.updateProgressNotification(context,index+1,a.length(),progressName+" • +"+number,false);
            String image=p.getString(MainActivity.AUTO_IMAGE_URI,"");
            if(!image.isEmpty()){
                Uri uri=Uri.parse(image);Intent i=new Intent(Intent.ACTION_SEND);i.setType(p.getString(MainActivity.AUTO_IMAGE_TYPE,"image/jpeg"));i.putExtra(Intent.EXTRA_STREAM,uri);if(!message.isEmpty())i.putExtra(Intent.EXTRA_TEXT,message);i.setClipData(ClipData.newRawUri("recipient image",uri));i.putExtra("jid",number.replaceAll("[^0-9]","")+"@s.whatsapp.net");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try{i.setPackage("com.whatsapp");context.grantUriPermission("com.whatsapp",uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);context.startActivity(i);}catch(Exception first){i.setPackage("com.whatsapp.w4b");context.grantUriPermission("com.whatsapp.w4b",uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);context.startActivity(i);}
            }else{
                String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.name()).replace("+", "%20");Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + number + "?text=" + encoded));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {i.setPackage("com.whatsapp");context.startActivity(i);} catch (Exception first) {i.setPackage("com.whatsapp.w4b");context.startActivity(i);}
            }
        } catch (Exception e) {
            try { JSONArray failed=new JSONArray(p.getString(MainActivity.AUTO_FAILED,"[]")); JSONArray nums=new JSONArray(p.getString(MainActivity.AUTO_NUMBERS,"[]")); int idx=p.getInt(MainActivity.AUTO_INDEX,0); if(idx<nums.length())failed.put(nums.getString(idx)); p.edit().putString(MainActivity.AUTO_FAILED,failed.toString()).putBoolean(MainActivity.AUTO_RUNNING,false).apply(); } catch(Exception ignored) { p.edit().putBoolean(MainActivity.AUTO_RUNNING,false).apply(); }
            releaseQueueWakeLock();
            TaskDeviceController.cancel(context);
        }
    }

    private void completeDeviceCycle(){
        boolean relock=TaskDeviceController.consumeRelockRequest(this);
        if(!relock)return;
        if(android.os.Build.VERSION.SDK_INT>=28){
            boolean locked=performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
            getSharedPreferences(TaskDeviceController.PREFS,MODE_PRIVATE).edit()
                    .putString(TaskDeviceController.LAST_STATUS,locked?"Task completed • device locked again":"Task completed • lock action unavailable").apply();
        }
    }

    @SuppressWarnings("deprecation")
    private static synchronized void wakeForQueue(Context context){
        try{
            PowerManager pm=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            if(pm==null)return;
            if(queueWakeLock==null){
                queueWakeLock=pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE,"LathaBulk:AutoSendScreen");
                queueWakeLock.setReferenceCounted(false);
            }
            if(queueWakeLock.isHeld())queueWakeLock.release();
            queueWakeLock.acquire(120000L);
        }catch(Exception ignored){}
    }

    static synchronized void releaseQueueWakeLock(){
        try{if(queueWakeLock!=null&&queueWakeLock.isHeld())queueWakeLock.release();}catch(Exception ignored){}
    }

    private static void stopExpiredQueues(Context context){
        context.getSharedPreferences(MainActivity.AUTO_PREFS,Context.MODE_PRIVATE).edit()
                .putBoolean(MainActivity.AUTO_RUNNING,false)
                .putBoolean(MainActivity.BROADCAST_RUNNING,false).apply();
        context.getSharedPreferences(AutoReplyNotificationService.PREFS,Context.MODE_PRIVATE).edit()
                .putBoolean(AutoReplyNotificationService.PENDING_SHARE,false)
                .remove(AutoReplyNotificationService.CATALOG_QUEUE)
                .remove(AutoReplyNotificationService.CATALOG_QUEUE_INDEX).apply();
        releaseQueueWakeLock();
        TaskDeviceController.cancel(context);
    }

    @Override public void onInterrupt() { }
}
