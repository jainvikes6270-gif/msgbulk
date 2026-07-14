package com.lathaeps.lathabulk;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AutoReplyNotificationService extends NotificationListenerService {
    public static final String PREFS="auto_reply_prefs", ENABLED="enabled", KEYWORD="keyword", REPLY="reply", IMAGE="image", COOLDOWN="cooldown";
    public static final String LEDGER_URI="ledger_uri", CATALOG_URI="catalog_uri", PRICE_URI="price_uri";
    public static final String LEDGER_KEY="ledger_key", CATALOG_KEY="catalog_key", PRICE_KEY="price_key";
    private final Map<String,Long> lastReply=new HashMap<>();
    private final Handler handler=new Handler(Looper.getMainLooper());

    @Override public void onNotificationPosted(StatusBarNotification sbn){
        if(sbn==null) return;
        String pkg=sbn.getPackageName();
        if(!"com.whatsapp".equals(pkg) && !"com.whatsapp.w4b".equals(pkg)) return;
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        if(!p.getBoolean(ENABLED,false)) return;
        Notification n=sbn.getNotification();
        Bundle e=n.extras;
        String title=String.valueOf(e.getCharSequence(Notification.EXTRA_TITLE,""));
        String text=String.valueOf(e.getCharSequence(Notification.EXTRA_TEXT,""));
        String lower=text.toLowerCase(Locale.ROOT);
        if(title.toLowerCase(Locale.ROOT).contains("messages") || title.toLowerCase(Locale.ROOT).contains("whatsapp")) return;
        long now=System.currentTimeMillis();
        long wait=p.getInt(COOLDOWN,5)*60_000L;
        if(now-lastReply.getOrDefault(title,0L)<wait) return;

        String file="", type="", caption="";
        String lk=p.getString(LEDGER_KEY,"ledger").trim().toLowerCase(Locale.ROOT);
        String ck=p.getString(CATALOG_KEY,"catalog").trim().toLowerCase(Locale.ROOT);
        String pk=p.getString(PRICE_KEY,"price").trim().toLowerCase(Locale.ROOT);
        if(!lk.isEmpty() && lower.contains(lk)){file=p.getString(LEDGER_URI,"");type=p.getString(LEDGER_URI+"_type","application/pdf");caption="LATHA EPS Ledger";}
        else if(!ck.isEmpty() && lower.contains(ck)){file=p.getString(CATALOG_URI,"");type=p.getString(CATALOG_URI+"_type","application/pdf");caption="LATHA EPS Catalog";}
        else if(!pk.isEmpty() && lower.contains(pk)){file=p.getString(PRICE_URI,"");type=p.getString(PRICE_URI+"_type","application/pdf");caption="LATHA EPS Price List";}
        else {
            String key=p.getString(KEYWORD,"").trim().toLowerCase(Locale.ROOT);
            if(key.isEmpty() || !lower.contains(key)) return;
            caption=p.getString(REPLY,"").trim();
            file=p.getString(IMAGE,""); type=p.getString(IMAGE+"_type","image/*");
        }
        lastReply.put(title,now);
        if(!caption.isEmpty()) sendRemoteReply(n,caption);
        if(!file.isEmpty() && n.contentIntent!=null){
            try{n.contentIntent.send();}catch(Exception ignored){}
            final String f=file,t=type,c=caption;
            handler.postDelayed(()->shareFile(Uri.parse(f),t,c,pkg),1400);
        }
    }

    private void sendRemoteReply(Notification n,String message){
        if(n.actions==null) return;
        for(Notification.Action a:n.actions){
            RemoteInput[] inputs=a.getRemoteInputs();
            if(inputs==null || inputs.length==0) continue;
            Intent fill=new Intent(); Bundle b=new Bundle();
            for(RemoteInput r:inputs) b.putCharSequence(r.getResultKey(),message);
            RemoteInput.addResultsToIntent(inputs,fill,b);
            try{a.actionIntent.send(this,0,fill);return;}catch(PendingIntent.CanceledException ignored){}
        }
    }

    private void shareFile(Uri uri,String mime,String caption,String pkg){
        try{
            Intent i=new Intent(Intent.ACTION_SEND);
            i.setType(mime==null||mime.isEmpty()?"application/octet-stream":mime);
            i.putExtra(Intent.EXTRA_STREAM,uri);
            if(!caption.isEmpty()) i.putExtra(Intent.EXTRA_TEXT,caption);
            i.setClipData(ClipData.newRawUri("business file",uri));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setPackage(pkg);
            startActivity(i);
        }catch(Exception ignored){}
    }
}
