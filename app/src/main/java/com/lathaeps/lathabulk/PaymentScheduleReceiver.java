package com.lathaeps.lathabulk;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentScheduleReceiver extends BroadcastReceiver {
    static final String ACTION_SEND = "com.lathaeps.lathabulk.SEND_SCHEDULED_PAYMENT";
    static final int SCHEDULE_NOTIFICATION_ID = 620;

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_SEND.equals(intent.getAction())) return;
        if(!SubscriptionManager.hasAccess(context)){
            context.getSharedPreferences(MainActivity.AUTO_PREFS,Context.MODE_PRIVATE).edit().putBoolean(MainActivity.AUTO_RUNNING,false).apply();
            showPlanRequiredNotification(context);
            return;
        }
        SharedPreferences settings=context.getSharedPreferences(MainActivity.PREFS,Context.MODE_PRIVATE);
        String numbers=settings.getString(MainActivity.PAYMENT_SCHEDULE_NUMBERS,"[]");
        String names=settings.getString(MainActivity.PAYMENT_SCHEDULE_NAMES,"[]");
        String messages=settings.getString(MainActivity.PAYMENT_SCHEDULE_MESSAGES,"[]");
        String repeat=settings.getString(MainActivity.PAYMENT_SCHEDULE_REPEAT,"One Time");
        long scheduledAt=settings.getLong(MainActivity.PAYMENT_SCHEDULE_AT,System.currentTimeMillis());
        try {
            JSONArray list=new JSONArray(numbers);
            if(list.length()==0)return;
            context.getSharedPreferences(MainActivity.AUTO_PREFS,Context.MODE_PRIVATE).edit()
                    .putString(MainActivity.AUTO_NUMBERS,numbers)
                    .putString(MainActivity.AUTO_NAMES,names)
                    .putString(MainActivity.AUTO_MESSAGES,messages)
                    .putString(MainActivity.AUTO_MESSAGE,"")
                    .putString(MainActivity.AUTO_QUEUE_TOKEN,String.valueOf(System.currentTimeMillis()))
                    .putString(MainActivity.AUTO_FAILED,"[]")
                    .putInt(MainActivity.AUTO_INDEX,0)
                    .putInt(MainActivity.AUTO_MIN_DELAY,settings.getInt(MainActivity.AUTO_MIN_DELAY,3))
                    .putInt(MainActivity.AUTO_MAX_DELAY,settings.getInt(MainActivity.AUTO_MAX_DELAY,7))
                    .remove(MainActivity.AUTO_IMAGE_URI).remove(MainActivity.AUTO_IMAGE_TYPE)
                    .putBoolean(MainActivity.AUTO_RUNNING,true).apply();
            String stamp=new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new Date());
            String old=settings.getString(MainActivity.PAYMENT_HISTORY_KEY,"");
            long nextAt=nextScheduleAt(scheduledAt,repeat);
            if(nextAt>0){settings.edit().putLong(MainActivity.PAYMENT_SCHEDULE_AT,nextAt).putString(MainActivity.PAYMENT_HISTORY_KEY,stamp+" • "+repeat+" reminder started • "+list.length()+" customers"+(old.isEmpty()?"":"\n"+old)).apply();scheduleNext(context,nextAt);showScheduledNotification(context,list.length(),nextAt,repeat);}
            else{settings.edit().remove(MainActivity.PAYMENT_SCHEDULE_AT).remove(MainActivity.PAYMENT_SCHEDULE_NUMBERS).remove(MainActivity.PAYMENT_SCHEDULE_NAMES).remove(MainActivity.PAYMENT_SCHEDULE_MESSAGES).remove(MainActivity.PAYMENT_SCHEDULE_REPEAT).putString(MainActivity.PAYMENT_HISTORY_KEY,stamp+" • One Time reminder started • "+list.length()+" customers"+(old.isEmpty()?"":"\n"+old)).apply();NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.cancel(SCHEDULE_NOTIFICATION_ID);}
            MainActivity.updateProgressNotification(context,0,list.length(),"Scheduled payment reminders starting",false);
            WhatsAppAccessibilityService.openCurrentChat(context);
        } catch (Exception ignored) {
            context.getSharedPreferences(MainActivity.AUTO_PREFS,Context.MODE_PRIVATE).edit().putBoolean(MainActivity.AUTO_RUNNING,false).apply();
        }
    }

    static long nextScheduleAt(long from,String repeat){
        if("One Time".equals(repeat))return 0L;
        java.util.Calendar c=java.util.Calendar.getInstance();c.setTimeInMillis(from);
        do{if("Daily".equals(repeat))c.add(java.util.Calendar.DAY_OF_YEAR,1);else if("Weekly".equals(repeat))c.add(java.util.Calendar.WEEK_OF_YEAR,1);else if("Monthly".equals(repeat))c.add(java.util.Calendar.MONTH,1);else return 0L;}while(c.getTimeInMillis()<=System.currentTimeMillis());
        return c.getTimeInMillis();
    }

    static boolean scheduleNext(Context context,long at){
        try{AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);if(manager==null)return false;Intent alarmIntent=new Intent(context,PaymentScheduleReceiver.class).setAction(ACTION_SEND);PendingIntent alarm=PendingIntent.getBroadcast(context,620,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Intent showIntent=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent show=PendingIntent.getActivity(context,621,showIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);if(Build.VERSION.SDK_INT>=31&&!manager.canScheduleExactAlarms())return false;manager.setAlarmClock(new AlarmManager.AlarmClockInfo(at,show),alarm);return true;}catch(Exception ignored){return false;}
    }

    static void restoreStoredSchedule(Context context){
        if(!SubscriptionManager.hasAccess(context))return;
        SharedPreferences p=context.getSharedPreferences(MainActivity.PREFS,Context.MODE_PRIVATE);
        long at=p.getLong(MainActivity.PAYMENT_SCHEDULE_AT,0L);if(at<=0)return;
        try{if(new JSONArray(p.getString(MainActivity.PAYMENT_SCHEDULE_NUMBERS,"[]")).length()==0)return;}catch(Exception e){return;}
        String repeat=p.getString(MainActivity.PAYMENT_SCHEDULE_REPEAT,"One Time");
        if(at<=System.currentTimeMillis())at="One Time".equals(repeat)?System.currentTimeMillis()+15000L:nextScheduleAt(at,repeat);
        if(at>0&&scheduleNext(context,at)){p.edit().putLong(MainActivity.PAYMENT_SCHEDULE_AT,at).apply();int count=0;try{count=new JSONArray(p.getString(MainActivity.PAYMENT_SCHEDULE_NUMBERS,"[]")).length();}catch(Exception ignored){}showScheduledNotification(context,count,at,repeat);}
    }

    static void showScheduledNotification(Context context,int count,long at,String repeat){
        Intent launch=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent open=PendingIntent.getActivity(context,622,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String when=new SimpleDateFormat("dd MMM, hh:mm a",Locale.getDefault()).format(new Date(at));
        NotificationCompat.Builder b=new NotificationCompat.Builder(context,MainActivity.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Payment Reminder scheduled")
                .setContentText(repeat+" • "+count+" customers • Next "+when).setContentIntent(open).setAutoCancel(false)
                .setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_LOW);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.notify(SCHEDULE_NOTIFICATION_ID,b.build());
    }

    private static void showPlanRequiredNotification(Context context){
        Intent launch=new Intent(context,SubscriptionActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent open=PendingIntent.getActivity(context,623,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(context,MainActivity.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert).setContentTitle("Payment Reminder paused")
                .setContentText("Plan expired • Activate subscription to continue").setContentIntent(open).setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.notify(SCHEDULE_NOTIFICATION_ID,b.build());
    }
}
