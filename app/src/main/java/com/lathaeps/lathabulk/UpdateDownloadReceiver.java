package com.lathaeps.lathabulk;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class UpdateDownloadReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if(!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction()))return;
        long completed=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1L);
        SharedPreferences prefs=context.getSharedPreferences(MainActivity.PREFS,Context.MODE_PRIVATE);
        if(completed<0||completed!=prefs.getLong("app_update_download_id",-2L))return;
        DownloadManager dm=(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        if(dm==null)return;
        try(Cursor c=dm.query(new DownloadManager.Query().setFilterById(completed))){
            if(c==null||!c.moveToFirst()||c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))!=DownloadManager.STATUS_SUCCESSFUL){Toast.makeText(context,"Update download failed • retry from Settings",Toast.LENGTH_LONG).show();return;}
        }
        String name=prefs.getString("app_update_file","");
        File apk=new File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS),name);
        if(!apk.exists()){Toast.makeText(context,"Downloaded APK nahi mili",Toast.LENGTH_LONG).show();return;}
        Uri uri=FileProvider.getUriForFile(context,context.getPackageName()+".fileprovider",apk);
        Intent install=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try{context.startActivity(install);prefs.edit().remove("app_update_download_id").apply();}catch(Exception e){Toast.makeText(context,"Update notification open karke APK install karein",Toast.LENGTH_LONG).show();}
    }
}
