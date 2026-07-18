package com.lathaeps.lathabulk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class ScheduleRestoreReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if(intent==null)return;
        String action=intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)||Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)){
            PaymentScheduleReceiver.restoreStoredSchedule(context);
            boolean floatingEnabled=context.getSharedPreferences(MainActivity.PREFS,Context.MODE_PRIVATE).getBoolean(FloatingMicService.PREF_ENABLED,false);
            if(floatingEnabled&&Settings.canDrawOverlays(context))try{FloatingMicService.start(context);}catch(Exception ignored){}
        }
    }
}
