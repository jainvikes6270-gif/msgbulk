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
import android.os.Build;
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
    public static final String CATALOG_QUEUE="catalog_share_queue", CATALOG_QUEUE_INDEX="catalog_share_index";
    public static final String CATALOG_QUEUE_CAPTION="catalog_share_caption", CATALOG_QUEUE_PACKAGE="catalog_share_package", CATALOG_QUEUE_PHONE="catalog_share_phone";
    public static final String LEDGER_CUSTOMERS="ledger_customers";
    private final Map<String,Long> lastReply=new HashMap<>();
    private final Handler handler=new Handler(Looper.getMainLooper());

    @Override public void onNotificationPosted(StatusBarNotification sbn){
        if(!LicenseManager.isEntitled(this)) return;
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
        String senderPhone=resolvePhoneFromNotification(sbn,n,e,title);
        // Ignore only WhatsApp's group-summary notification. A real customer
        // notification can be titled "Name (2 messages)" and must still run.
        if((n.flags&Notification.FLAG_GROUP_SUMMARY)!=0)return;
        long now=System.currentTimeMillis();
        long wait=p.getInt(COOLDOWN,5)*60_000L;

        String file="", type="", caption="";
        ArrayList<Uri> catalogFiles=new ArrayList<>();
        String lk=p.getString(LEDGER_KEY,"ledger").trim().toLowerCase(Locale.ROOT);
        String ck=p.getString(CATALOG_KEY,"catalog").trim().toLowerCase(Locale.ROOT);
        String command="rule";
        if(!lk.isEmpty() && lower.contains(lk)){
            command="ledger";
            JSONObject customer=findLedgerCustomer(p,title,senderPhone);
            if(customer==null){saveStatus(p,"Ledger not sent • customer/phone not matched: "+title);return;}
            file=customer.optString("ledger_uri","");
            if(file.isEmpty()){saveStatus(p,"Ledger not sent • PDF missing for: "+customer.optString("name",title));return;}
            String customerPhone=digits(customer.optString("phone",""));
            if(!customerPhone.isEmpty())senderPhone=customerPhone.length()==10?"91"+customerPhone:customerPhone;
            type="application/pdf";caption="LATHA EPS Ledger";
        }
        else if(findCatalogsForMessage(lower,ck).length()>0||matchesBusinessKeyword(lower,ck,"catalog","catalogue","catlog")){
            command="catalog";JSONArray catalogs=findCatalogsForMessage(lower,ck);
            if(catalogs.length()==0){sendRemoteReply(n,"Catalog abhi save nahi hai.");return;}
            // Without a jid WhatsApp opens its recipient picker and the Catalog
            // workflow appears to stop. Reuse the verified Ledger/customer map
            // when the notification itself only exposes a saved contact name.
            if(last10(senderPhone).isEmpty()){
                JSONObject recipient=findLedgerCustomer(p,title,senderPhone);
                if(recipient!=null){
                    String customerPhone=digits(recipient.optString("phone",""));
                    if(!customerPhone.isEmpty())senderPhone=customerPhone.length()==10?"91"+customerPhone:customerPhone;
                }
            }
            if(last10(senderPhone).isEmpty()){
                saveStatus(p,"Catalog not sent • sender phone not matched: "+title);
                sendRemoteReply(n,"Catalog ready hai, lekin aapka WhatsApp number match nahi hua.");
                return;
            }
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
            if(n.contentIntent!=null){ try{n.contentIntent.send();}catch(Exception ignored){} }
            final String f=file,t=type,c=caption;
            final ArrayList<Uri> files=new ArrayList<>(catalogFiles);
            final String phone=senderPhone;
            handler.postDelayed(()->{
                // Share a whole Catalog type in one WhatsApp preview. This
                // avoids reopening a separate preview window for every file.
                if(files.size()>1)shareFiles(files,c,pkg,phone);
                else if(files.size()==1)shareFile(files.get(0),t,c,pkg,phone);
                else shareFile(Uri.parse(f),t,c,pkg,phone);
            },1200);
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
            String wanted=normalName(title);
            if(!wanted.isEmpty())for(int i=0;i<a.length();i++){
                JSONObject o=a.optJSONObject(i);if(o==null)continue;
                String saved=normalName(o.optString("name",""));
                if(!saved.isEmpty()&&(saved.equals(wanted)||saved.contains(wanted)||wanted.contains(saved)))return o;
            }
        }catch(Exception ignored){}
        return null;
    }

    private static String normalName(String value){
        if(value==null)return "";
        return value.toLowerCase(Locale.ROOT).replaceAll("\\([0-9]+ messages?\\)","").replaceAll("[^a-z0-9]+"," ").trim();
    }

    private void saveStatus(SharedPreferences p,String status){
        p.edit().putString("last_business_status",status).putLong("last_business_status_at",System.currentTimeMillis()).apply();
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

    private String resolvePhoneFromNotification(StatusBarNotification sbn,Notification n,Bundle extras,String title){
        ArrayList<String> candidates=new ArrayList<>();
        candidates.add(title);
        if(n!=null){if(Build.VERSION.SDK_INT>=26)candidates.add(n.getShortcutId());if(n.getGroup()!=null)candidates.add(n.getGroup());}
        if(sbn!=null){candidates.add(sbn.getKey());candidates.add(sbn.getTag());}
        if(extras!=null){
            candidates.add(String.valueOf(extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE,"")));
            candidates.add(String.valueOf(extras.getCharSequence(Notification.EXTRA_SUB_TEXT,"")));
            candidates.add(String.valueOf(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT,"")));
            try{
                String[] people=extras.getStringArray(Notification.EXTRA_PEOPLE);if(people!=null)for(String person:people)candidates.add(person);
            }catch(Exception ignored){}
            if(Build.VERSION.SDK_INT>=28)try{
                ArrayList<android.app.Person> people=extras.getParcelableArrayList(Notification.EXTRA_PEOPLE_LIST);
                if(people!=null)for(android.app.Person person:people)if(person!=null){candidates.add(person.getUri());candidates.add(person.getKey());candidates.add(String.valueOf(person.getName()));}
            }catch(Exception ignored){}
            try{
                android.os.Parcelable[] messages=extras.getParcelableArray(Notification.EXTRA_MESSAGES);
                if(messages!=null)for(int i=messages.length-1;i>=0;i--){
                    if(!(messages[i] instanceof Bundle))continue;Bundle message=(Bundle)messages[i];
                    candidates.add(String.valueOf(message.getCharSequence("sender","")));
                    if(Build.VERSION.SDK_INT>=28){android.app.Person person=message.getParcelable("sender_person");if(person!=null){candidates.add(person.getUri());candidates.add(person.getKey());candidates.add(String.valueOf(person.getName()));}}
                }
            }catch(Exception ignored){}
        }
        for(String value:candidates){String phone=validIndianPhone(value);if(!phone.isEmpty())return phone;}
        return resolvePhoneFromContacts(title);
    }

    private static String validIndianPhone(String value){
        if(value==null)return "";
        String compact=value.replaceAll("[\\s()\\-]","");
        java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?:\\+?91)?([6-9][0-9]{9})(?![0-9])").matcher(compact);
        return m.find()?"91"+m.group(1):"";
    }

    private String resolvePhoneFromContacts(String title){
        if(checkSelfPermission(android.Manifest.permission.READ_CONTACTS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)return "";
        Cursor c=null;try{
            c=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},null,null,null);
            String wanted=normalName(title),found="";
            if(c!=null)while(c.moveToNext()){
                String name=normalName(c.getString(1));if(wanted.isEmpty()||!name.equals(wanted))continue;
                String phone=validIndianPhone(c.getString(0));if(phone.isEmpty())continue;
                if(!found.isEmpty()&&!last10(found).equals(last10(phone)))return "";found=phone;
            }
            return found;
        }catch(Exception ignored){}finally{if(c!=null)c.close();}
        return "";
    }

    private void shareFile(Uri uri,String mime,String caption,String pkg,String phone){
        try{
            SharedPreferences state=getSharedPreferences(PREFS,MODE_PRIVATE);clearCatalogQueue(state);
            state.edit().putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,System.currentTimeMillis()).apply();
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
            SharedPreferences state=getSharedPreferences(PREFS,MODE_PRIVATE);clearCatalogQueue(state);
            state.edit().putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,System.currentTimeMillis()).apply();
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

    private void prepareCatalogQueue(ArrayList<Uri> uris,String caption,String pkg,String phone){
        JSONArray queue=new JSONArray();for(Uri uri:uris)queue.put(uri.toString());
        getSharedPreferences(PREFS,MODE_PRIVATE).edit()
            .putString(CATALOG_QUEUE,queue.toString()).putInt(CATALOG_QUEUE_INDEX,0)
            .putString(CATALOG_QUEUE_CAPTION,caption==null?"":caption)
            .putString(CATALOG_QUEUE_PACKAGE,pkg==null?"com.whatsapp":pkg)
            .putString(CATALOG_QUEUE_PHONE,phone==null?"":phone)
            .putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,System.currentTimeMillis()).apply();
    }

    public static boolean shareNextCatalogFile(android.content.Context context){
        SharedPreferences p=context.getSharedPreferences(PREFS,MODE_PRIVATE);
        try{
            JSONArray queue=new JSONArray(p.getString(CATALOG_QUEUE,"[]"));int index=p.getInt(CATALOG_QUEUE_INDEX,0);
            if(index<0||index>=queue.length()){clearCatalogQueue(p);return false;}
            wakeScreen(context);
            android.app.KeyguardManager keyguard=(android.app.KeyguardManager)context.getSystemService(android.content.Context.KEYGUARD_SERVICE);
            if(keyguard!=null&&keyguard.isKeyguardLocked()){
                LockScreenSendActivity.open(context,LockScreenSendActivity.MODE_CATALOG);
                p.edit().putString("last_business_status","Screen awake • unlock to continue catalog").putLong("last_business_status_at",System.currentTimeMillis()).apply();
                return true;
            }
            Uri uri=Uri.parse(queue.getString(index));String pkg=p.getString(CATALOG_QUEUE_PACKAGE,"com.whatsapp");String phone=p.getString(CATALOG_QUEUE_PHONE,"");
            Intent i=new Intent(Intent.ACTION_SEND);i.setType(context.getContentResolver().getType(uri)==null?"application/octet-stream":context.getContentResolver().getType(uri));
            i.putExtra(Intent.EXTRA_STREAM,uri);
            if(index==0){String caption=p.getString(CATALOG_QUEUE_CAPTION,"");if(!caption.isEmpty())i.putExtra(Intent.EXTRA_TEXT,caption);}
            i.setClipData(ClipData.newRawUri("catalog file",uri));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setPackage(pkg);if(phone!=null&&!phone.isEmpty())i.putExtra("jid",digits(phone)+"@s.whatsapp.net");
            context.grantUriPermission(pkg,uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);context.startActivity(i);
            p.edit().putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,System.currentTimeMillis()).putString("last_business_status","Sending catalog file "+(index+1)+" / "+queue.length()).apply();
            return true;
        }catch(Exception error){clearCatalogQueue(p);p.edit().putString("last_business_status","Catalog send failed • "+error.getClass().getSimpleName()).apply();return false;}
    }

    public static boolean advanceCatalogQueue(android.content.Context context){
        SharedPreferences p=context.getSharedPreferences(PREFS,MODE_PRIVATE);
        try{
            JSONArray queue=new JSONArray(p.getString(CATALOG_QUEUE,"[]"));int next=p.getInt(CATALOG_QUEUE_INDEX,0)+1;
            if(next>=queue.length()){clearCatalogQueue(p);p.edit().putString("last_business_status","Catalog sent • "+queue.length()+" files").putLong("last_business_status_at",System.currentTimeMillis()).apply();return false;}
            p.edit().putInt(CATALOG_QUEUE_INDEX,next).putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,System.currentTimeMillis()).apply();return true;
        }catch(Exception e){clearCatalogQueue(p);return false;}
    }

    private static void clearCatalogQueue(SharedPreferences p){
        p.edit().putBoolean(PENDING_SHARE,false).remove(CATALOG_QUEUE).remove(CATALOG_QUEUE_INDEX).remove(CATALOG_QUEUE_CAPTION).remove(CATALOG_QUEUE_PACKAGE).remove(CATALOG_QUEUE_PHONE).apply();
    }

    @SuppressWarnings("deprecation") private static void wakeScreen(android.content.Context context){
        try{
            android.os.PowerManager pm=(android.os.PowerManager)context.getSystemService(android.content.Context.POWER_SERVICE);if(pm==null)return;
            android.os.PowerManager.WakeLock lock=pm.newWakeLock(android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK|android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP|android.os.PowerManager.ON_AFTER_RELEASE,"LathaBulk:CatalogWake");
            lock.setReferenceCounted(false);lock.acquire(30000L);
        }catch(Exception ignored){}
    }
}
