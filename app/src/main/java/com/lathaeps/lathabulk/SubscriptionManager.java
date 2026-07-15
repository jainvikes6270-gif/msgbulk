package com.lathaeps.lathabulk;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class SubscriptionManager {
    static final String PREFS = "latha_subscription";
    static final String UPI_ID = "jainvikes6270@oksbi";
    static final int YEARLY_PRICE = 800;
    static final int TRIAL_DAYS = 12;
    private static final String TRIAL_START = "trial_start";
    private static final String ACTIVATION_CODE = "activation_code";
    private static final long DAY = 24L * 60L * 60L * 1000L;
    private static final String SIGNING_SECRET = "LathaEPS-Offline-License-v3.23-2026-7A9C";

    private SubscriptionManager() {}

    static void ensureTrial(Context c) {
        SharedPreferences p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        if(!p.contains(TRIAL_START)) p.edit().putLong(TRIAL_START,System.currentTimeMillis()).apply();
    }

    static String deviceId(Context c) {
        String raw=Settings.Secure.getString(c.getContentResolver(),Settings.Secure.ANDROID_ID);
        if(raw==null||raw.trim().isEmpty()) raw="unknown-device";
        return sha256(raw+"|"+c.getPackageName()).substring(0,12).toUpperCase(Locale.ROOT);
    }

    static boolean hasAccess(Context c) {
        ensureTrial(c);
        String code=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(ACTIVATION_CODE,"");
        License license=verify(c,code);
        if(license.valid && (license.lifetime || license.expiryDay>=todayDay())) return true;
        long start=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getLong(TRIAL_START,System.currentTimeMillis());
        return System.currentTimeMillis() < start + TRIAL_DAYS*DAY;
    }

    static int trialDaysLeft(Context c) {
        long start=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getLong(TRIAL_START,System.currentTimeMillis());
        long left=(start+TRIAL_DAYS*DAY)-System.currentTimeMillis();
        return left<=0?0:(int)Math.ceil(left/(double)DAY);
    }

    static String statusText(Context c) {
        License l=verify(c,c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(ACTIVATION_CODE,""));
        if(l.valid&&l.lifetime) return "Lifetime Free • Active";
        if(l.valid&&l.expiryDay>=todayDay()) return "₹800/year • Active until "+formatDay(l.expiryDay);
        int left=trialDaysLeft(c);
        return left>0?"Free trial • "+left+" day(s) left":"Plan expired • Payment required";
    }

    static String activate(Context c,String code) {
        License l=verify(c,code);
        if(!l.valid) return "Invalid activation code for this device";
        if(!l.lifetime&&l.expiryDay<todayDay()) return "Activation code has expired";
        c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(ACTIVATION_CODE,code.trim().toUpperCase(Locale.ROOT)).apply();
        return l.lifetime?"Lifetime Free activated":"1 Year plan activated until "+formatDay(l.expiryDay);
    }

    static String generateCode(String deviceId,boolean lifetime) {
        String device=deviceId.replaceAll("[^A-Za-z0-9]","").toUpperCase(Locale.ROOT);
        if(device.length()!=12) return "";
        long expiry=lifetime?0:todayDay()+365;
        String plan=lifetime?"L":"Y";
        return plan+"-"+expiry+"-"+signature(device,plan,expiry);
    }

    private static License verify(Context c,String code) {
        try{
            String[] parts=code.trim().toUpperCase(Locale.ROOT).split("-");
            if(parts.length!=3||(!parts[0].equals("L")&&!parts[0].equals("Y"))) return License.invalid();
            long expiry=Long.parseLong(parts[1]);
            String expected=signature(deviceId(c),parts[0],expiry);
            boolean ok=MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),parts[2].getBytes(StandardCharsets.UTF_8));
            return ok?new License(true,parts[0].equals("L"),expiry):License.invalid();
        }catch(Exception e){return License.invalid();}
    }

    private static String signature(String device,String plan,long expiry) {
        try{
            Mac mac=Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SIGNING_SECRET.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
            byte[] out=mac.doFinal((device+"|"+plan+"|"+expiry).getBytes(StandardCharsets.UTF_8));
            StringBuilder b=new StringBuilder();
            for(byte x:out)b.append(String.format(Locale.ROOT,"%02X",x));
            return b.substring(0,16);
        }catch(Exception e){return "";}
    }

    private static String sha256(String value) {
        try{
            byte[] out=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder b=new StringBuilder();for(byte x:out)b.append(String.format(Locale.ROOT,"%02x",x));return b.toString();
        }catch(Exception e){return "00000000000000000000000000000000";}
    }

    private static long todayDay(){return System.currentTimeMillis()/DAY;}
    private static String formatDay(long day){return new java.text.SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(new java.util.Date(day*DAY));}

    private static final class License {
        final boolean valid,lifetime;final long expiryDay;
        License(boolean valid,boolean lifetime,long expiryDay){this.valid=valid;this.lifetime=lifetime;this.expiryDay=expiryDay;}
        static License invalid(){return new License(false,false,0);}
    }
}
