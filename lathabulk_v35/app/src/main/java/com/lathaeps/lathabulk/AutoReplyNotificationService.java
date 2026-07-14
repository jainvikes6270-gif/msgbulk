package com.lathaeps.lathabulk;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class AutoReplyNotificationService extends NotificationListenerService {
    public static final String PREFS="auto_reply_prefs", ENABLED="enabled", KEYWORD="keyword", REPLY="reply", IMAGE="image", COOLDOWN="cooldown";
    public static final String LEDGER_URI="ledger_uri", CATALOG_URI="catalog_uri", PRICE_URI="price_uri";
    public static final String LEDGER_KEY="ledger_key", CATALOG_KEY="catalog_key", PRICE_KEY="price_key";
    public static final String PENDING_SHARE="pending_share", PENDING_SHARE_AT="pending_share_at";
    public static final String LEDGER_CUSTOMERS="ledger_customers";
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
        String text=extractMessageText(e);
        String lower=text.toLowerCase(Locale.ROOT);
        String senderPhone=resolvePhoneFromTitle(title);
        if(title.toLowerCase(Locale.ROOT).contains("messages") || title.toLowerCase(Locale.ROOT).contains("whatsapp")) return;
        long now=System.currentTimeMillis();
        long wait=p.getInt(COOLDOWN,5)*60_000L;
        if(now-lastReply.getOrDefault(title,0L)<wait) return;

        String file="", type="", caption="";
        String lk=p.getString(LEDGER_KEY,"ledger").trim().toLowerCase(Locale.ROOT);
        String ck=p.getString(CATALOG_KEY,"catalog").trim().toLowerCase(Locale.ROOT);
        String pk=p.getString(PRICE_KEY,"price").trim().toLowerCase(Locale.ROOT);
        if(!lk.isEmpty() && lower.contains(lk)){boolean customerOk=isLedgerCustomerAllowed(p,title,senderPhone);if(!customerOk)return;file=p.getString(LEDGER_URI,"");type=p.getString(LEDGER_URI+"_type","application/pdf");caption="LATHA EPS Ledger";}
        else if(!ck.isEmpty() && lower.contains(ck)){file=p.getString(CATALOG_URI,"");type=p.getString(CATALOG_URI+"_type","application/pdf");caption="LATHA EPS Catalog";}
        else if(!pk.isEmpty() && lower.contains(pk)){file=p.getString(PRICE_URI,"");type=p.getString(PRICE_URI+"_type","application/pdf");caption="LATHA EPS Price List";}
        else {
            boolean matched=false;
            try{
                JSONArray rules=new JSONArray(p.getString("rules","[]"));
                for(int i=0;i<rules.length();i++){
                    JSONObject r=rules.getJSONObject(i);String key=r.optString("keyword","").trim();if(key.isEmpty())continue;
                    boolean cs=r.optBoolean("case",false);String source=cs?text:text.toLowerCase(Locale.ROOT);String target=cs?key:key.toLowerCase(Locale.ROOT);int mode=r.optInt("match",0);
                    boolean ok=mode==1?source.trim().equals(target):mode==2?source.startsWith(target):mode==3?source.endsWith(target):source.contains(target);
                    if(ok){caption=r.optString("reply","").trim();file=r.optString("image","");type=r.optString("type","image/*");matched=true;break;}
                }
            }catch(Exception ignored){}
            if(!matched){String key=p.getString(KEYWORD,"").trim().toLowerCase(Locale.ROOT);if(key.isEmpty()||!lower.contains(key))return;caption=p.getString(REPLY,"").trim();file=p.getString(IMAGE,"");type=p.getString(IMAGE+"_type","image/*");}
        }
        lastReply.put(title,now);
        if(!file.isEmpty()){
            p.edit().putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,now).apply();
            if(n.contentIntent!=null){ try{n.contentIntent.send();}catch(Exception ignored){} }
            final String f=file,t=type,c=caption;
            final String phone=senderPhone;
            handler.postDelayed(()->shareFile(Uri.parse(f),t,c,pkg,phone),900);
        } else if(!caption.isEmpty()) {
            sendRemoteReply(n,caption);
        }
    }

    private boolean isLedgerCustomerAllowed(SharedPreferences p,String title,String senderPhone){
        String td=last10(title), sp=last10(senderPhone), lowTitle=title==null?"":title.toLowerCase(Locale.ROOT);
        try{
            JSONArray a=new JSONArray(p.getString(LEDGER_CUSTOMERS,"[]"));
            if(a.length()>0){
                for(int i=0;i<a.length();i++){
                    JSONObject o=a.optJSONObject(i);if(o==null)continue;
                    String ph=last10(o.optString("phone",""));
                    String nm=o.optString("name","").trim().toLowerCase(Locale.ROOT);
                    if((!ph.isEmpty()&&(ph.equals(td)||ph.equals(sp)))||(!nm.isEmpty()&&lowTitle.contains(nm)))return true;
                }
                return false;
            }
        }catch(Exception ignored){}
        String lp=last10(p.getString("ledger_phone",""));
        String ln=p.getString("ledger_name","").trim().toLowerCase(Locale.ROOT);
        return (!lp.isEmpty()&&(lp.equals(td)||lp.equals(sp)))||(!ln.isEmpty()&&lowTitle.contains(ln));
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

    private static String digits(String s){return s==null?"":s.replaceAll("[^0-9]","");}
    private static String last10(String s){String d=digits(s);return d.length()>10?d.substring(d.length()-10):d;}

    private String extractMessageText(Bundle e){
        CharSequence direct=e.getCharSequence(Notification.EXTRA_TEXT,"");
        if(direct!=null&&!direct.toString().trim().isEmpty())return direct.toString();
        try{android.os.Parcelable[] msgs=e.getParcelableArray(Notification.EXTRA_MESSAGES);if(msgs!=null&&msgs.length>0){Bundle b=(Bundle)msgs[msgs.length-1];CharSequence t=b.getCharSequence("text");if(t!=null)return t.toString();}}catch(Exception ignored){}
        return "";
    }

    private String resolvePhoneFromTitle(String title){
        String direct=last10(title);if(direct.length()==10)return "91"+direct;
        if(checkSelfPermission(android.Manifest.permission.READ_CONTACTS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)return "";
        Cursor c=null;try{
            c=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" = ?",new String[]{title},null);
            if(c!=null&&c.moveToFirst()){String d=digits(c.getString(0));if(d.startsWith("0")&&d.length()==11)d=d.substring(1);if(d.length()==10)d="91"+d;return d;}
        }catch(Exception ignored){}finally{if(c!=null)c.close();}
        return "";
    }

    private void shareFile(Uri uri,String mime,String caption,String pkg,String phone){
        try{
            Intent i=new Intent(Intent.ACTION_SEND);
            i.setType(mime==null||mime.isEmpty()?"application/octet-stream":mime);
            i.putExtra(Intent.EXTRA_STREAM,uri);
            if(!caption.isEmpty()) i.putExtra(Intent.EXTRA_TEXT,caption);
            i.setClipData(ClipData.newRawUri("business file",uri));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            i.setPackage(pkg);
            if(phone!=null&&!phone.isEmpty())i.putExtra("jid",digits(phone)+"@s.whatsapp.net");
            grantUriPermission(pkg,uri,Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(i);
        }catch(Exception ignored){
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(PENDING_SHARE,false).apply();
        }
    }
}
