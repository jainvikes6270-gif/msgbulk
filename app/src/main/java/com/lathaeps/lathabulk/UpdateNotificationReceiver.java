package com.lathaeps.lathabulk;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateNotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_LATER = "com.lathaeps.lathabulk.UPDATE_LATER";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_LATER.equals(intent.getAction())) {
            NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(manager!=null)manager.cancel(MainActivity.UPDATE_NOTIFICATION_ID);
        }
    }
}
