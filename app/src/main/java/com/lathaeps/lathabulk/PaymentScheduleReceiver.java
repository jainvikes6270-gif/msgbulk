package com.lathaeps.lathabulk;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

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
        SharedPreferences settings=context.getSharedPreferences(MainActivity.PREFS,Context.MODE_PRIVATE);
        String numbers=settings.getString(MainActivity.PAYMENT_SCHEDULE_NUMBERS,"[]");
        String names=settings.getString(MainActivity.PAYMENT_SCHEDULE_NAMES,"[]");
        String messages=settings.getString(MainActivity.PAYMENT_SCHEDULE_MESSAGES,"[]");
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
            settings.edit().remove(MainActivity.PAYMENT_SCHEDULE_AT).remove(MainActivity.PAYMENT_SCHEDULE_NUMBERS).remove(MainActivity.PAYMENT_SCHEDULE_NAMES).remove(MainActivity.PAYMENT_SCHEDULE_MESSAGES).putString(MainActivity.PAYMENT_HISTORY_KEY,stamp+" • Scheduled started • "+list.length()+" reminders"+(old.isEmpty()?"":"\n"+old)).apply();
            NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.cancel(SCHEDULE_NOTIFICATION_ID);
            MainActivity.updateProgressNotification(context,0,list.length(),"Scheduled payment reminders starting",false);
            WhatsAppAccessibilityService.openCurrentChat(context);
        } catch (Exception ignored) {
            context.getSharedPreferences(MainActivity.AUTO_PREFS,Context.MODE_PRIVATE).edit().putBoolean(MainActivity.AUTO_RUNNING,false).apply();
        }
    }

    static void showScheduledNotification(Context context,int count,long at){
        Intent launch=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent open=PendingIntent.getActivity(context,622,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String when=new SimpleDateFormat("dd MMM, hh:mm a",Locale.getDefault()).format(new Date(at));
        NotificationCompat.Builder b=new NotificationCompat.Builder(context,MainActivity.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Payment Reminder scheduled")
                .setContentText(count+" customers • "+when).setContentIntent(open).setAutoCancel(false)
                .setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_LOW);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.notify(SCHEDULE_NOTIFICATION_ID,b.build());
    }
}
