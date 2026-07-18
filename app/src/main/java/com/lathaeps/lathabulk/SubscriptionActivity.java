package com.lathaeps.lathabulk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SubscriptionActivity extends Activity {
    private TextView status;
    private Button refresh;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SubscriptionManager.ensureTrial(this);
        setContentView(build());
        refreshOnline(false);
    }

    @Override protected void onResume() {
        super.onResume();
        if (status != null) status.setText(SubscriptionManager.statusText(this));
    }

    private android.view.View build() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(28));
        root.setBackgroundColor(Color.rgb(245,250,249));
        scroll.addView(root);

        TextView title = text("LATHAEPS SMART",27,true,Color.rgb(0,91,78));
        title.setGravity(Gravity.CENTER);
        title.setOnLongClickListener(v -> { startActivity(new Intent(this,AdminActivity.class)); return true; });
        root.addView(title);
        TextView sub = text("Secure Online Subscription",17,true,Color.DKGRAY);
        sub.setGravity(Gravity.CENTER); sub.setPadding(0,dp(5),0,dp(18)); root.addView(sub);

        status = text(SubscriptionManager.statusText(this),17,true,Color.rgb(18,105,72));
        status.setGravity(Gravity.CENTER); status.setBackgroundColor(Color.WHITE);
        status.setPadding(dp(12),dp(14),dp(12),dp(14)); root.addView(status,new LinearLayout.LayoutParams(-1,-2));
        root.addView(text("12-day trial Supabase server par save hota hai. Reinstall ya app-data clear karne se trial reset nahi hoga.",14,false,Color.DKGRAY),space(8,12));

        refresh = button("REFRESH ONLINE STATUS");
        refresh.setOnClickListener(v -> refreshOnline(true)); root.addView(refresh,space(0,12));

        LinearLayout yearly = card();
        yearly.addView(text("₹800 / YEAR",24,true,Color.rgb(0,91,78)));
        yearly.addView(text("All features • 1 device • Payment ke baad admin online activate karega",14,false,Color.DKGRAY));
        Button pay = button("PAY ₹800 BY UPI"); pay.setOnClickListener(v -> openUpi()); yearly.addView(pay,space(12,12));
        root.addView(yearly,space(0,14));

        LinearLayout life = card();
        life.addView(text("LIFETIME FREE",22,true,Color.rgb(116,76,0)));
        life.addView(text("Purchase option nahi • Sirf secure admin panel se activate hoga.",14,false,Color.DKGRAY));
        root.addView(life,space(0,14));

        String id = SubscriptionManager.deviceId(this);
        root.addView(text("YOUR DEVICE ID",13,true,Color.GRAY));
        TextView device = text(id,22,true,Color.BLACK); device.setGravity(Gravity.CENTER); device.setPadding(0,dp(8),0,dp(8));
        device.setOnClickListener(v -> { copy("Device ID",id); toast("Device ID copied"); }); root.addView(device);
        Button copy = button("COPY DEVICE ID");
        copy.setOnClickListener(v -> { copy("Device ID",id); toast("Device ID copied • Payment ke baad admin ko bhejein"); });
        root.addView(copy,space(6,10));

        Button open = button("OPEN APP");
        open.setOnClickListener(v -> { if (SubscriptionManager.hasAccess(this)) openMain(); else refreshOnline(true); });
        root.addView(open,space(2,12));
        return scroll;
    }

    private void refreshOnline(boolean showToast) {
        refresh.setEnabled(false); refresh.setText("CHECKING SERVER...");
        SubscriptionManager.refresh(this, (ok,message) -> {
            refresh.setEnabled(true); refresh.setText("REFRESH ONLINE STATUS"); status.setText(SubscriptionManager.statusText(this));
            if (showToast || !ok) toast(message);
        });
    }

    private void openUpi() {
        try {
            String note = "LathaBulk 1 Year - " + SubscriptionManager.deviceId(this);
            String uri = "upi://pay?pa="+SubscriptionManager.UPI_ID+"&pn="+Uri.encode("LATHA EPS")+"&am="+SubscriptionManager.YEARLY_PRICE+".00&cu=INR&tn="+Uri.encode(note);
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW,Uri.parse(uri)),"Pay with UPI"));
            toast("Payment ke baad Device ID admin ko bhejein");
        } catch(Exception e) {
            new AlertDialog.Builder(this).setTitle("UPI app not found")
                    .setMessage("UPI ID: "+SubscriptionManager.UPI_ID+"\nAmount: ₹800")
                    .setPositiveButton("COPY UPI ID",(d,w)->copy("UPI ID",SubscriptionManager.UPI_ID)).setNegativeButton("Close",null).show();
        }
    }

    private void openMain(){startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));finish();}
    private LinearLayout card(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(18),dp(16),dp(18),dp(16));x.setBackgroundColor(Color.WHITE);return x;}
    private TextView text(String s,float size,boolean bold,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(Typeface.DEFAULT_BOLD);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setTypeface(Typeface.DEFAULT_BOLD);return b;}
    private LinearLayout.LayoutParams space(int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(top),0,dp(bottom));return p;}
    private void copy(String label,String value){((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(label,value));}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}
