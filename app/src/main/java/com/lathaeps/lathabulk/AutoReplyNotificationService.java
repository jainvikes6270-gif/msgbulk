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
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.ArrayList;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class AutoReplyNotificationService extends NotificationListenerService {
    public static final String PREFS="auto_reply_prefs", ENABLED="enabled", KEYWORD="keyword", REPLY="reply", IMAGE="image";
    public static final String LEDGER_URI="ledger_uri", CATALOG_URI="catalog_uri";
    public static final String LEDGER_KEY="ledger_key", CATALOG_KEY="catalog_key";
    public static final String PENDING_SHARE="pending_share", PENDING_SHARE_AT="pending_share_at";
    public static final String PREPARING_SHARE="preparing_share";
    public static final String CATALOG_QUEUE="catalog_share_queue", CATALOG_QUEUE_INDEX="catalog_share_index";
    public static final String CATALOG_QUEUE_CAPTION="catalog_share_caption", CATALOG_QUEUE_PACKAGE="catalog_share_package", CATALOG_QUEUE_PHONE="catalog_share_phone";
    public static final String CATALOG_QUEUE_CONTACT="catalog_share_contact", SHARE_PICKER_STAGE="catalog_share_picker_stage", SHARE_PICKER_TRIES="catalog_share_picker_tries";
    public static final String LEDGER_CUSTOMERS="ledger_customers";
    private static final String APP_PREFS="latha_bulk_prefs";
    private static final String PRICE_SOURCE_FILES="price_source_files";
    private static final String RECENT_EVENTS="recent_notification_events";
    private static final long EVENT_FALLBACK_WINDOW_MS=20000L;
    private static final long EVENT_HISTORY_MS=600000L;
    private final Handler handler=new Handler(Looper.getMainLooper());

    @Override public void onNotificationPosted(StatusBarNotification sbn){
        if(sbn==null) return;
        if(!SubscriptionManager.hasAccess(this)) return;
        String pkg=sbn.getPackageName();
        if(!"com.whatsapp".equals(pkg) && !"com.whatsapp.w4b".equals(pkg)) return;
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        if(!p.getBoolean(ENABLED,false)) return;
        Notification n=sbn.getNotification();
        Bundle e=n.extras;
        String title=String.valueOf(e.getCharSequence(Notification.EXTRA_TITLE,""));
        if(isWhatsAppGroupNotification(sbn,n,e)){
            saveStatus(p,"Group message ignored • individual chats only");
            return;
        }
        String text=extractMessageText(e);
        if(text.trim().isEmpty())return;
        String lower=text.toLowerCase(Locale.ROOT);
        String senderPhone=resolvePhoneFromNotification(sbn,n,e,title);
        String titleLower=title.trim().toLowerCase(Locale.ROOT);
        if((n.flags&Notification.FLAG_GROUP_SUMMARY)!=0||titleLower.equals("whatsapp")||titleLower.matches("[0-9]+\\s+new messages?"))return;
        long messageTime=extractLatestMessageTime(e);
        String lk=p.getString(LEDGER_KEY,"ledger").trim().toLowerCase(Locale.ROOT);
        String ck=p.getString(CATALOG_KEY,"catalog").trim().toLowerCase(Locale.ROOT);
        JSONArray priceFiles=findPriceSourcesForMessage(lower);
        boolean explicitPriceRequest=isExplicitPriceListRequest(lower);
        boolean explicitLedgerRequest=keywordMatches(lower,lk);
        if(explicitPriceRequest||(priceFiles.length()>0&&!explicitLedgerRequest)){
            String stablePriceId=messageTime>0?String.valueOf(messageTime):sbn.getKey();
            String priceEventKey=pkg+"|price|"+text.trim()+"|"+stablePriceId;
            if(!markNotificationEventOnce(p,priceEventKey,messageTime>0))return;
            if(priceFiles.length()==0){
                sendRemoteReply(n,"Requested price list nahi mili. Kripya LATHAEPS se contact karein.");
                return;
            }
            ArrayList<Uri> files=new ArrayList<>();
            JSONObject first=priceFiles.optJSONObject(0);
            for(int i=0;i<priceFiles.length();i++){
                JSONObject item=priceFiles.optJSONObject(i);if(item==null)continue;
                String uri=item.optString("uri","");if(!uri.isEmpty())files.add(Uri.parse(uri));
            }
            if(files.isEmpty()){
                sendRemoteReply(n,"Requested price list file available nahi hai. Kripya LATHAEPS se contact karein.");
                return;
            }
            String brand=first==null?"":first.optString("brand","").trim();
            String section=first==null?"":first.optString("category","").trim();
            String caption="LATHA EPS Price List"+(brand.isEmpty()?"":" • "+brand)+(section.isEmpty()?"":" • "+section);
            startShareWhenReady(files,caption,pkg,senderPhone,cleanContactTitle(title),n.contentIntent,0);
            return;
        }
        if(explicitLedgerRequest){
            // WhatsApp often posts the same message several times. The first post can
            // contain only a display name, while a later update contains the real JID.
            // Deduplicate by the logical message (not by the changing sender identity)
            // and wait briefly for the notification identity to settle before replying.
            String stableLedgerId=messageTime>0?String.valueOf(messageTime):sbn.getKey();
            String ledgerEventKey=pkg+"|ledger|"+text.trim()+"|"+stableLedgerId;
            if(!markNotificationEventOnce(p,ledgerEventKey,messageTime>0))return;
            resolveAndSendLedger(sbn,n,pkg,text,messageTime,senderPhone,title,0);
            return;
        }
        String senderIdentity=!last10(senderPhone).isEmpty()?last10(senderPhone):normaliseContactName(cleanContactTitle(title));
        String eventKey=pkg+"|"+senderIdentity+"|"+text.trim()+"|"+(messageTime>0?messageTime:0);
        if(!markNotificationEventOnce(p,eventKey,messageTime>0))return;
        String file="", type="", caption="";
        ArrayList<Uri> catalogFiles=new ArrayList<>();
        if(findCatalogsForMessage(lower,ck).length()>0||matchesBusinessKeyword(lower,ck,"catalog","catalogue","catlog")){
            JSONArray catalogs=findCatalogsForMessage(lower,ck);
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
        if(!file.isEmpty()){
            final String f=file,c=caption;
            final ArrayList<Uri> files=new ArrayList<>(catalogFiles);
            final String phone=senderPhone,contact=cleanContactTitle(title);
            if(files.isEmpty())files.add(Uri.parse(f));
            startShareWhenReady(files,c,pkg,phone,contact,n.contentIntent,0);
        } else if(!caption.isEmpty()) {
            sendRemoteReply(n,caption);
        }
    }

    /** Waits for WhatsApp's updated notification identity, then sends exactly one ledger result. */
    private void resolveAndSendLedger(StatusBarNotification original,Notification originalNotification,String pkg,String requestedText,long requestedTime,String originalPhone,String originalTitle,int attempt){
        handler.postDelayed(()->{
            SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
            if(!p.getBoolean(ENABLED,false))return;
            Notification replyNotification=originalNotification;
            String resolvedPhone=originalPhone;
            String resolvedTitle=originalTitle;
            JSONObject customer=findLedgerCustomer(p,resolvedPhone,resolvedTitle);
            try{
                StatusBarNotification[] active=getActiveNotifications();
                if(active!=null)for(StatusBarNotification candidate:active){
                    if(candidate==null||!pkg.equals(candidate.getPackageName()))continue;
                    Notification cn=candidate.getNotification();if(cn==null||(cn.flags&Notification.FLAG_GROUP_SUMMARY)!=0||isWhatsAppGroupNotification(candidate,cn,cn.extras))continue;
                    Bundle ce=cn.extras;String candidateText=extractMessageText(ce);
                    if(!requestedText.trim().equals(candidateText.trim()))continue;
                    long candidateTime=extractLatestMessageTime(ce);
                    boolean sameKey=original.getKey()!=null&&original.getKey().equals(candidate.getKey());
                    boolean sameTime=requestedTime>0&&candidateTime==requestedTime;
                    if(!sameKey&&!sameTime)continue;
                    String candidateTitle=String.valueOf(ce.getCharSequence(Notification.EXTRA_TITLE,""));
                    String candidatePhone=resolvePhoneFromNotification(candidate,cn,ce,candidateTitle);
                    JSONObject candidateCustomer=findLedgerCustomer(p,candidatePhone,candidateTitle);
                    replyNotification=cn;resolvedTitle=candidateTitle;
                    if(!candidatePhone.isEmpty())resolvedPhone=candidatePhone;
                    if(candidateCustomer!=null){customer=candidateCustomer;break;}
                }
            }catch(Exception ignored){}
            if(customer==null&&attempt<3){
                resolveAndSendLedger(original,replyNotification,pkg,requestedText,requestedTime,resolvedPhone,resolvedTitle,attempt+1);
                return;
            }
            if(customer==null){
                String help="आपका मोबाइल नंबर Ledger रिकॉर्ड से मैच नहीं हुआ। कृपया सहायता के लिए LATHAEPS से संपर्क करें।";
                saveStatus(p,"Ledger not sent • exact phone not matched: "+resolvedTitle);
                sendRemoteReply(replyNotification,help);
                return;
            }
            String file=customer.optString("ledger_uri","");
            if(file.isEmpty()){saveStatus(p,"Ledger not sent • customer PDF missing: "+customer.optString("name",resolvedTitle));return;}
            String customerPhone=digits(customer.optString("phone",""));
            if(!customerPhone.isEmpty())resolvedPhone=customerPhone.length()==10?"91"+customerPhone:customerPhone;
            ArrayList<Uri> files=new ArrayList<>();files.add(Uri.parse(file));
            String caption="LATHA EPS Ledger • "+customer.optString("name","Customer");
            startShareWhenReady(files,caption,pkg,resolvedPhone,cleanContactTitle(resolvedTitle),replyNotification.contentIntent,0);
        },attempt==0?600L:700L);
    }

    /** Serialises attachment replies and activates Accessibility only after the real share screen starts. */
    private void startShareWhenReady(ArrayList<Uri> files,String caption,String pkg,String phone,String contact,PendingIntent openChat,int attempt){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        if(p.getBoolean(PENDING_SHARE,false)||p.getBoolean(PREPARING_SHARE,false)){
            if(attempt>=240){saveStatus(p,"Reply skipped • previous attachment task did not finish");return;}
            handler.postDelayed(()->startShareWhenReady(files,caption,pkg,phone,contact,openChat,attempt+1),500L);
            return;
        }
        p.edit().putBoolean(PREPARING_SHARE,true).putString("last_business_status","Preparing WhatsApp attachment…").putLong("last_business_status_at",System.currentTimeMillis()).apply();
        if(openChat!=null){try{openChat.send();}catch(Exception ignored){}}
        handler.postDelayed(()->{
            try{
                prepareCatalogQueue(files,caption,pkg,phone,contact);
                getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(PREPARING_SHARE,false).apply();
                TaskDeviceController.begin(this);
                shareNextCatalogFile(this);
            }catch(Exception error){
                getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(PREPARING_SHARE,false).putBoolean(PENDING_SHARE,false).putString("last_business_status","Reply prepare failed • "+error.getClass().getSimpleName()).apply();
            }
        },1200L);
    }

    private boolean matchesBusinessKeyword(String message,String saved,String... aliases){
        if(saved!=null&&!saved.trim().isEmpty()&&message.contains(saved.trim().toLowerCase(Locale.ROOT)))return true;
        for(String alias:aliases)if(message.contains(alias))return true;
        return false;
    }

    private JSONObject findLedgerCustomer(SharedPreferences p,String senderPhone,String senderTitle){
        String sp=last10(senderPhone);
        try{
            JSONArray a=new JSONArray(p.getString(LEDGER_CUSTOMERS,"[]"));
            if(sp.length()==10){
                for(int i=0;i<a.length();i++){
                    JSONObject o=a.optJSONObject(i);if(o==null)continue;
                    String ph=last10(o.optString("phone",""));
                    if(ph.length()==10&&ph.equals(sp))return o;
                }
                return null;
            }
            // WhatsApp can expose only the saved contact name. Every phone under
            // that exact device contact is intersected with the Ledger map; the
            // name by itself is never accepted as authorization.
            ArrayList<String> contactPhones=resolvePhonesFromContacts(senderTitle);
            JSONObject matched=null;
            for(int i=0;i<a.length();i++){
                JSONObject o=a.optJSONObject(i);if(o==null)continue;
                String ph=last10(o.optString("phone",""));
                if(ph.length()!=10||!contactPhones.contains(ph))continue;
                if(matched!=null&&!last10(matched.optString("phone","")).equals(ph))return null;
                matched=o;
            }
            return matched;
        }catch(Exception ignored){}
        return null;
    }

    /** Ignore reposts/updates of one logical WhatsApp message without adding a general reply cooldown. */
    private boolean markNotificationEventOnce(SharedPreferences p,String fingerprint,boolean hasStableMessageTime){
        long now=System.currentTimeMillis();
        String key=Integer.toHexString(fingerprint.hashCode())+"_"+fingerprint.length();
        try{
            JSONObject recent=new JSONObject(p.getString(RECENT_EVENTS,"{}"));
            long previous=recent.optLong(key,0L);
            long duplicateWindow=hasStableMessageTime?EVENT_HISTORY_MS:EVENT_FALLBACK_WINDOW_MS;
            if(previous>0&&now-previous<duplicateWindow)return false;
            JSONArray names=recent.names();
            if(names!=null)for(int i=0;i<names.length();i++){
                String old=names.optString(i);
                if(now-recent.optLong(old,0L)>EVENT_HISTORY_MS)recent.remove(old);
            }
            recent.put(key,now);
            p.edit().putString(RECENT_EVENTS,recent.toString()).apply();
            return true;
        }catch(Exception ignored){
            p.edit().putString(RECENT_EVENTS,"{}").apply();
            return true;
        }
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

    /** Matches saved Price List Manager image/PDF sources before Ledger routing. */
    private JSONArray findPriceSourcesForMessage(String message){
        JSONArray found=new JSONArray();
        try{
            JSONArray items=new JSONArray(getSharedPreferences(APP_PREFS,MODE_PRIVATE).getString(PRICE_SOURCE_FILES,"[]"));
            if(items.length()==0)return found;
            String query=normaliseSearch(message);
            ArrayList<String> words=new ArrayList<>();
            for(String word:query.split("\\s+")){
                if(word.length()<2||isGenericPriceWord(word))continue;
                if(!words.contains(word))words.add(word);
            }
            JSONObject best=null;int bestScore=0;
            for(int i=0;i<items.length();i++){
                JSONObject item=items.optJSONObject(i);if(item==null||item.optString("uri","").isEmpty())continue;
                String hay=normaliseSearch(item.optString("search_text","")+" "+item.optString("name","")+" "+item.optString("brand","")+" "+item.optString("category","")+" "+item.optString("keywords",""));
                int score=0;boolean all=true;
                for(String word:words){if((" "+hay+" ").contains(" "+word+" "))score++;else all=false;}
                if(words.isEmpty())continue;
                if(all&&score>bestScore){best=item;bestScore=score;}
            }
            if(best==null)return found;
            String batch=best.optString("batch_id","");
            if(batch.isEmpty()){found.put(best);return found;}
            for(int i=0;i<items.length();i++){
                JSONObject item=items.optJSONObject(i);
                if(item!=null&&batch.equals(item.optString("batch_id",""))&&!item.optString("uri","").isEmpty())found.put(item);
            }
        }catch(Exception ignored){}
        return found;
    }

    private boolean isExplicitPriceListRequest(String message){
        String q=normaliseSearch(message);
        return q.contains("price list")||q.contains("pricelist")||q.contains("rate list")||q.contains("rates list");
    }

    private boolean isGenericPriceWord(String word){
        return "price".equals(word)||"pricelist".equals(word)||"rate".equals(word)||"rates".equals(word)||"list".equals(word)||"send".equals(word)||"please".equals(word)||"pls".equals(word)||"chahiye".equals(word)||"bhejo".equals(word)||"ka".equals(word)||"ki".equals(word)||"do".equals(word);
    }

    private String normaliseSearch(String value){
        if(value==null)return "";
        String clean=value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim().replaceAll("\\s+"," ");
        return clean.replaceAll("\\b([0-9]+)\\s*(?:mtr|meter|metre|meters|metres)\\b","$1mtr");
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

    private String cleanContactTitle(String title){
        if(title==null)return "";
        return title.replaceAll("(?i)\\s*\\([0-9]+\\s+(?:new\\s+)?messages?\\)\\s*$","").trim();
    }

    private String extractMessageText(Bundle e){
        CharSequence direct=e.getCharSequence(Notification.EXTRA_TEXT,"");
        if(direct!=null&&!direct.toString().trim().isEmpty())return direct.toString();
        try{android.os.Parcelable[] msgs=e.getParcelableArray(Notification.EXTRA_MESSAGES);if(msgs!=null&&msgs.length>0){Bundle b=(Bundle)msgs[msgs.length-1];CharSequence t=b.getCharSequence("text");if(t!=null)return t.toString();}}catch(Exception ignored){}
        return "";
    }

    private long extractLatestMessageTime(Bundle extras){
        if(extras==null)return 0L;
        try{
            android.os.Parcelable[] messages=extras.getParcelableArray(Notification.EXTRA_MESSAGES);
            if(messages!=null&&messages.length>0&&messages[messages.length-1] instanceof Bundle)
                return ((Bundle)messages[messages.length-1]).getLong("time",0L);
        }catch(Exception ignored){}
        return 0L;
    }

    /** Never auto-reply to WhatsApp groups. Only one-to-one notifications are accepted. */
    private boolean isWhatsAppGroupNotification(StatusBarNotification sbn,Notification notification,Bundle extras){
        if(notification==null)return true;
        if((notification.flags&Notification.FLAG_GROUP_SUMMARY)!=0)return true;
        if(extras!=null&&extras.getBoolean("android.isGroupConversation",false))return true;
        if(Build.VERSION.SDK_INT>=26&&containsGroupJid(notification.getShortcutId()))return true;
        if(sbn!=null&&(containsGroupJid(sbn.getKey())||containsGroupJid(sbn.getTag())))return true;
        if(extras==null)return false;
        try{String[] people=extras.getStringArray(Notification.EXTRA_PEOPLE);if(people!=null)for(String person:people)if(containsGroupJid(person))return true;}catch(Exception ignored){}
        if(Build.VERSION.SDK_INT>=30)try{
            ArrayList<android.app.Person> people=extras.getParcelableArrayList(Notification.EXTRA_PEOPLE_LIST);
            if(people!=null)for(android.app.Person person:people)if(person!=null&&containsGroupJid(person.getUri()))return true;
        }catch(Exception ignored){}
        try{
            android.os.Parcelable[] messages=extras.getParcelableArray(Notification.EXTRA_MESSAGES);
            if(messages!=null)for(android.os.Parcelable item:messages){
                if(!(item instanceof Bundle))continue;Bundle message=(Bundle)item;
                if(containsGroupJid(String.valueOf(message.getCharSequence("sender",""))))return true;
                if(Build.VERSION.SDK_INT>=28){android.app.Person person=message.getParcelable("sender_person");if(person!=null&&containsGroupJid(person.getUri()))return true;}
            }
        }catch(Exception ignored){}
        return false;
    }

    private boolean containsGroupJid(String value){return value!=null&&value.toLowerCase(Locale.ROOT).contains("@g.us");}

    private String resolvePhoneFromNotification(StatusBarNotification sbn,Notification n,Bundle extras,String title){
        ArrayList<String> candidates=new ArrayList<>();
        candidates.add(title);
        if(extras!=null){
            candidates.add(String.valueOf(extras.getCharSequence(Notification.EXTRA_SUB_TEXT,"")));
            if(Build.VERSION.SDK_INT>=24)candidates.add(String.valueOf(extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE,"")));
            try{String[] people=extras.getStringArray(Notification.EXTRA_PEOPLE);if(people!=null)for(String person:people)addTrustedPersonCandidate(candidates,person);}catch(Exception ignored){}
            if(Build.VERSION.SDK_INT>=30)try{
                ArrayList<android.app.Person> people=extras.getParcelableArrayList(Notification.EXTRA_PEOPLE_LIST);
                if(people!=null)for(android.app.Person person:people)if(person!=null){addTrustedPersonCandidate(candidates,person.getUri());candidates.add(String.valueOf(person.getName()));}
            }catch(Exception ignored){}
            try{
                android.os.Parcelable[] messages=extras.getParcelableArray(Notification.EXTRA_MESSAGES);
                if(messages!=null)for(int i=messages.length-1;i>=0;i--){
                    if(!(messages[i] instanceof Bundle))continue;
                    Bundle message=(Bundle)messages[i];candidates.add(String.valueOf(message.getCharSequence("sender","")));
                    if(Build.VERSION.SDK_INT>=28){android.app.Person person=message.getParcelable("sender_person");if(person!=null){addTrustedPersonCandidate(candidates,person.getUri());candidates.add(String.valueOf(person.getName()));}}
                }
            }catch(Exception ignored){}
            if(Build.VERSION.SDK_INT>=28)try{
                java.util.List<Notification.MessagingStyle.Message> styled=Notification.MessagingStyle.Message.getMessagesFromBundleArray(extras.getParcelableArray(Notification.EXTRA_MESSAGES));
                for(int i=styled.size()-1;i>=0;i--){Notification.MessagingStyle.Message message=styled.get(i);android.app.Person person=message.getSenderPerson();if(person!=null){addTrustedPersonCandidate(candidates,person.getUri());candidates.add(String.valueOf(person.getName()));}}
            }catch(Exception ignored){}
        }
        for(String value:candidates){String phone=validIndianPhone(value);if(!phone.isEmpty())return phone;}
        for(String value:candidates){String phone=resolvePhoneFromContacts(value);if(!phone.isEmpty())return phone;}
        return "";
    }

    /** Accept only explicit phone identities. Contact/notification IDs may contain random phone-shaped digits. */
    private void addTrustedPersonCandidate(ArrayList<String> out,String value){
        if(value==null)return;String v=value.trim().toLowerCase(Locale.ROOT);
        if(v.startsWith("tel:")||v.contains("@s.whatsapp.net")||(v.startsWith("whatsapp:")&&v.contains("phone=")))out.add(value);
    }

    private static String validIndianPhone(String value){
        if(value==null)return "";
        String compact=value.replaceAll("[\\s()\\-]","");
        java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?:\\+?91)?([6-9][0-9]{9})(?![0-9])").matcher(compact);
        return m.find()?"91"+m.group(1):"";
    }

    private String resolvePhoneFromContacts(String title){
        ArrayList<String> phones=resolvePhonesFromContacts(title);
        return phones.size()==1?"91"+phones.get(0):"";
    }

    private ArrayList<String> resolvePhonesFromContacts(String title){
        ArrayList<String> phones=new ArrayList<>();
        if(title==null||title.trim().isEmpty()||"null".equalsIgnoreCase(title.trim()))return phones;
        if(checkSelfPermission(android.Manifest.permission.READ_CONTACTS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)return phones;
        Cursor c=null;try{
            c=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},null,null,null);
            String wanted=normaliseContactName(cleanContactTitle(title));
            if(wanted.isEmpty())return phones;
            if(c!=null)while(c.moveToNext()){
                if(!wanted.equals(normaliseContactName(c.getString(1))))continue;
                String ten=last10(c.getString(0));
                if(ten.matches("[6-9][0-9]{9}")&&!phones.contains(ten))phones.add(ten);
            }
        }catch(Exception ignored){}finally{if(c!=null)c.close();}
        return phones;
    }

    private String normaliseContactName(String value){
        if(value==null)return "";
        return value.replaceAll("(?i)\\s*\\([0-9]+\\s+messages?\\)\\s*$","")
                .toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").trim();
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

    private void prepareCatalogQueue(ArrayList<Uri> uris,String caption,String pkg,String phone,String contact){
        JSONArray queue=new JSONArray();for(Uri uri:uris)queue.put(uri.toString());
        getSharedPreferences(PREFS,MODE_PRIVATE).edit()
            .putString(CATALOG_QUEUE,queue.toString()).putInt(CATALOG_QUEUE_INDEX,0)
            .putString(CATALOG_QUEUE_CAPTION,caption==null?"":caption)
            .putString(CATALOG_QUEUE_PACKAGE,pkg==null?"com.whatsapp":pkg)
            .putString(CATALOG_QUEUE_PHONE,phone==null?"":phone)
            .putString(CATALOG_QUEUE_CONTACT,contact==null?"":contact)
            .putInt(SHARE_PICKER_STAGE,0).putInt(SHARE_PICKER_TRIES,0)
            .putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,System.currentTimeMillis()).apply();
    }

    public static boolean shareNextCatalogFile(android.content.Context context){
        SharedPreferences p=context.getSharedPreferences(PREFS,MODE_PRIVATE);
        if(!SubscriptionManager.hasAccess(context)){clearCatalogQueue(p);TaskDeviceController.cancel(context);return false;}
        try{
            JSONArray queue=new JSONArray(p.getString(CATALOG_QUEUE,"[]"));int index=p.getInt(CATALOG_QUEUE_INDEX,0);
            if(index<0||index>=queue.length()){clearCatalogQueue(p);return false;}
            TaskDeviceController.begin(context);wakeScreen(context);
            android.app.KeyguardManager keyguard=(android.app.KeyguardManager)context.getSystemService(android.content.Context.KEYGUARD_SERVICE);
            if(keyguard!=null&&keyguard.isKeyguardLocked()){
                if(TaskDeviceController.shouldRequestUnlock(context))LockScreenSendActivity.open(context,LockScreenSendActivity.MODE_CATALOG);
                p.edit().putString("last_business_status",TaskDeviceController.autoUnlockEnabled(context)?"Screen awake • unlock to continue task":"Phone locked • Auto Unlock is OFF").putLong("last_business_status_at",System.currentTimeMillis()).apply();
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
            p.edit().putBoolean(PENDING_SHARE,true).putLong(PENDING_SHARE_AT,System.currentTimeMillis()).putInt(SHARE_PICKER_STAGE,0).putInt(SHARE_PICKER_TRIES,0).putString("last_business_status","Sending catalog file "+(index+1)+" / "+queue.length()).apply();
            return true;
        }catch(Exception error){clearCatalogQueue(p);TaskDeviceController.cancel(context);p.edit().putString("last_business_status","Catalog/Ledger send failed • "+error.getClass().getSimpleName()).apply();return false;}
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
        p.edit().putBoolean(PENDING_SHARE,false).putBoolean(PREPARING_SHARE,false).remove(CATALOG_QUEUE).remove(CATALOG_QUEUE_INDEX).remove(CATALOG_QUEUE_CAPTION).remove(CATALOG_QUEUE_PACKAGE).remove(CATALOG_QUEUE_PHONE).remove(CATALOG_QUEUE_CONTACT).remove(SHARE_PICKER_STAGE).remove(SHARE_PICKER_TRIES).apply();
    }

    public static void cancelPendingShare(android.content.Context context,String status){
        SharedPreferences p=context.getSharedPreferences(PREFS,android.content.Context.MODE_PRIVATE);clearCatalogQueue(p);p.edit().putString("last_business_status",status).putLong("last_business_status_at",System.currentTimeMillis()).apply();TaskDeviceController.cancel(context);
    }

    @SuppressWarnings("deprecation") private static void wakeScreen(android.content.Context context){
        try{
            android.os.PowerManager pm=(android.os.PowerManager)context.getSystemService(android.content.Context.POWER_SERVICE);if(pm==null)return;
            android.os.PowerManager.WakeLock lock=pm.newWakeLock(android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK|android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP|android.os.PowerManager.ON_AFTER_RELEASE,"LathaBulk:CatalogWake");
            lock.setReferenceCounted(false);lock.acquire(30000L);
        }catch(Exception ignored){}
    }
}
