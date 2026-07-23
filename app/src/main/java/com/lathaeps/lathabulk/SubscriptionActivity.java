package com.lathaeps.lathabulk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

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

        TextView title = text("Business Dost",27,true,Color.rgb(0,91,78));
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
        Button copyUpi = button("COPY UPI ID");
        copyUpi.setOnClickListener(v -> { copy("UPI ID",SubscriptionManager.UPI_ID); toast("UPI ID copied"); });
        yearly.addView(copyUpi,space(0,8));
        Button qr = button("SHOW PAYMENT QR"); qr.setOnClickListener(v -> showPaymentQr(paymentUri()));
        yearly.addView(qr,space(0,8));
        yearly.addView(text("Payment ke baad screenshot WhatsApp 9025156444 par bhejein.",13,true,Color.rgb(18,105,72)),space(4,4));
        Button confirm = button("SEND PAYMENT SCREENSHOT ON WHATSAPP");
        confirm.setOnClickListener(v -> openPaymentConfirmationWhatsApp());
        yearly.addView(confirm,space(4,8));
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
        String uri = paymentUri();
        try {
            Intent payment = new Intent(Intent.ACTION_VIEW);
            payment.setData(Uri.parse(uri));
            payment.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(Intent.createChooser(payment,"Pay ₹800 with UPI"));
            toast("Payment ke baad screenshot WhatsApp par bhejein");
        } catch(Exception e) {
            showPaymentQr(uri);
        }
    }

    private String paymentUri() {
        String note = "Business Dost 1 Year - " + SubscriptionManager.deviceId(this);
        return new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa",SubscriptionManager.UPI_ID)
                .appendQueryParameter("pn","LATHA EPS")
                .appendQueryParameter("tn",note)
                .appendQueryParameter("am",SubscriptionManager.YEARLY_PRICE+".00")
                .appendQueryParameter("cu","INR")
                .build().toString();
    }

    private void openPaymentConfirmationWhatsApp() {
        String phone = "919025156444";
        String message = "Hello LATHAEPS, maine ₹800 yearly subscription payment kiya hai.\n"
                + "Device ID: " + SubscriptionManager.deviceId(this) + "\n"
                + "Payment screenshot attach kar raha/rahi hoon. Please activate my app.";
        Uri chat = Uri.parse("https://wa.me/"+phone+"?text="+Uri.encode(message));
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,chat);
            if (isInstalled("com.whatsapp.w4b")) intent.setPackage("com.whatsapp.w4b");
            else if (isInstalled("com.whatsapp")) intent.setPackage("com.whatsapp");
            startActivity(intent);
            toast("WhatsApp chat khul gaya • Ab payment screenshot attach karke SEND karein");
        } catch(Exception e) {
            copy("Payment confirmation",message);
            toast("WhatsApp open nahi hua • Message copy ho gaya. 9025156444 par bhejein");
        }
    }

    private boolean isInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName,0);
            return true;
        } catch(Exception ignored) {
            return false;
        }
    }

    private void showPaymentQr(String uri) {
        try {
            int size = dp(260);
            BitMatrix matrix = new QRCodeWriter().encode(uri, BarcodeFormat.QR_CODE,size,size);
            Bitmap bitmap = Bitmap.createBitmap(size,size,Bitmap.Config.RGB_565);
            for(int y=0;y<size;y++) for(int x=0;x<size;x++) bitmap.setPixel(x,y,matrix.get(x,y)?Color.BLACK:Color.WHITE);
            ImageView image = new ImageView(this); image.setImageBitmap(bitmap); image.setPadding(dp(12),dp(12),dp(12),dp(12));
            new AlertDialog.Builder(this).setTitle("Pay ₹800 by UPI")
                    .setMessage("QR ko dusre phone ke UPI app se scan karein.\nUPI ID: "+SubscriptionManager.UPI_ID)
                    .setView(image)
                    .setPositiveButton("COPY UPI ID",(d,w)->{copy("UPI ID",SubscriptionManager.UPI_ID);toast("UPI ID copied");})
                    .setNegativeButton("CLOSE",null).show();
        } catch(Exception e) {
            new AlertDialog.Builder(this).setTitle("UPI payment")
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
