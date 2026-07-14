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
import android.content.ComponentName;
import android.text.TextUtils;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.MediaStore;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.util.Random;
import android.util.Base64;
import android.text.InputType;
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
import android.widget.Spinner;
import android.widget.CheckBox;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

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
    private static final int PICK_CSV = 104;
    private static final int PICK_REPLY_IMAGE = 105;
    private static final int PICK_LEDGER_FILE = 106;
    private static final int PICK_CATALOG_FILE = 107;
    private static final int PICK_PRICE_FILE = 108;
    private static final int CREATE_BACKUP = 109;
    private static final int PICK_RESTORE = 110;
    private static final String CHANNEL_ID = "latha_bulk_progress";
    private static final int NOTIFICATION_ID = 511;
    private static final String PREFS = "latha_bulk_prefs";
    private static final String GROUPS_KEY = "saved_groups";
    static final String AUTO_PREFS = "latha_auto_send";
    static final String AUTO_NUMBERS = "numbers";
    static final String AUTO_MESSAGE = "message";
    static final String AUTO_INDEX = "index";
    static final String AUTO_RUNNING = "running";
    static final String AUTO_NAMES = "names";
    static final String AUTO_MIN_DELAY = "min_delay";
    static final String AUTO_MAX_DELAY = "max_delay";
    static final String AUTO_HISTORY = "history";
    static final String AUTO_FAILED = "failed";
    private static final String DARK_KEY = "dark_mode";
    private static final String SAVED_CONTACTS_KEY = "saved_contacts";
    private static final String PIN_KEY = "login_pin";
    private static final String LOGIN_ENABLED_KEY = "login_enabled";
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

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
        showLoginOrApp();
    }

    private void showLoginOrApp(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        String pin=p.getString(PIN_KEY,"");
        if(pin.isEmpty()){
            setContentView(buildUi());
            uiHandler.postDelayed(this::showCreatePinDialog,300);
        }else if(p.getBoolean(LOGIN_ENABLED_KEY,true)){
            showUnlockDialog(pin);
        }else setContentView(buildUi());
    }

    private void showCreatePinDialog(){
        EditText input=new EditText(this); input.setHint("Create 4-digit PIN"); input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); input.setMaxLines(1);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Set app login PIN").setMessage("LathaBulk open karne ke liye 4-digit PIN banaye.").setView(input)
            .setPositiveButton("Save",null).setNegativeButton("Not now",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String pin=input.getText().toString().trim();if(pin.length()!=4){input.setError("Enter exactly 4 digits");return;}getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PIN_KEY,pin).putBoolean(LOGIN_ENABLED_KEY,true).apply();toast("Login PIN saved");d.dismiss();}));
        d.show();
    }

    private void showUnlockDialog(String savedPin){
        EditText input=new EditText(this); input.setHint("Enter PIN"); input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("LathaBulk Login").setMessage("4-digit PIN enter kare").setView(input).setCancelable(false)
            .setPositiveButton("Login",null).setNegativeButton("Exit",(a,b)->finish()).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(savedPin.equals(input.getText().toString().trim())){d.dismiss();setContentView(buildUi());}else input.setError("Wrong PIN");}));
        d.show();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshAccessibilityButton();
        boolean running=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getBoolean(AUTO_RUNNING,false);
        if(sendButton!=null)sendButton.setText(running?"STOP AUTO SENDING":"AUTO SEND TO SELECTED CONTACTS");
        if(miniProgress!=null&&running){
            int i=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getInt(AUTO_INDEX,0);
            miniProgress.setText("Sending contact "+(i+1));
        }
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(10), dp(6), dp(10), dp(8));
        root.setBackgroundColor(isDark()?Color.rgb(28,28,28):Color.rgb(248,246,240));

        TextView title = new TextView(this);
        title.setText("LATHA BULK v3.5 LOGIN • BACKUP");
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        GradientDrawable headerBg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.rgb(25,80,180),Color.rgb(125,55,190),Color.rgb(218,156,25)});
        headerBg.setCornerRadius(dp(14)); title.setBackground(headerBg);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(34)));

        LinearLayout tools = row();
        Button load = button("Contacts");
        Button savedContacts = button("Saved list");
        Button selectAll = button("Select all");
        Button clear = button("Clear");
        tools.addView(load, weighted(1f, 42));
        tools.addView(savedContacts, weighted(1f, 42));
        tools.addView(selectAll, weighted(.9f, 42));
        tools.addView(clear, weighted(.7f, 42));
        root.addView(tools);
        load.setOnClickListener(v -> requestContacts());
        savedContacts.setOnClickListener(v -> showSavedContactsDialog());
        selectAll.setOnClickListener(v -> selectAllVisible());
        clear.setOnClickListener(v -> clearSelection());

        LinearLayout featureRow = row();
        Button csv = button("Import CSV");
        Button delay = button("Delay");
        Button schedule = button("Schedule");
        Button theme = button(isDark()?"Light":"Dark");
        featureRow.addView(csv, weighted(1f, 38));
        featureRow.addView(delay, weighted(.8f, 38));
        featureRow.addView(schedule, weighted(1f, 38));
        featureRow.addView(theme, weighted(.8f, 38));
        root.addView(featureRow);
        csv.setOnClickListener(v -> chooseCsv());
        delay.setOnClickListener(v -> showDelayDialog());
        schedule.setOnClickListener(v -> showScheduleDialog());
        theme.setOnClickListener(v -> { getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(DARK_KEY,!isDark()).apply(); recreate(); });

        LinearLayout accountRow=row();
        Button login=button("Login / PIN");
        Button backup=button("Backup");
        Button restore=button("Restore");
        accountRow.addView(login,weighted(1f,38)); accountRow.addView(backup,weighted(1f,38)); accountRow.addView(restore,weighted(1f,38));
        root.addView(accountRow);
        login.setOnClickListener(v->showLoginSettings());
        backup.setOnClickListener(v->createBackupFile());
        restore.setOnClickListener(v->chooseRestoreFile());

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

        LinearLayout businessRow=row();
        Button businessFiles=button("BUSINESS FILES");
        Button autoReplyButton=button("AUTO REPLY");
        businessFiles.setTypeface(Typeface.DEFAULT_BOLD);
        autoReplyButton.setTypeface(Typeface.DEFAULT_BOLD);
        businessRow.addView(businessFiles,weighted(1f,42));
        businessRow.addView(autoReplyButton,weighted(1f,42));
        root.addView(businessRow);
        businessFiles.setOnClickListener(v->showBusinessFilesDialog());
        autoReplyButton.setOnClickListener(v->showAutoReplyDialog());

        messageBox = new EditText(this);
        messageBox.setHint("Message • use {Name} for personal name");
        messageBox.setMinLines(1);
        messageBox.setMaxLines(2);
        messageBox.setTextSize(14);
        messageBox.setGravity(Gravity.TOP);
        root.addView(messageBox, new LinearLayout.LayoutParams(-1, dp(57)));

        sendButton = button("AUTO SEND TO SELECTED CONTACTS");
        sendButton.setTextSize(15);
        sendButton.setTypeface(Typeface.DEFAULT_BOLD);
        sendButton.setTextColor(Color.WHITE);
        sendButton.setBackgroundColor(Color.rgb(25,110,60));
        sendButton.setOnClickListener(v -> startOrStopAutoSend());
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, dp(52));
        slp.setMargins(0, dp(3), 0, dp(3));
        root.addView(sendButton, slp);

        miniProgress = new TextView(this);
        miniProgress.setText("Ready");
        miniProgress.setTextSize(11);
        miniProgress.setGravity(Gravity.CENTER);
        miniProgress.setBackgroundColor(Color.rgb(235,232,222));
        root.addView(miniProgress, new LinearLayout.LayoutParams(-1, dp(24)));

        LinearLayout reportRow=row();
        Button history=button("History");
        Button retry=button("Retry failed");
        Button resume=button("Resume");
        reportRow.addView(history,weighted(1f,36));
        reportRow.addView(retry,weighted(1f,36));
        reportRow.addView(resume,weighted(1f,36));
        root.addView(reportRow);
        history.setOnClickListener(v->showHistory());
        retry.setOnClickListener(v->retryFailed());
        resume.setOnClickListener(v->resumeQueue());

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
            saveSelectedContacts();
            refreshChecks();
        });
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1f));

        return root;
    }

    private LinearLayout row(){ LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); return r; }
    private Button button(String text){ Button b=new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(13); b.setPadding(dp(3),0,dp(3),0); return b; }
    private LinearLayout.LayoutParams weighted(float w,int h){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(h),w); lp.setMargins(dp(1),dp(1),dp(1),dp(1)); return lp; }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }

    private void refreshAccessibilityButton(){
        if(accessibilityButton==null)return;
        boolean enabled=isAccessibilityServiceEnabled();
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
    private void clearSelection(){selectedNumbers.clear();queue.clear();queueIndex=0;activeGroup="";sendButton.setText("AUTO SEND TO SELECTED CONTACTS");miniProgress.setText("Ready");cancelProgressNotification();refreshChecks();}

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

    private boolean isAccessibilityServiceEnabled(){
        ComponentName expected=new ComponentName(this,WhatsAppAccessibilityService.class);
        String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if(enabled==null)return false;
        TextUtils.SimpleStringSplitter splitter=new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);
        while(splitter.hasNext()){
            ComponentName c=ComponentName.unflattenFromString(splitter.next());
            if(expected.equals(c))return true;
        }
        return false;
    }

    private void startOrStopAutoSend(){
        hideKeyboard();
        boolean running=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getBoolean(AUTO_RUNNING,false);
        if(running){
            stopAutoSend("Stopped");
            return;
        }
        if(!isAccessibilityServiceEnabled()){
            toast("Pehle Accessibility ON karein");
            try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(Exception ignored){}
            return;
        }
        String message=messageBox.getText().toString().trim();
        if(selectedNumbers.isEmpty()){toast("Select contacts or open group");return;}
        if(message.isEmpty()){toast("Type a message first");return;}
        JSONArray nums=new JSONArray();
        JSONArray names=new JSONArray();
        for(String n:selectedNumbers){
            nums.put(n);
            names.put(findName(n));
        }
        SharedPreferences settings=getSharedPreferences(PREFS,MODE_PRIVATE);
        getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit()
                .putString(AUTO_NUMBERS,nums.toString()).putString(AUTO_NAMES,names.toString()).putString(AUTO_MESSAGE,message)
                .putInt(AUTO_MIN_DELAY,settings.getInt(AUTO_MIN_DELAY,3)).putInt(AUTO_MAX_DELAY,settings.getInt(AUTO_MAX_DELAY,7))
                .putString(AUTO_FAILED,"[]").putInt(AUTO_INDEX,0).putBoolean(AUTO_RUNNING,true).apply();
        sendButton.setText("STOP AUTO SENDING");
        miniProgress.setText("Starting 1 / "+selectedNumbers.size());
        showProgressNotification(0,selectedNumbers.size(),"Starting");
        WhatsAppAccessibilityService.openCurrentChat(this);
    }

    private void stopAutoSend(String label){
        getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().putBoolean(AUTO_RUNNING,false).apply();
        sendButton.setText("AUTO SEND TO SELECTED CONTACTS");
        miniProgress.setText(label);
        cancelProgressNotification();
        toast(label);
    }

    private void hideKeyboard(){View v=getCurrentFocus();if(v!=null)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(),0);}

    private void choosePdf(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");startActivityForResult(i,PICK_PDF);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_PDF&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){pdfUri=data.getData();try{getContentResolver().takePersistableUriPermission(pdfUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}if(pdfText!=null)pdfText.setText("PDF: "+pdfUri.getLastPathSegment());}
        if(requestCode==PICK_CSV&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) importCsv(data.getData());
        if(requestCode==PICK_REPLY_IMAGE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){saveReplyImagePermanently(data.getData());}
        if(requestCode==PICK_LEDGER_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){saveBusinessUri(AutoReplyNotificationService.LEDGER_URI,data.getData(),"Ledger file saved");}
        if(requestCode==PICK_CATALOG_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){saveBusinessUri(AutoReplyNotificationService.CATALOG_URI,data.getData(),"Catalog file saved");}
        if(requestCode==PICK_PRICE_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){saveBusinessUri(AutoReplyNotificationService.PRICE_URI,data.getData(),"Price List file saved");}
        if(requestCode==CREATE_BACKUP&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) writeBackup(data.getData());
        if(requestCode==PICK_RESTORE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) restoreBackup(data.getData());
    }
    private void sharePdf(){if(pdfUri==null){toast("Choose PDF first");return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,pdfUri);i.putExtra(Intent.EXTRA_TEXT,messageBox.getText().toString().trim());i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{i.setPackage("com.whatsapp");startActivity(i);}catch(Exception e){i.setPackage(null);startActivity(Intent.createChooser(i,"Share PDF"));}}

    private void createNotificationChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL_ID,"Sending progress",NotificationManager.IMPORTANCE_LOW);c.setDescription("Compact queue progress");c.setSound(null,null);getSystemService(NotificationManager.class).createNotificationChannel(c);}}
    private void showProgressNotification(int current,int total,String contact){Intent launch=new Intent(this,MainActivity.class);PendingIntent p=PendingIntent.getActivity(this,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(this,CHANNEL_ID).setSmallIcon(android.R.drawable.stat_sys_upload).setContentTitle(current>=total?"Latha Bulk completed":"Sending "+current+"/"+total).setContentText(contact).setOnlyAlertOnce(true).setOngoing(current<total).setPriority(NotificationCompat.PRIORITY_LOW).setProgress(total,Math.min(current,total),false).setContentIntent(p);
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID,b.build());}
    private void cancelProgressNotification(){((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);}

    private boolean isDark(){return getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean(DARK_KEY,false);}
    private String findName(String number){for(ContactItem c:allContacts)if(c.number.equals(number))return c.name;return number;}
    private void showLoginSettings(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        String[] items={"Change PIN","Turn login "+(p.getBoolean(LOGIN_ENABLED_KEY,true)?"OFF":"ON")};
        new AlertDialog.Builder(this).setTitle("Login settings").setItems(items,(d,which)->{
            if(which==0){EditText in=new EditText(this);in.setHint("New 4-digit PIN");in.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);new AlertDialog.Builder(this).setTitle("Change PIN").setView(in).setPositiveButton("Save",(a,b)->{String x=in.getText().toString().trim();if(x.length()==4){p.edit().putString(PIN_KEY,x).putBoolean(LOGIN_ENABLED_KEY,true).apply();toast("PIN changed");}else toast("PIN must be 4 digits");}).setNegativeButton("Cancel",null).show();}
            else {boolean n=!p.getBoolean(LOGIN_ENABLED_KEY,true);p.edit().putBoolean(LOGIN_ENABLED_KEY,n).apply();toast("Login "+(n?"ON":"OFF"));}
        }).setNegativeButton("Close",null).show();
    }

    private void createBackupFile(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"LathaBulk_Backup_"+new java.text.SimpleDateFormat("yyyyMMdd_HHmm",Locale.getDefault()).format(new java.util.Date())+".json");startActivityForResult(i,CREATE_BACKUP);
    }
    private void chooseRestoreFile(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");startActivityForResult(Intent.createChooser(i,"Select LathaBulk backup from phone or Drive"),PICK_RESTORE);}

    private JSONObject prefsToJson(String name)throws Exception{
        JSONObject o=new JSONObject(); Map<String,?> all=getSharedPreferences(name,MODE_PRIVATE).getAll();
        for(Map.Entry<String,?> e:all.entrySet()){Object v=e.getValue();if(v instanceof Set)o.put(e.getKey(),new JSONArray((Set<?>)v));else o.put(e.getKey(),v);}
        return o;
    }
    private void jsonToPrefs(String name,JSONObject o)throws Exception{
        SharedPreferences.Editor ed=getSharedPreferences(name,MODE_PRIVATE).edit().clear();java.util.Iterator<String> it=o.keys();
        while(it.hasNext()){String k=it.next();Object v=o.get(k);if(v instanceof Boolean)ed.putBoolean(k,(Boolean)v);else if(v instanceof Integer)ed.putInt(k,(Integer)v);else if(v instanceof Long)ed.putLong(k,(Long)v);else if(v instanceof Double)ed.putFloat(k,((Double)v).floatValue());else if(v instanceof JSONArray){Set<String> set=new LinkedHashSet<>();JSONArray a=(JSONArray)v;for(int i=0;i<a.length();i++)set.add(a.getString(i));ed.putStringSet(k,set);}else ed.putString(k,String.valueOf(v));}ed.apply();
    }
    private void addFilesToBackup(File dir,String prefix,JSONObject out)throws Exception{File[] fs=dir.listFiles();if(fs==null)return;for(File f:fs){String path=prefix+f.getName();if(f.isDirectory())addFilesToBackup(f,path+"/",out);else{ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream in=new FileInputStream(f)){byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)b.write(buf,0,n);}out.put(path,Base64.encodeToString(b.toByteArray(),Base64.NO_WRAP));}}}
    private void restoreFiles(JSONObject files)throws Exception{java.util.Iterator<String> it=files.keys();while(it.hasNext()){String rel=it.next();File f=new File(getFilesDir(),rel);File parent=f.getParentFile();if(parent!=null)parent.mkdirs();byte[] data=Base64.decode(files.getString(rel),Base64.DEFAULT);try(OutputStream out=new FileOutputStream(f)){out.write(data);}}}
    private void writeBackup(Uri uri){
        try{JSONObject root=new JSONObject();root.put("app","LathaBulk");root.put("version","3.5.0");root.put("created",System.currentTimeMillis());root.put(PREFS,prefsToJson(PREFS));root.put(AUTO_PREFS,prefsToJson(AUTO_PREFS));root.put(AutoReplyNotificationService.PREFS,prefsToJson(AutoReplyNotificationService.PREFS));JSONObject files=new JSONObject();addFilesToBackup(getFilesDir(),"",files);root.put("files",files);try(OutputStream out=getContentResolver().openOutputStream(uri)){out.write(root.toString(2).getBytes(StandardCharsets.UTF_8));}toast("Backup saved successfully");}catch(Exception e){toast("Backup failed: "+e.getMessage());}
    }
    private void restoreBackup(Uri uri){
        try{StringBuilder b=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri),StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)b.append(line);}JSONObject root=new JSONObject(b.toString());if(!"LathaBulk".equals(root.optString("app")))throw new Exception("Invalid backup file");jsonToPrefs(PREFS,root.getJSONObject(PREFS));jsonToPrefs(AUTO_PREFS,root.getJSONObject(AUTO_PREFS));jsonToPrefs(AutoReplyNotificationService.PREFS,root.getJSONObject(AutoReplyNotificationService.PREFS));if(root.has("files"))restoreFiles(root.getJSONObject("files"));toast("Restore complete");uiHandler.postDelayed(this::recreate,600);}catch(Exception e){toast("Restore failed: "+e.getMessage());}
    }

    private void chooseCsv(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("text/*");startActivityForResult(i,PICK_CSV);}
    private void importCsv(Uri uri){
        int added=0;try(BufferedReader br=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))){
            String line;while((line=br.readLine())!=null){String[] a=line.split(",");if(a.length==0)continue;String raw=a.length>1?a[1]:a[0];String num=normalize(raw);if(num.isEmpty())continue;String name=a.length>1?a[0].trim():"CSV Contact";boolean exists=false;for(ContactItem c:allContacts)if(c.number.equals(num)){exists=true;break;}if(!exists){allContacts.add(new ContactItem(name.isEmpty()?"CSV Contact":name,num));added++;}selectedNumbers.add(num);}Collections.sort(allContacts,Comparator.comparing(x->x.name.toLowerCase(Locale.ROOT)));filterContacts("");toast(added+" CSV contacts imported");}
        catch(Exception e){toast("CSV import failed. Format: Name,Phone");}
    }
    private void showDelayDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);EditText min=new EditText(this);EditText max=new EditText(this);min.setHint("Minimum seconds");max.setHint("Maximum seconds");min.setInputType(2);max.setInputType(2);SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);min.setText(String.valueOf(p.getInt(AUTO_MIN_DELAY,3)));max.setText(String.valueOf(p.getInt(AUTO_MAX_DELAY,7)));box.addView(min);box.addView(max);
        new AlertDialog.Builder(this).setTitle("Random delay").setMessage("Recommended 3–8 seconds").setView(box).setPositiveButton("Save",(d,w)->{try{int a=Integer.parseInt(min.getText().toString());int b=Integer.parseInt(max.getText().toString());if(a<2)a=2;if(b<a)b=a;getSharedPreferences(PREFS,MODE_PRIVATE).edit().putInt(AUTO_MIN_DELAY,a).putInt(AUTO_MAX_DELAY,b).apply();toast("Delay saved: "+a+"–"+b+" sec");}catch(Exception e){toast("Enter valid seconds");}}).setNegativeButton("Cancel",null).show();
    }
    private void showScheduleDialog(){EditText mins=new EditText(this);mins.setHint("Start after minutes");mins.setInputType(2);new AlertDialog.Builder(this).setTitle("Schedule auto send").setMessage("App open rakhein. Phone unlocked aur Accessibility ON honi chahiye.").setView(mins).setPositiveButton("Schedule",(d,w)->{try{int m=Integer.parseInt(mins.getText().toString());if(m<1)m=1;miniProgress.setText("Scheduled after "+m+" min");uiHandler.postDelayed(this::startOrStopAutoSend,m*60_000L);toast("Scheduled");}catch(Exception e){toast("Enter minutes");}}).setNegativeButton("Cancel",null).show();}
    private void showHistory(){String h=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getString(AUTO_HISTORY,"No sending history yet");new AlertDialog.Builder(this).setTitle("Sending history").setMessage(h).setPositiveButton("Close",null).setNeutralButton("Clear",(d,w)->getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().remove(AUTO_HISTORY).apply()).show();}
    private void retryFailed(){try{JSONArray f=new JSONArray(getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getString(AUTO_FAILED,"[]"));if(f.length()==0){toast("No failed contacts");return;}selectedNumbers.clear();for(int i=0;i<f.length();i++)selectedNumbers.add(f.getString(i));refreshChecks();toast(f.length()+" failed contacts selected");}catch(Exception e){toast("No failed contacts");}}
    private void resumeQueue(){SharedPreferences p=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE);try{JSONArray a=new JSONArray(p.getString(AUTO_NUMBERS,"[]"));int i=p.getInt(AUTO_INDEX,0);if(a.length()==0||i>=a.length()){toast("No paused queue");return;}p.edit().putBoolean(AUTO_RUNNING,true).apply();sendButton.setText("STOP AUTO SENDING");miniProgress.setText("Resuming "+(i+1)+" / "+a.length());WhatsAppAccessibilityService.openCurrentChat(this);}catch(Exception e){toast("No paused queue");}}


    private void openPhoneGalleryFirst(){
        try{
            Intent i;
            if(Build.VERSION.SDK_INT>=33){
                i=new Intent(MediaStore.ACTION_PICK_IMAGES);
                i.setType("image/*");
            }else{
                i=new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                i.addCategory(Intent.CATEGORY_OPENABLE);
            }
            startActivityForResult(Intent.createChooser(i,"Select image from Phone Gallery"),PICK_REPLY_IMAGE);
        }catch(Exception first){
            try{
                Intent fallback=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                fallback.addCategory(Intent.CATEGORY_OPENABLE);
                fallback.setType("image/*");
                startActivityForResult(fallback,PICK_REPLY_IMAGE);
            }catch(Exception e){toast("Phone Gallery open nahi hui");}
        }
    }

    private void saveReplyImagePermanently(Uri source){
        try{
            String mime=getContentResolver().getType(source);
            String ext="jpg";
            if(mime!=null&&mime.contains("png"))ext="png";
            else if(mime!=null&&mime.contains("webp"))ext="webp";
            else if(mime!=null&&mime.contains("gif"))ext="gif";
            File dir=new File(getFilesDir(),"reply_images");
            if(!dir.exists()&&!dir.mkdirs())throw new Exception("Folder create failed");
            File target=new File(dir,"auto_reply_"+System.currentTimeMillis()+"."+ext);
            try(InputStream in=getContentResolver().openInputStream(source);OutputStream out=new FileOutputStream(target)){
                if(in==null)throw new Exception("Image read failed");
                byte[] buf=new byte[16*1024];int n;
                while((n=in.read(buf))>0)out.write(buf,0,n);
            }
            Uri safe=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",target);
            SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
            p.edit().putString(AutoReplyNotificationService.IMAGE,safe.toString())
                    .putString(AutoReplyNotificationService.IMAGE+"_type",mime==null?"image/*":mime)
                    .putString(AutoReplyNotificationService.IMAGE+"_name",target.getName())
                    .putLong(AutoReplyNotificationService.IMAGE+"_updated",System.currentTimeMillis()).apply();
            toast("Gallery image saved ✓ Auto reply ke liye ready");
        }catch(Exception e){
            toast("Image save failed: phone Gallery se dobara select karein");
        }
    }

    private void saveBusinessUri(String key, Uri uri, String message){
        try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
        String type=getContentResolver().getType(uri);
        SharedPreferences bp=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        SharedPreferences.Editor ed=bp.edit().putString(key,uri.toString()).putString(key+"_type",type==null?"application/octet-stream":type).putString(key+"_name",uri.getLastPathSegment()==null?uri.toString():uri.getLastPathSegment()).putLong(key+"_updated",System.currentTimeMillis());
        String history=bp.getString("file_history",""); String line=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date())+" • "+message+" • "+(uri.getLastPathSegment()==null?"file":uri.getLastPathSegment()); ed.putString("file_history",line+(history.isEmpty()?"":"\n"+history)).apply();
        toast(message);
    }

    private void pickBusinessFile(int code){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/pdf","image/*"});startActivityForResult(i,code);
    }

    private void showBusinessFilesDialog(){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        Button ledger=button(p.getString(AutoReplyNotificationService.LEDGER_URI,"").isEmpty()?"Add Master Ledger":"Change Master Ledger ✓");
        Button catalog=button(p.getString(AutoReplyNotificationService.CATALOG_URI,"").isEmpty()?"Add Catalog":"Change Catalog ✓");
        Button price=button(p.getString(AutoReplyNotificationService.PRICE_URI,"").isEmpty()?"Add Price List":"Change Price List ✓");
        Button history=button("View updated files history");
        EditText ledgerKey=new EditText(this);ledgerKey.setHint("Ledger keyword");ledgerKey.setText(p.getString(AutoReplyNotificationService.LEDGER_KEY,"ledger"));
        EditText ledgerPhone=new EditText(this);ledgerPhone.setHint("Customer phone (ledger only)");ledgerPhone.setText(p.getString("ledger_phone",""));ledgerPhone.setInputType(3);
        EditText ledgerName=new EditText(this);ledgerName.setHint("Customer / WhatsApp name");ledgerName.setText(p.getString("ledger_name",""));
        EditText catalogKey=new EditText(this);catalogKey.setHint("Catalog keyword");catalogKey.setText(p.getString(AutoReplyNotificationService.CATALOG_KEY,"catalog"));
        EditText priceKey=new EditText(this);priceKey.setHint("Price keyword");priceKey.setText(p.getString(AutoReplyNotificationService.PRICE_KEY,"price"));
        TextView current=new TextView(this);current.setPadding(4,8,4,8);current.setText("Current ledger: "+p.getString(AutoReplyNotificationService.LEDGER_URI+"_name","Not selected"));
        box.addView(current);box.addView(ledger,new LinearLayout.LayoutParams(-1,dp(44)));box.addView(ledgerKey);box.addView(ledgerPhone);box.addView(ledgerName);
        box.addView(catalog,new LinearLayout.LayoutParams(-1,dp(44)));box.addView(catalogKey);box.addView(price,new LinearLayout.LayoutParams(-1,dp(44)));box.addView(priceKey);box.addView(history,new LinearLayout.LayoutParams(-1,dp(42)));
        ledger.setOnClickListener(v->pickBusinessFile(PICK_LEDGER_FILE));catalog.setOnClickListener(v->pickBusinessFile(PICK_CATALOG_FILE));price.setOnClickListener(v->pickBusinessFile(PICK_PRICE_FILE));
        history.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("File update history").setMessage(p.getString("file_history","No file updates yet")).setPositiveButton("Close",null).setNeutralButton("Clear",(d,w)->p.edit().remove("file_history").apply()).show());
        new AlertDialog.Builder(this).setTitle("Business Files & Ledger Mapping").setMessage("Ledger sirf matching customer phone/name ko reply hoga.").setView(box)
            .setPositiveButton("Save",(d,w)->{p.edit().putString(AutoReplyNotificationService.LEDGER_KEY,ledgerKey.getText().toString().trim()).putString("ledger_phone",normalize(ledgerPhone.getText().toString())).putString("ledger_name",ledgerName.getText().toString().trim()).putString(AutoReplyNotificationService.CATALOG_KEY,catalogKey.getText().toString().trim()).putString(AutoReplyNotificationService.PRICE_KEY,priceKey.getText().toString().trim()).apply();toast("Business settings saved");})
            .setNegativeButton("Close",null).show();
    }
    private void showAutoReplyDialog(){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText keyword=new EditText(this);keyword.setHint("Keyword");
        Spinner match=new Spinner(this);String[] modes={"Contains","Exact match","Starts with","Ends with"};match.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));
        EditText reply=new EditText(this);reply.setHint("Text / image caption");reply.setMinLines(2);
        CheckBox caseSensitive=new CheckBox(this);caseSensitive.setText("Case sensitive");
        EditText cool=new EditText(this);cool.setHint("Cooldown minutes");cool.setInputType(2);cool.setText(String.valueOf(p.getInt(AutoReplyNotificationService.COOLDOWN,5)));
        Button image=button("Choose image from phone albums"); Button rules=button("View saved keywords & images"); Button access=button("Open Notification Access");
        box.addView(keyword);box.addView(match);box.addView(reply);box.addView(caseSensitive);box.addView(cool);box.addView(image,new LinearLayout.LayoutParams(-1,dp(42)));box.addView(rules,new LinearLayout.LayoutParams(-1,dp(42)));box.addView(access,new LinearLayout.LayoutParams(-1,dp(42)));
        image.setOnClickListener(v->openPhoneGalleryFirst());
        rules.setOnClickListener(v->showRulesDialog());
        access.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}});
        new AlertDialog.Builder(this).setTitle("WhatsApp Auto Reply Rules").setMessage("Keyword match type choose karo, text/image save karo.").setView(box)
            .setPositiveButton("Save rule & turn ON",(d,w)->{String k=keyword.getText().toString().trim();if(k.isEmpty()){toast("Keyword required");return;}int c=5;try{c=Math.max(1,Integer.parseInt(cool.getText().toString()));}catch(Exception ignored){}try{JSONArray arr=new JSONArray(p.getString("rules","[]"));JSONObject o=new JSONObject();o.put("keyword",k);o.put("match",match.getSelectedItemPosition());o.put("case",caseSensitive.isChecked());o.put("reply",reply.getText().toString().trim());o.put("image",p.getString(AutoReplyNotificationService.IMAGE,""));o.put("type",p.getString(AutoReplyNotificationService.IMAGE+"_type","image/*"));arr.put(o);p.edit().putString("rules",arr.toString()).putInt(AutoReplyNotificationService.COOLDOWN,c).putBoolean(AutoReplyNotificationService.ENABLED,true).apply();toast("Rule saved • Auto reply ON");}catch(Exception e){toast("Rule save failed");}})
            .setNeutralButton("Turn OFF",(d,w)->{p.edit().putBoolean(AutoReplyNotificationService.ENABLED,false).apply();toast("Auto reply OFF");}).setNegativeButton("Close",null).show();
    }

    private void showRulesDialog(){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        StringBuilder out=new StringBuilder();
        try{
            JSONArray a=new JSONArray(p.getString("rules","[]"));
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);
                String[] m={"Contains","Exact","Starts","Ends"};
                int mi=o.optInt("match",0);
                out.append(i+1).append(". ")
                   .append(o.optString("keyword"))
                   .append(" • ")
                   .append(m[Math.max(0,Math.min(3,mi))])
                   .append("\n   Reply: ")
                   .append(o.optString("reply").isEmpty()?"(image only)":o.optString("reply"))
                   .append("\n   Image: ")
                   .append(o.optString("image").isEmpty()?"No":"Yes")
                   .append("\n\n");
            }
            if(a.length()==0)out.append("No saved rules");
        }catch(Exception e){out.append("No saved rules");}
        new AlertDialog.Builder(this)
            .setTitle("Saved keywords & reply images")
            .setMessage(out.toString())
            .setPositiveButton("Close",null)
            .setNeutralButton("Clear all",(d,w)->p.edit().remove("rules").apply())
            .show();
    }

    private void saveSelectedContacts(){
        JSONArray a=new JSONArray();for(String n:selectedNumbers)a.put(n);getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(SAVED_CONTACTS_KEY,a.toString()).apply();
    }
    private void showSavedContactsDialog(){
        StringBuilder b=new StringBuilder();
        try{
            JSONArray a=new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(SAVED_CONTACTS_KEY,"[]"));
            for(int i=0;i<a.length();i++){
                String n=a.getString(i);
                b.append(i+1).append(". ")
                 .append(findName(n))
                 .append(" • +")
                 .append(n)
                 .append("\n");
            }
            if(a.length()==0)b.append("No saved contacts yet");
        }catch(Exception e){b.append("No saved contacts yet");}
        new AlertDialog.Builder(this)
            .setTitle("Saved contacts list")
            .setMessage(b.toString())
            .setPositiveButton("Close",null)
            .setNeutralButton("Clear list",(d,w)->{
                selectedNumbers.clear();
                saveSelectedContacts();
                refreshChecks();
            })
            .show();
    }

    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private static class ContactItem{final String name,number;ContactItem(String n,String p){name=n;number=p;}@Override public String toString(){return name+"  •  +"+number;}}
}
