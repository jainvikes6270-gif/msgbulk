package com.lathaeps.lathabulk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int CONTACT_PERMISSION = 101;
    private static final int PICK_PDF = 102;
    private static final int NOTIFICATION_PERMISSION = 103;
    private static final String CHANNEL_ID = "latha_bulk_progress";
    private static final int NOTIFICATION_ID = 511;
    private static final String PREFS = "latha_bulk_prefs";
    private static final String GROUPS_KEY = "saved_groups";

    private final List<ContactItem> allContacts = new ArrayList<>();
    private final List<ContactItem> visibleContacts = new ArrayList<>();
    private final Set<String> selectedNumbers = new LinkedHashSet<>();
    private final List<ContactItem> queue = new ArrayList<>();

    private ArrayAdapter<ContactItem> adapter;
    private ListView listView;
    private TextView statusText, pdfText, miniProgress;
    private EditText messageBox, searchBox;
    private Button sendButton, accessibilityButton, editGroupButton;
    private Uri pdfUri;
    private int queueIndex = 0;
    private String activeGroup = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        createNotificationChannel();
        requestNotificationPermissionIfNeeded();
        setContentView(buildUi());
    }

    @Override protected void onResume() {
        super.onResume();
        refreshAccessibilityButton();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(10), dp(6), dp(10), dp(8));
        root.setBackgroundColor(Color.rgb(248, 246, 240));

        TextView title = new TextView(this);
        title.setText("LATHA BULK v2.2");
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.rgb(25,25,25));
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(34)));

        LinearLayout tools = row();
        Button load = button("Contacts");
        Button selectAll = button("Select all");
        Button clear = button("Clear");
        tools.addView(load, weighted(1.1f, 42));
        tools.addView(selectAll, weighted(1f, 42));
        tools.addView(clear, weighted(.8f, 42));
        root.addView(tools);
        load.setOnClickListener(v -> requestContacts());
        selectAll.setOnClickListener(v -> selectAllVisible());
        clear.setOnClickListener(v -> clearSelection());

        searchBox = new EditText(this);
        searchBox.setHint("Search name or mobile number");
        searchBox.setSingleLine(true);
        searchBox.setTextSize(14);
        searchBox.setPadding(dp(10), 0, dp(10), 0);
        root.addView(searchBox, new LinearLayout.LayoutParams(-1, dp(43)));
        searchBox.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int b,int c){ filterContacts(s.toString()); }
            public void afterTextChanged(Editable e){}
        });

        LinearLayout groups = row();
        Button saveGroup = button("Save group");
        Button myGroups = button("My groups");
        editGroupButton = button("Add / remove");
        groups.addView(saveGroup, weighted(1f, 41));
        groups.addView(myGroups, weighted(1f, 41));
        groups.addView(editGroupButton, weighted(1.15f, 41));
        root.addView(groups);
        saveGroup.setOnClickListener(v -> showSaveGroupDialog());
        myGroups.setOnClickListener(v -> showGroupsDialog());
        editGroupButton.setOnClickListener(v -> editActiveGroupContacts());

        accessibilityButton = button("Accessibility: OFF");
        accessibilityButton.setTextSize(13);
        accessibilityButton.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (Exception e) { toast("Accessibility settings open nahi hui"); }
        });
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(-1, dp(39));
        alp.setMargins(dp(2), dp(1), dp(2), dp(1));
        root.addView(accessibilityButton, alp);

        statusText = new TextView(this);
        statusText.setText("Selected: 0 | Contacts: 0");
        statusText.setTypeface(Typeface.DEFAULT_BOLD);
        statusText.setTextSize(12);
        statusText.setPadding(dp(3), dp(3), dp(3), dp(3));
        root.addView(statusText, new LinearLayout.LayoutParams(-1, dp(28)));

        listView = new ListView(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setDividerHeight(1);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, visibleContacts);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((p,v,pos,id) -> {
            ContactItem item = visibleContacts.get(pos);
            if (selectedNumbers.contains(item.number)) selectedNumbers.remove(item.number);
            else selectedNumbers.add(item.number);
            refreshChecks();
        });
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1f));

        messageBox = new EditText(this);
        messageBox.setHint("WhatsApp message");
        messageBox.setMinLines(1);
        messageBox.setMaxLines(2);
        messageBox.setTextSize(14);
        messageBox.setGravity(Gravity.TOP);
        root.addView(messageBox, new LinearLayout.LayoutParams(-1, dp(57)));

        LinearLayout fileRow = row();
        Button pdf = button("Choose PDF");
        Button share = button("Share PDF");
        fileRow.addView(pdf, weighted(1f, 38));
        fileRow.addView(share, weighted(1f, 38));
        root.addView(fileRow);
        pdf.setOnClickListener(v -> choosePdf());
        share.setOnClickListener(v -> sharePdf());

        pdfText = new TextView(this);
        pdfText.setText("No PDF selected");
        pdfText.setTextSize(11);
        pdfText.setSingleLine(true);
        root.addView(pdfText, new LinearLayout.LayoutParams(-1, dp(20)));

        miniProgress = new TextView(this);
        miniProgress.setText("Ready");
        miniProgress.setTextSize(11);
        miniProgress.setGravity(Gravity.CENTER);
        miniProgress.setBackgroundColor(Color.rgb(235,232,222));
        root.addView(miniProgress, new LinearLayout.LayoutParams(-1, dp(24)));

        sendButton = button("SEND TO SELECTED CONTACTS");
        sendButton.setTextSize(16);
        sendButton.setTypeface(Typeface.DEFAULT_BOLD);
        sendButton.setTextColor(Color.WHITE);
        sendButton.setBackgroundColor(Color.rgb(25,110,60));
        sendButton.setOnClickListener(v -> startOrNext());
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, dp(54));
        slp.setMargins(0, dp(3), 0, 0);
        root.addView(sendButton, slp);
        return root;
    }

    private LinearLayout row(){ LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); return r; }
    private Button button(String text){ Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(13); b.setPadding(dp(3),0,dp(3),0); return b; }
    private LinearLayout.LayoutParams weighted(float w,int h){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(h),w); lp.setMargins(dp(1),dp(1),dp(1),dp(1)); return lp; }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }

    private void refreshAccessibilityButton(){
        if(accessibilityButton==null)return;
        boolean enabled=Settings.Secure.getInt(getContentResolver(),Settings.Secure.ACCESSIBILITY_ENABLED,0)==1;
        accessibilityButton.setText(enabled?"Accessibility: ON":"Accessibility: OFF");
        accessibilityButton.setTextColor(enabled?Color.rgb(0,95,35):Color.rgb(150,35,35));
    }

    private void requestContacts(){
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED) loadContacts();
        else requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},CONTACT_PERMISSION);
    }
    private void requestNotificationPermissionIfNeeded(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFICATION_PERMISSION);
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if(requestCode==CONTACT_PERMISSION){
            if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)loadContacts(); else toast("Contacts permission required");
        }
    }

    private void loadContacts(){
        allContacts.clear(); visibleContacts.clear();
        Cursor c=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER},
                null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
        Set<String> seen=new LinkedHashSet<>();
        if(c!=null){
            int ni=c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int pi=c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while(c.moveToNext()){
                String num=normalize(c.getString(pi));
                if(!num.isEmpty()&&seen.add(num)){
                    String name=c.getString(ni); allContacts.add(new ContactItem(name==null?"Unknown":name,num));
                }
            } c.close();
        }
        Collections.sort(allContacts, Comparator.comparing(a->a.name.toLowerCase(Locale.ROOT)));
        filterContacts(searchBox.getText().toString()); toast(allContacts.size()+" contacts loaded");
    }
    private String normalize(String raw){
        if(raw==null)return""; String d=raw.replaceAll("[^0-9]","");
        if(d.startsWith("91")&&d.length()==12)return d;
        if(d.startsWith("0")&&d.length()==11)d=d.substring(1);
        if(d.length()==10)return"91"+d; return"";
    }
    private void filterContacts(String query){
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT); visibleContacts.clear();
        for(ContactItem i:allContacts) if(q.isEmpty()||i.name.toLowerCase(Locale.ROOT).contains(q)||i.number.contains(q))visibleContacts.add(i);
        if(adapter!=null){adapter.notifyDataSetChanged();refreshChecks();}
    }
    private void refreshChecks(){
        if(listView==null)return; listView.clearChoices();
        for(int i=0;i<visibleContacts.size();i++)listView.setItemChecked(i,selectedNumbers.contains(visibleContacts.get(i).number));
        statusText.setText("Selected: "+selectedNumbers.size()+" | Showing: "+visibleContacts.size()+" / "+allContacts.size()+
                (activeGroup.isEmpty()?"":" | "+activeGroup));
        editGroupButton.setEnabled(!activeGroup.isEmpty());
    }
    private void selectAllVisible(){for(ContactItem i:visibleContacts)selectedNumbers.add(i.number);refreshChecks();}
    private void clearSelection(){selectedNumbers.clear();queue.clear();queueIndex=0;activeGroup="";sendButton.setText("SEND TO SELECTED CONTACTS");miniProgress.setText("Ready");cancelProgressNotification();refreshChecks();}

    private Map<String,List<String>> readGroups(){
        Map<String,List<String>> groups=new LinkedHashMap<>();
        try{
            JSONObject root=new JSONObject(getSharedPreferences(PREFS,MODE_PRIVATE).getString(GROUPS_KEY,"{}"));
            JSONArray names=root.names(); if(names!=null)for(int i=0;i<names.length();i++){
                String n=names.getString(i); JSONArray a=root.getJSONArray(n); List<String> nums=new ArrayList<>();
                for(int j=0;j<a.length();j++)nums.add(a.getString(j)); groups.put(n,nums);
            }
        }catch(Exception ignored){} return groups;
    }
    private void writeGroups(Map<String,List<String>> groups){
        try{JSONObject root=new JSONObject(); for(Map.Entry<String,List<String>>e:groups.entrySet())root.put(e.getKey(),new JSONArray(e.getValue()));
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(GROUPS_KEY,root.toString()).apply();
        }catch(Exception e){toast("Group save failed");}
    }
    private void showSaveGroupDialog(){
        if(selectedNumbers.isEmpty()){toast("Select contacts first");return;}
        EditText input=new EditText(this);input.setHint("Example: Dealers");input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Save group").setMessage(selectedNumbers.size()+" contacts selected").setView(input)
                .setPositiveButton("Save",(d,w)->{String n=input.getText().toString().trim();if(n.isEmpty()){toast("Enter group name");return;}
                    Map<String,List<String>>g=readGroups();g.put(n,new ArrayList<>(selectedNumbers));writeGroups(g);activeGroup=n;refreshChecks();toast("Saved: "+n);})
                .setNegativeButton("Cancel",null).show();
    }
    private void showGroupsDialog(){
        Map<String,List<String>> groups=readGroups(); if(groups.isEmpty()){toast("No saved groups");return;}
        String[] names=groups.keySet().toArray(new String[0]);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Tap to open • Long press to manage").setItems(names,null).setNegativeButton("Close",null).create();
        dialog.setOnShowListener(x->{
            ListView lv=dialog.getListView();
            lv.setOnItemClickListener((p,v,pos,id)->{openGroup(names[pos]);dialog.dismiss();});
            lv.setOnItemLongClickListener((p,v,pos,id)->{showGroupLongPressMenu(names[pos]);dialog.dismiss();return true;});
        }); dialog.show();
    }
    private void openGroup(String name){
        List<String> nums=readGroups().get(name); if(nums==null)return;
        selectedNumbers.clear();selectedNumbers.addAll(nums);activeGroup=name;searchBox.setText("");refreshChecks();toast(name+" opened");
    }
    private void showGroupLongPressMenu(String name){
        String[] actions={"Edit contacts","Rename","Duplicate","Delete"};
        new AlertDialog.Builder(this).setTitle(name).setItems(actions,(d,which)->{
            if(which==0){openGroup(name);editActiveGroupContacts();}
            else if(which==1)renameGroup(name);
            else if(which==2)duplicateGroup(name);
            else confirmDeleteGroup(name);
        }).setNegativeButton("Cancel",null).show();
    }
    private void renameGroup(String oldName){
        EditText input=new EditText(this);input.setText(oldName);input.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this).setTitle("Rename group").setView(input).setPositiveButton("Rename",(d,w)->{
            String n=input.getText().toString().trim();if(n.isEmpty()||n.equals(oldName))return;
            Map<String,List<String>>g=readGroups();List<String>nums=g.remove(oldName);g.put(n,nums);writeGroups(g);if(activeGroup.equals(oldName))activeGroup=n;refreshChecks();toast("Renamed to "+n);
        }).setNegativeButton("Cancel",null).show();
    }
    private void duplicateGroup(String name){
        Map<String,List<String>>g=readGroups();String base=name+" Copy",n=base;int i=2;while(g.containsKey(n))n=base+" "+i++;
        g.put(n,new ArrayList<>(g.get(name)));writeGroups(g);toast("Created: "+n);
    }
    private void confirmDeleteGroup(String name){
        new AlertDialog.Builder(this).setTitle("Delete group?").setMessage(name+" will be removed. Phone contacts will not be deleted.")
                .setPositiveButton("Delete",(d,w)->{Map<String,List<String>>g=readGroups();g.remove(name);writeGroups(g);if(activeGroup.equals(name)){activeGroup="";selectedNumbers.clear();}refreshChecks();toast("Deleted: "+name);})
                .setNegativeButton("Cancel",null).show();
    }
    private void editActiveGroupContacts(){
        if(activeGroup.isEmpty()){toast("Open a saved group first");return;}
        if(allContacts.isEmpty()){toast("Load phone contacts first");return;}
        String[] labels=new String[allContacts.size()];boolean[] checked=new boolean[allContacts.size()];
        for(int i=0;i<allContacts.size();i++){labels[i]=allContacts.get(i).toString();checked[i]=selectedNumbers.contains(allContacts.get(i).number);}
        new AlertDialog.Builder(this).setTitle("Add / remove • "+activeGroup).setMultiChoiceItems(labels,checked,(d,which,isChecked)->checked[which]=isChecked)
                .setPositiveButton("Save changes",(d,w)->{
                    selectedNumbers.clear();for(int i=0;i<checked.length;i++)if(checked[i])selectedNumbers.add(allContacts.get(i).number);
                    Map<String,List<String>>g=readGroups();g.put(activeGroup,new ArrayList<>(selectedNumbers));writeGroups(g);refreshChecks();toast("Group updated: "+selectedNumbers.size()+" contacts");
                }).setNegativeButton("Cancel",null).show();
    }

    private void startOrNext(){
        hideKeyboard();
        if(queue.isEmpty()){
            if(selectedNumbers.isEmpty()){toast("Select contacts or open group");return;}
            if(messageBox.getText().toString().trim().isEmpty()){toast("Type a message first");return;}
            for(ContactItem i:allContacts)if(selectedNumbers.contains(i.number))queue.add(i);
            if(queue.isEmpty()){toast("Load contacts, then open group again");return;} queueIndex=0;
        } openNext();
    }
    private void openNext(){
        if(queueIndex>=queue.size()){
            int total=queue.size();miniProgress.setText("Completed: "+total+" / "+total);showProgressNotification(total,total,"Completed");toast("Completed "+total+" chats");
            queue.clear();queueIndex=0;sendButton.setText("SEND TO SELECTED CONTACTS");return;
        }
        ContactItem item=queue.get(queueIndex);int current=queueIndex+1;
        miniProgress.setText("Opening "+current+"/"+queue.size()+" • "+item.name);showProgressNotification(current,queue.size(),item.name);queueIndex++;
        String encoded=URLEncoder.encode(messageBox.getText().toString().trim(),StandardCharsets.UTF_8);
        Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/"+item.number+"?text="+encoded));
        try{i.setPackage("com.whatsapp");startActivity(i);}catch(Exception e1){try{i.setPackage("com.whatsapp.w4b");startActivity(i);}catch(Exception e2){i.setPackage(null);try{startActivity(i);}catch(Exception e3){toast("WhatsApp not found");}}}
        sendButton.setText(queueIndex<queue.size()?"OPEN NEXT ("+(queueIndex+1)+"/"+queue.size()+")":"FINISH");
    }
    private void hideKeyboard(){View v=getCurrentFocus();if(v!=null)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(),0);}

    private void choosePdf(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");startActivityForResult(i,PICK_PDF);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_PDF&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){pdfUri=data.getData();try{getContentResolver().takePersistableUriPermission(pdfUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}pdfText.setText("PDF: "+pdfUri.getLastPathSegment());}}
    private void sharePdf(){if(pdfUri==null){toast("Choose PDF first");return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,pdfUri);i.putExtra(Intent.EXTRA_TEXT,messageBox.getText().toString().trim());i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{i.setPackage("com.whatsapp");startActivity(i);}catch(Exception e){i.setPackage(null);startActivity(Intent.createChooser(i,"Share PDF"));}}

    private void createNotificationChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL_ID,"Sending progress",NotificationManager.IMPORTANCE_LOW);c.setDescription("Compact queue progress");c.setSound(null,null);getSystemService(NotificationManager.class).createNotificationChannel(c);}}
    private void showProgressNotification(int current,int total,String contact){Intent launch=new Intent(this,MainActivity.class);PendingIntent p=PendingIntent.getActivity(this,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(this,CHANNEL_ID).setSmallIcon(android.R.drawable.stat_sys_upload).setContentTitle(current>=total?"Latha Bulk completed":"Sending "+current+"/"+total).setContentText(contact).setOnlyAlertOnce(true).setOngoing(current<total).setPriority(NotificationCompat.PRIORITY_LOW).setProgress(total,Math.min(current,total),false).setContentIntent(p);
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID,b.build());}
    private void cancelProgressNotification(){((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private static class ContactItem{final String name,number;ContactItem(String n,String p){name=n;number=p;}@Override public String toString(){return name+"  •  +"+number;}}
}
