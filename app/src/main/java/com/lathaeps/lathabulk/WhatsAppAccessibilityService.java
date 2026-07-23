package com.lathaeps.lathabulk;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.graphics.Rect;
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
    private long lastRetryToken = 0L;
    private static PowerManager.WakeLock queueWakeLock;

    @Override protected void onServiceConnected(){
        super.onServiceConnected();clickLocked=false;
        handler.postDelayed(()->{
            AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null||root.getPackageName()==null)return;
            String pkg=root.getPackageName().toString();if(!pkg.equals("com.whatsapp")&&!pkg.equals("com.whatsapp.w4b"))return;
            SharedPreferences a=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);SharedPreferences p=getSharedPreferences(MainActivity.AUTO_PREFS,MODE_PRIVATE);
            boolean pending=a.getBoolean(AutoReplyNotificationService.PENDING_SHARE,false),bulk=p.getBoolean(MainActivity.AUTO_RUNNING,false);
            long pendingAt=a.getLong(AutoReplyNotificationService.PENDING_SHARE_AT,0L);
            if(pending&&(pendingAt<=0L||System.currentTimeMillis()-pendingAt>120000L)){
                AutoReplyNotificationService.cancelPendingShare(this,"Old pending attachment cleared • ready");pending=false;
            }
            if(pending||bulk)attemptPendingSend(pkg,pending,bulk);
        },650L);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if(!SubscriptionManager.hasAccess(this)){stopExpiredQueues(this);return;}
        String pkg = event.getPackageName().toString();
        if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")) return;
        SharedPreferences autoReply = getSharedPreferences(AutoReplyNotificationService.PREFS, MODE_PRIVATE);
        boolean pendingShare = autoReply.getBoolean(AutoReplyNotificationService.PENDING_SHARE, false);
        long pendingAt = autoReply.getLong(AutoReplyNotificationService.PENDING_SHARE_AT, 0L);
        if (pendingShare && System.currentTimeMillis()-pendingAt > 120000L) {
            autoReply.edit().putBoolean(AutoReplyNotificationService.PENDING_SHARE,false).putBoolean(AutoReplyNotificationService.PREPARING_SHARE,false).apply();
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
        attemptPendingSend(pkg, pendingShare, bulkRunning);
    }

    /** Retry because some WhatsApp builds publish the final preview tree late. */
    private void attemptPendingSend(String pkg, boolean pendingShare, boolean bulkRunning) {
        if (clickLocked) return;
        SharedPreferences autoReply = getSharedPreferences(AutoReplyNotificationService.PREFS, MODE_PRIVATE);
        SharedPreferences p = getSharedPreferences(MainActivity.AUTO_PREFS, MODE_PRIVATE);
        pendingShare = pendingShare && autoReply.getBoolean(AutoReplyNotificationService.PENDING_SHARE, false);
        bulkRunning = bulkRunning && p.getBoolean(MainActivity.AUTO_RUNNING, false);
        if(bulkRunning&&pendingShare){
            AutoReplyNotificationService.cancelPendingShare(this,"Pending Ledger/Catalog cleared • manual list has priority");
            pendingShare=false;
        }
        if (!pendingShare && !bulkRunning) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { scheduleSendRetry(pkg); return; }
        if(pendingShare && handleShareRecipientPicker(root,autoReply,pkg)){
            scheduleSendRetry(pkg);
            return;
        }
        AccessibilityNodeInfo send = findSendButton(root, pkg);
        if (send != null && send.isEnabled()) {
            clickLocked = true;
            if(!clickNodeOrParent(send)){clickLocked=false;return;}
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
        } else {
            scheduleSendRetry(pkg);
        }
    }

    private void scheduleSendRetry(String pkg) {
        final long token=++lastRetryToken;
        handler.postDelayed(()->retryPendingSend(pkg,token),500L);
    }

    private void retryPendingSend(String pkg,long token){
        if(token!=lastRetryToken||clickLocked)return;
        SharedPreferences a=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        SharedPreferences p=getSharedPreferences(MainActivity.AUTO_PREFS,MODE_PRIVATE);
        boolean pending=a.getBoolean(AutoReplyNotificationService.PENDING_SHARE,false);
        boolean bulk=p.getBoolean(MainActivity.AUTO_RUNNING,false);
        if(pending||bulk)attemptPendingSend(pkg,pending,bulk);
    }

    private AccessibilityNodeInfo findSendButton(AccessibilityNodeInfo root, String pkg) {
        String[] ids={"send","send_button","media_send","send_btn","send_media","send_message","fab"};
        for(String id:ids){
            List<AccessibilityNodeInfo> byId=root.findAccessibilityNodeInfosByViewId(pkg+":id/"+id);
            if(byId!=null)for(AccessibilityNodeInfo n:byId)if(n!=null&&n.isEnabled()&&(n.isClickable()||clickableParent(n)!=null))return n;
        }
        AccessibilityNodeInfo found = findByDescription(root);
        if (found != null) return found;
        found=findActionNode(root,new String[]{"send","send message","send media","send photo","send document","भेजें","அனுப்பு"});
        if(found!=null)return found;
        return findBottomRightSendCandidate(root,root);
    }

    /** Handles WhatsApp's "Send to…" screen when the jid extra is ignored. */
    private boolean handleShareRecipientPicker(AccessibilityNodeInfo root,SharedPreferences prefs,String pkg){
        if(!isShareRecipientPicker(root))return false;
        int stage=prefs.getInt(AutoReplyNotificationService.SHARE_PICKER_STAGE,0);
        String phone=last10(prefs.getString(AutoReplyNotificationService.CATALOG_QUEUE_PHONE,""));
        String contact=prefs.getString(AutoReplyNotificationService.CATALOG_QUEUE_CONTACT,"").trim();
        String query=!phone.isEmpty()?phone:(!contact.equalsIgnoreCase("null")?contact:"");
        if(query.isEmpty()){
            prefs.edit().putString("last_business_status","Send stopped • recipient phone/name not available").apply();
            return true;
        }
        if(stage==0){
            AccessibilityNodeInfo edit=findFirstEditable(root);
            if(edit!=null){setNodeText(edit,query);lockSharePickerStep(prefs,2,700);return true;}
            AccessibilityNodeInfo search=findActionNode(root,new String[]{"search","search name or number","खोज","தேடு"});
            if(search!=null){clickLocked=true;clickNodeOrParent(search);prefs.edit().putInt(AutoReplyNotificationService.SHARE_PICKER_STAGE,1).apply();handler.postDelayed(()->clickLocked=false,500);return true;}
            return true;
        }
        if(stage==1){
            AccessibilityNodeInfo edit=findFirstEditable(root);
            if(edit!=null){setNodeText(edit,query);lockSharePickerStep(prefs,2,700);}
            return true;
        }
        if(stage==2){
            AccessibilityNodeInfo result=findRecipientResult(root,contact,phone);
            if(result!=null){clickLocked=true;clickNodeOrParent(result);prefs.edit().putInt(AutoReplyNotificationService.SHARE_PICKER_STAGE,3).putInt(AutoReplyNotificationService.SHARE_PICKER_TRIES,0).apply();handler.postDelayed(()->clickLocked=false,650);}
            else{int tries=prefs.getInt(AutoReplyNotificationService.SHARE_PICKER_TRIES,0)+1;prefs.edit().putInt(AutoReplyNotificationService.SHARE_PICKER_TRIES,tries).apply();if(tries>=12)AutoReplyNotificationService.cancelPendingShare(this,"Recipient not matched • nothing sent");}
            return true;
        }
        AccessibilityNodeInfo next=findActionNode(root,new String[]{"next","continue","send","आगे","भेजें","அடுத்து","அனுப்பு"});
        if(next==null)next=findBottomRightSendCandidate(root,root);
        if(next!=null){clickLocked=true;clickNodeOrParent(next);prefs.edit().putInt(AutoReplyNotificationService.SHARE_PICKER_STAGE,4).apply();handler.postDelayed(()->clickLocked=false,850);}
        return true;
    }

    private void lockSharePickerStep(SharedPreferences p,int stage,long delay){
        clickLocked=true;p.edit().putInt(AutoReplyNotificationService.SHARE_PICKER_STAGE,stage).apply();handler.postDelayed(()->clickLocked=false,delay);
    }

    private boolean isShareRecipientPicker(AccessibilityNodeInfo root){
        return treeContains(root,new String[]{"send to","share to","select contact","search name or number","frequently contacted","recent chats","भेजें इसे","संपर्क चुनें","அனுப்ப வேண்டியவர்"});
    }

    private boolean treeContains(AccessibilityNodeInfo node,String[] needles){
        if(node==null)return false;
        String text=((node.getText()==null?"":node.getText().toString())+" "+(node.getContentDescription()==null?"":node.getContentDescription().toString())).toLowerCase(Locale.ROOT);
        for(String needle:needles)if(text.contains(needle.toLowerCase(Locale.ROOT)))return true;
        for(int i=0;i<node.getChildCount();i++)if(treeContains(node.getChild(i),needles))return true;
        return false;
    }

    private AccessibilityNodeInfo findRecipientResult(AccessibilityNodeInfo root,String contact,String phone){
        // Substring search can select a different contact with a similar name/number.
        // Walk the result tree and accept only an exact 10-digit phone or exact full name.
        return findExactRecipientIdentity(root,contact,phone);
    }

    private boolean isUsableRecipientNode(AccessibilityNodeInfo n){return n!=null&&!n.isEditable()&&n.isEnabled()&&(n.isClickable()||clickableParent(n)!=null);}

    private AccessibilityNodeInfo findExactRecipientIdentity(AccessibilityNodeInfo node,String contact,String phone){
        if(node==null)return null;
        String text=node.getText()==null?"":node.getText().toString().trim();String digits=text.replaceAll("[^0-9]","");String ten=digits.length()>10?digits.substring(digits.length()-10):digits;String wantedName=normaliseIdentity(contact),actualName=normaliseIdentity(text);
        boolean phoneMatch=phone!=null&&phone.length()==10&&phone.equals(ten);boolean nameMatch=!wantedName.isEmpty()&&wantedName.equals(actualName);
        if((phoneMatch||nameMatch)&&isUsableRecipientNode(node))return node;
        for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo r=findExactRecipientIdentity(node.getChild(i),contact,phone);if(r!=null)return r;}
        return null;
    }

    private String normaliseIdentity(String value){return value==null?"":value.toLowerCase(Locale.ROOT).replaceAll("(?i)\\s*\\([0-9]+\\s+(?:new\\s+)?messages?\\)\\s*$","").replaceAll("[^\\p{L}\\p{N}]+"," ").trim();}

    private String last10(String value){String d=value==null?"":value.replaceAll("[^0-9]","");return d.length()>10?d.substring(d.length()-10):d;}

    /** Last resort for WhatsApp versions whose Send icon has no public ID/label. */
    private AccessibilityNodeInfo findBottomRightSendCandidate(AccessibilityNodeInfo root,AccessibilityNodeInfo node){
        if(root==null||node==null)return null;
        Rect screen=new Rect();root.getBoundsInScreen(screen);
        return findBottomRightSendCandidate(node,screen,null);
    }

    private AccessibilityNodeInfo findBottomRightSendCandidate(AccessibilityNodeInfo node,Rect screen,AccessibilityNodeInfo best){
        if(node==null)return best;
        Rect b=new Rect();node.getBoundsInScreen(b);
        CharSequence raw=node.getContentDescription();String desc=raw==null?"":raw.toString().toLowerCase(Locale.ROOT);
        String cls=node.getClassName()==null?"":node.getClassName().toString();
        boolean excluded=desc.contains("camera")||desc.contains("attach")||desc.contains("emoji")||desc.contains("voice")||desc.contains("microphone")||desc.contains("gallery");
        boolean button=cls.contains("Button")||cls.contains("ImageView")||node.isClickable();
        boolean right=b.centerX()>screen.left+(screen.width()*65/100);
        boolean bottom=b.centerY()>screen.top+(screen.height()*55/100);
        boolean usable=node.isEnabled()&&!excluded&&button&&(node.isClickable()||clickableParent(node)!=null)&&b.width()>=32&&b.height()>=32;
        if(right&&bottom&&usable){
            if(best==null)best=node;
            else{Rect old=new Rect();best.getBoundsInScreen(old);if(b.centerX()+b.centerY()>old.centerX()+old.centerY())best=node;}
        }
        for(int i=0;i<node.getChildCount();i++)best=findBottomRightSendCandidate(node.getChild(i),screen,best);
        return best;
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
    private boolean clickNodeOrParent(AccessibilityNodeInfo node){if(node==null)return false;if(node.isClickable())return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);AccessibilityNodeInfo p=clickableParent(node);return p!=null&&p.performAction(AccessibilityNodeInfo.ACTION_CLICK);}
    private void finishBroadcast(boolean success,String label){SharedPreferences p=getSharedPreferences(MainActivity.AUTO_PREFS,MODE_PRIVATE);p.edit().putBoolean(MainActivity.BROADCAST_RUNNING,false).remove(MainActivity.BROADCAST_STAGE).remove(MainActivity.BROADCAST_FILE_URI).remove(MainActivity.BROADCAST_FILE_TYPE).apply();MainActivity.updateProgressNotification(this,success?1:0,1,label,success);clickLocked=false;if(success){Intent done=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);startActivity(done);}}

    private AccessibilityNodeInfo findByDescription(AccessibilityNodeInfo node) {
        if (node == null) return null;
        CharSequence d = node.getContentDescription();
        if (d != null) {
            String x = d.toString().trim().toLowerCase();
            if ((x.equals("send") || x.equals("भेजें") || x.equals("அனுப்பு") || x.contains("send")) && node.isEnabled() && (node.isClickable()||clickableParent(node)!=null)) return node;
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
                .putBoolean(AutoReplyNotificationService.PREPARING_SHARE,false)
                .remove(AutoReplyNotificationService.CATALOG_QUEUE)
                .remove(AutoReplyNotificationService.CATALOG_QUEUE_INDEX)
                .remove(AutoReplyNotificationService.CATALOG_QUEUE_CONTACT)
                .remove(AutoReplyNotificationService.SHARE_PICKER_STAGE)
                .remove(AutoReplyNotificationService.SHARE_PICKER_TRIES).apply();
        releaseQueueWakeLock();
        TaskDeviceController.cancel(context);
    }

    @Override public void onInterrupt() { }
}
