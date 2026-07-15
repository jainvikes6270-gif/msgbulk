package com.lathaeps.lathabulk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.PopupMenu;

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
    private static final int CREATE_BACKUP = 109;
    private static final int PICK_RESTORE = 110;
    private static final int PICK_LEDGER_CUSTOMERS_XLSX = 111;
    private static final int PICK_MASTER_PDF_FOR_EXCEL = 112;
    private static final int CREATE_MASTER_LEDGER_XLSX = 113;
    private static final int PICK_DIRECT_IMAGE = 114;
    private static final int PICK_BROADCAST_FILE = 115;
    static final String CHANNEL_ID = "latha_bulk_progress";
    static final int NOTIFICATION_ID = 511;
    static final String PREFS = "latha_bulk_prefs";
    static final String GROUPS_KEY = "saved_groups";
    static final String CATALOG_ITEMS_KEY = "catalog_items";
    private static final String PENDING_CATALOG_NAME_KEY="pending_catalog_name";
    private static final String PENDING_CATALOG_CATEGORY_KEY="pending_catalog_category";
    private static final String PENDING_CATALOG_KEYWORDS_KEY="pending_catalog_keywords";
    static final String AUTO_PREFS = "latha_auto_send";
    static final String AUTO_NUMBERS = "numbers";
    static final String AUTO_MESSAGE = "message";
    static final String AUTO_MESSAGES = "messages_by_recipient";
    static final String AUTO_INDEX = "index";
    static final String AUTO_RUNNING = "running";
    static final String AUTO_NAMES = "names";
    static final String AUTO_MIN_DELAY = "min_delay";
    static final String AUTO_MAX_DELAY = "max_delay";
    static final String AUTO_HISTORY = "history";
    static final String AUTO_FAILED = "failed";
    static final String AUTO_IMAGE_URI = "send_image_uri";
    static final String AUTO_IMAGE_TYPE = "send_image_type";
    static final String AUTO_QUEUE_TOKEN = "queue_token";
    static final String BROADCAST_RUNNING = "whatsapp_broadcast_running";
    static final String BROADCAST_LIST_NAME = "whatsapp_broadcast_list_name";
    static final String BROADCAST_MESSAGE = "whatsapp_broadcast_message";
    static final String BROADCAST_FILE_URI = "whatsapp_broadcast_file_uri";
    static final String BROADCAST_FILE_TYPE = "whatsapp_broadcast_file_type";
    static final String BROADCAST_MODE = "whatsapp_broadcast_mode";
    static final String BROADCAST_STAGE = "whatsapp_broadcast_stage";
    private static final String SCHEDULE_AT_KEY = "schedule_start_at";
    private static final String DARK_KEY = "dark_mode";
    private static final String SAVED_CONTACTS_KEY = "saved_contacts";
    private static final String PIN_KEY = "login_pin";
    private static final String RECOVERY_KEY = "pin_recovery_word";
    private static final String LOGIN_ENABLED_KEY = "login_enabled";
    private static final String MESSAGE_HEADER_KEY = "message_header";
    private static final String MESSAGE_FOOTER_KEY = "message_footer";
    private static final String MESSAGE_TEMPLATES_KEY = "message_templates";
    private static final String MESSAGE_DRAFT_KEY = "message_draft";
    private static final String DO_NOT_SEND_KEY = "do_not_send_numbers";
    private static final String LIST_TEMPLATES_KEY = "recipient_list_templates";
    private static final String PAYMENT_REMINDERS_KEY = "payment_reminders";
    private static final String PAYMENT_TEMPLATE_KEY = "payment_reminder_template";
    static final String PAYMENT_HISTORY_KEY = "payment_reminder_history";
    static final String PAYMENT_SCHEDULE_AT = "payment_schedule_at";
    static final String PAYMENT_SCHEDULE_NUMBERS = "payment_schedule_numbers";
    static final String PAYMENT_SCHEDULE_NAMES = "payment_schedule_names";
    static final String PAYMENT_SCHEDULE_MESSAGES = "payment_schedule_messages";
    static final String PAYMENT_SCHEDULE_REPEAT = "payment_schedule_repeat";
    static final String PAYMENT_SCHEDULE_LAST_REPEAT = "payment_schedule_last_repeat";
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private final List<ContactItem> allContacts = new ArrayList<>();
    private final List<ContactItem> visibleContacts = new ArrayList<>();
    private final Set<String> selectedNumbers = new LinkedHashSet<>();
    private final List<ContactItem> queue = new ArrayList<>();

    private ArrayAdapter<ContactItem> adapter;
    private ListView listView;
    private TextView statusText, pdfText, miniProgress, ledgerFileNameText;
    private EditText messageBox, searchBox;
    private Button sendButton, accessibilityButton, notificationAccessButton, automaticLockButton, editGroupButton, contactsButton, scheduleButton, selectedReviewButton;
    private Uri pdfUri;
    private Uri pendingMasterPdfUri;
    private String pendingCatalogName = "";
    private String pendingCatalogCategory = "";
    private String pendingCatalogKeywords = "";
    private int queueIndex = 0;
    private String activeGroup = "";
    private boolean pendingRecipientContactPicker = false;
    private boolean pendingPaymentContactPicker = false;
    private Runnable pendingPaymentRefresh;
    private boolean pendingMainContactToggle = false;
    private boolean contactsVisible = false;
    private Uri directSendImageUri;
    private Uri broadcastFileUri;
    private String broadcastFileType = "";
    private TextView broadcastFileLabel;
    private ImageView directSendPreview;
    private Button directSendImageButton;
    private Runnable scheduleTicker;

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
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        EditText input=new EditText(this); input.setHint("Create 4-digit PIN"); input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); input.setMaxLines(1);
        EditText recovery=new EditText(this);recovery.setHint("Recovery word • easy to remember");recovery.setSingleLine(true);box.addView(input);box.addView(recovery);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Set app login PIN").setMessage("Forgot PIN ke liye recovery word bhi save kare.").setView(box)
            .setPositiveButton("Save",null).setNegativeButton("Not now",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String pin=input.getText().toString().trim();String word=recovery.getText().toString().trim();if(pin.length()!=4){input.setError("Enter exactly 4 digits");return;}if(word.length()<3){recovery.setError("Enter at least 3 letters");return;}getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PIN_KEY,pin).putString(RECOVERY_KEY,word.toLowerCase(Locale.ROOT)).putBoolean(LOGIN_ENABLED_KEY,true).apply();toast("Login PIN & recovery saved");d.dismiss();}));
        d.show();
    }

    private void showUnlockDialog(String savedPin){
        EditText input=new EditText(this); input.setHint("Enter PIN"); input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("LathaBulk Login").setMessage("4-digit PIN enter kare").setView(input).setCancelable(false)
            .setPositiveButton("Login",null).setNeutralButton("Forgot PIN",(a,b)->showForgotPinDialog()).setNegativeButton("Exit",(a,b)->finish()).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(savedPin.equals(input.getText().toString().trim())){d.dismiss();setContentView(buildUi());}else input.setError("Wrong PIN");}));
        d.show();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshAccessButtons();
        boolean running=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getBoolean(AUTO_RUNNING,false);
        if(running)getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(sendButton!=null)sendButton.setText(running?"STOP AUTO SENDING":"AUTO SEND TO SELECTED CONTACTS");
        if(miniProgress!=null&&running){
            int i=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getInt(AUTO_INDEX,0);
            try{int total=new JSONArray(getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getString(AUTO_NUMBERS,"[]")).length();miniProgress.setText("Sending "+Math.min(i+1,total)+" / "+total);}catch(Exception e){miniProgress.setText("Sending contact "+(i+1));}
        }
        restoreScheduledTimer();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(10), dp(6), dp(10), dp(8));
        root.setBackgroundColor(isDark()?Color.rgb(28,28,28):Color.rgb(248,246,240));

        TextView title = new TextView(this);
        title.setText("LATHAEPS SMART");
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        GradientDrawable headerBg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.rgb(25,80,180),Color.rgb(125,55,190),Color.rgb(218,156,25)});
        headerBg.setCornerRadius(dp(14)); title.setBackground(headerBg);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(34)));

        LinearLayout tools = row();
        contactsButton = button("Contacts");
        Button selectAll = button("Select all");
        tools.addView(contactsButton, weighted(1f, 42));
        tools.addView(selectAll, weighted(1f, 42));
        root.addView(tools);
        contactsButton.setOnClickListener(v -> toggleContacts());
        selectAll.setOnClickListener(v -> selectAllVisible());

        LinearLayout selectionRow = row();
        selectedReviewButton = button("Review Selected (0)");
        selectedReviewButton.setTypeface(Typeface.DEFAULT_BOLD);
        selectedReviewButton.setTextColor(Color.rgb(20,75,150));
        Button cancelSelection = button("Clear Selection");
        cancelSelection.setTextColor(Color.rgb(175,35,35));
        selectionRow.addView(selectedReviewButton, weighted(1.35f, 39));
        selectionRow.addView(cancelSelection, weighted(1f, 39));
        root.addView(selectionRow);
        selectedReviewButton.setOnClickListener(v -> showSelectedContactsDialog());
        cancelSelection.setOnClickListener(v -> {
            if(selectedNumbers.isEmpty()){toast("No contacts selected");return;}
            new AlertDialog.Builder(this).setTitle("Cancel contact selection?")
                    .setMessage(selectedNumbers.size()+" selected contacts remove honge.")
                    .setPositiveButton("Clear All",(d,w)->clearSelection())
                    .setNegativeButton("Keep",null).show();
        });

        LinearLayout featureRow = row();
        Button csv = button("Import CSV");
        Button delay = button("Delay");
        scheduleButton = button("Schedule");
        featureRow.addView(csv, weighted(1f, 38));
        featureRow.addView(delay, weighted(.8f, 38));
        featureRow.addView(scheduleButton, weighted(1f, 38));
        root.addView(featureRow);
        csv.setOnClickListener(v -> chooseCsv());
        delay.setOnClickListener(v -> showDelayDialog());
        scheduleButton.setOnClickListener(v -> showScheduleDialog());

        LinearLayout accountRow=row();
        Button login=button("SETTINGS");
        Button backup=button("Local Backup");
        Button restore=button("Restore Backup");
        accountRow.addView(login,weighted(1f,38)); accountRow.addView(backup,weighted(1f,38)); accountRow.addView(restore,weighted(1f,38));
        root.addView(accountRow);
        login.setOnClickListener(v->showSettingsScreen());
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

        Button myGroups = button("MY RECIPIENT LISTS");
        myGroups.setTextSize(16);
        myGroups.setTypeface(Typeface.DEFAULT_BOLD);
        myGroups.setTextColor(Color.WHITE);
        myGroups.setBackground(rounded(Color.rgb(22,105,190),14));
        editGroupButton = button("Add / remove");
        LinearLayout.LayoutParams myListLp=new LinearLayout.LayoutParams(-1,dp(49));
        myListLp.setMargins(dp(2),dp(3),dp(2),dp(3));
        root.addView(myGroups,myListLp);
        myGroups.setOnClickListener(v -> showRecipientListsScreen());
        editGroupButton.setOnClickListener(v -> editActiveGroupContacts());

        LinearLayout accessRow=row();
        accessibilityButton = button("Accessibility: OFF");
        accessibilityButton.setTextSize(13);
        accessibilityButton.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (Exception e) { toast("Accessibility settings open nahi hui"); }
        });
        notificationAccessButton=button("Notification Access: OFF");
        notificationAccessButton.setTextSize(12);
        notificationAccessButton.setOnClickListener(v->{
            try{startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}
            catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}
        });
        accessRow.addView(accessibilityButton,weighted(1f,42));
        accessRow.addView(notificationAccessButton,weighted(1f,42));
        root.addView(accessRow);

        automaticLockButton=button("AUTO UNLOCK + LOCK: ON");
        automaticLockButton.setTextSize(13);
        automaticLockButton.setTypeface(Typeface.DEFAULT_BOLD);
        automaticLockButton.setOnClickListener(v->{
            boolean enabled=!TaskDeviceController.autoUnlockEnabled(this);
            TaskDeviceController.setAutomaticCycle(this,enabled);
            refreshAccessButtons();
            toast(enabled?"Task start par screen open, complete par lock ✓":"Automatic unlock + lock OFF");
        });
        LinearLayout.LayoutParams autoLockLp=new LinearLayout.LayoutParams(-1,dp(40));
        autoLockLp.setMargins(dp(2),dp(1),dp(2),dp(2));root.addView(automaticLockButton,autoLockLp);
        uiHandler.post(this::refreshAccessButtons);

        LinearLayout businessRow=row();
        Button businessFiles=button("BUSINESS FILES");
        Button catalogSection=button("CATALOG");
        Button autoReplyButton=button("AUTO REPLY");
        businessFiles.setTypeface(Typeface.DEFAULT_BOLD);
        catalogSection.setTypeface(Typeface.DEFAULT_BOLD);
        autoReplyButton.setTypeface(Typeface.DEFAULT_BOLD);
        businessRow.addView(businessFiles,weighted(1f,42));
        businessRow.addView(catalogSection,weighted(.85f,42));
        businessRow.addView(autoReplyButton,weighted(1f,42));
        root.addView(businessRow);
        businessFiles.setOnClickListener(v->showBusinessFilesDialog());
        catalogSection.setOnClickListener(v->showCatalogScreen());
        autoReplyButton.setOnClickListener(v->showAutoReplyScreen());

        LinearLayout messageShell=row();
        messageShell.setGravity(Gravity.CENTER_VERTICAL);
        messageShell.setPadding(dp(3),dp(2),dp(5),dp(2));
        GradientDrawable messageBg=new GradientDrawable();
        messageBg.setColor(isDark()?Color.rgb(48,48,48):Color.WHITE);
        messageBg.setStroke(dp(1),Color.rgb(95,125,185));
        messageBg.setCornerRadius(dp(24));
        messageShell.setBackground(messageBg);
        Button emoji=button("🙂");
        emoji.setTextSize(20);
        emoji.setContentDescription("Add emoji");
        messageShell.addView(emoji,new LinearLayout.LayoutParams(dp(46),dp(54)));
        messageBox = new EditText(this);
        messageBox.setHint("Type message • {Name} for customer name");
        messageBox.setMinLines(2);
        messageBox.setMaxLines(3);
        messageBox.setTextSize(14);
        messageBox.setGravity(Gravity.CENTER_VERTICAL);
        messageBox.setBackgroundColor(Color.TRANSPARENT);
        messageBox.setPadding(dp(4),0,dp(5),0);
        messageBox.setText(getSharedPreferences(PREFS,MODE_PRIVATE).getString(MESSAGE_DRAFT_KEY,""));
        messageShell.addView(messageBox,new LinearLayout.LayoutParams(0,dp(66),1f));
        LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(-1,dp(70));
        mlp.setMargins(dp(1),dp(2),dp(1),dp(2));
        root.addView(messageShell,mlp);
        emoji.setOnClickListener(v->showEmojiPicker());
        messageBox.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(MESSAGE_DRAFT_KEY,s.toString()).apply();}public void afterTextChanged(Editable e){}});

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

        LinearLayout sendRow=row();
        Button testSendButton=button("TEST SEND • 1 CONTACT");
        testSendButton.setTextSize(12);
        testSendButton.setTypeface(Typeface.DEFAULT_BOLD);
        testSendButton.setTextColor(Color.rgb(25,75,155));
        sendButton = button("AUTO SEND TO SELECTED CONTACTS");
        sendButton.setTextSize(15);
        sendButton.setTypeface(Typeface.DEFAULT_BOLD);
        sendButton.setTextColor(Color.WHITE);
        sendButton.setBackgroundColor(Color.rgb(25,110,60));
        testSendButton.setOnClickListener(v -> testSendOneContact());
        sendButton.setOnClickListener(v -> startOrStopAutoSend());
        sendRow.addView(testSendButton,weighted(1f,52));
        sendRow.addView(sendButton,weighted(1.45f,52));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, dp(54));
        slp.setMargins(0, dp(3), 0, dp(3));
        root.addView(sendRow, slp);

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
        listView.setVisibility(View.GONE);
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

    private void refreshAccessibilityButton(){refreshAccessButtons();}
    private void refreshAccessButtons(){
        boolean accessibility=isAccessibilityServiceEnabled();
        if(accessibilityButton!=null){
            accessibilityButton.setText(accessibility?"Accessibility: ON":"Accessibility: OFF");
            styleLiveStatus(accessibilityButton,accessibility);
        }
        boolean notification=isNotificationAccessEnabled();
        if(notificationAccessButton!=null){
            notificationAccessButton.setText(notification?"Notification Access: ON":"Notification Access: OFF");
            styleLiveStatus(notificationAccessButton,notification);
        }
        if(automaticLockButton!=null){
            boolean enabled=TaskDeviceController.autoUnlockEnabled(this)&&TaskDeviceController.autoRelockEnabled(this);
            automaticLockButton.setText(enabled?"AUTO UNLOCK + LOCK: ON":"AUTO UNLOCK + LOCK: OFF");
            styleLiveStatus(automaticLockButton,enabled);
        }
    }
    private void styleLiveStatus(Button button,boolean enabled){
        button.setTextColor(Color.WHITE);
        button.setBackground(rounded(enabled?Color.rgb(18,135,70):Color.rgb(190,45,45),12));
    }
    private boolean isNotificationAccessEnabled(){
        try{
            String flat=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
            if(flat==null||flat.trim().isEmpty())return false;
            String expected=new ComponentName(this,AutoReplyNotificationService.class).flattenToString();
            for(String item:flat.split(":")){ComponentName c=ComponentName.unflattenFromString(item);if(c!=null&&(c.flattenToString().equals(expected)||c.getPackageName().equals(getPackageName())))return true;}
        }catch(Exception ignored){}
        return false;
    }

    private void requestContacts(){
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED) loadContacts();
        else requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},CONTACT_PERMISSION);
    }
    private void toggleContacts(){
        if(contactsVisible){setContactsVisible(false);return;}
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED){
            loadContacts();
            setContactsVisible(true);
        }else{
            pendingMainContactToggle=true;
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},CONTACT_PERMISSION);
        }
    }
    private void setContactsVisible(boolean visible){
        contactsVisible=visible;
        if(listView!=null)listView.setVisibility(visible?View.VISIBLE:View.GONE);
        if(contactsButton!=null)contactsButton.setText(visible?"Hide Contacts":"Contacts");
    }
    private void requestNotificationPermissionIfNeeded(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFICATION_PERMISSION);
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if(requestCode==CONTACT_PERMISSION){
            if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED){
                loadContacts();
                if(pendingMainContactToggle){pendingMainContactToggle=false;setContactsVisible(true);}
                if(pendingPaymentContactPicker){pendingPaymentContactPicker=false;Runnable after=pendingPaymentRefresh;pendingPaymentRefresh=null;uiHandler.postDelayed(()->showPaymentContactPicker(after),180);}
            }else{pendingMainContactToggle=false;pendingPaymentContactPicker=false;pendingPaymentRefresh=null;toast("Contacts permission required");}
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
        if(pendingRecipientContactPicker){pendingRecipientContactPicker=false;uiHandler.postDelayed(this::showRecipientContactPicker,180);}
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
        if(selectedReviewButton!=null){
            selectedReviewButton.setText("Review Selected ("+selectedNumbers.size()+")");
            selectedReviewButton.setEnabled(!selectedNumbers.isEmpty());
        }
        editGroupButton.setEnabled(!activeGroup.isEmpty());
    }
    private void selectAllVisible(){for(ContactItem i:visibleContacts)selectedNumbers.add(i.number);saveSelectedContacts();refreshChecks();}
    private void clearSelection(){selectedNumbers.clear();queue.clear();queueIndex=0;activeGroup="";saveSelectedContacts();sendButton.setText("AUTO SEND TO SELECTED CONTACTS");miniProgress.setText("Ready");cancelProgressNotification();refreshChecks();toast("Contact selection cleared");}

    private void showSelectedContactsDialog(){
        if(selectedNumbers.isEmpty()){toast("No contacts selected");return;}
        List<String> numbers=new ArrayList<>(selectedNumbers);
        String[] labels=new String[numbers.size()];
        boolean[] checked=new boolean[numbers.size()];
        Set<String> working=new LinkedHashSet<>(selectedNumbers);
        for(int i=0;i<numbers.size();i++){
            String number=numbers.get(i);
            labels[i]=findName(number)+"  •  +"+number;
            checked[i]=true;
        }
        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle("Selected Contacts • "+numbers.size())
                .setMessage("Galat contact ko uncheck karke Save Changes dabayein.")
                .setMultiChoiceItems(labels,checked,(d,which,isChecked)->{
                    String number=numbers.get(which);
                    if(isChecked)working.add(number);else working.remove(number);
                })
                .setPositiveButton("Save Changes",(d,w)->{
                    selectedNumbers.clear();selectedNumbers.addAll(working);
                    saveSelectedContacts();refreshChecks();
                    toast(selectedNumbers.size()+" contacts selected");
                })
                .setNeutralButton("Clear All",(d,w)->clearSelection())
                .setNegativeButton("Cancel",null).create();
        dialog.show();
    }

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
        }catch(Exception e){toast("Recipient List save failed");}
    }
    private void showSaveGroupDialog(){
        if(selectedNumbers.isEmpty()){toast("Select contacts first");return;}
        EditText input=new EditText(this);input.setHint("Example: Dealers");input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Save recipient list").setMessage(selectedNumbers.size()+" contacts selected").setView(input)
                .setPositiveButton("Save",(d,w)->{String n=input.getText().toString().trim();if(n.isEmpty()){toast("Enter list name");return;}
                    Map<String,List<String>>g=readGroups();g.put(n,new ArrayList<>(selectedNumbers));writeGroups(g);activeGroup=n;refreshChecks();toast("Saved: "+n);})
                .setNegativeButton("Cancel",null).show();
    }
    private void showRecipientListsScreen(){
        Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(Color.BLACK);
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),0,dp(12),0);head.setBackgroundColor(Color.rgb(28,26,27));
        Button back=button("‹");back.setTextSize(36);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);
        TextView title=new TextView(this);title.setText("My Recipient Lists");title.setTextSize(25);title.setTypeface(Typeface.DEFAULT_BOLD);title.setTextColor(Color.WHITE);title.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(back,new LinearLayout.LayoutParams(dp(58),dp(72)));head.addView(title,new LinearLayout.LayoutParams(0,dp(72),1f));page.addView(head);
        EditText listSearch=new EditText(this);listSearch.setHint("Search list name or mobile number");listSearch.setSingleLine(true);listSearch.setTextColor(Color.WHITE);listSearch.setHintTextColor(Color.GRAY);listSearch.setTextSize(15);listSearch.setPadding(dp(16),0,dp(16),0);page.addView(listSearch,new LinearLayout.LayoutParams(-1,dp(52)));
        ScrollView scroll=new ScrollView(this);LinearLayout cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);cards.setPadding(dp(16),dp(16),dp(16),dp(100));scroll.addView(cards);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        Button plus=button("+");plus.setTextSize(34);plus.setTextColor(Color.WHITE);plus.setBackground(rounded(Color.rgb(20,112,210),60));
        LinearLayout foot=row();foot.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);foot.setPadding(0,0,dp(18),dp(14));foot.addView(plus,new LinearLayout.LayoutParams(dp(70),dp(70)));page.addView(foot,new LinearLayout.LayoutParams(-1,dp(88)));
        Runnable refresh=()->renderRecipientListCards(cards,dialog,listSearch.getText().toString());refresh.run();
        listSearch.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){renderRecipientListCards(cards,dialog,s.toString());}public void afterTextChanged(Editable e){}});
        back.setOnClickListener(v->dialog.dismiss());plus.setOnClickListener(v->{dialog.dismiss();showCreateRecipientListDialog();});
        dialog.setContentView(page);dialog.show();
    }
    private void renderRecipientListCards(LinearLayout parent,Dialog dialog,String query){
        parent.removeAllViews();Map<String,List<String>> groups=readGroups();
        if(groups.isEmpty()){TextView empty=new TextView(this);empty.setText("No recipient lists yet\nTap + to create one");empty.setGravity(Gravity.CENTER);empty.setTextColor(Color.LTGRAY);empty.setTextSize(17);parent.addView(empty,new LinearLayout.LayoutParams(-1,dp(160)));return;}
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);int shown=0;
        for(Map.Entry<String,List<String>> e:groups.entrySet()){
            boolean match=q.isEmpty()||e.getKey().toLowerCase(Locale.ROOT).contains(q);if(!match)for(String number:e.getValue())if(number.contains(q)||last10Digits(number).contains(q)){match=true;break;}if(!match)continue;shown++;
            String name=e.getKey();LinearLayout card=row();card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(16),dp(10),dp(8),dp(10));card.setBackground(rounded(Color.rgb(42,42,42),14));
            LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);TextView a=new TextView(this);a.setText(name);a.setTextColor(Color.WHITE);a.setTextSize(24);a.setTypeface(Typeface.DEFAULT_BOLD);
            TextView b=new TextView(this);b.setText(e.getValue().size()+" contacts   ◉");b.setTextColor(Color.rgb(180,180,185));b.setTextSize(17);b.setPadding(0,dp(3),0,0);words.addView(a);words.addView(b);
            Button send=button("SEND");send.setTextSize(12);send.setTextColor(Color.WHITE);send.setBackground(rounded(Color.rgb(25,125,72),12));Button more=button("⋮");more.setTextSize(30);more.setTextColor(Color.rgb(25,118,210));more.setBackgroundColor(Color.TRANSPARENT);card.addView(words,new LinearLayout.LayoutParams(0,dp(88),1f));card.addView(send,new LinearLayout.LayoutParams(dp(72),dp(42)));card.addView(more,new LinearLayout.LayoutParams(dp(48),dp(72)));
            card.setOnClickListener(v->{openGroup(name);dialog.dismiss();});send.setOnClickListener(v->{dialog.dismiss();showDirectRecipientSendDialog(name,new ArrayList<>(e.getValue()));});more.setOnClickListener(v->showRecipientListMenu(more,name,dialog));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(112));lp.setMargins(0,0,0,dp(14));parent.addView(card,lp);
        }
        if(shown==0){TextView empty=new TextView(this);empty.setText("No list found for this number");empty.setGravity(Gravity.CENTER);empty.setTextColor(Color.LTGRAY);empty.setTextSize(16);parent.addView(empty,new LinearLayout.LayoutParams(-1,dp(140)));}
    }
    private void showCreateRecipientListDialog(){
        EditText input=new EditText(this);input.setHint("Recipient list name");input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("New Recipient List").setMessage("New list 0 contacts se create hogi. Next screen me phone contacts select karein.").setView(input).setPositiveButton("Create & Add Contacts",(d,w)->{
            String n=input.getText().toString().trim();if(n.isEmpty()){toast("Enter list name");return;}Map<String,List<String>>g=readGroups();if(g.containsKey(n)){toast("List name already exists");return;}g.put(n,new ArrayList<>());writeGroups(g);selectedNumbers.clear();activeGroup=n;refreshChecks();toast("Empty recipient list created: "+n);openRecipientContactPicker();
        }).setNegativeButton("Cancel",null).show();
    }
    private void showRecipientListMenu(View anchor,String name,Dialog parent){
        PopupMenu m=new PopupMenu(this,anchor);m.getMenu().add("Send Text / Image");m.getMenu().add("Open contacts");m.getMenu().add("Add / remove contacts");m.getMenu().add("Rename");m.getMenu().add("Duplicate");m.getMenu().add("Delete");
        m.setOnMenuItemClickListener(item->{String x=item.getTitle().toString();if(x.startsWith("Send")){List<String> nums=readGroups().get(name);parent.dismiss();showDirectRecipientSendDialog(name,nums==null?new ArrayList<>():new ArrayList<>(nums));}else if(x.startsWith("Open")){openGroup(name);parent.dismiss();}else if(x.startsWith("Add")){openGroup(name);parent.dismiss();editActiveGroupContacts();}else if(x.equals("Rename")){parent.dismiss();renameGroup(name);}else if(x.equals("Duplicate")){duplicateGroup(name);parent.dismiss();showRecipientListsScreen();}else confirmDeleteGroup(name);return true;});m.show();
    }

    private void showDirectRecipientSendDialog(String listName,List<String> numbers){
        directSendImageUri=null;LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        TextView count=new TextView(this);count.setText(listName+" • "+numbers.size()+" recipients");count.setTextSize(16);count.setTypeface(Typeface.DEFAULT_BOLD);count.setTextColor(Color.rgb(20,90,65));count.setPadding(0,dp(4),0,dp(6));
        EditText message=new EditText(this);message.setHint("Type message / caption");message.setMinLines(3);message.setMaxLines(6);message.setGravity(Gravity.TOP);message.setText(getSharedPreferences(PREFS,MODE_PRIVATE).getString(MESSAGE_DRAFT_KEY,""));
        JSONArray listTemplates=templatesForList(listName);Spinner templatePicker=null;if(listTemplates.length()>0){List<String> templateNames=new ArrayList<>();templateNames.add("Choose Recipient List Template");for(int i=0;i<listTemplates.length();i++){JSONObject o=listTemplates.optJSONObject(i);templateNames.add(o==null?"Template "+(i+1):o.optString("name","Template "+(i+1)));}templatePicker=new Spinner(this);templatePicker.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,templateNames));final Spinner picker=templatePicker;picker.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){if(pos>0){JSONObject o=listTemplates.optJSONObject(pos-1);if(o!=null)message.setText(o.optString("body",""));}}public void onNothingSelected(android.widget.AdapterView<?> p){}});}
        directSendPreview=new ImageView(this);directSendPreview.setAdjustViewBounds(true);directSendPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);directSendPreview.setVisibility(View.GONE);
        directSendImageButton=button("SELECT IMAGE FROM PHONE GALLERY");directSendImageButton.setOnClickListener(v->openPhoneGallery(PICK_DIRECT_IMAGE));
        TextView help=new TextView(this);help.setText("Phone Gallery / Albums se image select karein. Select hone ke baad editor open hoga; text, sticker/emoji, color, size aur position optional hain.");help.setTextSize(12);help.setPadding(0,dp(7),0,0);
        box.addView(count);if(templatePicker!=null)box.addView(templatePicker,new LinearLayout.LayoutParams(-1,dp(48)));box.addView(message);box.addView(directSendPreview,new LinearLayout.LayoutParams(-1,dp(190)));box.addView(directSendImageButton,new LinearLayout.LayoutParams(-1,dp(48)));box.addView(help);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Send from Recipient List").setView(box).setPositiveButton("START AUTO SEND",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String text=message.getText().toString().trim();if(numbers.isEmpty()){toast("Recipient list empty");return;}if(text.isEmpty()&&directSendImageUri==null){message.setError("Type message or choose image");return;}if(!isAccessibilityServiceEnabled()){toast("Pehle Accessibility ON karein");try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(Exception ignored){}return;}startRecipientMediaQueue(listName,numbers,text,directSendImageUri);d.dismiss();}));d.show();
    }

    private void startRecipientMediaQueue(String listName,List<String> numbers,String message,Uri image){
        List<String> safeNumbers=new ArrayList<>();Set<String> seen=new LinkedHashSet<>();for(String raw:numbers){String n=normalize(raw);if(!n.isEmpty()&&!isDoNotSend(n)&&seen.add(n))safeNumbers.add(n);}if(safeNumbers.isEmpty()){toast("All contacts invalid or in Do Not Send List");return;}int skipped=numbers.size()-safeNumbers.size();
        JSONArray nums=new JSONArray(),names=new JSONArray();for(String n:safeNumbers){nums.put(n);names.put(findName(n));}
        SharedPreferences settings=getSharedPreferences(PREFS,MODE_PRIVATE);SharedPreferences.Editor ed=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().putString(AUTO_NUMBERS,nums.toString()).putString(AUTO_NAMES,names.toString()).putString(AUTO_MESSAGE,message).remove(AUTO_MESSAGES).putString(AUTO_QUEUE_TOKEN,String.valueOf(System.currentTimeMillis())).putString(AUTO_FAILED,"[]").putInt(AUTO_INDEX,0).putInt(AUTO_MIN_DELAY,settings.getInt(AUTO_MIN_DELAY,3)).putInt(AUTO_MAX_DELAY,settings.getInt(AUTO_MAX_DELAY,7)).putBoolean(AUTO_RUNNING,true);
        if(image==null)ed.remove(AUTO_IMAGE_URI).remove(AUTO_IMAGE_TYPE);else ed.putString(AUTO_IMAGE_URI,image.toString()).putString(AUTO_IMAGE_TYPE,"image/jpeg");ed.apply();
        selectedNumbers.clear();selectedNumbers.addAll(safeNumbers);activeGroup=listName;getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);if(sendButton!=null)sendButton.setText("STOP AUTO SENDING");if(miniProgress!=null)miniProgress.setText((image==null?"Text":"Image + Text")+" • Starting 1 / "+safeNumbers.size()+(skipped>0?" • "+skipped+" blocked/skipped":""));showProgressNotification(0,safeNumbers.size(),listName+" • "+(image==null?"Text":"Image + Text"));WhatsAppAccessibilityService.openCurrentChat(this);
    }

    private void showDirectImageEditor(Uri source){
        Bitmap original=loadScaledBitmap(source,1600);if(original==null){toast("Image open nahi hui");return;}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),0,dp(14),0);
        ImageView preview=new ImageView(this);preview.setImageBitmap(original);preview.setAdjustViewBounds(true);preview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);box.addView(preview,new LinearLayout.LayoutParams(-1,dp(230)));
        EditText overlay=new EditText(this);overlay.setHint("Text on image • optional");overlay.setSingleLine(false);overlay.setMaxLines(2);box.addView(overlay);
        TextView stickerLabel=new TextView(this);stickerLabel.setText("Choose sticker / emoji");stickerLabel.setTypeface(Typeface.DEFAULT_BOLD);stickerLabel.setPadding(0,dp(7),0,dp(4));box.addView(stickerLabel);
        LinearLayout stickers=row();String[] stickerValue={""};String[] choices={"NONE","❤️","⭐","⚡","🎉","👍"};for(String s:choices){Button b=button(s);b.setTextSize(s.equals("NONE")?10:21);stickers.addView(b,weighted(1f,42));b.setOnClickListener(v->{stickerValue[0]=s.equals("NONE")?"":s;toast(stickerValue[0].isEmpty()?"Sticker removed":"Sticker selected "+stickerValue[0]);});}box.addView(stickers);
        Spinner position=new Spinner(this);String[] positions={"Bottom","Center","Top"};position.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,positions));
        Spinner color=new Spinner(this);String[] colors={"White","Yellow","Red","Black","Blue"};color.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,colors));
        Spinner size=new Spinner(this);String[] sizes={"Medium","Large","Small"};size.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sizes));
        LinearLayout options=row();options.addView(position,weighted(1f,48));options.addView(color,weighted(1f,48));options.addView(size,weighted(1f,48));box.addView(options);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Edit Image").setView(box).setPositiveButton("APPLY & USE",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{Uri edited=createEditedImage(original,overlay.getText().toString().trim(),stickerValue[0],positions[position.getSelectedItemPosition()],colors[color.getSelectedItemPosition()],sizes[size.getSelectedItemPosition()]);if(edited==null){toast("Image edit save failed");return;}directSendImageUri=edited;if(directSendPreview!=null){directSendPreview.setImageURI(null);directSendPreview.setImageURI(edited);directSendPreview.setVisibility(View.VISIBLE);}if(directSendImageButton!=null)directSendImageButton.setText("CHANGE / EDIT IMAGE ✓");d.dismiss();}));d.show();
    }

    private Bitmap loadScaledBitmap(Uri uri,int maxSide){
        try{BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,bounds);}int sample=1;while(Math.max(bounds.outWidth,bounds.outHeight)/sample>maxSide)sample*=2;BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=Math.max(1,sample);try(InputStream in=getContentResolver().openInputStream(uri)){return BitmapFactory.decodeStream(in,null,options);}}catch(Exception e){return null;}
    }

    private Uri createEditedImage(Bitmap original,String text,String sticker,String position,String colorName,String sizeName){
        try{Bitmap output=original.copy(Bitmap.Config.ARGB_8888,true);Canvas canvas=new Canvas(output);float scale=output.getWidth()/720f;float textSize=("Large".equals(sizeName)?62:"Small".equals(sizeName)?34:48)*Math.max(.65f,scale);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));paint.setTextSize(textSize);paint.setTextAlign(Paint.Align.CENTER);paint.setColor("Yellow".equals(colorName)?Color.YELLOW:"Red".equals(colorName)?Color.RED:"Black".equals(colorName)?Color.BLACK:"Blue".equals(colorName)?Color.rgb(30,100,240):Color.WHITE);paint.setShadowLayer(Math.max(3,4*scale),0,Math.max(2,2*scale),Color.BLACK);
            float baseY="Top".equals(position)?output.getHeight()*.18f:"Center".equals(position)?output.getHeight()*.52f:output.getHeight()*.84f;float center=output.getWidth()/2f;
            if(sticker!=null&&!sticker.isEmpty()){Paint sp=new Paint(Paint.ANTI_ALIAS_FLAG);sp.setTextAlign(Paint.Align.CENTER);sp.setTextSize(textSize*1.75f);canvas.drawText(sticker,center,text.isEmpty()?baseY:baseY-textSize*1.25f,sp);}
            if(text!=null&&!text.isEmpty()){String[] lines=text.split("\\n",2);float firstY=baseY-(lines.length-1)*textSize*.58f;for(int i=0;i<lines.length;i++){String line=lines[i];float y=firstY+i*textSize*1.15f;float width=Math.min(output.getWidth()*.92f,paint.measureText(line)+textSize*.55f);Paint bg=new Paint(Paint.ANTI_ALIAS_FLAG);bg.setColor(Color.argb(105,0,0,0));canvas.drawRoundRect(center-width/2,y-textSize*.9f,center+width/2,y+textSize*.25f,textSize*.22f,textSize*.22f,bg);canvas.drawText(line,center,y,paint);}}
            File dir=new File(getFilesDir(),"edited_images");if(!dir.exists()&&!dir.mkdirs())return null;File out=new File(dir,"edited_"+System.currentTimeMillis()+".jpg");try(FileOutputStream stream=new FileOutputStream(out)){output.compress(Bitmap.CompressFormat.JPEG,92,stream);}return FileProvider.getUriForFile(this,getPackageName()+".fileprovider",out);
        }catch(Exception e){return null;}
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
        new AlertDialog.Builder(this).setTitle("Rename recipient list").setView(input).setPositiveButton("Rename",(d,w)->{
            String n=input.getText().toString().trim();if(n.isEmpty()||n.equals(oldName))return;
            Map<String,List<String>>g=readGroups();List<String>nums=g.remove(oldName);g.put(n,nums);writeGroups(g);if(activeGroup.equals(oldName))activeGroup=n;refreshChecks();toast("Renamed to "+n);
        }).setNegativeButton("Cancel",null).show();
    }
    private void duplicateGroup(String name){
        Map<String,List<String>>g=readGroups();String base=name+" Copy",n=base;int i=2;while(g.containsKey(n))n=base+" "+i++;
        g.put(n,new ArrayList<>(g.get(name)));writeGroups(g);toast("Created: "+n);
    }
    private void confirmDeleteGroup(String name){
        new AlertDialog.Builder(this).setTitle("Delete recipient list?").setMessage(name+" will be removed. Phone contacts will not be deleted.")
                .setPositiveButton("Delete",(d,w)->{Map<String,List<String>>g=readGroups();g.remove(name);writeGroups(g);if(activeGroup.equals(name)){activeGroup="";selectedNumbers.clear();}refreshChecks();toast("Deleted: "+name);})
                .setNegativeButton("Cancel",null).show();
    }
    private void editActiveGroupContacts(){
        if(activeGroup.isEmpty()){toast("Open a recipient list first");return;}
        openRecipientContactPicker();
    }
    private void openRecipientContactPicker(){
        pendingRecipientContactPicker=true;
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED)loadContacts();
        else requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},CONTACT_PERMISSION);
    }
    private void showRecipientContactPicker(){
        if(activeGroup.isEmpty())return;
        Dialog d=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(12),dp(10),dp(12),dp(10));page.setBackgroundColor(Color.WHITE);
        TextView title=new TextView(this);title.setText("Add Contacts • "+activeGroup);title.setTextSize(22);title.setTypeface(Typeface.DEFAULT_BOLD);title.setTextColor(Color.rgb(25,65,125));title.setPadding(dp(4),dp(8),dp(4),dp(8));page.addView(title);
        EditText find=new EditText(this);find.setHint("Search contact name or mobile number");find.setSingleLine(true);page.addView(find,new LinearLayout.LayoutParams(-1,dp(52)));
        List<ContactItem> filtered=new ArrayList<>(allContacts);Set<String> working=new LinkedHashSet<>(selectedNumbers);ArrayAdapter<ContactItem> a=new ArrayAdapter<>(this,android.R.layout.simple_list_item_multiple_choice,filtered);ListView list=new ListView(this);list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);list.setAdapter(a);page.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        Runnable checks=()->{list.clearChoices();for(int i=0;i<filtered.size();i++)list.setItemChecked(i,working.contains(filtered.get(i).number));};checks.run();
        list.setOnItemClickListener((p,v,pos,id)->{String num=filtered.get(pos).number;if(working.contains(num))working.remove(num);else working.add(num);checks.run();});
        find.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int x){}public void onTextChanged(CharSequence s,int st,int b,int c){String q=s.toString().trim().toLowerCase(Locale.ROOT);filtered.clear();for(ContactItem x:allContacts)if(q.isEmpty()||x.name.toLowerCase(Locale.ROOT).contains(q)||x.number.contains(q)||last10Digits(x.number).contains(q))filtered.add(x);a.notifyDataSetChanged();checks.run();}public void afterTextChanged(Editable e){}});
        LinearLayout actions=row();Button cancel=button("Cancel");Button save=button("Save Contacts");save.setTypeface(Typeface.DEFAULT_BOLD);save.setTextColor(Color.WHITE);save.setBackgroundColor(Color.rgb(25,120,70));actions.addView(cancel,weighted(1f,48));actions.addView(save,weighted(1.3f,48));page.addView(actions);cancel.setOnClickListener(v->d.dismiss());save.setOnClickListener(v->{selectedNumbers.clear();selectedNumbers.addAll(working);Map<String,List<String>>g=readGroups();g.put(activeGroup,new ArrayList<>(selectedNumbers));writeGroups(g);refreshChecks();toast(activeGroup+" • "+selectedNumbers.size()+" contacts saved");d.dismiss();showRecipientListsScreen();});
        d.setContentView(page);d.show();
    }
    private String last10Digits(String raw){String d=raw==null?"":raw.replaceAll("[^0-9]","");return d.length()>10?d.substring(d.length()-10):d;}

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

    private void startOrStopAutoSend(){startOrStopAutoSend(true);}

    private void testSendOneContact(){
        hideKeyboard();
        if(getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getBoolean(AUTO_RUNNING,false)){toast("Auto Send already running");return;}
        if(!isAccessibilityServiceEnabled()){
            toast("Pehle Accessibility ON karein");
            try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(Exception ignored){}
            return;
        }
        String body=messageBox.getText().toString().trim();
        if(body.isEmpty()){toast("Type a message first");return;}
        String testNumber="";
        for(String raw:selectedNumbers){String n=normalize(raw);if(!n.isEmpty()&&!isDoNotSend(n)){testNumber=n;break;}}
        if(testNumber.isEmpty()){toast("Select at least one valid contact");return;}
        String finalNumber=testNumber;
        String message=buildFinalMessage();
        String name=findName(finalNumber);
        new AlertDialog.Builder(this).setTitle("Test Send • 1 Contact")
                .setMessage("Only this contact will receive the test:\n\n"+name+"\n+"+finalNumber+"\n\nBulk queue will not start.")
                .setPositiveButton("SEND TEST",(d,w)->{
                    List<String> one=new ArrayList<>();one.add(finalNumber);
                    beginTextAutoSend(one,message,0);
                    miniProgress.setText("TEST SEND • "+name);
                })
                .setNegativeButton("Cancel",null).show();
    }

    private void startOrStopAutoSend(boolean askConfirmation){
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
        if(selectedNumbers.isEmpty()){toast("Select contacts or open Recipient List");return;}
        if(body.isEmpty()){toast("Type a message first");return;}
        LinkedHashSet<String> validSet=new LinkedHashSet<>();
        for(String raw:selectedNumbers){String n=normalize(raw);if(!n.isEmpty()&&!isDoNotSend(n))validSet.add(n);}
        List<String> validNumbers=new ArrayList<>(validSet);
        int skipped=selectedNumbers.size()-validNumbers.size();
        if(validNumbers.isEmpty()){toast("No valid mobile numbers selected");return;}
        if(askConfirmation){showSendConfirmation(validNumbers,message,skipped);return;}
        beginTextAutoSend(validNumbers,message,skipped);
    }

    private void showSendConfirmation(List<String> validNumbers,String message,int skipped){
        StringBuilder preview=new StringBuilder();
        preview.append("Recipients: ").append(validNumbers.size());
        if(skipped>0)preview.append("\nInvalid / duplicate / Do Not Send skipped: ").append(skipped);
        preview.append("\n\nSending to:\n");
        int limit=Math.min(5,validNumbers.size());
        for(int i=0;i<limit;i++)preview.append("• ").append(findName(validNumbers.get(i))).append("  +").append(validNumbers.get(i)).append("\n");
        if(validNumbers.size()>limit)preview.append("• +").append(validNumbers.size()-limit).append(" more contacts\n");
        String clean=message.trim();
        if(clean.length()>240)clean=clean.substring(0,240)+"…";
        preview.append("\nMessage preview:\n").append(clean);
        new AlertDialog.Builder(this).setTitle("Confirm Auto Send")
                .setMessage(preview.toString())
                .setPositiveButton("START SEND",(d,w)->beginTextAutoSend(validNumbers,message,skipped))
                .setNeutralButton("Review Contacts",(d,w)->showSelectedContactsDialog())
                .setNegativeButton("Cancel",null).show();
    }

    private void beginTextAutoSend(List<String> validNumbers,String message,int skipped){
        JSONArray nums=new JSONArray();
        JSONArray names=new JSONArray();
        for(String n:validNumbers){
            nums.put(n);
            names.put(findName(n));
        }
        SharedPreferences settings=getSharedPreferences(PREFS,MODE_PRIVATE);
        getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit()
                .putString(AUTO_NUMBERS,nums.toString()).putString(AUTO_NAMES,names.toString()).putString(AUTO_MESSAGE,message)
                .remove(AUTO_MESSAGES)
                .putString(AUTO_QUEUE_TOKEN,String.valueOf(System.currentTimeMillis()))
                .putInt(AUTO_MIN_DELAY,settings.getInt(AUTO_MIN_DELAY,3)).putInt(AUTO_MAX_DELAY,settings.getInt(AUTO_MAX_DELAY,7))
                .putString(AUTO_FAILED,"[]").putInt(AUTO_INDEX,0).remove(AUTO_IMAGE_URI).remove(AUTO_IMAGE_TYPE).putBoolean(AUTO_RUNNING,true).apply();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sendButton.setText("STOP AUTO SENDING");
        miniProgress.setText("Screen awake ON • Starting 1 / "+validNumbers.size()+(skipped>0?" • "+skipped+" skipped":""));
        showProgressNotification(0,validNumbers.size(),"Starting");
        WhatsAppAccessibilityService.openCurrentChat(this);
    }

    private void stopAutoSend(String label){
        getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().putBoolean(AUTO_RUNNING,false).remove(AUTO_IMAGE_URI).remove(AUTO_IMAGE_TYPE).remove(AUTO_MESSAGES).remove(AUTO_QUEUE_TOKEN).apply();
        WhatsAppAccessibilityService.releaseQueueWakeLock();
        TaskDeviceController.cancel(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        if(requestCode==PICK_LEDGER_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){importMasterLedgerPdf(data.getData());}
        if(requestCode==PICK_CATALOG_FILE&&resultCode==RESULT_OK&&data!=null){saveCatalogSelections(data);}
        if(requestCode==CREATE_BACKUP&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) writeBackup(data.getData());
        if(requestCode==PICK_RESTORE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) restoreBackup(data.getData());
        if(requestCode==PICK_LEDGER_CUSTOMERS_XLSX&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) importLedgerCustomersXlsx(data.getData());
        if(requestCode==PICK_MASTER_PDF_FOR_EXCEL&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){pendingMasterPdfUri=data.getData();createMasterLedgerExcelFile();}
        if(requestCode==CREATE_MASTER_LEDGER_XLSX&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null&&pendingMasterPdfUri!=null) convertMasterPdfToExcel(pendingMasterPdfUri,data.getData());
        if(requestCode==PICK_DIRECT_IMAGE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null)showDirectImageEditor(data.getData());
        if(requestCode==PICK_BROADCAST_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){broadcastFileUri=data.getData();broadcastFileType=getContentResolver().getType(broadcastFileUri);try{getContentResolver().takePersistableUriPermission(broadcastFileUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}if(broadcastFileLabel!=null)broadcastFileLabel.setText("Selected: "+(broadcastFileUri.getLastPathSegment()==null?"Attachment ready ✓":broadcastFileUri.getLastPathSegment()));}
    }
    private void sharePdf(){if(pdfUri==null){toast("Choose PDF first");return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,pdfUri);i.putExtra(Intent.EXTRA_TEXT,buildFinalMessage());i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{i.setPackage("com.whatsapp");startActivity(i);}catch(Exception e){i.setPackage(null);startActivity(Intent.createChooser(i,"Share PDF"));}}

    private void createNotificationChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL_ID,"Sending progress",NotificationManager.IMPORTANCE_LOW);c.setDescription("Compact queue progress");c.setSound(null,null);getSystemService(NotificationManager.class).createNotificationChannel(c);}}
    private void showProgressNotification(int current,int total,String contact){updateProgressNotification(this,current,total,contact,current>=total);}
    static void updateProgressNotification(Context context,int current,int total,String contact,boolean completed){Intent launch=new Intent(context,MainActivity.class);PendingIntent p=PendingIntent.getActivity(context,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent cancelIntent=new Intent(context,AutoSendCancelReceiver.class).setAction(AutoSendCancelReceiver.ACTION_CANCEL);PendingIntent cancel=PendingIntent.getBroadcast(context,511,cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        int safeTotal=Math.max(1,total),safeCurrent=Math.max(0,Math.min(current,safeTotal));NotificationCompat.Builder b=new NotificationCompat.Builder(context,CHANNEL_ID).setSmallIcon(completed?android.R.drawable.stat_sys_upload_done:android.R.drawable.stat_sys_upload).setContentTitle(completed?"Completed "+total+"/"+total:"Sending "+safeCurrent+"/"+total).setContentText(contact).setOnlyAlertOnce(true).setOngoing(!completed).setPriority(NotificationCompat.PRIORITY_LOW).setProgress(safeTotal,safeCurrent,false).setContentIntent(p).setAutoCancel(completed);
        if(current<total)b.addAction(android.R.drawable.ic_menu_close_clear_cancel,"CANCEL AUTO SEND",cancel);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.notify(NOTIFICATION_ID,b.build());}
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
        String[] items={"Change PIN","Change recovery word","Turn login "+(p.getBoolean(LOGIN_ENABLED_KEY,true)?"OFF":"ON"),"Forgot PIN / Recover"};
        new AlertDialog.Builder(this).setTitle("Login settings").setItems(items,(d,which)->{
            if(which==0){EditText in=new EditText(this);in.setHint("New 4-digit PIN");in.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);new AlertDialog.Builder(this).setTitle("Change PIN").setView(in).setPositiveButton("Save",(a,b)->{String x=in.getText().toString().trim();if(x.length()==4){p.edit().putString(PIN_KEY,x).putBoolean(LOGIN_ENABLED_KEY,true).apply();toast("PIN changed");}else toast("PIN must be 4 digits");}).setNegativeButton("Cancel",null).show();}
            else if(which==1){EditText in=new EditText(this);in.setHint("New recovery word");new AlertDialog.Builder(this).setTitle("Recovery word").setView(in).setPositiveButton("Save",(a,b)->{String x=in.getText().toString().trim();if(x.length()>=3){p.edit().putString(RECOVERY_KEY,x.toLowerCase(Locale.ROOT)).apply();toast("Recovery word saved");}else toast("Minimum 3 letters required");}).setNegativeButton("Cancel",null).show();}
            else if(which==2){boolean n=!p.getBoolean(LOGIN_ENABLED_KEY,true);p.edit().putBoolean(LOGIN_ENABLED_KEY,n).apply();toast("Login "+(n?"ON":"OFF"));}
            else showForgotPinDialog();
        }).setNegativeButton("Close",null).show();
    }

    private void showForgotPinDialog(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);String saved=p.getString(RECOVERY_KEY,"");
        if(saved.isEmpty()){new AlertDialog.Builder(this).setTitle("Recovery not set").setMessage("Recovery word pehle set nahi hua. App data safe rakhne ke liye PIN reset nahi kiya gaya.").setPositiveButton("OK",null).show();return;}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);EditText word=new EditText(this);word.setHint("Recovery word");EditText pin=new EditText(this);pin.setHint("New 4-digit PIN");pin.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);box.addView(word);box.addView(pin);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Recover PIN").setView(box).setPositiveButton("Reset PIN",null).setNegativeButton("Cancel",null).create();d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(!saved.equals(word.getText().toString().trim().toLowerCase(Locale.ROOT))){word.setError("Recovery word does not match");return;}String n=pin.getText().toString().trim();if(n.length()!=4){pin.setError("Enter exactly 4 digits");return;}p.edit().putString(PIN_KEY,n).putBoolean(LOGIN_ENABLED_KEY,true).apply();toast("PIN reset successful");d.dismiss();setContentView(buildUi());}));d.show();
    }

    private void showSettingsScreen(){
        Dialog d=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(16),dp(12),dp(16),dp(16));page.setBackgroundColor(isDark()?Color.rgb(25,28,31):Color.rgb(241,248,247));
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);Button back=button("‹");back.setTextSize(30);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Settings");title.setTextColor(Color.WHITE);title.setTextSize(24);title.setTypeface(Typeface.DEFAULT_BOLD);title.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(6),0,dp(8),0);head.setBackground(rounded(Color.rgb(0,91,78),18));head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);back.setOnClickListener(v->d.dismiss());
        ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(0,dp(14),0,dp(20));scroll.addView(list);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        addSettingsButton(list,"◐  Dark / Light Theme",isDark()?"Currently Dark":"Currently Light",v->{getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(DARK_KEY,!isDark()).apply();d.dismiss();recreate();});
        addSettingsButton(list,"ⓘ  Current Version","LathaBulk v"+appVersion(),v->new AlertDialog.Builder(this).setTitle("Current Version").setMessage("LathaBulk v"+appVersion()+"\nLATHAEPS SMART").setPositiveButton("OK",null).show());
        addSettingsButton(list,"👥  Contact Settings","Queue controls, Do Not Send & recipient list templates",v->{d.dismiss();showContactSettingsScreen();});
        addSettingsButton(list,"☀  Screen-off Auto Send","Keeps screen awake while bulk sending",v->showScreenOffHelp());
        addSettingsButton(list,"✉  Contact Us","lathaeps@gmail.com",v->contactSupport());
        addSettingsButton(list,"🔒  Login & Forgot PIN","Change PIN, recovery word, login ON/OFF",v->showLoginSettings());
        addSettingsButton(list,"▣  Local Backup","Save contacts, rules, images, templates & ledger to phone",v->createBackupFile());
        addSettingsButton(list,"☁  Drive Backup","Choose Google Drive in the save window",v->createDriveBackupFile());
        addSettingsButton(list,"↻  Restore Backup","Restore backup from phone or Google Drive",v->chooseRestoreFile());
        addSettingsButton(list,"🗑  Clear All Data","Remove contacts, recipient lists, catalogs, rules and files",v->confirmClearAllData(d));
        d.setContentView(page);d.show();
    }

    private void addSettingsButton(LinearLayout parent,String title,String subtitle,View.OnClickListener click){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(18),dp(13),dp(14),dp(12));card.setBackground(rounded(isDark()?Color.rgb(45,50,54):Color.WHITE,18));TextView a=new TextView(this);a.setText(title);a.setTextSize(18);a.setTypeface(Typeface.DEFAULT_BOLD);a.setTextColor(isDark()?Color.WHITE:Color.rgb(10,65,59));TextView b=new TextView(this);b.setText(subtitle);b.setTextSize(13);b.setTextColor(isDark()?Color.LTGRAY:Color.DKGRAY);b.setPadding(0,dp(4),0,0);card.addView(a);card.addView(b);card.setOnClickListener(click);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(82));lp.setMargins(0,0,0,dp(10));parent.addView(card,lp);}

    private void showContactSettingsScreen(){
        Dialog d=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(16),dp(12),dp(16),dp(16));page.setBackgroundColor(isDark()?Color.rgb(25,28,31):Color.rgb(241,248,247));
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);Button back=button("‹");back.setTextSize(30);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Contact Settings");title.setTextColor(Color.WHITE);title.setTextSize(23);title.setTypeface(Typeface.DEFAULT_BOLD);head.setPadding(dp(6),0,dp(8),0);head.setBackground(rounded(Color.rgb(25,83,155),18));head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);back.setOnClickListener(v->{d.dismiss();showSettingsScreen();});
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(0,dp(16),0,0);page.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        addSettingsButton(list,"1  Pause / Resume / Skip","Control the saved Auto Send queue",v->showQueueControls());
        addSettingsButton(list,"2  Do Not Send List",readDoNotSendNumbers().size()+" blocked numbers",v->showDoNotSendScreen());
        addSettingsButton(list,"3  Recipient List Templates","Separate message templates for every list",v->showRecipientListTemplatesScreen());
        d.setContentView(page);d.show();
    }

    private String queueStatusText(){
        try{SharedPreferences p=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE);JSONArray nums=new JSONArray(p.getString(AUTO_NUMBERS,"[]"));JSONArray names=new JSONArray(p.getString(AUTO_NAMES,"[]"));int index=p.getInt(AUTO_INDEX,0);boolean running=p.getBoolean(AUTO_RUNNING,false);if(nums.length()==0||index>=nums.length())return "No saved sending queue";String number=nums.optString(index,"");String name=index<names.length()?names.optString(index,findName(number)):findName(number);return (running?"RUNNING":"PAUSED")+"\nCurrent: "+(index+1)+" / "+nums.length()+"\n"+name+"  +"+number;}catch(Exception e){return "No saved sending queue";}
    }

    private void showQueueControls(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);TextView status=new TextView(this);status.setTextSize(17);status.setTypeface(Typeface.DEFAULT_BOLD);status.setPadding(dp(8),dp(12),dp(8),dp(16));box.addView(status);
        LinearLayout row=row();Button pause=button("PAUSE");Button resume=button("RESUME");pause.setTextColor(Color.rgb(180,80,0));resume.setTextColor(Color.rgb(0,120,60));row.addView(pause,weighted(1f,48));row.addView(resume,weighted(1f,48));box.addView(row);Button skip=button("SKIP CURRENT CONTACT");skip.setTextColor(Color.rgb(175,35,35));box.addView(skip,new LinearLayout.LayoutParams(-1,dp(48)));
        Runnable refresh=()->status.setText(queueStatusText());refresh.run();
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Sending Queue Controls").setView(box).setNegativeButton("Close",null).create();
        pause.setOnClickListener(v->{pauseCurrentQueue();refresh.run();});resume.setOnClickListener(v->{resumeQueue();refresh.run();});skip.setOnClickListener(v->{skipCurrentQueueContact();refresh.run();});d.show();
    }

    private void pauseCurrentQueue(){
        SharedPreferences p=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE);if(!p.getBoolean(AUTO_RUNNING,false)){toast("Queue already paused");return;}p.edit().putBoolean(AUTO_RUNNING,false).apply();WhatsAppAccessibilityService.releaseQueueWakeLock();getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);if(sendButton!=null)sendButton.setText("AUTO SEND TO SELECTED CONTACTS");if(miniProgress!=null)miniProgress.setText("Queue paused • Resume from Contact Settings");cancelProgressNotification();toast("Sending queue paused");
    }

    private void skipCurrentQueueContact(){
        try{SharedPreferences p=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE);JSONArray nums=new JSONArray(p.getString(AUTO_NUMBERS,"[]"));int index=p.getInt(AUTO_INDEX,0);if(nums.length()==0||index>=nums.length()){toast("No current contact to skip");return;}boolean running=p.getBoolean(AUTO_RUNNING,false);String skipped=nums.optString(index,"");int next=index+1;if(next>=nums.length()){p.edit().putInt(AUTO_INDEX,next).putBoolean(AUTO_RUNNING,false).apply();WhatsAppAccessibilityService.releaseQueueWakeLock();cancelProgressNotification();toast("Skipped +"+skipped+" • Queue finished");return;}p.edit().putInt(AUTO_INDEX,next).apply();toast("Skipped +"+skipped);if(running)WhatsAppAccessibilityService.openCurrentChat(this);}catch(Exception e){toast("Queue skip failed");}
    }

    private Set<String> readDoNotSendNumbers(){
        Set<String> out=new LinkedHashSet<>();try{JSONArray a=new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(DO_NOT_SEND_KEY,"[]"));for(int i=0;i<a.length();i++){String n=normalize(a.optString(i,""));if(!n.isEmpty())out.add(n);}}catch(Exception ignored){}return out;
    }
    private void writeDoNotSendNumbers(Set<String> numbers){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(DO_NOT_SEND_KEY,new JSONArray(numbers).toString()).apply();}
    private boolean isDoNotSend(String number){String n=normalize(number);return !n.isEmpty()&&readDoNotSendNumbers().contains(n);}

    private void showDoNotSendScreen(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),0,dp(14),0);EditText phone=new EditText(this);phone.setHint("10-digit mobile number");phone.setInputType(InputType.TYPE_CLASS_PHONE);box.addView(phone);LinearLayout actions=row();Button add=button("ADD NUMBER");Button selected=button("BLOCK SELECTED");actions.addView(add,weighted(1f,45));actions.addView(selected,weighted(1f,45));box.addView(actions);TextView count=new TextView(this);count.setTypeface(Typeface.DEFAULT_BOLD);count.setPadding(dp(4),dp(8),dp(4),dp(5));box.addView(count);ListView list=new ListView(this);box.addView(list,new LinearLayout.LayoutParams(-1,dp(330)));
        List<String> rows=new ArrayList<>();List<String> values=new ArrayList<>();Runnable[] refresh={null};refresh[0]=()->{rows.clear();values.clear();for(String n:readDoNotSendNumbers()){values.add(n);rows.add(findName(n)+"\n+"+n);}list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,rows));count.setText("Blocked contacts: "+values.size()+" • Tap to remove");};refresh[0].run();
        add.setOnClickListener(v->{String n=normalize(phone.getText().toString());if(n.isEmpty()){phone.setError("Valid mobile number required");return;}Set<String>s=readDoNotSendNumbers();s.add(n);writeDoNotSendNumbers(s);phone.setText("");refresh[0].run();toast("Added to Do Not Send");});
        selected.setOnClickListener(v->{if(selectedNumbers.isEmpty()){toast("No contacts selected");return;}Set<String>s=readDoNotSendNumbers();int before=s.size();for(String n:selectedNumbers){String x=normalize(n);if(!x.isEmpty())s.add(x);}writeDoNotSendNumbers(s);refresh[0].run();toast((s.size()-before)+" selected contacts blocked");});
        list.setOnItemClickListener((p,v,pos,id)->{String n=values.get(pos);new AlertDialog.Builder(this).setTitle("Remove from Do Not Send?").setMessage(findName(n)+"\n+"+n).setPositiveButton("Remove",(d,w)->{Set<String>s=readDoNotSendNumbers();s.remove(n);writeDoNotSendNumbers(s);refresh[0].run();}).setNegativeButton("Cancel",null).show();});
        new AlertDialog.Builder(this).setTitle("Do Not Send List").setView(box).setNegativeButton("Close",null).show();
    }

    private JSONObject readRecipientListTemplates(){try{return new JSONObject(getSharedPreferences(PREFS,MODE_PRIVATE).getString(LIST_TEMPLATES_KEY,"{}"));}catch(Exception e){return new JSONObject();}}
    private JSONArray templatesForList(String listName){JSONObject root=readRecipientListTemplates();JSONArray a=root.optJSONArray(listName);return a==null?new JSONArray():a;}
    private void writeTemplatesForList(String listName,JSONArray templates){try{JSONObject root=readRecipientListTemplates();root.put(listName,templates);getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(LIST_TEMPLATES_KEY,root.toString()).apply();}catch(Exception e){toast("Template save failed");}}

    private void showRecipientListTemplatesScreen(){
        Map<String,List<String>> groups=readGroups();if(groups.isEmpty()){toast("Create a Recipient List first");return;}List<String> groupNames=new ArrayList<>(groups.keySet());LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),0,dp(14),0);Spinner group=new Spinner(this);group.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,groupNames));EditText name=new EditText(this);name.setHint("Template name");EditText body=new EditText(this);body.setHint("Message for this Recipient List");body.setMinLines(3);body.setGravity(Gravity.TOP);Button save=button("SAVE LIST TEMPLATE");save.setTextColor(Color.rgb(0,110,60));TextView help=new TextView(this);help.setText("Saved template Recipient List ke SEND window me available hoga.");help.setTextSize(12);ListView list=new ListView(this);box.addView(group);box.addView(name);box.addView(body);box.addView(save,new LinearLayout.LayoutParams(-1,dp(46)));box.addView(help);box.addView(list,new LinearLayout.LayoutParams(-1,dp(250)));
        List<String> rows=new ArrayList<>();Runnable[] refresh={null};refresh[0]=()->{String g=groupNames.get(group.getSelectedItemPosition());JSONArray a=templatesForList(g);rows.clear();for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null)rows.add(o.optString("name","Template "+(i+1))+"\n"+o.optString("body",""));}list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,rows));};refresh[0].run();
        group.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refresh[0].run();}public void onNothingSelected(android.widget.AdapterView<?> p){}});
        save.setOnClickListener(v->{String g=groupNames.get(group.getSelectedItemPosition());String n=name.getText().toString().trim();String b=body.getText().toString().trim();if(b.isEmpty()){body.setError("Message required");return;}if(n.isEmpty())n="Template "+(templatesForList(g).length()+1);try{JSONArray a=templatesForList(g);JSONObject o=new JSONObject();o.put("name",n);o.put("body",b);a.put(o);writeTemplatesForList(g,a);name.setText("");body.setText("");refresh[0].run();toast("Template saved for "+g);}catch(Exception e){toast("Template save failed");}});
        list.setOnItemClickListener((p,v,pos,id)->{String g=groupNames.get(group.getSelectedItemPosition());JSONArray a=templatesForList(g);JSONObject o=a.optJSONObject(pos);if(o==null)return;String[] options={"Load on main message box","Delete template"};new AlertDialog.Builder(this).setTitle(o.optString("name","Template")).setItems(options,(d,which)->{if(which==0){messageBox.setText(o.optString("body",""));messageBox.setSelection(messageBox.length());toast("Template loaded");}else{JSONArray out=new JSONArray();for(int i=0;i<a.length();i++)if(i!=pos)out.put(a.opt(i));writeTemplatesForList(g,out);refresh[0].run();}}).setNegativeButton("Cancel",null).show();});
        new AlertDialog.Builder(this).setTitle("Recipient List Templates").setView(box).setNegativeButton("Close",null).show();
    }

    private void showCatalogSearchFilterScreen(){
        Dialog d=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(14),dp(12),dp(14),dp(12));page.setBackgroundColor(Color.rgb(20,20,22));LinearLayout head=row();Button back=button("‹");back.setTextSize(30);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Catalog Search & Filter");title.setTextSize(22);title.setTextColor(Color.WHITE);title.setTypeface(Typeface.DEFAULT_BOLD);head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(56)));head.addView(title,new LinearLayout.LayoutParams(0,dp(56),1f));page.addView(head);EditText search=new EditText(this);search.setHint("Search name, type or auto-reply word");search.setTextColor(Color.WHITE);search.setHintTextColor(Color.GRAY);page.addView(search,new LinearLayout.LayoutParams(-1,dp(50)));
        JSONArray catalogs=readCatalogs();List<String> categories=new ArrayList<>();categories.add("All Types");for(int i=0;i<catalogs.length();i++){JSONObject o=catalogs.optJSONObject(i);if(o!=null&&!categories.contains(o.optString("category","Other")))categories.add(o.optString("category","Other"));}Spinner filter=new Spinner(this);filter.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,categories));page.addView(filter,new LinearLayout.LayoutParams(-1,dp(48)));ScrollView scroll=new ScrollView(this);LinearLayout cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);cards.setPadding(0,dp(10),0,dp(30));scroll.addView(cards);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));Runnable refresh=()->renderCatalogSearchResults(cards,search.getText().toString(),categories.get(filter.getSelectedItemPosition()));refresh.run();search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh.run();}public void afterTextChanged(Editable e){}});filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refresh.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}});back.setOnClickListener(v->d.dismiss());d.setContentView(page);d.show();
    }

    private void renderCatalogSearchResults(LinearLayout parent,String query,String category){
        parent.removeAllViews();String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);JSONArray a=readCatalogs();int found=0;for(int i=0;i<a.length();i++){JSONObject item=a.optJSONObject(i);if(item==null)continue;String c=item.optString("category","Other");String hay=(item.optString("name","")+" "+c+" "+item.optString("keywords","")+" "+item.optString("original_name","")).toLowerCase(Locale.ROOT);if(!"All Types".equals(category)&&!category.equals(c))continue;if(!q.isEmpty()&&!hay.contains(q))continue;found++;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(12),dp(16),dp(12));card.setBackground(rounded(Color.rgb(43,43,47),14));TextView name=new TextView(this);name.setText(item.optString("name","Catalog"));name.setTextSize(19);name.setTextColor(Color.WHITE);name.setTypeface(Typeface.DEFAULT_BOLD);TextView detail=new TextView(this);detail.setText(c+" • "+(item.optString("type","").contains("pdf")?"PDF":"Picture")+"\nWords: "+item.optString("keywords",""));detail.setTextColor(Color.LTGRAY);detail.setTextSize(13);detail.setPadding(0,dp(5),0,0);card.addView(name);card.addView(detail);card.setOnClickListener(v->openCatalog(item));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(92));lp.setMargins(0,0,0,dp(10));parent.addView(card,lp);}if(found==0){TextView empty=new TextView(this);empty.setText("No Catalog found");empty.setTextColor(Color.LTGRAY);empty.setGravity(Gravity.CENTER);empty.setTextSize(17);parent.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));}
    }
    private String appVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "3.20.5";}}
    private void showScreenOffHelp(){
        new AlertDialog.Builder(this).setTitle("Screen-off Auto Send")
            .setMessage("Auto Send screen ko awake rakhega. Phone pehle se lock ho to screen ON hogi. Swipe lock automatically dismiss hoga.\n\nPIN, pattern ya fingerprint lock par Android unlock prompt dikhayega; unlock karte hi pending Bulk/Catalog queue automatically continue hogi. Security lock ko app bypass nahi kar sakta.")
            .setPositiveButton("Display Settings",(d,w)->{try{startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));}catch(Exception e){toast("Display settings open nahi hui");}})
            .setNegativeButton("Close",null).show();
    }
    private void contactSupport(){
        try{
            Intent i=new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:lathaeps@gmail.com"));
            i.putExtra(Intent.EXTRA_SUBJECT,"LathaBulk Support • v"+appVersion());
            startActivity(Intent.createChooser(i,"Contact LATHAEPS"));
        }catch(Exception e){toast("Email: lathaeps@gmail.com");}
    }
    private void createDriveBackupFile(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"LathaBulk_Drive_Backup_"+new java.text.SimpleDateFormat("yyyyMMdd_HHmm",Locale.getDefault()).format(new java.util.Date())+".json");startActivityForResult(Intent.createChooser(i,"Choose Google Drive and save backup"),CREATE_BACKUP);}
    private void confirmClearAllData(Dialog settings){new AlertDialog.Builder(this).setTitle("Clear all app data?").setMessage("Contacts, recipient lists, catalogs, auto-reply rules, images, templates, ledgers and history delete honge. Login PIN aur recovery word safe rahenge.").setPositiveButton("Clear All",(d,w)->{clearAllUserData();settings.dismiss();recreate();}).setNegativeButton("Cancel",null).show();}
    private void clearAllUserData(){SharedPreferences main=getSharedPreferences(PREFS,MODE_PRIVATE);String pin=main.getString(PIN_KEY,"");String recovery=main.getString(RECOVERY_KEY,"");boolean login=main.getBoolean(LOGIN_ENABLED_KEY,true);boolean dark=main.getBoolean(DARK_KEY,false);main.edit().clear().putString(PIN_KEY,pin).putString(RECOVERY_KEY,recovery).putBoolean(LOGIN_ENABLED_KEY,login).putBoolean(DARK_KEY,dark).apply();getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().clear().apply();getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).edit().clear().apply();deleteAppFiles(getFilesDir());selectedNumbers.clear();allContacts.clear();visibleContacts.clear();toast("All data cleared • PIN kept safe");}
    private void deleteAppFiles(File dir){File[] files=dir.listFiles();if(files==null)return;for(File f:files){if(f.isDirectory())deleteAppFiles(f);f.delete();}}

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
        try{JSONObject root=new JSONObject();root.put("app","LathaBulk");root.put("version",appVersion());root.put("created",System.currentTimeMillis());root.put(PREFS,prefsToJson(PREFS));root.put(AUTO_PREFS,prefsToJson(AUTO_PREFS));root.put(AutoReplyNotificationService.PREFS,prefsToJson(AutoReplyNotificationService.PREFS));JSONObject files=new JSONObject();addFilesToBackup(getFilesDir(),"",files);root.put("files",files);try(OutputStream out=getContentResolver().openOutputStream(uri)){out.write(root.toString(2).getBytes(StandardCharsets.UTF_8));}toast("Backup saved • recipient lists, catalogs, images, keywords & templates included");}catch(Exception e){toast("Backup failed: "+e.getMessage());}
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
    private void showScheduleDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        EditText hours=new EditText(this);hours.setHint("Hours • 0 to 23");hours.setInputType(InputType.TYPE_CLASS_NUMBER);hours.setText("0");
        EditText mins=new EditText(this);mins.setHint("Minutes • 0 to 59");mins.setInputType(InputType.TYPE_CLASS_NUMBER);mins.setText("5");
        TextView note=new TextView(this);note.setText("Countdown main screen par HH:MM:SS me dikhega. Timer complete hote hi Auto Send start hoga. Phone unlocked aur Accessibility ON rakhein.");note.setTextSize(13);note.setPadding(0,dp(8),0,0);
        box.addView(hours);box.addView(mins);box.addView(note);
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle("Schedule Timer").setView(box)
            .setPositiveButton("Start Timer",(d,w)->{try{int h=Integer.parseInt(hours.getText().toString().trim());int m=Integer.parseInt(mins.getText().toString().trim());if(h<0||h>23||m<0||m>59||(h==0&&m==0)){toast("Valid timer enter karein");return;}long at=System.currentTimeMillis()+((h*60L+m)*60_000L);getSharedPreferences(PREFS,MODE_PRIVATE).edit().putLong(SCHEDULE_AT_KEY,at).apply();startScheduleCountdown(at);toast("Timer started • "+formatCountdown(at-System.currentTimeMillis()));}catch(Exception e){toast("Hours aur minutes enter karein");}})
            .setNegativeButton("Close",null);
        if(getSharedPreferences(PREFS,MODE_PRIVATE).getLong(SCHEDULE_AT_KEY,0L)>System.currentTimeMillis())b.setNeutralButton("Cancel Timer",(d,w)->cancelScheduleTimer());
        b.show();
    }
    private void restoreScheduledTimer(){long at=getSharedPreferences(PREFS,MODE_PRIVATE).getLong(SCHEDULE_AT_KEY,0L);if(at>0)startScheduleCountdown(at);}
    private void startScheduleCountdown(long at){
        if(scheduleTicker!=null)uiHandler.removeCallbacks(scheduleTicker);
        scheduleTicker=new Runnable(){@Override public void run(){
            long left=at-System.currentTimeMillis();
            if(left<=0){
                getSharedPreferences(PREFS,MODE_PRIVATE).edit().remove(SCHEDULE_AT_KEY).apply();
                if(scheduleButton!=null)scheduleButton.setText("Schedule");
                if(miniProgress!=null)miniProgress.setText("Timer complete • Starting Auto Send");
                scheduleTicker=null;
                if(!getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getBoolean(AUTO_RUNNING,false))startOrStopAutoSend(false);
                return;
            }
            String time=formatCountdown(left);
            if(scheduleButton!=null)scheduleButton.setText("Timer "+time);
            if(miniProgress!=null)miniProgress.setText("Scheduled Auto Send • "+time+" remaining");
            uiHandler.postDelayed(this,1000L);
        }};
        uiHandler.post(scheduleTicker);
    }
    private void cancelScheduleTimer(){
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().remove(SCHEDULE_AT_KEY).apply();
        if(scheduleTicker!=null)uiHandler.removeCallbacks(scheduleTicker);
        scheduleTicker=null;
        if(scheduleButton!=null)scheduleButton.setText("Schedule");
        if(miniProgress!=null)miniProgress.setText("Schedule timer cancelled");
        toast("Timer cancelled");
    }
    private String formatCountdown(long millis){long total=Math.max(0,millis/1000L);long h=total/3600L;long m=(total%3600L)/60L;long s=total%60L;return String.format(Locale.US,"%02d:%02d:%02d",h,m,s);}
    private void showHistory(){String h=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getString(AUTO_HISTORY,"No sending history yet");new AlertDialog.Builder(this).setTitle("Sending history").setMessage(h).setPositiveButton("Close",null).setNeutralButton("Clear",(d,w)->getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().remove(AUTO_HISTORY).apply()).show();}
    private void retryFailed(){try{JSONArray f=new JSONArray(getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).getString(AUTO_FAILED,"[]"));if(f.length()==0){toast("No failed contacts");return;}selectedNumbers.clear();for(int i=0;i<f.length();i++)selectedNumbers.add(f.getString(i));refreshChecks();toast(f.length()+" failed contacts selected");}catch(Exception e){toast("No failed contacts");}}
    private void resumeQueue(){SharedPreferences p=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE);try{JSONArray a=new JSONArray(p.getString(AUTO_NUMBERS,"[]"));int i=p.getInt(AUTO_INDEX,0);if(a.length()==0||i>=a.length()){toast("No paused queue");return;}p.edit().putBoolean(AUTO_RUNNING,true).apply();sendButton.setText("STOP AUTO SENDING");miniProgress.setText("Resuming "+(i+1)+" / "+a.length());WhatsAppAccessibilityService.openCurrentChat(this);}catch(Exception e){toast("No paused queue");}}


    private void openPhoneGalleryFirst(){openPhoneGallery(PICK_REPLY_IMAGE);}

    private void openPhoneGallery(int requestCode){
        try{
            // ACTION_PICK opens the phone manufacturer's Gallery / Albums app first
            // (including Vivo Albums) and grants temporary read access to the result.
            Intent i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(i,requestCode);
        }catch(Exception first){
            try{
                Intent fallback;
                if(Build.VERSION.SDK_INT>=33){
                    fallback=new Intent(MediaStore.ACTION_PICK_IMAGES);
                    fallback.setType("image/*");
                }else{
                    fallback=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    fallback.setType("image/*");
                    fallback.addCategory(Intent.CATEGORY_OPENABLE);
                }
                fallback.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(fallback,requestCode);
            }catch(Exception second){
                try{
                    Intent files=new Intent(Intent.ACTION_GET_CONTENT);
                    files.setType("image/*");
                    files.addCategory(Intent.CATEGORY_OPENABLE);
                    files.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(Intent.createChooser(files,"Select image from Phone Gallery / Albums"),requestCode);
                }catch(Exception e){toast("Phone Gallery open nahi hui");}
            }
        }
    }

    private void openCatalogPhoneGallery(){
        persistPendingCatalog();
        try{
            Intent i;
            if(Build.VERSION.SDK_INT>=33){
                i=new Intent(MediaStore.ACTION_PICK_IMAGES);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                i.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX,Math.min(50,MediaStore.getPickImagesMaxLimit()));
            }else{
                i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            }
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(i,PICK_CATALOG_FILE);
        }catch(Exception first){
            try{
                Intent fallback=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                fallback.setType("image/*");
                fallback.addCategory(Intent.CATEGORY_OPENABLE);
                fallback.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                fallback.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(Intent.createChooser(fallback,"Phone Gallery / Albums • select pictures"),PICK_CATALOG_FILE);
            }catch(Exception e){toast("Phone Gallery open nahi hui");}
        }
    }

    private void chooseCatalogSource(){
        String[] choices={"Phone Gallery Images","PDF / Files"};
        new AlertDialog.Builder(this).setTitle("Add Catalog Files").setItems(choices,(d,which)->{
            if(which==0)openCatalogPhoneGallery();else pickBusinessFile(PICK_CATALOG_FILE);
        }).setNegativeButton("Cancel",null).show();
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
            SharedPreferences.Editor ed=bp.edit().putString(key,safe.toString()).putString(key+"_type",type==null?"application/octet-stream":type).putString(key+"_name",display).putLong(key+"_updated",System.currentTimeMillis()).putBoolean(AutoReplyNotificationService.ENABLED,true);
            String history=bp.getString("file_history",""); String line=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date())+" • "+message+" • "+display; ed.putString("file_history",line+(history.isEmpty()?"":"\n"+history)).apply();
            toast(message+" ✓");
        }catch(Exception e){toast(message+" failed • file dobara select karein");}
    }

    private void pickBusinessFile(int code){
        if(code==PICK_CATALOG_FILE)persistPendingCatalog();
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);
        if(code==PICK_LEDGER_FILE)i.setType("application/pdf");else{i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/pdf","image/jpeg","image/png","image/webp"});}
        if(code==PICK_CATALOG_FILE)i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(i,code==PICK_CATALOG_FILE?"Select one or multiple PDFs / pictures":"Select PDF or image"),code);
    }

    private void persistPendingCatalog(){
        getSharedPreferences(PREFS,MODE_PRIVATE).edit()
                .putString(PENDING_CATALOG_NAME_KEY,pendingCatalogName)
                .putString(PENDING_CATALOG_CATEGORY_KEY,pendingCatalogCategory)
                .putString(PENDING_CATALOG_KEYWORDS_KEY,pendingCatalogKeywords).apply();
    }
    private void restorePendingCatalog(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        if(pendingCatalogName.trim().isEmpty())pendingCatalogName=p.getString(PENDING_CATALOG_NAME_KEY,"");
        if(pendingCatalogCategory.trim().isEmpty())pendingCatalogCategory=p.getString(PENDING_CATALOG_CATEGORY_KEY,"");
        if(pendingCatalogKeywords.trim().isEmpty())pendingCatalogKeywords=p.getString(PENDING_CATALOG_KEYWORDS_KEY,"");
    }
    private void clearPendingCatalog(){
        pendingCatalogName="";pendingCatalogCategory="";pendingCatalogKeywords="";
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().remove(PENDING_CATALOG_NAME_KEY)
                .remove(PENDING_CATALOG_CATEGORY_KEY).remove(PENDING_CATALOG_KEYWORDS_KEY).apply();
    }

    private JSONArray readCatalogs(){
        try{
            SharedPreferences main=getSharedPreferences(PREFS,MODE_PRIVATE);JSONArray items=new JSONArray(main.getString(CATALOG_ITEMS_KEY,"[]"));
            if(items.length()==0){SharedPreferences old=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);String uri=old.getString(AutoReplyNotificationService.CATALOG_URI,"");if(!uri.isEmpty()){JSONObject item=new JSONObject();item.put("name",old.getString(AutoReplyNotificationService.CATALOG_URI+"_name","Saved Catalog"));item.put("uri",uri);item.put("type",old.getString(AutoReplyNotificationService.CATALOG_URI+"_type","application/pdf"));item.put("updated",old.getLong(AutoReplyNotificationService.CATALOG_URI+"_updated",System.currentTimeMillis()));items.put(item);main.edit().putString(CATALOG_ITEMS_KEY,items.toString()).apply();}}
            return items;
        }catch(Exception e){return new JSONArray();}
    }
    private void writeCatalogs(JSONArray items){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(CATALOG_ITEMS_KEY,items.toString()).apply();}
    private void saveCatalogSelections(Intent data){
        restorePendingCatalog();
        List<Uri> selected=new ArrayList<>();
        android.content.ClipData clips=data.getClipData();
        if(clips!=null){for(int i=0;i<clips.getItemCount();i++){Uri uri=clips.getItemAt(i).getUri();if(uri!=null)selected.add(uri);}}
        else if(data.getData()!=null)selected.add(data.getData());
        if(selected.isEmpty()){toast("No Catalog file selected");return;}
        int saved=0;
        for(int i=0;i<selected.size();i++)if(saveCatalog(selected.get(i),i,selected.size()))saved++;
        String category=pendingCatalogCategory.trim().isEmpty()?"Other":pendingCatalogCategory.trim();
        clearPendingCatalog();
        toast(saved+" / "+selected.size()+" "+category+" files saved ✓");
        uiHandler.postDelayed(this::showCatalogScreen,250);
    }
    private boolean saveCatalog(Uri source,int selectedIndex,int selectedTotal){
        try{
            String type=resolveCatalogMime(source);String ext=type.contains("pdf")?"pdf":type.contains("png")?"png":type.contains("webp")?"webp":"jpg";
            File dir=new File(getFilesDir(),"catalogs");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Folder create failed");File target=new File(dir,"catalog_"+System.currentTimeMillis()+"_"+selectedIndex+"."+ext);
            try(InputStream in=getContentResolver().openInputStream(source);OutputStream out=new FileOutputStream(target)){if(in==null)throw new Exception("File read failed");byte[] buf=new byte[16*1024];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}
            Uri safe=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",target);String original=source.getLastPathSegment()==null?target.getName():source.getLastPathSegment();String baseName=pendingCatalogName.trim().isEmpty()?original:pendingCatalogName.trim();String display=selectedTotal>1?baseName+" • "+(selectedIndex+1):baseName;String category=pendingCatalogCategory.trim().isEmpty()?"Other":pendingCatalogCategory.trim();String keywords=pendingCatalogKeywords.trim();JSONArray a=readCatalogs();JSONObject item=new JSONObject();item.put("name",display);item.put("category",category);item.put("keywords",keywords);item.put("original_name",original);item.put("uri",safe.toString());item.put("type",type);item.put("updated",System.currentTimeMillis());item.put("path",target.getAbsolutePath());a.put(item);writeCatalogs(a);getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).edit().putBoolean(AutoReplyNotificationService.ENABLED,true).apply();return true;
        }catch(Exception e){return false;}
    }
    private String resolveCatalogMime(Uri source){
        String type=getContentResolver().getType(source);if(type!=null&&!type.trim().isEmpty())return type;
        String name=source.getLastPathSegment()==null?"":source.getLastPathSegment().toLowerCase(Locale.ROOT);
        if(name.endsWith(".pdf"))return "application/pdf";if(name.endsWith(".png"))return "image/png";if(name.endsWith(".webp"))return "image/webp";return "image/jpeg";
    }
    private void showCatalogScreen(){
        Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(Color.BLACK);
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(10),0,dp(12),0);head.setBackgroundColor(Color.rgb(28,26,27));Button back=button("‹");back.setTextSize(36);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Catalog");title.setTextSize(25);title.setTypeface(Typeface.DEFAULT_BOLD);title.setTextColor(Color.WHITE);head.addView(back,new LinearLayout.LayoutParams(dp(56),dp(72)));head.addView(title,new LinearLayout.LayoutParams(0,dp(72),1f));page.addView(head);
        EditText search=new EditText(this);search.setHint("Search Catalog name, type or auto-reply word");search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(Color.GRAY);search.setPadding(dp(16),0,dp(16),0);page.addView(search,new LinearLayout.LayoutParams(-1,dp(50)));
        JSONArray saved=readCatalogs();List<String> categories=new ArrayList<>();categories.add("All Types");for(int i=0;i<saved.length();i++){JSONObject o=saved.optJSONObject(i);if(o!=null&&!categories.contains(o.optString("category","Other")))categories.add(o.optString("category","Other"));}Spinner filter=new Spinner(this);filter.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,categories));page.addView(filter,new LinearLayout.LayoutParams(-1,dp(46)));
        ScrollView scroll=new ScrollView(this);LinearLayout cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);cards.setPadding(dp(16),dp(16),dp(16),dp(100));scroll.addView(cards);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));Button plus=button("+");plus.setTextSize(34);plus.setTextColor(Color.WHITE);plus.setBackground(rounded(Color.rgb(20,112,210),60));LinearLayout foot=row();foot.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);foot.setPadding(0,0,dp(18),dp(14));foot.addView(plus,new LinearLayout.LayoutParams(dp(70),dp(70)));page.addView(foot,new LinearLayout.LayoutParams(-1,dp(88)));
        Runnable refresh=()->{String q=search.getText().toString().trim();String c=categories.get(filter.getSelectedItemPosition());if(q.isEmpty()&&"All Types".equals(c))renderCatalogCards(cards,dialog);else renderCatalogSearchResults(cards,q,c);};refresh.run();search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh.run();}public void afterTextChanged(Editable e){}});filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refresh.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}});back.setOnClickListener(v->dialog.dismiss());plus.setOnClickListener(v->{dialog.dismiss();showAddCatalogDialog();});dialog.setContentView(page);dialog.show();
    }
    private void showAddCatalogDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        EditText category=new EditText(this);category.setHint("Type • Wires / Switch / DB");category.setSingleLine(true);
        EditText name=new EditText(this);name.setHint("Catalog name");name.setSingleLine(true);
        EditText words=new EditText(this);words.setHint("Auto reply words • wires, cable");words.setSingleLine(true);
        TextView help=new TextView(this);help.setText("Ek saath multiple pictures aur PDFs select kar sakte hain. Sab files isi type ki list me save hongi.");help.setTextSize(13);help.setPadding(0,dp(8),0,0);
        box.addView(category);box.addView(name);box.addView(words);box.addView(help);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Add Catalog Type").setView(box).setPositiveButton("PDF / FILES",null).setNeutralButton("PHONE GALLERY",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->{
            View.OnClickListener select=v->{String c=category.getText().toString().trim();String n=name.getText().toString().trim();String k=words.getText().toString().trim();if(c.isEmpty()){category.setError("Enter Wires, Switch, DB or another type");return;}if(n.isEmpty()){name.setError("Enter catalog name");return;}if(k.isEmpty())k=c.toLowerCase(Locale.ROOT);pendingCatalogCategory=c;pendingCatalogName=n;pendingCatalogKeywords=k;boolean gallery=v==d.getButton(AlertDialog.BUTTON_NEUTRAL);d.dismiss();if(gallery)openCatalogPhoneGallery();else pickBusinessFile(PICK_CATALOG_FILE);};
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(select);
            d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(select);
        });d.show();
    }
    private void renderCatalogCards(LinearLayout parent,Dialog dialog){
        parent.removeAllViews();JSONArray a=readCatalogs();if(a.length()==0){TextView empty=new TextView(this);empty.setText("No Catalog saved\nTap + to add PDF or image");empty.setTextColor(Color.LTGRAY);empty.setGravity(Gravity.CENTER);empty.setTextSize(17);parent.addView(empty,new LinearLayout.LayoutParams(-1,dp(160)));return;}
        Set<String> categories=new LinkedHashSet<>();for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null)categories.add(x.optString("category","Other"));}
        for(String category:categories){
            LinearLayout sectionHead=row();sectionHead.setGravity(Gravity.CENTER_VERTICAL);
            TextView heading=new TextView(this);heading.setText(category.toUpperCase(Locale.ROOT)+"  •  "+countCatalogCategory(a,category)+" files");heading.setTextColor(Color.rgb(80,170,255));heading.setTextSize(18);heading.setTypeface(Typeface.DEFAULT_BOLD);heading.setPadding(dp(3),dp(9),dp(3),dp(9));
            Button edit=button("EDIT");edit.setTextSize(12);edit.setTextColor(Color.WHITE);edit.setBackground(rounded(Color.rgb(25,105,185),12));
            sectionHead.addView(heading,new LinearLayout.LayoutParams(0,dp(48),1f));sectionHead.addView(edit,new LinearLayout.LayoutParams(dp(72),dp(38)));parent.addView(sectionHead);
            edit.setOnClickListener(v->{dialog.dismiss();showEditCatalogType(category);});
            for(int i=0;i<a.length();i++){
                JSONObject item=a.optJSONObject(i);if(item==null||!category.equals(item.optString("category","Other")))continue;final int index=i;
                LinearLayout card=row();card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(16),dp(10),dp(8),dp(10));card.setBackground(rounded(Color.rgb(42,42,42),14));
                LinearLayout wordBox=new LinearLayout(this);wordBox.setOrientation(LinearLayout.VERTICAL);TextView n=new TextView(this);n.setText(item.optString("name","Catalog "+(i+1)));n.setTextColor(Color.WHITE);n.setTextSize(19);n.setTypeface(Typeface.DEFAULT_BOLD);
                TextView detail=new TextView(this);String mime=item.optString("type","").contains("pdf")?"PDF":"Picture";detail.setText(mime+"  •  words: "+item.optString("keywords",category.toLowerCase(Locale.ROOT)));detail.setTextColor(Color.LTGRAY);detail.setTextSize(13);detail.setPadding(0,dp(5),0,0);wordBox.addView(n);wordBox.addView(detail);
                Button more=button("⋮");more.setTextSize(29);more.setTextColor(Color.rgb(25,118,210));more.setBackgroundColor(Color.TRANSPARENT);card.addView(wordBox,new LinearLayout.LayoutParams(0,dp(78),1f));card.addView(more,new LinearLayout.LayoutParams(dp(52),dp(68)));card.setOnClickListener(v->openCatalog(item));more.setOnClickListener(v->showCatalogMenu(more,index,item,dialog));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(102));lp.setMargins(0,0,0,dp(10));parent.addView(card,lp);
            }
        }
    }
    private int countCatalogCategory(JSONArray a,String category){int count=0;for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null&&category.equals(x.optString("category","Other")))count++;}return count;}
    private void showEditCatalogType(String oldCategory){
        JSONArray items=readCatalogs();String savedWords="";for(int i=0;i<items.length();i++){JSONObject x=items.optJSONObject(i);if(x!=null&&oldCategory.equals(x.optString("category","Other"))){savedWords=x.optString("keywords",oldCategory.toLowerCase(Locale.ROOT));break;}}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        EditText category=new EditText(this);category.setHint("Catalog type");category.setSingleLine(true);category.setText(oldCategory);
        EditText words=new EditText(this);words.setHint("Auto reply words • wires, cable");words.setSingleLine(true);words.setText(savedWords);
        EditText addName=new EditText(this);addName.setHint("Name for newly added files");addName.setSingleLine(true);addName.setText(oldCategory+" Catalog");
        TextView help=new TextView(this);help.setText(countCatalogCategory(items,oldCategory)+" files saved\nSave se type/keywords sab files par update honge. Add More se isi list me new PDFs/pictures judenge.");help.setTextSize(13);help.setPadding(0,dp(8),0,0);
        box.addView(category);box.addView(words);box.addView(addName);box.addView(help);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Edit Catalog Type").setView(box).setPositiveButton("Save Changes",null).setNeutralButton("Add More Files",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->{
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String c=category.getText().toString().trim();String k=words.getText().toString().trim();if(c.isEmpty()){category.setError("Catalog type required");return;}if(k.isEmpty())k=c.toLowerCase(Locale.ROOT);if(updateCatalogType(oldCategory,c,k)){toast("Catalog type updated ✓");d.dismiss();showCatalogScreen();}});
            d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{String c=category.getText().toString().trim();String k=words.getText().toString().trim();String n=addName.getText().toString().trim();if(c.isEmpty()){category.setError("Catalog type required");return;}if(n.isEmpty()){addName.setError("New files name required");return;}if(k.isEmpty())k=c.toLowerCase(Locale.ROOT);if(!updateCatalogType(oldCategory,c,k))return;pendingCatalogCategory=c;pendingCatalogKeywords=k;pendingCatalogName=n;d.dismiss();chooseCatalogSource();});
        });d.show();
    }
    private boolean updateCatalogType(String oldCategory,String newCategory,String keywords){
        try{JSONArray a=readCatalogs();for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null&&oldCategory.equals(x.optString("category","Other"))){x.put("category",newCategory);x.put("keywords",keywords);x.put("updated",System.currentTimeMillis());}}writeCatalogs(a);return true;}catch(Exception e){toast("Catalog edit failed");return false;}
    }
    private void showCatalogMenu(View anchor,int index,JSONObject item,Dialog parent){PopupMenu m=new PopupMenu(this,anchor);m.getMenu().add("View");m.getMenu().add("Send on WhatsApp");m.getMenu().add("Rename");m.getMenu().add("Delete");m.setOnMenuItemClickListener(menu->{String x=menu.getTitle().toString();if(x.equals("View"))openCatalog(item);else if(x.startsWith("Send"))shareCatalog(item);else if(x.equals("Rename")){parent.dismiss();renameCatalog(index,item);}else{deleteCatalog(index,item);parent.dismiss();showCatalogScreen();}return true;});m.show();}
    private void openCatalog(JSONObject item){try{Uri uri=Uri.parse(item.optString("uri"));Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(uri,item.optString("type","application/pdf"));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Open Catalog"));}catch(Exception e){toast("Catalog open nahi hua");}}
    private void shareCatalog(JSONObject item){
        try{
            Uri uri=Uri.parse(item.optString("uri"));Intent i=new Intent(Intent.ACTION_SEND);i.setType(item.optString("type","application/pdf"));i.putExtra(Intent.EXTRA_STREAM,uri);i.putExtra(Intent.EXTRA_TEXT,"LATHA EPS "+item.optString("category","Catalog")+" • "+item.optString("name","Catalog"));i.setClipData(android.content.ClipData.newRawUri("catalog file",uri));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            grantUriPermission("com.whatsapp",uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);grantUriPermission("com.whatsapp.w4b",uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try{i.setPackage("com.whatsapp");startActivity(i);}catch(Exception normal){try{i.setPackage("com.whatsapp.w4b");startActivity(i);}catch(Exception business){i.setPackage(null);startActivity(Intent.createChooser(i,"Send Catalog"));}}
        }catch(Exception e){toast("Catalog send nahi hua • file dobara save karein");}
    }

    private void showWhatsAppBroadcastScreen(){
        Dialog d=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(14),dp(10),dp(14),dp(14));page.setBackgroundColor(Color.rgb(240,248,244));
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.setBackground(rounded(Color.rgb(18,128,78),17));
        Button back=button("‹");back.setTextSize(31);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);
        TextView title=new TextView(this);title.setText("WhatsApp Broadcast");title.setTextSize(23);title.setTypeface(Typeface.DEFAULT_BOLD);title.setTextColor(Color.WHITE);
        head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        EditText listName=new EditText(this);listName.setHint("Exact WhatsApp Broadcast List name");listName.setSingleLine(true);listName.setText(p.getString(BROADCAST_LIST_NAME,"LATHA CUSTOMERS"));page.addView(listName,new LinearLayout.LayoutParams(-1,dp(52)));
        EditText message=new EditText(this);message.setHint("One message for the complete broadcast list");message.setMinLines(4);message.setGravity(Gravity.TOP);message.setText(p.getString(BROADCAST_MESSAGE,""));page.addView(message,new LinearLayout.LayoutParams(-1,dp(116)));
        LinearLayout tools=row();Button contacts=button("PHONE CONTACT LISTS");Button manage=button("OPEN WHATSAPP");tools.addView(contacts,weighted(1f,44));tools.addView(manage,weighted(1f,44));page.addView(tools);
        Button attachment=button("ADD IMAGE / PDF (OPTIONAL)");attachment.setTextColor(Color.rgb(20,80,145));page.addView(attachment,new LinearLayout.LayoutParams(-1,dp(46)));
        broadcastFileLabel=new TextView(this);broadcastFileLabel.setText(broadcastFileUri==null?"No attachment selected":"Attachment ready ✓");broadcastFileLabel.setGravity(Gravity.CENTER);broadcastFileLabel.setTextSize(13);page.addView(broadcastFileLabel,new LinearLayout.LayoutParams(-1,dp(32)));
        TextView help=new TextView(this);help.setText("One-time setup: same exact list WhatsApp Business me pehle create honi chahiye. Broadcast sirf un customers ko deliver hota hai jinhone aapka number save kiya hai. App list ko ek hi baar open karke send karega.");help.setTextSize(13);help.setTextColor(Color.DKGRAY);help.setPadding(dp(5),dp(8),dp(5),dp(10));page.addView(help,new LinearLayout.LayoutParams(-1,0,1f));
        Button send=button("SEND ONCE TO WHATSAPP BROADCAST");send.setTypeface(Typeface.DEFAULT_BOLD);send.setTextColor(Color.WHITE);send.setBackground(rounded(Color.rgb(20,135,75),15));page.addView(send,new LinearLayout.LayoutParams(-1,dp(54)));
        back.setOnClickListener(v->d.dismiss());
        contacts.setOnClickListener(v->{d.dismiss();showRecipientListsScreen();});
        manage.setOnClickListener(v->openWhatsAppHome("WhatsApp me 3 dots → New broadcast se list create/manage karein"));
        attachment.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg","image/png","image/webp","application/pdf"});startActivityForResult(Intent.createChooser(i,"Select broadcast image or PDF"),PICK_BROADCAST_FILE);});
        send.setOnClickListener(v->{String name=listName.getText().toString().trim();String body=message.getText().toString().trim();if(name.isEmpty()){listName.setError("WhatsApp Broadcast List name required");return;}if(body.isEmpty()&&broadcastFileUri==null){message.setError("Type message or choose attachment");return;}p.edit().putString(BROADCAST_LIST_NAME,name).putString(BROADCAST_MESSAGE,body).apply();if(!isAccessibilityServiceEnabled()){toast("Pehle Accessibility ON karein");try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(Exception ignored){}return;}startWhatsAppBroadcast(name,body,broadcastFileUri,broadcastFileType);d.dismiss();});
        d.setContentView(page);d.show();
    }

    private void openWhatsAppHome(String note){
        try{Intent i=getPackageManager().getLaunchIntentForPackage("com.whatsapp.w4b");if(i==null)i=getPackageManager().getLaunchIntentForPackage("com.whatsapp");if(i==null){toast("WhatsApp not installed");return;}startActivity(i);toast(note);}catch(Exception e){toast("WhatsApp open nahi hua");}
    }

    private void startWhatsAppBroadcast(String listName,String message,Uri file,String type){
        SharedPreferences.Editor ed=getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().putBoolean(BROADCAST_RUNNING,true).putString(BROADCAST_LIST_NAME,listName).putString(BROADCAST_MESSAGE,message).putInt(BROADCAST_STAGE,0);
        if(file==null)ed.putString(BROADCAST_MODE,"text").remove(BROADCAST_FILE_URI).remove(BROADCAST_FILE_TYPE);else ed.putString(BROADCAST_MODE,"share").putString(BROADCAST_FILE_URI,file.toString()).putString(BROADCAST_FILE_TYPE,type==null||type.isEmpty()?"application/octet-stream":type);ed.apply();
        try{
            Intent i;
            if(file!=null){i=new Intent(Intent.ACTION_SEND);i.setType(type==null||type.isEmpty()?"application/octet-stream":type);i.putExtra(Intent.EXTRA_STREAM,file);if(!message.isEmpty())i.putExtra(Intent.EXTRA_TEXT,message);i.setClipData(android.content.ClipData.newRawUri("broadcast attachment",file));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);grantUriPermission("com.whatsapp",file,Intent.FLAG_GRANT_READ_URI_PERMISSION);grantUriPermission("com.whatsapp.w4b",file,Intent.FLAG_GRANT_READ_URI_PERMISSION);}
            else{i=getPackageManager().getLaunchIntentForPackage("com.whatsapp.w4b");if(i==null)i=getPackageManager().getLaunchIntentForPackage("com.whatsapp");if(i==null)throw new Exception("WhatsApp not installed");}
            if(file!=null){try{i.setPackage("com.whatsapp.w4b");startActivity(i);}catch(Exception first){i.setPackage("com.whatsapp");startActivity(i);}}
            else startActivity(i);
            updateProgressNotification(this,0,1,"Opening broadcast: "+listName,false);
        }catch(Exception e){getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().putBoolean(BROADCAST_RUNNING,false).apply();toast("WhatsApp Broadcast open failed");}
    }
    private void renameCatalog(int index,JSONObject item){EditText input=new EditText(this);input.setText(item.optString("name","Catalog"));input.setSelectAllOnFocus(true);new AlertDialog.Builder(this).setTitle("Rename Catalog").setView(input).setPositiveButton("Save",(d,w)->{String n=input.getText().toString().trim();if(n.isEmpty())return;try{JSONArray a=readCatalogs();if(index<a.length()){a.getJSONObject(index).put("name",n);writeCatalogs(a);}showCatalogScreen();}catch(Exception e){toast("Rename failed");}}).setNegativeButton("Cancel",null).show();}
    private void deleteCatalog(int index,JSONObject item){try{JSONArray old=readCatalogs(),fresh=new JSONArray();for(int i=0;i<old.length();i++)if(i!=index)fresh.put(old.get(i));writeCatalogs(fresh);String path=item.optString("path","");if(!path.isEmpty())new File(path).delete();toast("Catalog deleted");}catch(Exception e){toast("Delete failed");}}

    private void showBusinessFilesDialog(){
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},CONTACT_PERMISSION);
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        Button ledger=button(p.getString(AutoReplyNotificationService.LEDGER_URI,"").isEmpty()?"UPLOAD & PREPARE MASTER LEDGER PDF":"UPDATE & PREPARE MASTER LEDGER PDF ✓");
        Button customers=button("Manage Ledger Customers ("+ledgerCustomerCount()+")");
        Button convertPdf=button("MASTER PDF → PHONE + BALANCE EXCEL");
        Button paymentReminder=button("PAYMENT REMINDER");
        Button importCsv=button("IMPORT CUSTOMER EXCEL (OPTIONAL)");
        Button history=button("View updated files history");
        EditText ledgerKey=new EditText(this);ledgerKey.setHint("Ledger keyword");ledgerKey.setText(p.getString(AutoReplyNotificationService.LEDGER_KEY,"ledger"));
        String savedLedgerName=p.getString(AutoReplyNotificationService.LEDGER_URI+"_name","");
        TextView fileLabel=new TextView(this);fileLabel.setText("SAVED LEDGER FILE");fileLabel.setTextSize(13);fileLabel.setTypeface(Typeface.DEFAULT_BOLD);fileLabel.setTextColor(Color.DKGRAY);fileLabel.setPadding(dp(4),dp(9),dp(4),dp(4));
        ledgerFileNameText=new TextView(this);ledgerFileNameText.setText(savedLedgerName.isEmpty()?"No Ledger file saved":savedLedgerName);ledgerFileNameText.setTextSize(17);ledgerFileNameText.setTypeface(Typeface.DEFAULT_BOLD);ledgerFileNameText.setTextColor(savedLedgerName.isEmpty()?Color.rgb(190,45,45):Color.rgb(0,125,70));ledgerFileNameText.setBackground(rounded(savedLedgerName.isEmpty()?Color.rgb(255,235,235):Color.rgb(225,248,235),12));ledgerFileNameText.setPadding(dp(12),dp(10),dp(12),dp(10));
        TextView current=new TextView(this);current.setPadding(dp(4),dp(8),dp(4),dp(8));current.setText("Customers: "+ledgerCustomerCount()+"\nLast status: "+p.getString("last_business_status","No send attempt yet"));
        box.addView(fileLabel);LinearLayout.LayoutParams flp=new LinearLayout.LayoutParams(-1,-2);flp.setMargins(0,0,0,dp(6));box.addView(ledgerFileNameText,flp);box.addView(current);box.addView(paymentReminder,new LinearLayout.LayoutParams(-1,dp(48)));box.addView(convertPdf,new LinearLayout.LayoutParams(-1,dp(46)));box.addView(ledger,new LinearLayout.LayoutParams(-1,dp(44)));box.addView(ledgerKey);box.addView(customers,new LinearLayout.LayoutParams(-1,dp(42)));box.addView(importCsv,new LinearLayout.LayoutParams(-1,dp(42)));
        box.addView(history,new LinearLayout.LayoutParams(-1,dp(42)));
        ledger.setOnClickListener(v->pickBusinessFile(PICK_LEDGER_FILE));
        paymentReminder.setOnClickListener(v->showPaymentReminderScreen());
        customers.setOnClickListener(v->showLedgerCustomersDialog());
        convertPdf.setOnClickListener(v->chooseMasterPdfForExcel());
        importCsv.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");startActivityForResult(Intent.createChooser(i,"Select optional customer Excel"),PICK_LEDGER_CUSTOMERS_XLSX);});
        history.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("File update history").setMessage(p.getString("file_history","No file updates yet")).setPositiveButton("Close",null).setNeutralButton("Clear",(d,w)->p.edit().remove("file_history").apply()).show());
        ScrollView scroll=new ScrollView(this);scroll.addView(box);
        new AlertDialog.Builder(this).setTitle("Business Files • Ledger").setMessage("Catalog main screen par alag section mein hai. Ledger Auto Reply ke liye Notification Access ON rakhein.").setView(scroll)
            .setPositiveButton("Save & Turn ON",(d,w)->{p.edit().putString(AutoReplyNotificationService.LEDGER_KEY,ledgerKey.getText().toString().trim()).putBoolean(AutoReplyNotificationService.ENABLED,true).apply();toast("Ledger settings saved • Auto Reply ON");})
            .setNegativeButton("Close",null).show();
    }

    private JSONArray readPaymentReminders(){try{return new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(PAYMENT_REMINDERS_KEY,"[]"));}catch(Exception e){return new JSONArray();}}
    private void writePaymentReminders(JSONArray items){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PAYMENT_REMINDERS_KEY,items.toString()).apply();}
    private String defaultPaymentTemplate(){return "Dear {Name}, kindly arrange the pending balance payment. Thank you — LATHA EPS.";}

    private void showPaymentReminderScreen(){
        Dialog d=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(12),dp(10),dp(12),dp(12));page.setBackgroundColor(Color.rgb(242,248,247));LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);Button back=button("‹");back.setTextSize(31);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Payment Reminder");title.setTextSize(23);title.setTextColor(Color.WHITE);title.setTypeface(Typeface.DEFAULT_BOLD);head.setBackground(rounded(Color.rgb(0,105,82),17));head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);
        LinearLayout first=row();Button phoneContacts=button("PHONE CONTACTS");Button add=button("+ ADD CUSTOMER");first.addView(phoneContacts,weighted(1.2f,44));first.addView(add,weighted(1f,44));page.addView(first);LinearLayout importRow=row();Button importLedger=button("IMPORT MASTER LEDGER");Button selectVisible=button("SELECT FILTERED");importRow.addView(importLedger,weighted(1.2f,42));importRow.addView(selectVisible,weighted(1f,42));page.addView(importRow);LinearLayout second=row();Button clear=button("CLEAR SELECTION");Button template=button("MESSAGE TEMPLATE");second.addView(clear,weighted(1f,42));second.addView(template,weighted(1f,42));page.addView(second);LinearLayout third=row();Button history=button("REMINDER HISTORY");Button scheduled=button(paymentScheduleButtonText());third.addView(history,weighted(1f,42));third.addView(scheduled,weighted(1.2f,42));page.addView(third);
        String[] filters={"All Customers","Overdue","Due Today","Upcoming","No Due Date"};Spinner filter=new Spinner(this);filter.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,filters));page.addView(filter,new LinearLayout.LayoutParams(-1,dp(48)));TextView summary=new TextView(this);summary.setTypeface(Typeface.DEFAULT_BOLD);summary.setPadding(dp(5),dp(5),dp(5),dp(5));page.addView(summary,new LinearLayout.LayoutParams(-1,dp(30)));ListView list=new ListView(this);list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);page.addView(list,new LinearLayout.LayoutParams(-1,0,1f));LinearLayout sendRow=row();Button test=button("TEST FIRST");Button schedule=button("SCHEDULE TIME");Button send=button("SEND NOW");test.setTextColor(Color.rgb(25,75,155));schedule.setTextColor(Color.rgb(175,90,10));send.setTextColor(Color.WHITE);send.setBackgroundColor(Color.rgb(20,125,70));sendRow.addView(test,weighted(.85f,50));sendRow.addView(schedule,weighted(1.1f,50));sendRow.addView(send,weighted(1f,50));page.addView(sendRow);
        Set<String> chosen=new LinkedHashSet<>();List<JSONObject> visible=new ArrayList<>();List<String> rows=new ArrayList<>();Runnable[] refresh={null};refresh[0]=()->{visible.clear();rows.clear();JSONArray a=readPaymentReminders();String selectedFilter=filters[filter.getSelectedItemPosition()];for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null||!paymentMatchesFilter(o,selectedFilter))continue;visible.add(o);String due=o.optString("due","");rows.add(o.optString("name","Customer")+"\n+"+o.optString("phone","")+"  •  "+(due.isEmpty()?"No due date":"Due "+due));}list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,rows));for(int i=0;i<visible.size();i++)list.setItemChecked(i,chosen.contains(normalize(visible.get(i).optString("phone",""))));summary.setText("Showing "+visible.size()+" • Selected "+chosen.size()+" • Long press to edit");};refresh[0].run();
        filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refresh[0].run();}public void onNothingSelected(android.widget.AdapterView<?> p){}});list.setOnItemClickListener((p,v,pos,id)->{String n=normalize(visible.get(pos).optString("phone",""));if(chosen.contains(n))chosen.remove(n);else chosen.add(n);summary.setText("Showing "+visible.size()+" • Selected "+chosen.size()+" • Long press to edit");});list.setOnItemLongClickListener((p,v,pos,id)->{showEditPaymentReminder(visible.get(pos),refresh[0]);return true;});
        phoneContacts.setOnClickListener(v->openPaymentContactPicker(refresh[0]));importLedger.setOnClickListener(v->{importPaymentCustomersFromLedger();refresh[0].run();});add.setOnClickListener(v->showEditPaymentReminder(null,refresh[0]));selectVisible.setOnClickListener(v->{for(JSONObject o:visible){String n=normalize(o.optString("phone",""));if(!n.isEmpty())chosen.add(n);}refresh[0].run();});clear.setOnClickListener(v->{chosen.clear();refresh[0].run();});template.setOnClickListener(v->showPaymentTemplateDialog());history.setOnClickListener(v->showPaymentReminderHistory());scheduled.setOnClickListener(v->showPaymentScheduleStatus(scheduled));test.setOnClickListener(v->previewPaymentReminders(chosen,true));schedule.setOnClickListener(v->showPaymentDateTimePicker(chosen,scheduled));send.setOnClickListener(v->previewPaymentReminders(chosen,false));back.setOnClickListener(v->d.dismiss());d.setContentView(page);d.show();
    }

    private boolean paymentMatchesFilter(JSONObject item,String filter){
        String due=item.optString("due","").trim();if("All Customers".equals(filter))return true;if(due.isEmpty())return "No Due Date".equals(filter);long at=parsePaymentDate(due);if(at<0)return "No Due Date".equals(filter);java.util.Calendar now=java.util.Calendar.getInstance();now.set(java.util.Calendar.HOUR_OF_DAY,0);now.set(java.util.Calendar.MINUTE,0);now.set(java.util.Calendar.SECOND,0);now.set(java.util.Calendar.MILLISECOND,0);long today=now.getTimeInMillis();if("Overdue".equals(filter))return at<today;if("Due Today".equals(filter))return at==today;if("Upcoming".equals(filter))return at>today;return false;
    }

    private void openPaymentContactPicker(Runnable after){
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED){loadContacts();showPaymentContactPicker(after);}
        else{pendingPaymentContactPicker=true;pendingPaymentRefresh=after;requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},CONTACT_PERMISSION);}
    }

    private void showPaymentContactPicker(Runnable after){
        Dialog d=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(12),dp(10),dp(12),dp(10));page.setBackgroundColor(Color.WHITE);
        TextView title=new TextView(this);title.setText("Select Payment Customers");title.setTextSize(22);title.setTypeface(Typeface.DEFAULT_BOLD);title.setTextColor(Color.rgb(20,95,75));page.addView(title,new LinearLayout.LayoutParams(-1,dp(50)));
        EditText find=new EditText(this);find.setHint("Search phone contact name or number");find.setSingleLine(true);page.addView(find,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView count=new TextView(this);count.setTypeface(Typeface.DEFAULT_BOLD);count.setPadding(dp(4),dp(4),dp(4),dp(4));page.addView(count,new LinearLayout.LayoutParams(-1,dp(32)));
        List<ContactItem> filtered=new ArrayList<>(allContacts);Set<String> working=new LinkedHashSet<>();ArrayAdapter<ContactItem>a=new ArrayAdapter<>(this,android.R.layout.simple_list_item_multiple_choice,filtered);ListView list=new ListView(this);list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);list.setAdapter(a);page.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        Runnable checks=()->{list.clearChoices();for(int i=0;i<filtered.size();i++)list.setItemChecked(i,working.contains(filtered.get(i).number));count.setText("Selected "+working.size()+" • Showing "+filtered.size()+" / "+allContacts.size());};checks.run();
        list.setOnItemClickListener((p,v,pos,id)->{String n=filtered.get(pos).number;if(working.contains(n))working.remove(n);else working.add(n);checks.run();});
        find.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int x){}public void onTextChanged(CharSequence s,int st,int b,int c){String q=s.toString().trim().toLowerCase(Locale.ROOT);filtered.clear();for(ContactItem x:allContacts)if(q.isEmpty()||x.name.toLowerCase(Locale.ROOT).contains(q)||x.number.contains(q)||last10Digits(x.number).contains(q))filtered.add(x);a.notifyDataSetChanged();checks.run();}public void afterTextChanged(Editable e){}});
        LinearLayout actions=row();Button cancel=button("CANCEL");Button add=button("ADD SELECTED");add.setTypeface(Typeface.DEFAULT_BOLD);add.setTextColor(Color.WHITE);add.setBackgroundColor(Color.rgb(20,125,75));actions.addView(cancel,weighted(1f,48));actions.addView(add,weighted(1.3f,48));page.addView(actions);cancel.setOnClickListener(v->d.dismiss());add.setOnClickListener(v->{if(working.isEmpty()){toast("Select phone contacts first");return;}try{JSONArray old=readPaymentReminders();Map<String,JSONObject> map=new LinkedHashMap<>();for(int i=0;i<old.length();i++){JSONObject o=old.optJSONObject(i);if(o!=null)map.put(normalize(o.optString("phone","")),o);}int added=0;for(ContactItem c:allContacts)if(working.contains(c.number)&&!map.containsKey(c.number)){JSONObject o=new JSONObject();o.put("name",c.name);o.put("phone",c.number);o.put("balance","");o.put("due","");o.put("last_sent","");map.put(c.number,o);added++;}JSONArray out=new JSONArray();for(JSONObject o:map.values())out.put(o);writePaymentReminders(out);if(after!=null)after.run();toast(added+" phone contacts added • duplicates skipped");d.dismiss();}catch(Exception e){toast("Contacts add failed");}});
        d.setContentView(page);d.show();
    }

    private List<JSONObject> collectPaymentItems(Set<String> chosen,boolean firstOnly){
        List<JSONObject> items=new ArrayList<>();JSONArray all=readPaymentReminders();for(int i=0;i<all.length();i++){JSONObject o=all.optJSONObject(i);if(o==null)continue;String n=normalize(o.optString("phone",""));if(!chosen.contains(n)||n.isEmpty()||isDoNotSend(n))continue;items.add(o);if(firstOnly)break;}return items;
    }

    private void showPaymentDateTimePicker(Set<String> chosen,Button statusButton){
        List<JSONObject> items=collectPaymentItems(chosen,false);if(items.isEmpty()){toast("Select payment customers first");return;}
        String[] repeats={"One Time","Daily","Weekly","Monthly"};
        SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);String remembered=prefs.getString(PAYMENT_SCHEDULE_LAST_REPEAT,"One Time");int selected=0;for(int i=0;i<repeats.length;i++)if(repeats[i].equals(remembered))selected=i;
        final int initial=selected;new AlertDialog.Builder(this).setTitle("Repeat Payment Reminder").setSingleChoiceItems(repeats,selected,null).setPositiveButton("NEXT",(dialog,which)->{int pos=((AlertDialog)dialog).getListView().getCheckedItemPosition();String repeat=repeats[pos<0?initial:pos];prefs.edit().putString(PAYMENT_SCHEDULE_LAST_REPEAT,repeat).apply();showPaymentScheduleDateAndTime(items,repeat,statusButton);}).setNegativeButton("CANCEL",null).show();
    }

    private void showPaymentScheduleDateAndTime(List<JSONObject> items,String repeat,Button statusButton){
        java.util.Calendar c=java.util.Calendar.getInstance();new android.app.DatePickerDialog(this,(view,year,month,day)->{java.util.Calendar picked=java.util.Calendar.getInstance();picked.set(year,month,day);new android.app.TimePickerDialog(this,(timeView,hour,minute)->{picked.set(java.util.Calendar.HOUR_OF_DAY,hour);picked.set(java.util.Calendar.MINUTE,minute);picked.set(java.util.Calendar.SECOND,0);picked.set(java.util.Calendar.MILLISECOND,0);long at=picked.getTimeInMillis();if(at<=System.currentTimeMillis()){toast("Future date and time select karein");return;}schedulePaymentReminderQueue(items,at,repeat);if(statusButton!=null)statusButton.setText(paymentScheduleButtonText());},c.get(java.util.Calendar.HOUR_OF_DAY),c.get(java.util.Calendar.MINUTE),false).show();},c.get(java.util.Calendar.YEAR),c.get(java.util.Calendar.MONTH),c.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void schedulePaymentReminderQueue(List<JSONObject> items,long at,String repeat){
        try{android.app.AlarmManager manager=(android.app.AlarmManager)getSystemService(ALARM_SERVICE);if(manager==null)throw new Exception("Alarm unavailable");if(Build.VERSION.SDK_INT>=31&&!manager.canScheduleExactAlarms()){try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception ignored){startActivity(new Intent(Settings.ACTION_SETTINGS));}toast("Alarms & reminders Allow karke Schedule Time dobara dabayein");return;}JSONArray nums=new JSONArray(),names=new JSONArray(),messages=new JSONArray();for(JSONObject o:items){nums.put(normalize(o.optString("phone","")));names.put(o.optString("name","Customer"));messages.put(buildPaymentMessage(o));}SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);p.edit().putLong(PAYMENT_SCHEDULE_AT,at).putString(PAYMENT_SCHEDULE_NUMBERS,nums.toString()).putString(PAYMENT_SCHEDULE_NAMES,names.toString()).putString(PAYMENT_SCHEDULE_MESSAGES,messages.toString()).putString(PAYMENT_SCHEDULE_REPEAT,repeat).putString(PAYMENT_SCHEDULE_LAST_REPEAT,repeat).apply();Intent alarmIntent=new Intent(this,PaymentScheduleReceiver.class).setAction(PaymentScheduleReceiver.ACTION_SEND);PendingIntent alarm=PendingIntent.getBroadcast(this,620,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Intent showIntent=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent show=PendingIntent.getActivity(this,621,showIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);manager.setAlarmClock(new android.app.AlarmManager.AlarmClockInfo(at,show),alarm);String when=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date(at));toast(repeat+" reminder scheduled • "+when+" • "+items.size()+" customers");PaymentScheduleReceiver.showScheduledNotification(this,items.size(),at,repeat);}catch(Exception e){toast("Schedule save failed");}
    }

    private String paymentScheduleButtonText(){long at=getSharedPreferences(PREFS,MODE_PRIVATE).getLong(PAYMENT_SCHEDULE_AT,0L);return at>System.currentTimeMillis()?"SCHEDULED ✓":"SCHEDULE STATUS";}

    private void showPaymentScheduleStatus(Button button){SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);long at=p.getLong(PAYMENT_SCHEDULE_AT,0L);if(at<=System.currentTimeMillis()){toast("No active payment schedule");return;}String repeat=p.getString(PAYMENT_SCHEDULE_REPEAT,"One Time");String when=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date(at));new AlertDialog.Builder(this).setTitle("Scheduled Payment Reminder").setMessage("Repeat: "+repeat+"\nNext send: "+when+"\nPhone unlocked and Accessibility ON rakhein.").setPositiveButton("KEEP",null).setNegativeButton("CANCEL SCHEDULE",(d,w)->{cancelPaymentSchedule();if(button!=null)button.setText(paymentScheduleButtonText());}).show();}

    private void cancelPaymentSchedule(){Intent i=new Intent(this,PaymentScheduleReceiver.class).setAction(PaymentScheduleReceiver.ACTION_SEND);PendingIntent pi=PendingIntent.getBroadcast(this,620,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);android.app.AlarmManager manager=(android.app.AlarmManager)getSystemService(ALARM_SERVICE);if(manager!=null)manager.cancel(pi);getSharedPreferences(PREFS,MODE_PRIVATE).edit().remove(PAYMENT_SCHEDULE_AT).remove(PAYMENT_SCHEDULE_NUMBERS).remove(PAYMENT_SCHEDULE_NAMES).remove(PAYMENT_SCHEDULE_MESSAGES).remove(PAYMENT_SCHEDULE_REPEAT).apply();((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(PaymentScheduleReceiver.SCHEDULE_NOTIFICATION_ID);toast("Payment schedule cancelled");}
    private long parsePaymentDate(String value){try{java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("dd-MM-yyyy",Locale.US);f.setLenient(false);return f.parse(value).getTime();}catch(Exception e){return -1;}}

    private void showEditPaymentReminder(JSONObject existing,Runnable after){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);EditText name=new EditText(this);name.setHint("Customer name");EditText phone=new EditText(this);phone.setHint("10-digit mobile number");phone.setInputType(InputType.TYPE_CLASS_PHONE);EditText balance=new EditText(this);balance.setText(existing==null?"":existing.optString("balance",""));EditText due=new EditText(this);due.setHint("Due date • DD-MM-YYYY");String original="";if(existing!=null){original=normalize(existing.optString("phone",""));name.setText(existing.optString("name",""));phone.setText(last10Digits(original));due.setText(existing.optString("due",""));}final String oldPhone=original;box.addView(name);box.addView(phone);box.addView(due);
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle(existing==null?"Add Payment Customer":"Edit Payment Customer").setView(box).setPositiveButton("Save",(d,w)->{String n=normalize(phone.getText().toString());String date=due.getText().toString().trim();if(n.isEmpty()){toast("Valid mobile number required");return;}if(!date.isEmpty()&&parsePaymentDate(date)<0){toast("Due date DD-MM-YYYY format me enter karein");return;}try{JSONArray a=readPaymentReminders(),out=new JSONArray();String lastSent="";for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;String p=normalize(o.optString("phone",""));if(p.equals(oldPhone)||p.equals(n)){if(lastSent.isEmpty())lastSent=o.optString("last_sent","");continue;}out.put(o);}JSONObject item=new JSONObject();item.put("name",name.getText().toString().trim().isEmpty()?"Customer":name.getText().toString().trim());item.put("phone",n);item.put("balance",balance.getText().toString().trim());item.put("due",date);item.put("last_sent",lastSent);out.put(item);writePaymentReminders(out);after.run();toast("Payment reminder saved");}catch(Exception e){toast("Save failed");}}).setNegativeButton("Cancel",null);if(existing!=null)b.setNeutralButton("Delete",(d,w)->{JSONArray a=readPaymentReminders(),out=new JSONArray();for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&!normalize(o.optString("phone","")).equals(oldPhone))out.put(o);}writePaymentReminders(out);after.run();toast("Payment customer deleted");});b.show();
    }

    private void importPaymentCustomersFromLedger(){
        try{SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);JSONArray source=new JSONArray(p.getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));if(source.length()==0){toast("Pehle Master Ledger prepare/import karein");return;}JSONArray existing=readPaymentReminders();Map<String,JSONObject> map=new LinkedHashMap<>();for(int i=0;i<existing.length();i++){JSONObject o=existing.optJSONObject(i);if(o!=null)map.put(normalize(o.optString("phone","")),o);}int added=0;for(int i=0;i<source.length();i++){JSONObject s=source.optJSONObject(i);if(s==null)continue;String phone=normalize(s.optString("phone",""));if(phone.isEmpty())continue;JSONObject old=map.get(phone);if(old==null){old=new JSONObject();old.put("phone",phone);old.put("due","");old.put("last_sent","");added++;}if(!s.optString("name","").isEmpty())old.put("name",s.optString("name","Customer"));if(!s.optString("balance","").isEmpty())old.put("balance",s.optString("balance",""));map.put(phone,old);}JSONArray out=new JSONArray();for(JSONObject o:map.values())out.put(o);writePaymentReminders(out);toast(added+" new customers imported • "+out.length()+" total");}catch(Exception e){toast("Ledger customer import failed");}
    }

    private void showPaymentTemplateDialog(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);EditText text=new EditText(this);text.setMinLines(5);text.setGravity(Gravity.TOP);String saved=p.getString(PAYMENT_TEMPLATE_KEY,defaultPaymentTemplate());if(saved.contains("{Balance}")||saved.contains("{balance}")||saved.contains("{DueDate}")||saved.contains("{duedate}"))saved=defaultPaymentTemplate();text.setText(saved);new AlertDialog.Builder(this).setTitle("Payment Request Template").setMessage("Available: {Name} • Balance amount message me nahi jayega").setView(text).setPositiveButton("Save",(d,w)->{String value=text.getText().toString().trim().replace("{Balance}","").replace("{balance}","").replace("{DueDate}","").replace("{duedate}","");if(value.isEmpty())value=defaultPaymentTemplate();p.edit().putString(PAYMENT_TEMPLATE_KEY,value).apply();toast("Payment request template saved");}).setNeutralButton("Reset",(d,w)->p.edit().putString(PAYMENT_TEMPLATE_KEY,defaultPaymentTemplate()).apply()).setNegativeButton("Cancel",null).show();
    }
    private String buildPaymentMessage(JSONObject item){String template=getSharedPreferences(PREFS,MODE_PRIVATE).getString(PAYMENT_TEMPLATE_KEY,defaultPaymentTemplate());if(template.contains("{Balance}")||template.contains("{balance}")||template.contains("{DueDate}")||template.contains("{duedate}"))template=defaultPaymentTemplate();return template.replace("{Name}",item.optString("name","Customer")).replace("{name}",item.optString("name","Customer"));}

    private void previewPaymentReminders(Set<String> chosen,boolean testOnly){
        if(chosen.isEmpty()){toast("Select payment customers first");return;}List<JSONObject> items=new ArrayList<>();JSONArray all=readPaymentReminders();int skipped=0;for(int i=0;i<all.length();i++){JSONObject o=all.optJSONObject(i);if(o==null)continue;String n=normalize(o.optString("phone",""));if(!chosen.contains(n))continue;if(n.isEmpty()||isDoNotSend(n)){skipped++;continue;}items.add(o);if(testOnly)break;}if(items.isEmpty()){toast("Selected customers invalid or in Do Not Send List");return;}if(!isAccessibilityServiceEnabled()){toast("Pehle Accessibility ON karein");try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(Exception ignored){}return;}JSONObject first=items.get(0);String preview="Recipients: "+items.size()+(skipped>0?"\nBlocked/skipped: "+skipped:"")+"\n\nFirst: "+first.optString("name","Customer")+"  +"+first.optString("phone","")+"\n\n"+buildPaymentMessage(first);new AlertDialog.Builder(this).setTitle(testOnly?"Test Payment Reminder":"Confirm Payment Reminders").setMessage(preview).setPositiveButton(testOnly?"SEND TEST":"START SEND",(d,w)->startPaymentReminderQueue(items,testOnly)).setNegativeButton("Cancel",null).show();
    }

    private void startPaymentReminderQueue(List<JSONObject> items,boolean testOnly){
        try{JSONArray nums=new JSONArray(),names=new JSONArray(),messages=new JSONArray();for(JSONObject o:items){nums.put(normalize(o.optString("phone","")));names.put(o.optString("name","Customer"));messages.put(buildPaymentMessage(o));o.put("last_sent",System.currentTimeMillis());}JSONArray all=readPaymentReminders();Map<String,JSONObject> sent=new LinkedHashMap<>();for(JSONObject o:items)sent.put(normalize(o.optString("phone","")),o);JSONArray updated=new JSONArray();for(int i=0;i<all.length();i++){JSONObject o=all.optJSONObject(i);if(o==null)continue;JSONObject replacement=sent.get(normalize(o.optString("phone","")));updated.put(replacement==null?o:replacement);}writePaymentReminders(updated);SharedPreferences settings=getSharedPreferences(PREFS,MODE_PRIVATE);getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().putString(AUTO_NUMBERS,nums.toString()).putString(AUTO_NAMES,names.toString()).putString(AUTO_MESSAGES,messages.toString()).putString(AUTO_MESSAGE,"").putString(AUTO_QUEUE_TOKEN,String.valueOf(System.currentTimeMillis())).putString(AUTO_FAILED,"[]").putInt(AUTO_INDEX,0).putInt(AUTO_MIN_DELAY,settings.getInt(AUTO_MIN_DELAY,3)).putInt(AUTO_MAX_DELAY,settings.getInt(AUTO_MAX_DELAY,7)).remove(AUTO_IMAGE_URI).remove(AUTO_IMAGE_TYPE).putBoolean(AUTO_RUNNING,true).apply();String stamp=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date());String old=settings.getString(PAYMENT_HISTORY_KEY,"");settings.edit().putString(PAYMENT_HISTORY_KEY,stamp+" • "+(testOnly?"Test":"Sent")+" • "+items.size()+" reminder"+(old.isEmpty()?"":"\n"+old)).apply();getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);if(sendButton!=null)sendButton.setText("STOP AUTO SENDING");if(miniProgress!=null)miniProgress.setText("Payment Reminder • Starting 1 / "+items.size());showProgressNotification(0,items.size(),"Payment reminders starting");WhatsAppAccessibilityService.openCurrentChat(this);}catch(Exception e){toast("Payment reminder queue failed");}
    }

    private void showPaymentReminderHistory(){String h=getSharedPreferences(PREFS,MODE_PRIVATE).getString(PAYMENT_HISTORY_KEY,"No payment reminder history");new AlertDialog.Builder(this).setTitle("Payment Reminder History").setMessage(h).setPositiveButton("Close",null).setNeutralButton("Clear",(d,w)->getSharedPreferences(PREFS,MODE_PRIVATE).edit().remove(PAYMENT_HISTORY_KEY).apply()).show();}

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
                String display=getDisplayName(source);if(display.isEmpty())display="Master Ledger PDF";
                String history=p.getString("file_history","");String line=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date())+" • Master Ledger prepared • "+display;
                p.edit().putString(AutoReplyNotificationService.LEDGER_URI,safe.toString()).putString(AutoReplyNotificationService.LEDGER_URI+"_type","application/pdf").putString(AutoReplyNotificationService.LEDGER_URI+"_name",display).putLong(AutoReplyNotificationService.LEDGER_URI+"_updated",System.currentTimeMillis()).putString("file_history",line+(history.isEmpty()?"":"\n"+history)).putBoolean(AutoReplyNotificationService.ENABLED,true).apply();
                runOnUiThread(()->{if(miniProgress!=null)miniProgress.setText("Ledger ready • "+result.uniquePhones+" numbers");if(ledgerFileNameText!=null){ledgerFileNameText.setText(display);ledgerFileNameText.setTextColor(Color.rgb(0,125,70));ledgerFileNameText.setBackground(rounded(Color.rgb(225,248,235),12));}toast(result.entries.size()+" ledgers ready • "+result.skippedPages+" without number skipped");});
            }catch(Exception e){runOnUiThread(()->{if(miniProgress!=null)miniProgress.setText("Master Ledger failed");toast("Master Ledger prepare failed • valid Tally PDF select karein");});}
        }).start();
    }

    private MasterLedgerResult prepareCustomerLedgers(PDDocument document) throws Exception{
        MasterLedgerResult result=new MasterLedgerResult();result.totalPages=document.getNumberOfPages();
        if(result.totalPages<=0)throw new Exception("Empty PDF");
        PDFTextStripper stripper=new PDFTextStripper();stripper.setSortByPosition(true);
        LinkedHashMap<String,List<LedgerPageData>> byPhone=new LinkedHashMap<>();
        for(int page=1;page<=result.totalPages;page++){
            stripper.setStartPage(page);stripper.setEndPage(page);String text=stripper.getText(document);
            String phone=extractLedgerPhone(text);if(phone.isEmpty()){result.skippedPages++;continue;}
            LedgerPageData item=new LedgerPageData(phone,extractLedgerName(text),extractClosingBalance(text),page-1);
            result.entries.add(item);byPhone.computeIfAbsent(phone,k->new ArrayList<>()).add(item);
        }
        if(byPhone.isEmpty())throw new Exception("No labelled mobile numbers found");
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
        getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).edit().putString(AutoReplyNotificationService.LEDGER_CUSTOMERS,customers.toString()).putInt("ledger_source_pages",result.totalPages).putInt("ledger_skipped_pages",result.skippedPages).putLong("ledger_index_updated",System.currentTimeMillis()).putBoolean(AutoReplyNotificationService.ENABLED,true).apply();
        return result;
    }

    private String extractLedgerPhone(String text){
        if(text==null||text.trim().isEmpty())return "";
        Pattern labelled=Pattern.compile("(?i)(?:mobile|mob(?:ile)?\\.?|phone|telephone|tel\\.?|whats\\s*app|contact)(?:\\s*(?:no|number|num|#))?\\s*[:.\\-]?\\s*([+0-9][0-9() .\\-]{8,25})");
        Matcher labelledMatch=labelled.matcher(text);
        while(labelledMatch.find()){
            String phone=normaliseLedgerPhone(labelledMatch.group(1));if(!phone.isEmpty())return phone;
        }
        // Tally layouts sometimes place the label and value in separate PDF text columns.
        String[] lines=text.split("\\r?\\n");
        for(int i=0;i<lines.length;i++){
            String low=lines[i].toLowerCase(Locale.ROOT);
            if(!low.matches(".*\\b(mobile|mob|phone|telephone|tel|whatsapp|contact)\\b.*"))continue;
            String phone=normaliseLedgerPhone(lines[i]);if(!phone.isEmpty())return phone;
            if(i+1<lines.length){phone=normaliseLedgerPhone(lines[i+1]);if(!phone.isEmpty())return phone;}
        }
        // Last fallback: accept a clearly formatted Indian mobile, but never an amount/date/GST line.
        Pattern mobile=Pattern.compile("(?<![0-9])(?:\\+?91[ .\\-]?)?[6-9](?:[ .\\-]?[0-9]){9}(?![0-9])");
        for(String raw:lines){
            String low=raw.toLowerCase(Locale.ROOT);
            if(low.contains("balance")||low.contains("amount")||low.contains("gstin")||low.contains("invoice")||low.contains("date"))continue;
            Matcher m=mobile.matcher(raw);if(m.find()){String phone=normaliseLedgerPhone(m.group());if(!phone.isEmpty())return phone;}
        }
        return "";
    }

    private String normaliseLedgerPhone(String raw){
        if(raw==null)return "";String digits=raw.replaceAll("[^0-9]","");
        if(digits.startsWith("00")&&digits.length()>12)digits=digits.substring(2);
        if(digits.startsWith("0")&&digits.length()==11)digits=digits.substring(1);
        if(digits.length()>10)digits=digits.substring(digits.length()-10);
        return digits.matches("[6-9][0-9]{9}")?"91"+digits:"";
    }

    private String getDisplayName(Uri uri){
        Cursor cursor=null;try{cursor=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null);if(cursor!=null&&cursor.moveToFirst()){String name=cursor.getString(0);return name==null?"":name.trim();}}catch(Exception ignored){}finally{if(cursor!=null)cursor.close();}return "";
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
    private void showAutoReplyScreen(){
        final SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        final Dialog dialog=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(Color.rgb(239,249,248));

        LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(12),dp(8),dp(10),dp(8));header.setBackgroundColor(Color.rgb(0,91,78));
        Button back=button("‹");back.setTextSize(30);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);
        TextView title=new TextView(this);title.setText("AutoReply to Messages");title.setTextColor(Color.WHITE);title.setTextSize(23);title.setTypeface(Typeface.DEFAULT_BOLD);title.setGravity(Gravity.CENTER_VERTICAL);
        Button settings=button("⚙");settings.setTextSize(23);settings.setTextColor(Color.WHITE);settings.setBackgroundColor(Color.TRANSPARENT);
        header.addView(back,new LinearLayout.LayoutParams(dp(48),dp(54)));header.addView(title,new LinearLayout.LayoutParams(0,dp(54),1f));header.addView(settings,new LinearLayout.LayoutParams(dp(52),dp(54)));page.addView(header);
        back.setOnClickListener(v->dialog.dismiss());settings.setOnClickListener(v->showSettingsScreen());

        LinearLayout enable=row();enable.setGravity(Gravity.CENTER_VERTICAL);enable.setPadding(dp(18),dp(8),dp(12),dp(8));enable.setBackground(rounded(Color.rgb(190,232,226),22));
        TextView enableTitle=new TextView(this);enableTitle.setText("☘  Enable Auto Reply");enableTitle.setTextColor(Color.rgb(12,52,49));enableTitle.setTextSize(20);enableTitle.setTypeface(Typeface.DEFAULT_BOLD);
        Switch toggle=new Switch(this);toggle.setChecked(p.getBoolean(AutoReplyNotificationService.ENABLED,false));
        enable.addView(enableTitle,new LinearLayout.LayoutParams(0,dp(68),1f));enable.addView(toggle,new LinearLayout.LayoutParams(dp(64),dp(58)));
        LinearLayout.LayoutParams elp=new LinearLayout.LayoutParams(-1,dp(84));elp.setMargins(dp(16),dp(16),dp(16),dp(9));page.addView(enable,elp);
        toggle.setOnCheckedChangeListener((b,on)->{p.edit().putBoolean(AutoReplyNotificationService.ENABLED,on).apply();toast(on?"Auto reply ON":"Auto reply OFF");});

        TextView rulesTitle=new TextView(this);rulesTitle.setText("Rules");rulesTitle.setTextSize(22);rulesTitle.setTypeface(Typeface.DEFAULT_BOLD);rulesTitle.setTextColor(Color.rgb(12,52,49));rulesTitle.setPadding(dp(22),dp(8),0,dp(6));page.addView(rulesTitle,new LinearLayout.LayoutParams(-1,dp(50)));
        ScrollView scroll=new ScrollView(this);LinearLayout cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);cards.setPadding(dp(16),0,dp(16),dp(92));scroll.addView(cards);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));

        Button plus=button("+");plus.setTextSize(38);plus.setTextColor(Color.WHITE);plus.setBackground(rounded(Color.rgb(0,91,78),50));
        LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);bottom.setPadding(0,0,dp(20),dp(10));bottom.addView(plus,new LinearLayout.LayoutParams(dp(72),dp(72)));page.addView(bottom,new LinearLayout.LayoutParams(-1,dp(82)));
        Runnable refresh=()->renderAutoReplyRules(cards,dialog);
        plus.setOnClickListener(v->showRuleEditor(-1,refresh));refresh.run();dialog.setContentView(page);dialog.show();
    }

    private void renderAutoReplyRules(LinearLayout cards,Dialog parent){
        cards.removeAllViews();SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        try{
            JSONArray rules=new JSONArray(p.getString("rules","[]"));
            if(rules.length()==0){TextView empty=new TextView(this);empty.setText("No rules saved\nTap + to create your first auto reply");empty.setGravity(Gravity.CENTER);empty.setTextSize(16);empty.setTextColor(Color.DKGRAY);empty.setPadding(0,dp(70),0,0);cards.addView(empty,new LinearLayout.LayoutParams(-1,dp(190)));return;}
            for(int i=0;i<rules.length();i++){
                final int index=i;JSONObject rule=rules.getJSONObject(i);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(10),dp(12),dp(10));card.setBackground(rounded(Color.WHITE,20));
                TextView name=new TextView(this);name.setText("Rule "+(i+1));name.setTextSize(19);name.setTypeface(Typeface.DEFAULT_BOLD);name.setTextColor(Color.rgb(0,91,78));card.addView(name);
                LinearLayout content=row();content.setGravity(Gravity.CENTER_VERTICAL);
                TextView dot=new TextView(this);dot.setText("●");dot.setTextSize(25);dot.setTextColor(Color.rgb(0,166,125));dot.setGravity(Gravity.CENTER);
                String key=rule.optString("keyword","");String reply=rule.optString("reply","");String mode=new String[]{"Contains","Exact","Starts with","Ends with"}[Math.max(0,Math.min(3,rule.optInt("match",0)))];
                TextView summary=new TextView(this);summary.setText("Received Msg : "+key+"\nSend Msg : "+(reply.isEmpty()?(rule.optString("image","").isEmpty()?"—":"🖼 Image"):reply)+"\n"+mode+(rule.optBoolean("case",false)?" • Case sensitive":""));summary.setTextSize(15);summary.setTextColor(Color.rgb(28,28,28));summary.setMaxLines(4);summary.setEllipsize(TextUtils.TruncateAt.END);summary.setPadding(dp(5),0,dp(4),0);
                Button edit=button("✎");edit.setTextSize(20);edit.setTextColor(Color.rgb(0,91,78));edit.setBackground(rounded(Color.rgb(220,245,241),40));
                Button move=button("↕");move.setTextSize(21);move.setTextColor(Color.rgb(0,91,78));move.setBackground(rounded(Color.rgb(220,245,241),40));
                Button more=button("⋮");more.setTextSize(23);more.setTextColor(Color.rgb(0,91,78));more.setBackgroundColor(Color.TRANSPARENT);
                content.addView(dot,new LinearLayout.LayoutParams(dp(38),dp(72)));content.addView(summary,new LinearLayout.LayoutParams(0,dp(86),1f));content.addView(edit,new LinearLayout.LayoutParams(dp(44),dp(44)));content.addView(move,new LinearLayout.LayoutParams(dp(44),dp(44)));content.addView(more,new LinearLayout.LayoutParams(dp(38),dp(48)));card.addView(content);
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(132));cp.setMargins(0,dp(5),0,dp(7));cards.addView(card,cp);
                edit.setOnClickListener(v->showRuleEditor(index,()->renderAutoReplyRules(cards,parent)));
                move.setOnClickListener(v->showRuleMoveMenu(move,index,cards,parent));
                more.setOnClickListener(v->showRuleMoreMenu(more,index,cards,parent));
            }
        }catch(Exception e){toast("Rules open failed");}
    }

    private void showRuleEditor(int editIndex,Runnable refresh){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);JSONObject old=null;
        try{JSONArray a=new JSONArray(p.getString("rules","[]"));if(editIndex>=0&&editIndex<a.length())old=a.getJSONObject(editIndex);}catch(Exception ignored){}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText keyword=new EditText(this);keyword.setHint("Received message / keyword");Spinner match=new Spinner(this);String[] modes={"Contains","Exact match","Starts with","Ends with"};match.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));
        EditText reply=new EditText(this);reply.setHint("Reply text / image caption");reply.setMinLines(2);CheckBox caseSensitive=new CheckBox(this);caseSensitive.setText("Case sensitive");
        EditText cool=new EditText(this);cool.setHint("Cooldown minutes");cool.setInputType(InputType.TYPE_CLASS_NUMBER);cool.setText(String.valueOf(p.getInt(AutoReplyNotificationService.COOLDOWN,5)));
        if(old!=null){keyword.setText(old.optString("keyword"));match.setSelection(old.optInt("match",0));reply.setText(old.optString("reply"));caseSensitive.setChecked(old.optBoolean("case",false));}
        Button image=button(p.getString(AutoReplyNotificationService.IMAGE,"").isEmpty()?"Choose reply image from phone albums":"Change reply image ✓");box.addView(keyword);box.addView(match);box.addView(reply);box.addView(caseSensitive);box.addView(cool);box.addView(image,new LinearLayout.LayoutParams(-1,dp(46)));image.setOnClickListener(v->openPhoneGalleryFirst());
        final JSONObject original=old;AlertDialog d=new AlertDialog.Builder(this).setTitle(editIndex<0?"Add Auto Reply Rule":"Edit Rule "+(editIndex+1)).setView(box).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String k=keyword.getText().toString().trim();if(k.isEmpty()){keyword.setError("Keyword required");return;}try{int cooldown=Math.max(1,Integer.parseInt(cool.getText().toString().trim()));JSONArray arr=new JSONArray(p.getString("rules","[]"));JSONObject o=new JSONObject();o.put("keyword",k);o.put("match",match.getSelectedItemPosition());o.put("case",caseSensitive.isChecked());o.put("reply",reply.getText().toString().trim());String img=p.getString(AutoReplyNotificationService.IMAGE,"");String typ=p.getString(AutoReplyNotificationService.IMAGE+"_type","image/*");if(editIndex>=0&&original!=null&&img.isEmpty()){img=original.optString("image","");typ=original.optString("type","image/*");}o.put("image",img);o.put("type",typ);if(editIndex>=0){JSONArray out=new JSONArray();for(int i=0;i<arr.length();i++)out.put(i==editIndex?o:arr.get(i));arr=out;}else arr.put(o);p.edit().putString("rules",arr.toString()).putInt(AutoReplyNotificationService.COOLDOWN,cooldown).putBoolean(AutoReplyNotificationService.ENABLED,true).apply();toast(editIndex<0?"Rule added • Auto reply ON":"Rule updated");d.dismiss();refresh.run();}catch(Exception e){toast("Rule save failed");}}));d.show();
    }

    private void showRuleMoveMenu(View anchor,int index,LinearLayout cards,Dialog parent){PopupMenu m=new PopupMenu(this,anchor);m.getMenu().add("Move up");m.getMenu().add("Move down");m.setOnMenuItemClickListener(item->{moveRule(index,item.getTitle().toString().contains("up")?-1:1);renderAutoReplyRules(cards,parent);return true;});m.show();}
    private void showRuleMoreMenu(View anchor,int index,LinearLayout cards,Dialog parent){PopupMenu m=new PopupMenu(this,anchor);m.getMenu().add("Edit rule");m.getMenu().add("Delete rule");m.setOnMenuItemClickListener(item->{if(item.getTitle().toString().startsWith("Edit"))showRuleEditor(index,()->renderAutoReplyRules(cards,parent));else new AlertDialog.Builder(this).setTitle("Delete this rule?").setPositiveButton("Delete",(d,w)->{deleteRule(index);renderAutoReplyRules(cards,parent);}).setNegativeButton("Cancel",null).show();return true;});m.show();}
    private void moveRule(int index,int direction){try{SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);JSONArray a=new JSONArray(p.getString("rules","[]"));int to=index+direction;if(to<0||to>=a.length())return;ArrayList<JSONObject> list=new ArrayList<>();for(int i=0;i<a.length();i++)list.add(a.getJSONObject(i));Collections.swap(list,index,to);JSONArray out=new JSONArray();for(JSONObject o:list)out.put(o);p.edit().putString("rules",out.toString()).apply();}catch(Exception ignored){}}
    private void deleteRule(int index){try{SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);JSONArray a=new JSONArray(p.getString("rules","[]"));JSONArray out=new JSONArray();for(int i=0;i<a.length();i++)if(i!=index)out.put(a.get(i));p.edit().putString("rules",out.toString()).apply();toast("Rule deleted");}catch(Exception ignored){}}
    private GradientDrawable rounded(int color,int radiusDp){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radiusDp));return g;}

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
            .setNeutralButton("Save as recipient list",(d,w)->{if(nums.isEmpty()){toast("Saved list empty");return;}selectedNumbers.clear();selectedNumbers.addAll(nums);showSaveGroupDialog();})
            .setNegativeButton("Close",null).show();
    }

    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private static class ContactItem{final String name,number;ContactItem(String n,String p){name=n;number=p;}@Override public String toString(){return name+"  •  +"+number;}}
}
