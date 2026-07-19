package com.lathaeps.lathabulk;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class LedgerScheduleReceiver extends BroadcastReceiver {
    static final String ACTION_SEND="com.lathaeps.lathabulk.SEND_SCHEDULED_LEDGERS";
    static final int REQUEST_CODE=640,NOTIFICATION_ID=641;

    @Override public void onReceive(Context context,Intent intent){
        if(intent==null||!ACTION_SEND.equals(intent.getAction()))return;
        SharedPreferences app=context.getSharedPreferences(MainActivity.PREFS,Context.MODE_PRIVATE);
        long scheduledAt=app.getLong(MainActivity.LEDGER_SCHEDULE_AT,System.currentTimeMillis());String repeat=app.getString(MainActivity.LEDGER_SCHEDULE_REPEAT,"Weekly");String listName=app.getString(MainActivity.LEDGER_SCHEDULE_LIST_NAME,"Ledger List");
        long next=PaymentScheduleReceiver.nextScheduleAt(scheduledAt,repeat);if(next>0){app.edit().putLong(MainActivity.LEDGER_SCHEDULE_AT,next).apply();scheduleNext(context,next);}
        if(!SubscriptionManager.hasAccess(context)){showInfo(context,"Scheduled Ledger paused","Plan expired • Activate subscription to continue");return;}
        try{
            Set<String> selected=new LinkedHashSet<>();JSONArray saved=new JSONArray(app.getString(MainActivity.LEDGER_SAVED_PARTIES_KEY,"[]"));for(int i=0;i<saved.length();i++)selected.add(last10(saved.optString(i)));
            Set<String> blocked=new LinkedHashSet<>();JSONArray doNotSend=new JSONArray(app.getString("do_not_send_numbers","[]"));for(int i=0;i<doNotSend.length();i++)blocked.add(last10(doNotSend.optString(i)));
            JSONArray customers=new JSONArray(context.getSharedPreferences(AutoReplyNotificationService.PREFS,Context.MODE_PRIVATE).getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));ArrayList<Uri> files=new ArrayList<>();ArrayList<String> phones=new ArrayList<>(),names=new ArrayList<>();
            for(int i=0;i<customers.length();i++){JSONObject o=customers.optJSONObject(i);if(o==null)continue;String phone=last10(o.optString("phone","")),uri=o.optString("ledger_uri","");if(!selected.contains(phone)||blocked.contains(phone)||phone.length()!=10||uri.isEmpty())continue;files.add(Uri.parse(uri));phones.add(phone);names.add(o.optString("name","Customer"));}
            if(files.isEmpty()){showInfo(context,"Scheduled Ledger not sent","Saved parties ke mapped Ledger PDFs missing hain");return;}
            AutoReplyNotificationService.prepareLedgerBatchQueue(context,files,phones,names);showScheduledNotification(context,files.size(),next,repeat,listName);
        }catch(Exception e){showInfo(context,"Scheduled Ledger failed","Ledger list read nahi hui");}
    }

    static boolean scheduleNext(Context context,long at){try{AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);if(manager==null)return false;if(Build.VERSION.SDK_INT>=31&&!manager.canScheduleExactAlarms())return false;Intent i=new Intent(context,LedgerScheduleReceiver.class).setAction(ACTION_SEND);PendingIntent alarm=PendingIntent.getBroadcast(context,REQUEST_CODE,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Intent launch=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent show=PendingIntent.getActivity(context,REQUEST_CODE+1,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);manager.setAlarmClock(new AlarmManager.AlarmClockInfo(at,show),alarm);return true;}catch(Exception e){return false;}}

    static void restoreStoredSchedule(Context context){SharedPreferences p=context.getSharedPreferences(MainActivity.PREFS,Context.MODE_PRIVATE);long at=p.getLong(MainActivity.LEDGER_SCHEDULE_AT,0L);if(at<=0)return;String repeat=p.getString(MainActivity.LEDGER_SCHEDULE_REPEAT,"Weekly"),listName=p.getString(MainActivity.LEDGER_SCHEDULE_LIST_NAME,"Ledger List");if(at<=System.currentTimeMillis())at=PaymentScheduleReceiver.nextScheduleAt(at,repeat);if(at>0&&scheduleNext(context,at)){p.edit().putLong(MainActivity.LEDGER_SCHEDULE_AT,at).apply();int count=0;try{count=new JSONArray(p.getString(MainActivity.LEDGER_SAVED_PARTIES_KEY,"[]")).length();}catch(Exception ignored){}showScheduledNotification(context,count,at,repeat,listName);}}

    static void showScheduledNotification(Context context,int count,long at,String repeat){showScheduledNotification(context,count,at,repeat,"Ledger List");}
    static void showScheduledNotification(Context context,int count,long at,String repeat,String listName){if(at<=0)return;String when=new SimpleDateFormat("dd MMM, hh:mm a",Locale.getDefault()).format(new Date(at));showInfo(context,listName+" scheduled",repeat+" • "+count+" parties • Next "+when);}
    private static void showInfo(Context context,String title,String text){Intent launch=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent open=PendingIntent.getActivity(context,REQUEST_CODE+2,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);NotificationCompat.Builder b=new NotificationCompat.Builder(context,MainActivity.CHANNEL_ID).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(title).setContentText(text).setContentIntent(open).setAutoCancel(false).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_LOW);NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.notify(NOTIFICATION_ID,b.build());}
    private static String last10(String raw){String d=raw==null?"":raw.replaceAll("[^0-9]","");return d.length()>10?d.substring(d.length()-10):d;}
}
