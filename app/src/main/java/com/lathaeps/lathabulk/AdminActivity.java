package com.lathaeps.lathabulk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

public class AdminActivity extends Activity {
    private static final String ADMIN_PIN="4511";
    private LinearLayout root;

    @Override public void onCreate(Bundle state){super.onCreate(state);showLogin();}

    private void showLogin(){
        EditText pin=new EditText(this);pin.setHint("Admin PIN");pin.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);pin.setGravity(Gravity.CENTER);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("LathaBulk Admin").setMessage("Admin-only panel").setView(pin).setPositiveButton("LOGIN",null).setNegativeButton("CLOSE",(x,w)->finish()).setCancelable(false).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(ADMIN_PIN.equals(pin.getText().toString().trim())){d.dismiss();setContentView(buildPanel());}else pin.setError("Wrong admin PIN");}));d.show();
    }

    private android.view.View buildPanel(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(22),dp(20),dp(24));root.setBackgroundColor(Color.rgb(25,35,42));
        TextView title=text("ADMIN PANEL",27,true,Color.WHITE);title.setGravity(Gravity.CENTER);root.addView(title);
        TextView desc=text("User ka Device ID enter karke activation code banayein.",15,false,Color.LTGRAY);desc.setGravity(Gravity.CENTER);desc.setPadding(0,dp(6),0,dp(18));root.addView(desc);
        EditText device=new EditText(this);device.setHint("12-character User Device ID");device.setTextColor(Color.WHITE);device.setHintTextColor(Color.GRAY);device.setSingleLine(true);root.addView(device,new LinearLayout.LayoutParams(-1,dp(62)));
        RadioGroup plans=new RadioGroup(this);plans.setOrientation(RadioGroup.VERTICAL);RadioButton yearly=new RadioButton(this);yearly.setText("₹800 • 1 Year activation");yearly.setTextColor(Color.WHITE);yearly.setChecked(true);RadioButton lifetime=new RadioButton(this);lifetime.setText("Lifetime Free activation");lifetime.setTextColor(Color.rgb(255,210,92));plans.addView(yearly);plans.addView(lifetime);root.addView(plans);
        Button generate=button("GENERATE ACTIVATION CODE");root.addView(generate,space(14,10));
        TextView output=text("",18,true,Color.rgb(112,235,180));output.setGravity(Gravity.CENTER);output.setPadding(dp(8),dp(12),dp(8),dp(12));root.addView(output,new LinearLayout.LayoutParams(-1,-2));
        Button copy=button("COPY CODE");copy.setEnabled(false);root.addView(copy,space(8,8));
        Button activateThis=button("ACTIVATE THIS DEVICE LIFETIME");root.addView(activateThis,space(0,8));
        Button close=button("CLOSE ADMIN PANEL");close.setOnClickListener(v->finish());root.addView(close,space(0,0));
        generate.setOnClickListener(v->{String code=SubscriptionManager.generateCode(device.getText().toString(),lifetime.isChecked());if(code.isEmpty()){device.setError("Valid 12-character Device ID required");return;}output.setText(code);copy.setEnabled(true);Toast.makeText(this,"Code ready • User ko bhejein",Toast.LENGTH_LONG).show();});
        copy.setOnClickListener(v->{String code=output.getText().toString();((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("LathaBulk activation",code));Toast.makeText(this,"Activation code copied",Toast.LENGTH_SHORT).show();});
        activateThis.setOnClickListener(v->{String code=SubscriptionManager.generateCode(SubscriptionManager.deviceId(this),true);String result=SubscriptionManager.activate(this,code);new AlertDialog.Builder(this).setTitle("Admin activation").setMessage(result).setPositiveButton("OK",null).show();});
        return root;
    }

    private TextView text(String s,float size,boolean bold,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(Typeface.DEFAULT_BOLD);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTypeface(Typeface.DEFAULT_BOLD);return b;}
    private LinearLayout.LayoutParams space(int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54));p.setMargins(0,dp(top),0,dp(bottom));return p;}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}
