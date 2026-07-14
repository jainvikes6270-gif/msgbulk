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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.ScrollView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final int PICK_LEDGER_CUSTOMERS_XLSX = 111;
    private static final int PICK_MASTER_PDF_FOR_EXCEL = 112;
    private static final int CREATE_MASTER_LEDGER_XLSX = 113;
    private static final int PICK_BULK_IMAGE = 114;
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
    static final String AUTO_IMAGE = "bulk_image";
    static final String AUTO_IMAGE_TYPE = "bulk_image_type";
    private static final String DARK_KEY = "dark_mode";
    private static final String SAVED_CONTACTS_KEY = "saved_contacts";
    private static final String PIN_KEY = "login_pin";
    private static final String LOGIN_ENABLED_KEY = "login_enabled";
    private static final String MESSAGE_HEADER_KEY = "message_header";
    private static final String MESSAGE_FOOTER_KEY = "message_footer";
    private static final String MESSAGE_TEMPLATES_KEY = "message_templates";
    private static final String MESSAGE_DRAFT_KEY = "message_draft";
    private static final String BULK_IMAGE_URI_KEY = "selected_bulk_image";
    private static final String BULK_IMAGE_TYPE_KEY = "selected_bulk_image_type";
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private final List<ContactItem> allContacts = new ArrayList<>();
    private final List<ContactItem> visibleContacts = new ArrayList<>();
    private final Set<String> selectedNumbers = new LinkedHashSet<>();
    private final List<ContactItem> queue = new ArrayList<>();

    private ArrayAdapter<ContactItem> adapter;
    private ListView listView;
    private TextView statusText, pdfText, miniProgress;
    private EditText messageBox, searchBox;
    private ImageView bulkImagePreview;
    private LinearLayout bulkImageActions;
    private Button sendButton, accessibilityButton, editGroupButton;
    private Uri pdfUri;
    private Uri pendingMasterPdfUri;
    private Uri selectedBulkImageUri;
    private String selectedBulkImageType="image/*";
    private int queueIndex = 0;
    private String activeGroup = "";
    private boolean recipientListsVisible=false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        PDFBoxResourceLoader.init(getApplicationContext());
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
        title.setText("LATHA BULK v3.14 • SMART LEDGER");
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
        Button backup=button("Drive Backup");
        Button restore=button("Restore Backup");
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
        myGroups.setOnClickListener(v -> showRecipientListsScreen());
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

        Button ledgerExcel=button("MASTER LEDGER → PHONE + BALANCE EXCEL");
        ledgerExcel.setTypeface(Typeface.DEFAULT_BOLD);
        ledgerExcel.setTextSize(14);
        ledgerExcel.setTextColor(Color.WHITE);
        ledgerExcel.setBackgroundColor(Color.rgb(180,105,10));
        LinearLayout.LayoutParams xlp=new LinearLayout.LayoutParams(-1,dp(46));
        xlp.setMargins(dp(2),dp(2),dp(2),dp(3));
        root.addView(ledgerExcel,xlp);
        ledgerExcel.setOnClickListener(v->chooseMasterPdfForExcel());

        LinearLayout composeCard=new LinearLayout(this);
        composeCard.setOrientation(LinearLayout.VERTICAL);
        composeCard.setPadding(dp(5),dp(4),dp(5),dp(4));
        GradientDrawable messageBg=new GradientDrawable();
        messageBg.setColor(isDark()?Color.rgb(48,48,48):Color.WHITE);
        messageBg.setStroke(dp(1),Color.rgb(95,125,185));
        messageBg.setCornerRadius(dp(20));
        composeCard.setBackground(messageBg);

        bulkImagePreview=new ImageView(this);
        bulkImagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bulkImagePreview.setAdjustViewBounds(true);
        bulkImagePreview.setVisibility(View.GONE);
        composeCard.addView(bulkImagePreview,new LinearLayout.LayoutParams(-1,dp(145)));

        bulkImageActions=row();
        bulkImageActions.setGravity(Gravity.CENTER_VERTICAL);
        TextView imageLabel=new TextView(this);imageLabel.setText("Image selected • caption below");imageLabel.setTextSize(12);imageLabel.setPadding(dp(8),0,0,0);
        Button changeImage=button("Change");Button removeImage=button("Remove");
        bulkImageActions.addView(imageLabel,new LinearLayout.LayoutParams(0,dp(36),1f));
        bulkImageActions.addView(changeImage,new LinearLayout.LayoutParams(dp(76),dp(36)));
        bulkImageActions.addView(removeImage,new LinearLayout.LayoutParams(dp(76),dp(36)));
        bulkImageActions.setVisibility(View.GONE);
        composeCard.addView(bulkImageActions);

        LinearLayout messageShell=row();
        messageShell.setGravity(Gravity.CENTER_VERTICAL);
        Button emoji=button("🙂");
        emoji.setTextSize(20);
        emoji.setContentDescription("Add emoji");
        messageShell.addView(emoji,new LinearLayout.LayoutParams(dp(46),dp(54)));
        Button addImage=button("📷");
        addImage.setTextSize(19);
        addImage.setContentDescription("Add image");
        messageShell.addView(addImage,new LinearLayout.LayoutParams(dp(46),dp(54)));
        messageBox = new EditText(this);
        messageBox.setHint("Add caption or message • {Name}");
        messageBox.setMinLines(2);
        messageBox.setMaxLines(3);
        messageBox.setTextSize(14);
        messageBox.setGravity(Gravity.CENTER_VERTICAL);
        messageBox.setBackgroundColor(Color.TRANSPARENT);
        messageBox.setPadding(dp(4),0,dp(5),0);
        messageBox.setText(getSharedPreferences(PREFS,MODE_PRIVATE).getString(MESSAGE_DRAFT_KEY,""));
        messageShell.addView(messageBox,new LinearLayout.LayoutParams(0,dp(66),1f));
        composeCard.addView(messageShell,new LinearLayout.LayoutParams(-1,dp(66)));
        LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(-1,-2);
        mlp.setMargins(dp(1),dp(2),dp(1),dp(2));
        root.addView(composeCard,mlp);
        emoji.setOnClickListener(v->showEmojiPicker());
        addImage.setOnClickListener(v->openBulkImageGallery());
        changeImage.setOnClickListener(v->openBulkImageGallery());
        removeImage.setOnClickListener(v->removeBulkImage());
        messageBox.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(MESSAGE_DRAFT_KEY,s.toString()).apply();}public void afterTextChanged(Editable e){}});
        loadBulkImageSelection();

        LinearLayout messageTools=row();
        Button headerFooter=button("Header / Footer");
        Button templates=button("My Templates");
        Button saveTemplate=button("Save Template");
        messageTools.addView(headerFooter,weighted(1.1f,39));
        messageTools.addView(templates,weighted(1f,39));
        messageTools.addView(saveTemplate,weighted(1f,39));
        root.addView(messageTools);
        headerFooter.setOnClickListener(v->showHeaderFooterDialog());
        templates.setOnClickListener(v->showTemplatesDialog());
        saveTemplate.setOnClickListener(v->showSaveTemplateDialog());

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
    private void showRecipientListsScreen(){
        recipientListsVisible=true;
        FrameLayout page=new FrameLayout(this);page.setBackgroundColor(Color.BLACK);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        page.addView(body,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(10),0,dp(14),0);header.setBackgroundColor(Color.rgb(29,27,27));
        Button back=button("‹");back.setTextSize(36);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);
        TextView title=new TextView(this);title.setText("Recipient Lists");title.setTextSize(27);title.setTextColor(Color.WHITE);title.setTypeface(Typeface.DEFAULT_BOLD);title.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(back,new LinearLayout.LayoutParams(dp(62),dp(72)));header.addView(title,new LinearLayout.LayoutParams(0,dp(72),1f));body.addView(header);
        back.setOnClickListener(v->returnToMainScreen());

        ScrollView scroll=new ScrollView(this);LinearLayout cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);cards.setPadding(dp(16),dp(14),dp(16),dp(100));scroll.addView(cards,new ScrollView.LayoutParams(-1,-2));body.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        Map<String,List<String>> groups=readGroups();
        if(groups.isEmpty()){
            TextView empty=new TextView(this);empty.setText("No recipient lists yet\nTap + to create your first list");empty.setTextColor(Color.LTGRAY);empty.setTextSize(17);empty.setGravity(Gravity.CENTER);cards.addView(empty,new LinearLayout.LayoutParams(-1,dp(190)));
        }else for(Map.Entry<String,List<String>> entry:groups.entrySet()){
            String name=entry.getKey();int count=entry.getValue().size();
            LinearLayout card=row();card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(14),dp(9),dp(7),dp(9));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(42,42,42));bg.setCornerRadius(dp(14));card.setBackground(bg);
            LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);
            TextView groupName=new TextView(this);groupName.setText(name);groupName.setTextSize(25);groupName.setTextColor(Color.WHITE);groupName.setTypeface(Typeface.DEFAULT_BOLD);
            TextView groupCount=new TextView(this);groupCount.setText(count+" contacts   🟢");groupCount.setTextSize(17);groupCount.setTextColor(Color.rgb(175,175,175));
            info.addView(groupName,new LinearLayout.LayoutParams(-1,dp(43)));info.addView(groupCount,new LinearLayout.LayoutParams(-1,dp(34)));
            Button more=button("⋮");more.setTextSize(30);more.setTextColor(Color.rgb(20,115,220));more.setBackgroundColor(Color.TRANSPARENT);
            card.addView(info,new LinearLayout.LayoutParams(0,dp(84),1f));card.addView(more,new LinearLayout.LayoutParams(dp(52),dp(70)));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(106));cp.setMargins(0,dp(7),0,dp(7));cards.addView(card,cp);
            card.setOnClickListener(v->openRecipientList(name));more.setOnClickListener(v->showRecipientListMenu(more,name));
        }

        Button add=button("+");add.setTextSize(34);add.setTextColor(Color.WHITE);GradientDrawable fab=new GradientDrawable();fab.setColor(Color.rgb(20,105,205));fab.setShape(GradientDrawable.OVAL);add.setBackground(fab);
        FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(70),dp(70),Gravity.RIGHT|Gravity.BOTTOM);fp.setMargins(0,0,dp(26),dp(28));page.addView(add,fp);
        add.setOnClickListener(v->{returnToMainScreen();uiHandler.postDelayed(()->{if(selectedNumbers.isEmpty())toast("Contacts select karke Save group dabaye");else showSaveGroupDialog();},250);});
        setContentView(page);
    }
    private void showRecipientListMenu(View anchor,String name){
        PopupMenu menu=new PopupMenu(this,anchor);menu.getMenu().add("Open list");menu.getMenu().add("Edit contacts");menu.getMenu().add("Rename");menu.getMenu().add("Duplicate");menu.getMenu().add("Delete");
        menu.setOnMenuItemClickListener(item->{String a=item.getTitle().toString();if(a.equals("Open list"))openRecipientList(name);else if(a.equals("Edit contacts")){openGroup(name);returnToMainScreen();uiHandler.postDelayed(this::editActiveGroupContacts,250);}else if(a.equals("Rename"))renameGroup(name);else if(a.equals("Duplicate")){duplicateGroup(name);showRecipientListsScreen();}else confirmDeleteGroup(name);return true;});menu.show();
    }
    private void openRecipientList(String name){openGroup(name);returnToMainScreen();toast(name+" • "+selectedNumbers.size()+" recipients selected");}
    private void returnToMainScreen(){recipientListsVisible=false;setContentView(buildUi());uiHandler.postDelayed(this::refreshChecks,80);}
    @Override public void onBackPressed(){if(recipientListsVisible)returnToMainScreen();else super.onBackPressed();}
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
            Map<String,List<String>>g=readGroups();List<String>nums=g.remove(oldName);g.put(n,nums);writeGroups(g);if(activeGroup.equals(oldName))activeGroup=n;refreshChecks();toast("Renamed to "+n);if(recipientListsVisible)showRecipientListsScreen();
        }).setNegativeButton("Cancel",null).show();
    }
    private void duplicateGroup(String name){
        Map<String,List<String>>g=readGroups();String base=name+" Copy",n=base;int i=2;while(g.containsKey(n))n=base+" "+i++;
        g.put(n,new ArrayList<>(g.get(name)));writeGroups(g);toast("Created: "+n);
    }
    private void confirmDeleteGroup(String name){
        new AlertDialog.Builder(this).setTitle("Delete group?").setMessage(name+" will be removed. Phone contacts will not be deleted.")
                .setPositiveButton("Delete",(d,w)->{Map<String,List<String>>g=readGroups();g.remove(name);writeGroups(g);if(activeGroup.equals(name)){activeGroup="";selectedNumbers.clear();}refreshChecks();toast("Deleted: "+name);if(recipientListsVisible)showRecipientListsScreen();})
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
        String body=messageBox.getText().toString().trim();
        String message=buildFinalMessage();
        if(selectedNumbers.isEmpty()){toast("Select contacts or open group");return;}
        if(body.isEmpty()&&selectedBulkImageUri==null){toast("Type message or add an image first");return;}
        JSONArray nums=new JSONArray();
        JSONArray names=new JSONArray();
        for(String n:selectedNumbers){
            nums.put(n);
            names.put(findName(n));
        }
        SharedPreferences settings=getSharedPreferences(PREFS,MODE_PRIVATE);
        getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit()
                .putString(AUTO_NUMBERS,nums.toString()).putString(AUTO_NAMES,names.toString()).putString(AUTO_MESSAGE,message)
                .putString(AUTO_IMAGE,selectedBulkImageUri==null?"":selectedBulkImageUri.toString()).putString(AUTO_IMAGE_TYPE,selectedBulkImageType)
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
        if(requestCode==PICK_BULK_IMAGE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){saveBulkImagePermanently(data.getData());}
        if(requestCode==PICK_LEDGER_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){importMasterLedgerPdf(data.getData());}
        if(requestCode==PICK_CATALOG_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){saveBusinessUri(AutoReplyNotificationService.CATALOG_URI,data.getData(),"Catalog file saved");}
        if(requestCode==PICK_PRICE_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){saveBusinessUri(AutoReplyNotificationService.PRICE_URI,data.getData(),"Price List file saved");}
        if(requestCode==CREATE_BACKUP&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) writeBackup(data.getData());
        if(requestCode==PICK_RESTORE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) restoreBackup(data.getData());
        if(requestCode==PICK_LEDGER_CUSTOMERS_XLSX&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) importLedgerCustomersXlsx(data.getData());
        if(requestCode==PICK_MASTER_PDF_FOR_EXCEL&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){pendingMasterPdfUri=data.getData();createMasterLedgerExcelFile();}
        if(requestCode==CREATE_MASTER_LEDGER_XLSX&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&pendingMasterPdfUri!=null) convertMasterPdfToExcel(pendingMasterPdfUri,data.getData());
    }
    private void sharePdf(){if(pdfUri==null){toast("Choose PDF first");return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,pdfUri);i.putExtra(Intent.EXTRA_TEXT,buildFinalMessage());i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{i.setPackage("com.whatsapp");startActivity(i);}catch(Exception e){i.setPackage(null);startActivity(Intent.createChooser(i,"Share PDF"));}}

    private void createNotificationChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL_ID,"Sending progress",NotificationManager.IMPORTANCE_LOW);c.setDescription("Compact queue progress");c.setSound(null,null);getSystemService(NotificationManager.class).createNotificationChannel(c);}}
    private void showProgressNotification(int current,int total,String contact){Intent launch=new Intent(this,MainActivity.class);PendingIntent p=PendingIntent.getActivity(this,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(this,CHANNEL_ID).setSmallIcon(android.R.drawable.stat_sys_upload).setContentTitle(current>=total?"Latha Bulk completed":"Sending "+current+"/"+total).setContentText(contact).setOnlyAlertOnce(true).setOngoing(current<total).setPriority(NotificationCompat.PRIORITY_LOW).setProgress(total,Math.min(current,total),false).setContentIntent(p);
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID,b.build());}
    private void cancelProgressNotification(){((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);}

    private boolean isDark(){return getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean(DARK_KEY,false);}
    private String findName(String number){for(ContactItem c:allContacts)if(c.number.equals(number))return c.name;return number;}

    private String buildFinalMessage(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        String header=p.getString(MESSAGE_HEADER_KEY,"").trim();
        String body=messageBox==null?"":messageBox.getText().toString().trim();
        String footer=p.getString(MESSAGE_FOOTER_KEY,"").trim();
        StringBuilder out=new StringBuilder();
        if(!header.isEmpty())out.append(header);
        if(!body.isEmpty()){if(out.length()>0)out.append("\n");out.append(body);}
        if(!footer.isEmpty()){if(out.length()>0)out.append("\n");out.append(footer);}
        return out.toString();
    }

    private void showEmojiPicker(){
        String[] emojis={"😀","😊","🙏","👍","👌","🎉","❤️","✅","📌","📞","📄","💰","⚡","🔔","🌟","➡️","⬇️","🏷️","🛒","🤝"};
        new AlertDialog.Builder(this).setTitle("Choose emoji").setItems(emojis,(d,which)->{
            int start=Math.max(messageBox.getSelectionStart(),0);
            messageBox.getText().insert(start,emojis[which]);
            messageBox.requestFocus();
        }).setNegativeButton("Close",null).show();
    }

    private void showHeaderFooterDialog(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        EditText header=new EditText(this);header.setHint("Header • example: LATHA EPS");header.setText(p.getString(MESSAGE_HEADER_KEY,""));header.setMaxLines(2);
        EditText footer=new EditText(this);footer.setHint("Footer • example: Thank you");footer.setText(p.getString(MESSAGE_FOOTER_KEY,""));footer.setMaxLines(2);
        box.addView(header);box.addView(footer);
        new AlertDialog.Builder(this).setTitle("Message Header & Footer").setMessage("Ye har bulk message ke upar aur niche automatically add hoga.").setView(box)
            .setPositiveButton("Save",(d,w)->{p.edit().putString(MESSAGE_HEADER_KEY,header.getText().toString().trim()).putString(MESSAGE_FOOTER_KEY,footer.getText().toString().trim()).apply();toast("Header & footer saved");})
            .setNeutralButton("Clear",(d,w)->{p.edit().remove(MESSAGE_HEADER_KEY).remove(MESSAGE_FOOTER_KEY).apply();toast("Header & footer cleared");})
            .setNegativeButton("Cancel",null).show();
    }

    private JSONArray readTemplates(){try{return new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(MESSAGE_TEMPLATES_KEY,"[]"));}catch(Exception e){return new JSONArray();}}
    private void showSaveTemplateDialog(){
        String body=messageBox.getText().toString().trim();if(body.isEmpty()){toast("Type message before saving template");return;}
        EditText name=new EditText(this);name.setHint("Template name • example: Payment reminder");name.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Save Message Template").setView(name).setPositiveButton("Save",(d,w)->{
            String n=name.getText().toString().trim();if(n.isEmpty())n="Template "+(readTemplates().length()+1);
            try{SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);JSONArray a=readTemplates();JSONObject o=new JSONObject();o.put("name",n);o.put("body",body);o.put("header",p.getString(MESSAGE_HEADER_KEY,""));o.put("footer",p.getString(MESSAGE_FOOTER_KEY,""));a.put(o);p.edit().putString(MESSAGE_TEMPLATES_KEY,a.toString()).apply();toast("Template saved: "+n);}catch(Exception e){toast("Template save failed");}
        }).setNegativeButton("Cancel",null).show();
    }
    private void showTemplatesDialog(){
        JSONArray a=readTemplates();if(a.length()==0){toast("No saved templates");return;}
        String[] names=new String[a.length()];for(int i=0;i<a.length();i++)names[i]=a.optJSONObject(i)==null?"Template "+(i+1):a.optJSONObject(i).optString("name","Template "+(i+1));
        new AlertDialog.Builder(this).setTitle("My Message Templates").setItems(names,(d,which)->{
            JSONObject o=a.optJSONObject(which);if(o==null)return;SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);messageBox.setText(o.optString("body",""));messageBox.setSelection(messageBox.length());p.edit().putString(MESSAGE_HEADER_KEY,o.optString("header","")).putString(MESSAGE_FOOTER_KEY,o.optString("footer","")).apply();toast("Template loaded: "+names[which]);
        }).setNeutralButton("Delete all",(d,w)->{getSharedPreferences(PREFS,MODE_PRIVATE).edit().remove(MESSAGE_TEMPLATES_KEY).apply();toast("All templates deleted");}).setNegativeButton("Close",null).show();
    }

    private void showLoginSettings(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        String[] items={"Change PIN","Turn login "+(p.getBoolean(LOGIN_ENABLED_KEY,true)?"OFF":"ON"),"Test login now","Reset PIN"};
        new AlertDialog.Builder(this).setTitle("Login settings").setItems(items,(d,which)->{
            if(which==0){EditText in=new EditText(this);in.setHint("New 4-digit PIN");in.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);new AlertDialog.Builder(this).setTitle("Change PIN").setView(in).setPositiveButton("Save",(a,b)->{String x=in.getText().toString().trim();if(x.length()==4){p.edit().putString(PIN_KEY,x).putBoolean(LOGIN_ENABLED_KEY,true).apply();toast("PIN changed");}else toast("PIN must be 4 digits");}).setNegativeButton("Cancel",null).show();}
            else if(which==1){boolean n=!p.getBoolean(LOGIN_ENABLED_KEY,true);p.edit().putBoolean(LOGIN_ENABLED_KEY,n).apply();toast("Login "+(n?"ON":"OFF"));}
            else if(which==2){String pin=p.getString(PIN_KEY,"");if(pin.isEmpty())showCreatePinDialog();else showUnlockDialog(pin);}
            else {p.edit().remove(PIN_KEY).putBoolean(LOGIN_ENABLED_KEY,true).apply();toast("PIN reset • create new PIN");showCreatePinDialog();}
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
        try{JSONObject root=new JSONObject();root.put("app","LathaBulk");root.put("version","3.14.0");root.put("created",System.currentTimeMillis());root.put(PREFS,prefsToJson(PREFS));root.put(AUTO_PREFS,prefsToJson(AUTO_PREFS));root.put(AutoReplyNotificationService.PREFS,prefsToJson(AutoReplyNotificationService.PREFS));JSONObject files=new JSONObject();addFilesToBackup(getFilesDir(),"",files);root.put("files",files);try(OutputStream out=getContentResolver().openOutputStream(uri)){out.write(root.toString(2).getBytes(StandardCharsets.UTF_8));}toast("Backup saved • images, keywords & templates included");}catch(Exception e){toast("Backup failed: "+e.getMessage());}
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

    private void openBulkImageGallery(){
        try{
            Intent i=Build.VERSION.SDK_INT>=33?new Intent(MediaStore.ACTION_PICK_IMAGES):new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");if(Build.VERSION.SDK_INT<33)i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(i,"Choose image for WhatsApp message"),PICK_BULK_IMAGE);
        }catch(Exception first){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");startActivityForResult(i,PICK_BULK_IMAGE);}
    }
    private void saveBulkImagePermanently(Uri source){
        try{
            String mime=getContentResolver().getType(source);String ext=(mime!=null&&mime.contains("png"))?"png":(mime!=null&&mime.contains("webp"))?"webp":(mime!=null&&mime.contains("gif"))?"gif":"jpg";
            File dir=new File(getFilesDir(),"bulk_images");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Folder create failed");
            File target=new File(dir,"selected_"+System.currentTimeMillis()+"."+ext);
            try(InputStream in=getContentResolver().openInputStream(source);OutputStream out=new FileOutputStream(target)){if(in==null)throw new Exception("Image read failed");byte[] buf=new byte[16*1024];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}
            selectedBulkImageUri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",target);selectedBulkImageType=mime==null?"image/*":mime;
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(BULK_IMAGE_URI_KEY,selectedBulkImageUri.toString()).putString(BULK_IMAGE_TYPE_KEY,selectedBulkImageType).apply();
            showBulkImagePreview();toast("Image added • caption niche type karein");
        }catch(Exception e){toast("Image add failed • Gallery se dobara select karein");}
    }
    private void loadBulkImageSelection(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);String raw=p.getString(BULK_IMAGE_URI_KEY,"");selectedBulkImageType=p.getString(BULK_IMAGE_TYPE_KEY,"image/*");selectedBulkImageUri=raw.isEmpty()?null:Uri.parse(raw);showBulkImagePreview();
    }
    private void showBulkImagePreview(){
        if(bulkImagePreview==null||bulkImageActions==null)return;boolean has=selectedBulkImageUri!=null;bulkImagePreview.setVisibility(has?View.VISIBLE:View.GONE);bulkImageActions.setVisibility(has?View.VISIBLE:View.GONE);if(has)bulkImagePreview.setImageURI(selectedBulkImageUri);
    }
    private void removeBulkImage(){
        selectedBulkImageUri=null;selectedBulkImageType="image/*";getSharedPreferences(PREFS,MODE_PRIVATE).edit().remove(BULK_IMAGE_URI_KEY).remove(BULK_IMAGE_TYPE_KEY).apply();showBulkImagePreview();toast("Image removed");
    }

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

    private void saveBusinessUri(String key, Uri source, String message){
        try{
            String type=getContentResolver().getType(source);
            String ext=(type!=null&&type.contains("pdf"))?"pdf":(type!=null&&type.contains("png"))?"png":(type!=null&&type.contains("webp"))?"webp":"jpg";
            File dir=new File(getFilesDir(),"business_files");
            if(!dir.exists()&&!dir.mkdirs())throw new Exception("Folder create failed");
            String base=key.replace("_uri","");
            File target=new File(dir,base+"_"+System.currentTimeMillis()+"."+ext);
            try(InputStream in=getContentResolver().openInputStream(source);OutputStream out=new FileOutputStream(target)){
                if(in==null)throw new Exception("File read failed");
                byte[] buf=new byte[16*1024];int n;while((n=in.read(buf))>0)out.write(buf,0,n);
            }
            Uri safe=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",target);
            SharedPreferences bp=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
            String display=source.getLastPathSegment()==null?target.getName():source.getLastPathSegment();
            SharedPreferences.Editor ed=bp.edit().putString(key,safe.toString()).putString(key+"_type",type==null?"application/octet-stream":type).putString(key+"_name",display).putLong(key+"_updated",System.currentTimeMillis());
            String history=bp.getString("file_history",""); String line=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date())+" • "+message+" • "+display; ed.putString("file_history",line+(history.isEmpty()?"":"\n"+history)).apply();
            toast(message+" ✓");
        }catch(Exception e){toast(message+" failed • file dobara select karein");}
    }

    private void pickBusinessFile(int code){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/pdf","image/*"});startActivityForResult(i,code);
    }

    private void showBusinessFilesDialog(){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        Button ledger=button(p.getString(AutoReplyNotificationService.LEDGER_URI,"").isEmpty()?"UPLOAD & PREPARE MASTER LEDGER PDF":"UPDATE & PREPARE MASTER LEDGER PDF ✓");
        Button customers=button("Manage Ledger Customers ("+ledgerCustomerCount()+")");
        Button convertPdf=button("MASTER PDF → PHONE + BALANCE EXCEL");
        Button importCsv=button("IMPORT CUSTOMER EXCEL (OPTIONAL)");
        Button catalog=button(p.getString(AutoReplyNotificationService.CATALOG_URI,"").isEmpty()?"Add Catalog":"Change Catalog ✓");
        Button price=button(p.getString(AutoReplyNotificationService.PRICE_URI,"").isEmpty()?"Add Price List":"Change Price List ✓");
        Button history=button("View updated files history");
        EditText ledgerKey=new EditText(this);ledgerKey.setHint("Ledger keyword");ledgerKey.setText(p.getString(AutoReplyNotificationService.LEDGER_KEY,"ledger"));
        EditText catalogKey=new EditText(this);catalogKey.setHint("Catalog keyword");catalogKey.setText(p.getString(AutoReplyNotificationService.CATALOG_KEY,"catalog"));
        EditText priceKey=new EditText(this);priceKey.setHint("Price keyword");priceKey.setText(p.getString(AutoReplyNotificationService.PRICE_KEY,"price"));
        TextView current=new TextView(this);current.setPadding(4,8,4,8);current.setText("Current ledger: "+p.getString(AutoReplyNotificationService.LEDGER_URI+"_name","Not selected")+"\nCustomers: "+ledgerCustomerCount());
        box.addView(current);box.addView(convertPdf,new LinearLayout.LayoutParams(-1,dp(46)));box.addView(ledger,new LinearLayout.LayoutParams(-1,dp(44)));box.addView(ledgerKey);box.addView(customers,new LinearLayout.LayoutParams(-1,dp(42)));box.addView(importCsv,new LinearLayout.LayoutParams(-1,dp(42)));
        box.addView(catalog,new LinearLayout.LayoutParams(-1,dp(44)));box.addView(catalogKey);box.addView(price,new LinearLayout.LayoutParams(-1,dp(44)));box.addView(priceKey);box.addView(history,new LinearLayout.LayoutParams(-1,dp(42)));
        ledger.setOnClickListener(v->pickBusinessFile(PICK_LEDGER_FILE));catalog.setOnClickListener(v->pickBusinessFile(PICK_CATALOG_FILE));price.setOnClickListener(v->pickBusinessFile(PICK_PRICE_FILE));
        customers.setOnClickListener(v->showLedgerCustomersDialog());
        convertPdf.setOnClickListener(v->chooseMasterPdfForExcel());
        importCsv.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");startActivityForResult(Intent.createChooser(i,"Select optional customer Excel"),PICK_LEDGER_CUSTOMERS_XLSX);});
        history.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("File update history").setMessage(p.getString("file_history","No file updates yet")).setPositiveButton("Close",null).setNeutralButton("Clear",(d,w)->p.edit().remove("file_history").apply()).show());
        ScrollView scroll=new ScrollView(this);scroll.addView(box);
        new AlertDialog.Builder(this).setTitle("Smart Master Ledger").setMessage("Master PDF upload karte hi phone-number wale ledgers alag prepare honge. Customer WhatsApp se 'ledger' bheje to exact number ka PDF jayega. Bina number wale skip honge.").setView(scroll)
            .setPositiveButton("Save",(d,w)->{p.edit().putString(AutoReplyNotificationService.LEDGER_KEY,ledgerKey.getText().toString().trim()).putString(AutoReplyNotificationService.CATALOG_KEY,catalogKey.getText().toString().trim()).putString(AutoReplyNotificationService.PRICE_KEY,priceKey.getText().toString().trim()).apply();toast("Business settings saved");})
            .setNegativeButton("Close",null).show();
    }

    private void chooseMasterPdfForExcel(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");
        startActivityForResult(Intent.createChooser(i,"Select Tally Master Ledger PDF"),PICK_MASTER_PDF_FOR_EXCEL);
    }

    private void createMasterLedgerExcelFile(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        i.putExtra(Intent.EXTRA_TITLE,"Latha_Master_Ledger_"+new java.text.SimpleDateFormat("yyyyMMdd",Locale.US).format(new java.util.Date())+".xlsx");
        startActivityForResult(i,CREATE_MASTER_LEDGER_XLSX);
    }

    private void convertMasterPdfToExcel(Uri pdf,Uri output){
        toast("Phone number aur closing balance nikal rahe hain…");
        if(miniProgress!=null)miniProgress.setText("Master Ledger processing…");
        new Thread(()->{
            try(InputStream in=getContentResolver().openInputStream(pdf);PDDocument document=PDDocument.load(in)){
                MasterLedgerResult result=prepareCustomerLedgers(document);
                List<List<String>> rows=new ArrayList<>();
                rows.add(java.util.Arrays.asList("Phone Number","Closing Balance"));
                for(LedgerPageData item:result.entries)rows.add(java.util.Arrays.asList(last10Digits(item.phone),item.balance));
                try(OutputStream out=getContentResolver().openOutputStream(output)){if(out==null)throw new Exception("Output unavailable");writeSimpleXlsx(out,rows);}
                runOnUiThread(()->{if(miniProgress!=null)miniProgress.setText("Ready • "+result.entries.size()+" ledgers • "+result.uniquePhones+" numbers");toast("Excel ready ✓ Bina number: "+result.skippedPages+" skipped");});
            }catch(Exception e){runOnUiThread(()->{if(miniProgress!=null)miniProgress.setText("Master Ledger failed");toast("Failed • Tally text-based PDF select karein");});}
        }).start();
    }

    private void importMasterLedgerPdf(Uri source){
        toast("Master Ledger save aur prepare ho raha hai…");
        if(miniProgress!=null)miniProgress.setText("Master Ledger processing…");
        new Thread(()->{
            try{
                File dir=new File(getFilesDir(),"business_files");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Folder create failed");
                File target=new File(dir,"master_ledger_current.pdf");
                try(InputStream in=getContentResolver().openInputStream(source);OutputStream out=new FileOutputStream(target)){
                    if(in==null)throw new Exception("PDF read failed");byte[] buf=new byte[32*1024];int n;while((n=in.read(buf))>0)out.write(buf,0,n);
                }
                MasterLedgerResult result;
                try(PDDocument document=PDDocument.load(target)){result=prepareCustomerLedgers(document);}
                Uri safe=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",target);
                SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
                String display=source.getLastPathSegment()==null?"Master Ledger PDF":source.getLastPathSegment();
                String history=p.getString("file_history","");String line=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date())+" • Master Ledger prepared • "+display;
                p.edit().putString(AutoReplyNotificationService.LEDGER_URI,safe.toString()).putString(AutoReplyNotificationService.LEDGER_URI+"_type","application/pdf").putString(AutoReplyNotificationService.LEDGER_URI+"_name",display).putLong(AutoReplyNotificationService.LEDGER_URI+"_updated",System.currentTimeMillis()).putString("file_history",line+(history.isEmpty()?"":"\n"+history)).putBoolean(AutoReplyNotificationService.ENABLED,true).apply();
                runOnUiThread(()->{if(miniProgress!=null)miniProgress.setText("Ledger ready • "+result.uniquePhones+" numbers");toast(result.entries.size()+" ledgers ready • "+result.skippedPages+" without number skipped");});
            }catch(Exception e){runOnUiThread(()->{if(miniProgress!=null)miniProgress.setText("Master Ledger failed");toast("Master Ledger prepare failed • valid Tally PDF select karein");});}
        }).start();
    }

    private MasterLedgerResult prepareCustomerLedgers(PDDocument document) throws Exception{
        MasterLedgerResult result=new MasterLedgerResult();result.totalPages=document.getNumberOfPages();
        PDFTextStripper stripper=new PDFTextStripper();stripper.setSortByPosition(true);
        LinkedHashMap<String,List<LedgerPageData>> byPhone=new LinkedHashMap<>();
        for(int page=1;page<=result.totalPages;page++){
            stripper.setStartPage(page);stripper.setEndPage(page);String text=stripper.getText(document);
            String phone=extractLedgerPhone(text);if(phone.isEmpty()){result.skippedPages++;continue;}
            LedgerPageData item=new LedgerPageData(phone,extractLedgerName(text),extractClosingBalance(text),page-1);
            result.entries.add(item);byPhone.computeIfAbsent(phone,k->new ArrayList<>()).add(item);
        }
        File dir=new File(getFilesDir(),"business_files/customer_ledgers");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Ledger folder create failed");
        File[] old=dir.listFiles();if(old!=null)for(File f:old)if(f.isFile())f.delete();
        JSONArray customers=new JSONArray();
        for(Map.Entry<String,List<LedgerPageData>> group:byPhone.entrySet()){
            String phone=group.getKey();File file=new File(dir,last10Digits(phone)+".pdf");
            try(PDDocument customerPdf=new PDDocument()){
                for(LedgerPageData item:group.getValue())customerPdf.importPage(document.getPage(item.pageIndex));
                customerPdf.save(file);
            }
            Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
            LedgerPageData first=group.getValue().get(0);JSONObject o=new JSONObject();o.put("phone",phone);o.put("name",first.name);o.put("balance",first.balance);o.put("ledger_uri",uri.toString());o.put("ledger_file",file.getName());o.put("pages",group.getValue().size());customers.put(o);
        }
        result.uniquePhones=byPhone.size();
        getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).edit().putString(AutoReplyNotificationService.LEDGER_CUSTOMERS,customers.toString()).putInt("ledger_source_pages",result.totalPages).putInt("ledger_skipped_pages",result.skippedPages).apply();
        return result;
    }

    private String extractLedgerPhone(String text){
        for(String raw:text.split("\\r?\\n")){
            String low=raw.toLowerCase(Locale.ROOT);if(!low.contains("tel")||!low.contains("no"))continue;
            String digits=raw.replaceAll("[^0-9]","");if(digits.length()>=10){String last=digits.substring(digits.length()-10);if(last.matches("[6-9][0-9]{9}"))return "91"+last;}
        }
        return "";
    }

    private String extractLedgerName(String text){
        for(String raw:text.split("\\r?\\n")){String line=raw.trim();if(line.isEmpty())continue;if(line.toLowerCase(Locale.ROOT).contains("ledger account"))break;return line;}
        return "Ledger Customer";
    }

    private String extractClosingBalance(String text){
        Pattern money=Pattern.compile("[0-9][0-9,]*(?:\\.[0-9]{1,2})?");
        for(String raw:text.split("\\r?\\n"))if(raw.toLowerCase(Locale.ROOT).contains("closing balance")){
            Matcher m=money.matcher(raw);String value="";while(m.find())value=m.group();return value;
        }
        return "";
    }

    private String last10Digits(String value){String d=value==null?"":value.replaceAll("[^0-9]","");return d.length()>10?d.substring(d.length()-10):d;}

    private static class LedgerPageData{
        final String phone,name,balance;final int pageIndex;
        LedgerPageData(String phone,String name,String balance,int pageIndex){this.phone=phone;this.name=name;this.balance=balance;this.pageIndex=pageIndex;}
    }
    private static class MasterLedgerResult{
        final List<LedgerPageData> entries=new ArrayList<>();int totalPages,skippedPages,uniquePhones;
    }

    private void writeSimpleXlsx(OutputStream output,List<List<String>> rows) throws Exception{
        java.util.zip.ZipOutputStream zip=new java.util.zip.ZipOutputStream(output);
        putZipText(zip,"[Content_Types].xml","<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
        putZipText(zip,"_rels/.rels","<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
        putZipText(zip,"xl/workbook.xml","<?xml version=\"1.0\" encoding=\"UTF-8\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Master Ledger\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
        putZipText(zip,"xl/_rels/workbook.xml.rels","<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");
        StringBuilder sheet=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        for(int r=0;r<rows.size();r++){sheet.append("<row r=\"").append(r+1).append("\">");List<String> row=rows.get(r);for(int c=0;c<row.size();c++){String ref=excelColumn(c)+(r+1);sheet.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(xmlEscape(row.get(c))).append("</t></is></c>");}sheet.append("</row>");}
        sheet.append("</sheetData></worksheet>");putZipText(zip,"xl/worksheets/sheet1.xml",sheet.toString());zip.finish();zip.close();
    }

    private void putZipText(java.util.zip.ZipOutputStream zip,String name,String value) throws Exception{zip.putNextEntry(new java.util.zip.ZipEntry(name));zip.write(value.getBytes(StandardCharsets.UTF_8));zip.closeEntry();}
    private String xmlEscape(String s){return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;");}
    private String excelColumn(int index){StringBuilder s=new StringBuilder();for(int n=index+1;n>0;n=(n-1)/26)s.insert(0,(char)('A'+(n-1)%26));return s.toString();}

    private int ledgerCustomerCount(){try{return new JSONArray(getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]")).length();}catch(Exception e){return 0;}}

    private void importLedgerCustomersXlsx(Uri uri){
        int added=0,duplicates=0,invalid=0;
        try{
            SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
            JSONArray old=new JSONArray(p.getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));
            LinkedHashMap<String,JSONObject> map=new LinkedHashMap<>();
            for(int i=0;i<old.length();i++){
                JSONObject o=old.optJSONObject(i);if(o==null)continue;
                String ph=normalize(o.optString("phone",""));if(!ph.isEmpty())map.put(ph,o);
            }
            List<List<String>> rows=readFirstXlsxSheet(uri);
            boolean first=true;
            for(List<String> cols:rows){
                if(cols.isEmpty())continue;
                String rawPhone=cols.size()>0?cols.get(0):"";
                String ph=normalize(rawPhone);
                String name=cols.size()>1?cols.get(1).trim():"";
                String ledgerFile=cols.size()>2?cols.get(2).trim():"";
                if(first&&(rawPhone.toLowerCase(Locale.ROOT).contains("phone")||ph.length()<10)){first=false;continue;}
                first=false;
                if(ph.length()<10){invalid++;continue;}
                JSONObject existing=map.get(ph);if(existing!=null)duplicates++;else added++;
                JSONObject o=existing==null?new JSONObject():new JSONObject(existing.toString());o.put("phone",ph);if(!name.isEmpty())o.put("name",name);if(!ledgerFile.isEmpty())o.put("ledger_file",ledgerFile);map.put(ph,o);
            }
            JSONArray out=new JSONArray();for(JSONObject o:map.values())out.put(o);
            p.edit().putString(AutoReplyNotificationService.LEDGER_CUSTOMERS,out.toString()).apply();
            toast("Excel imported: "+added+" • duplicate updated: "+duplicates+" • invalid: "+invalid);
        }catch(Exception e){toast("Excel import failed • only .xlsx use karein");}
    }

    private List<List<String>> readFirstXlsxSheet(Uri uri) throws Exception{
        Map<String,byte[]> files=new LinkedHashMap<>();
        try(InputStream input=getContentResolver().openInputStream(uri);java.util.zip.ZipInputStream zin=new java.util.zip.ZipInputStream(input)){
            java.util.zip.ZipEntry entry;
            while((entry=zin.getNextEntry())!=null){
                if(entry.isDirectory())continue;
                ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;
                while((n=zin.read(b))>0)out.write(b,0,n);files.put(entry.getName(),out.toByteArray());
            }
        }
        ArrayList<String> shared=new ArrayList<>();
        byte[] ss=files.get("xl/sharedStrings.xml");
        if(ss!=null){
            org.w3c.dom.Document d=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new java.io.ByteArrayInputStream(ss));
            org.w3c.dom.NodeList sis=d.getElementsByTagName("si");
            for(int i=0;i<sis.getLength();i++){
                org.w3c.dom.NodeList ts=((org.w3c.dom.Element)sis.item(i)).getElementsByTagName("t");StringBuilder v=new StringBuilder();
                for(int j=0;j<ts.getLength();j++)v.append(ts.item(j).getTextContent());shared.add(v.toString());
            }
        }
        byte[] sheet=files.get("xl/worksheets/sheet1.xml");
        if(sheet==null)throw new Exception("First sheet missing");
        org.w3c.dom.Document d=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new java.io.ByteArrayInputStream(sheet));
        org.w3c.dom.NodeList rowNodes=d.getElementsByTagName("row");ArrayList<List<String>> rows=new ArrayList<>();
        for(int i=0;i<rowNodes.getLength();i++){
            org.w3c.dom.Element row=(org.w3c.dom.Element)rowNodes.item(i);org.w3c.dom.NodeList cells=row.getElementsByTagName("c");ArrayList<String> vals=new ArrayList<>();
            for(int j=0;j<cells.getLength();j++){
                org.w3c.dom.Element c=(org.w3c.dom.Element)cells.item(j);String ref=c.getAttribute("r");int col=0;
                for(int k=0;k<ref.length()&&Character.isLetter(ref.charAt(k));k++)col=col*26+(Character.toUpperCase(ref.charAt(k))-'A'+1);col=Math.max(0,col-1);
                while(vals.size()<=col)vals.add("");String type=c.getAttribute("t");String value="";
                org.w3c.dom.NodeList vs=c.getElementsByTagName("v");
                if("inlineStr".equals(type)){org.w3c.dom.NodeList ts=c.getElementsByTagName("t");if(ts.getLength()>0)value=ts.item(0).getTextContent();}
                else if(vs.getLength()>0){value=vs.item(0).getTextContent();if("s".equals(type)){try{value=shared.get(Integer.parseInt(value));}catch(Exception ignored){}}}
                if(value.endsWith(".0"))value=value.substring(0,value.length()-2);vals.set(col,value.trim());
            }
            rows.add(vals);
        }
        return rows;
    }

    private void showLedgerCustomersDialog(){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(12),0,dp(12),0);
        EditText search=new EditText(this);search.setHint("Search phone or customer name");
        ListView list=new ListView(this);box.addView(search);box.addView(list,new LinearLayout.LayoutParams(-1,dp(360)));
        final ArrayList<String> rows=new ArrayList<>();final ArrayList<JSONObject> objects=new ArrayList<>();
        Runnable refresh=()->{rows.clear();objects.clear();try{JSONArray a=new JSONArray(p.getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;String ph=o.optString("phone","");String nm=o.optString("name","");String lf=o.optString("ledger_file","");String row=(nm.isEmpty()?ph:nm+"\n"+ph)+(lf.isEmpty()?"":"\nLedger: "+lf);if(q.isEmpty()||row.toLowerCase(Locale.ROOT).contains(q)){rows.add(row);objects.add(o);}}}catch(Exception ignored){}list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,rows));};
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh.run();}public void afterTextChanged(Editable e){}});
        list.setOnItemClickListener((a,v,pos,id)->showEditLedgerCustomer(objects.get(pos),refresh));refresh.run();
        new AlertDialog.Builder(this).setTitle("Ledger Customers • "+ledgerCustomerCount()).setView(box).setPositiveButton("Add customer",(d,w)->showEditLedgerCustomer(null,()->showLedgerCustomersDialog())).setNeutralButton("Clear all",(d,w)->p.edit().remove(AutoReplyNotificationService.LEDGER_CUSTOMERS).apply()).setNegativeButton("Close",null).show();
    }

    private void showEditLedgerCustomer(JSONObject existing,Runnable after){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText phone=new EditText(this);phone.setHint("10-digit phone number");phone.setInputType(InputType.TYPE_CLASS_PHONE);EditText name=new EditText(this);name.setHint("Customer / WhatsApp name");
        String oldPhone="";if(existing!=null){oldPhone=normalize(existing.optString("phone",""));phone.setText(oldPhone);name.setText(existing.optString("name",""));}final String original=oldPhone;box.addView(phone);box.addView(name);
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle(existing==null?"Add customer":"Edit customer").setView(box).setPositiveButton("Save",(d,w)->{String ph=normalize(phone.getText().toString());if(ph.length()<10){toast("Valid phone number required");return;}try{JSONArray a=new JSONArray(p.getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));JSONArray out=new JSONArray();for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&!normalize(o.optString("phone","")).equals(original)&&!normalize(o.optString("phone","")).equals(ph))out.put(o);}JSONObject n=new JSONObject();n.put("phone",ph);n.put("name",name.getText().toString().trim());out.put(n);p.edit().putString(AutoReplyNotificationService.LEDGER_CUSTOMERS,out.toString()).apply();toast("Customer saved");after.run();}catch(Exception e){toast("Save failed");}}).setNegativeButton("Cancel",null);
        if(existing!=null)b.setNeutralButton("Delete",(d,w)->{try{JSONArray a=new JSONArray(p.getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));JSONArray out=new JSONArray();for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&!normalize(o.optString("phone","")).equals(original))out.put(o);}p.edit().putString(AutoReplyNotificationService.LEDGER_CUSTOMERS,out.toString()).apply();toast("Customer deleted");after.run();}catch(Exception ignored){}});b.show();
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
        JSONArray a=new JSONArray();JSONArray detailed=new JSONArray();
        try{for(String n:selectedNumbers){a.put(n);JSONObject o=new JSONObject();o.put("number",n);o.put("name",findName(n));detailed.put(o);}}catch(Exception ignored){}
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(SAVED_CONTACTS_KEY,a.toString()).putString("saved_contacts_detailed",detailed.toString()).apply();
    }
    private void showSavedContactsDialog(){
        SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        List<String> nums=new ArrayList<>();StringBuilder b=new StringBuilder();
        try{
            JSONArray detailed=new JSONArray(prefs.getString("saved_contacts_detailed","[]"));
            if(detailed.length()>0){for(int i=0;i<detailed.length();i++){JSONObject o=detailed.getJSONObject(i);String n=o.optString("number");nums.add(n);b.append(i+1).append(". ").append(o.optString("name",findName(n))).append(" • +").append(n).append("\n");}}
            else{JSONArray a=new JSONArray(prefs.getString(SAVED_CONTACTS_KEY,"[]"));for(int i=0;i<a.length();i++){String n=a.getString(i);nums.add(n);b.append(i+1).append(". ").append(findName(n)).append(" • +").append(n).append("\n");}}
            if(nums.isEmpty())b.append("No saved contacts yet");
        }catch(Exception e){b.append("No saved contacts yet");}
        new AlertDialog.Builder(this).setTitle("Saved contacts list • "+nums.size()).setMessage(b.toString())
            .setPositiveButton("Load list",(d,w)->{selectedNumbers.clear();selectedNumbers.addAll(nums);refreshChecks();toast(nums.size()+" saved contacts loaded");})
            .setNeutralButton("Save as group",(d,w)->{if(nums.isEmpty()){toast("Saved list empty");return;}selectedNumbers.clear();selectedNumbers.addAll(nums);showSaveGroupDialog();})
            .setNegativeButton("Close",null).show();
    }

    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private static class ContactItem{final String name,number;ContactItem(String n,String p){name=n;number=p;}@Override public String toString(){return name+"  •  +"+number;}}
}
