package com.lathaeps.lathabulk;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AdminActivity extends Activity {
    private EditText password;
    private EditText device;
    private RadioButton yearly;
    private RadioButton custom;
    private RadioButton lifetime;
    private RadioButton blocked;
    private EditText customDays;
    private Button update;

    @Override public void onCreate(Bundle state) { super.onCreate(state); setContentView(buildPanel()); }

    private android.view.View buildPanel() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(22),dp(20),dp(24)); root.setBackgroundColor(Color.rgb(25,35,42)); scroll.addView(root);

        TextView title=text("SECURE ONLINE ADMIN",27,true,Color.WHITE); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView desc=text("Admin password APK me save nahi hota. User ka Device ID enter karke Supabase par plan update karein.",14,false,Color.LTGRAY);
        desc.setGravity(Gravity.CENTER); desc.setPadding(0,dp(6),0,dp(18)); root.addView(desc);

        password = new EditText(this); password.setHint("Supabase admin password"); password.setTextColor(Color.WHITE);
        password.setHintTextColor(Color.GRAY); password.setSingleLine(true);
        password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); root.addView(password,new LinearLayout.LayoutParams(-1,dp(62)));

        device = new EditText(this); device.setHint("12-character User Device ID"); device.setTextColor(Color.WHITE);
        device.setHintTextColor(Color.GRAY); device.setSingleLine(true); root.addView(device,new LinearLayout.LayoutParams(-1,dp(62)));

        RadioGroup plans=new RadioGroup(this); plans.setOrientation(RadioGroup.VERTICAL);
        yearly=radio("₹800 • Activate 1 Year",true,Color.WHITE);
        custom=radio("Custom Days • Extend current validity",false,Color.rgb(125,220,255));
        lifetime=radio("Lifetime Free activation",false,Color.rgb(255,210,92));
        blocked=radio("Block / revoke access",false,Color.rgb(255,130,130));
        plans.addView(yearly); plans.addView(custom); plans.addView(lifetime); plans.addView(blocked); root.addView(plans);

        customDays=new EditText(this);customDays.setHint("Validity extend days • e.g. 30, 90, 500");customDays.setTextColor(Color.WHITE);customDays.setHintTextColor(Color.GRAY);customDays.setSingleLine(true);customDays.setInputType(InputType.TYPE_CLASS_NUMBER);customDays.setEnabled(false);customDays.setAlpha(.55f);root.addView(customDays,new LinearLayout.LayoutParams(-1,dp(58)));
        plans.setOnCheckedChangeListener((group,checkedId)->{boolean enabled=custom.isChecked();customDays.setEnabled(enabled);customDays.setAlpha(enabled?1f:.55f);if(enabled)customDays.requestFocus();});

        update=button("UPDATE ONLINE SUBSCRIPTION"); root.addView(update,space(14,10));
        update.setOnClickListener(v -> updatePlan());
        Button close=button("CLOSE ADMIN PANEL"); close.setOnClickListener(v->finish()); root.addView(close,space(0,0));
        return scroll;
    }

    private void updatePlan() {
        if(custom.isChecked()){
            int days;try{days=Integer.parseInt(customDays.getText().toString().trim());}catch(Exception e){customDays.setError("Valid days enter karein");return;}
            if(days<1||days>365000){customDays.setError("Days 1 se 365000 ke beech hone chahiye");return;}
            update.setEnabled(false);update.setText("EXTENDING VALIDITY...");
            SubscriptionManager.adminExtendDays(this,device.getText().toString(),days,password.getText().toString(),(ok,message)->{update.setEnabled(true);update.setText("UPDATE ONLINE SUBSCRIPTION");Toast.makeText(this,message,Toast.LENGTH_LONG).show();if(ok){device.setText("");customDays.setText("");}});return;
        }
        String plan = lifetime.isChecked() ? "lifetime" : blocked.isChecked() ? "blocked" : "yearly";
        update.setEnabled(false); update.setText("UPDATING SERVER...");
        SubscriptionManager.adminSetPlan(this, device.getText().toString(), plan, password.getText().toString(), (ok,message) -> {
            update.setEnabled(true); update.setText("UPDATE ONLINE SUBSCRIPTION");
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();
            if (ok) device.setText("");
        });
    }

    private RadioButton radio(String label,boolean checked,int color){RadioButton r=new RadioButton(this);r.setText(label);r.setTextColor(color);r.setChecked(checked);return r;}
    private TextView text(String s,float size,boolean bold,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(Typeface.DEFAULT_BOLD);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTypeface(Typeface.DEFAULT_BOLD);return b;}
    private LinearLayout.LayoutParams space(int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(56));p.setMargins(0,dp(top),0,dp(bottom));return p;}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}
