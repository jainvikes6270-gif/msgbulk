package com.lathaeps.lathabulk;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Wakes the display and asks Android to dismiss the keyguard before resuming an approved send queue. */
public class LockScreenSendActivity extends Activity {
    static final String MODE_BULK="bulk", MODE_CATALOG="catalog";
    private static final String EXTRA_MODE="send_mode";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean continued=false,requested=false;

    static void open(Context context,String mode){
        Intent i=new Intent(context,LockScreenSendActivity.class).putExtra(EXTRA_MODE,mode).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);
    }

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        if(!LicenseManager.isEntitled(this)){finish();return;}
        if(Build.VERSION.SDK_INT>=27){setShowWhenLocked(true);setTurnScreenOn(true);}
        else getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LinearLayout box=new LinearLayout(this);box.setGravity(Gravity.CENTER);box.setBackgroundColor(Color.argb(210,18,18,18));
        TextView text=new TextView(this);text.setText("LATHAEPS\nUnlock phone to continue automatic sending");text.setTextColor(Color.WHITE);text.setTextSize(20);text.setGravity(Gravity.CENTER);text.setPadding(30,30,30,30);box.addView(text);setContentView(box);
    }

    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);continued=false;requested=false;}

    @Override protected void onResume(){
        super.onResume();KeyguardManager km=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        if(km==null||!km.isKeyguardLocked()){continueQueue();return;}
        if(!requested&&Build.VERSION.SDK_INT>=26){
            requested=true;km.requestDismissKeyguard(this,new KeyguardManager.KeyguardDismissCallback(){
                @Override public void onDismissSucceeded(){continueQueue();}
                @Override public void onDismissCancelled(){requested=false;}
                @Override public void onDismissError(){requested=false;}
            });
        }
    }

    private void continueQueue(){
        if(continued)return;continued=true;String mode=getIntent().getStringExtra(EXTRA_MODE);Context app=getApplicationContext();
        handler.postDelayed(()->{finish();if(MODE_CATALOG.equals(mode))AutoReplyNotificationService.shareNextCatalogFile(app);else WhatsAppAccessibilityService.openCurrentChatAfterUnlock(app);},350);
    }
}
