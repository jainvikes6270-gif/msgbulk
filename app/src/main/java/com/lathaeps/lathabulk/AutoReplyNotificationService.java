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
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class AutoReplyNotificationService extends NotificationListenerService {
    public static final String PREFS="auto_reply_prefs", ENABLED="enabled", KEYWORD="keyword", REPLY="reply", IMAGE="image", COOLDOWN="cooldown";
    public static final String LEDGER_URI="ledger_uri", CATALOG_URI="catalog_uri";
    public static final String LEDGER_KEY="ledger_key", CATALOG_KEY="catalog_key";
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

        String file="", type="", caption="";
        ArrayList<Uri> catalogFiles=new ArrayList<>();
        String lk=p.getString(LEDGER_KEY,"ledger").trim().toLowerCase(Locale.ROOT);
        String ck=p.getString(CATALOG_KEY,"catalog").trim().toLowerCase(Locale.ROOT);
        String command="rule";
        if(!lk.isEmpty() && lower.contains(lk)){command="ledger";JSONObject customer=findLedgerCustomer(p,title,senderPhone);if(customer==null)return;file=customer.optString("ledger_uri","");if(file.isEmpty())return;type="application/pdf";caption="LATHA EPS Ledger";}
        else if(findCatalogsForMessage(lower,ck).length()>0||matchesBusinessKeyword(lower,ck,"catalog","catalogue","catlog")){
            command="catalog";JSONArray catalogs=findCatalogsForMessage(lower,ck);
            if(catalogs.length()==0){sendRemoteReply(n,"Catalog abhi save nahi hai.");return;}
            JSONObject first=catalogs.optJSONObject(0);if(first==null)return;
            for(int i=0;i<catalogs.length();i++){JSONObject item=catalogs.optJSONObject(i);if(item==null)continue;String uri=item.optString("uri","");if(!uri.isEmpty())catalogFiles.add(Uri.parse(uri));}
            if(catalogFiles.isEmpty())return;
            file=catalogFiles.get(0).toString();type=first.optString("type","application/pdf");
            caption="LATHA EPS "+first.optString("category","Catalog")+" • "+catalogFiles.size()+" file"+(catalogFiles.size()>1?"s":"");
        }
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
        String replyKey=title+"|"+command;
        if(now-lastReply.getOrDefault(replyKey,0L)<wait) return;
        lastReply.put(replyKey,now);
        if(!file.isEmpty()){
            p.edit().putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,now).apply();
            if(n.contentIntent!=null){ try{n.contentIntent.send();}catch(Exception ignored){} }
            final String f=file,t=type,c=caption;
            final ArrayList<Uri> files=new ArrayList<>(catalogFiles);
            final String phone=senderPhone;
            handler.postDelayed(()->{if(files.size()>1)shareFiles(files,c,pkg,phone);else shareFile(Uri.parse(f),t,c,pkg,phone);},1200);
        } else if(!caption.isEmpty()) {
            sendRemoteReply(n,caption);
        }
    }

    private boolean matchesBusinessKeyword(String message,String saved,String... aliases){
        if(saved!=null&&!saved.trim().isEmpty()&&message.contains(saved.trim().toLowerCase(Locale.ROOT)))return true;
        for(String alias:aliases)if(message.contains(alias))return true;
        return false;
    }

    private JSONObject findLedgerCustomer(SharedPreferences p,String title,String senderPhone){
        String td=last10(title), sp=last10(senderPhone);
        try{
            JSONArray a=new JSONArray(p.getString(LEDGER_CUSTOMERS,"[]"));
            for(int i=0;i<a.length();i++){
                JSONObject o=a.optJSONObject(i);if(o==null)continue;
                String ph=last10(o.optString("phone",""));
                if(!ph.isEmpty()&&(ph.equals(td)||ph.equals(sp)))return o;
            }
        }catch(Exception ignored){}
        return null;
    }

    private JSONArray findCatalogsForMessage(String message,String genericKeyword){
        JSONArray found=new JSONArray();
        try{
            JSONArray items=new JSONArray(getSharedPreferences("latha_bulk_prefs",MODE_PRIVATE).getString("catalog_items","[]"));
            JSONObject matched=null;
            for(int i=items.length()-1;i>=0&&matched==null;i--){JSONObject item=items.optJSONObject(i);if(item==null)continue;String terms=item.optString("keywords","")+","+item.optString("category","");for(String term:terms.split("[,;|]"))if(keywordMatches(message,term)){matched=item;break;}}
            if(matched==null&&matchesBusinessKeyword(message,genericKeyword,"catalog","catalogue","catlog")&&items.length()>0)matched=items.optJSONObject(items.length()-1);
            if(matched==null)return found;
            String category=matched.optString("category","Other");
            for(int i=0;i<items.length();i++){JSONObject item=items.optJSONObject(i);if(item!=null&&category.equalsIgnoreCase(item.optString("category","Other"))&&!item.optString("uri","").isEmpty())found.put(item);}
            if(found.length()==0)found.put(matched);
        }catch(Exception ignored){}
        return found;
    }
    private boolean keywordMatches(String message,String raw){
        String term=raw==null?"":raw.trim().toLowerCase(Locale.ROOT);if(term.length()<2)return false;
        String source=" "+message.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim()+" ";String target=" "+term.replaceAll("[^a-z0-9]+"," ").trim()+" ";return !target.trim().isEmpty()&&source.contains(target);
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
            String found="";if(c!=null)while(c.moveToNext()){String d=digits(c.getString(0));if(d.startsWith("0")&&d.length()==11)d=d.substring(1);String ten=last10(d);if(ten.length()!=10)continue;if(!found.isEmpty()&&!last10(found).equals(ten))return "";found=d.length()==10?"91"+d:d;}return found;
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
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("last_business_status","Opening WhatsApp • "+caption).putLong("last_business_status_at",System.currentTimeMillis()).apply();
        }catch(Exception error){
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(PENDING_SHARE,false).putString("last_business_status","Send failed • "+error.getClass().getSimpleName()).putLong("last_business_status_at",System.currentTimeMillis()).apply();
        }
    }

    private void shareFiles(ArrayList<Uri> uris,String caption,String pkg,String phone){
        try{
            Intent i=new Intent(Intent.ACTION_SEND_MULTIPLE);i.setType("*/*");
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);
            if(caption!=null&&!caption.isEmpty())i.putExtra(Intent.EXTRA_TEXT,caption);
            ClipData clips=ClipData.newRawUri("catalog file",uris.get(0));
            for(int x=1;x<uris.size();x++)clips.addItem(new ClipData.Item(uris.get(x)));
            i.setClipData(clips);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            i.setPackage(pkg);
            if(phone!=null&&!phone.isEmpty())i.putExtra("jid",digits(phone)+"@s.whatsapp.net");
            for(Uri uri:uris)grantUriPermission(pkg,uri,Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(i);
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("last_business_status","Opening WhatsApp • "+uris.size()+" catalog files").putLong("last_business_status_at",System.currentTimeMillis()).apply();
        }catch(Exception error){
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(PENDING_SHARE,false).putString("last_business_status","Catalog send failed • "+error.getClass().getSimpleName()).putLong("last_business_status_at",System.currentTimeMillis()).apply();
        }
    }
}
