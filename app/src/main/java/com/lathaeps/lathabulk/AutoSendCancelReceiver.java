package com.lathaeps.lathabulk;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/** Stops an accidental Bulk or Catalog send directly from the progress notification. */
public class AutoSendCancelReceiver extends BroadcastReceiver {
    static final String ACTION_CANCEL="com.lathaeps.lathabulk.CANCEL_AUTO_SEND";

    @Override public void onReceive(Context context,Intent intent){
        if(intent==null||!ACTION_CANCEL.equals(intent.getAction()))return;
        context.getSharedPreferences(MainActivity.AUTO_PREFS,Context.MODE_PRIVATE).edit().putBoolean(MainActivity.AUTO_RUNNING,false).putBoolean(MainActivity.BROADCAST_RUNNING,false).remove(MainActivity.BROADCAST_STAGE).remove(MainActivity.AUTO_IMAGE_URI).remove(MainActivity.AUTO_IMAGE_TYPE).remove(MainActivity.AUTO_MESSAGES).remove(MainActivity.AUTO_QUEUE_TOKEN).apply();
        SharedPreferences reply=context.getSharedPreferences(AutoReplyNotificationService.PREFS,Context.MODE_PRIVATE);
        reply.edit().putBoolean(AutoReplyNotificationService.PENDING_SHARE,false)
            .putBoolean(AutoReplyNotificationService.PREPARING_SHARE,false)
            .remove(AutoReplyNotificationService.CATALOG_QUEUE).remove(AutoReplyNotificationService.CATALOG_QUEUE_INDEX)
            .remove(AutoReplyNotificationService.CATALOG_QUEUE_CAPTION).remove(AutoReplyNotificationService.CATALOG_QUEUE_PACKAGE)
            .remove(AutoReplyNotificationService.CATALOG_QUEUE_PHONE)
            .remove(AutoReplyNotificationService.CATALOG_QUEUE_CONTACT)
            .remove(AutoReplyNotificationService.SHARE_PICKER_STAGE)
            .remove(AutoReplyNotificationService.SHARE_PICKER_TRIES)
            .putString("last_business_status","Auto Send cancelled by user").apply();
        WhatsAppAccessibilityService.releaseQueueWakeLock();
        TaskDeviceController.cancel(context);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.cancel(MainActivity.NOTIFICATION_ID);
        Toast.makeText(context,"Auto Send CANCELLED",Toast.LENGTH_LONG).show();
    }
}
