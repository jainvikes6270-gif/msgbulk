package com.lathaeps.lathabulk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.content.Context;
import android.content.ClipData;
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
import android.graphics.pdf.PdfDocument;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.CancellationSignal;
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
import java.net.HttpURLConnection;
import java.net.URL;
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
    public static final String ACTION_FLOATING_VOICE_RESULT = "com.lathaeps.lathabulk.FLOATING_VOICE_RESULT";
    public static final String EXTRA_FLOATING_VOICE_QUERY = "floating_voice_query";
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
    private static final int PRICE_VOICE_SEARCH = 116;
    private static final int PICK_PRICE_SOURCE_FILE = 117;
    private static final int PICK_PRICE_REPLACE_FILE = 118;
    static final String CHANNEL_ID = "latha_bulk_progress";
    static final int NOTIFICATION_ID = 511;
    static final String UPDATE_CHANNEL_ID = "lathaeps_app_updates";
    static final int UPDATE_NOTIFICATION_ID = 523;
    static final String PREFS = "latha_bulk_prefs";
    static final String GROUPS_KEY = "saved_groups";
    static final String CATALOG_ITEMS_KEY = "catalog_items";
    static final String PRICE_LIST_ITEMS_KEY = "price_list_items";
    static final String PRICE_SOURCE_FILES_KEY = "price_source_files";
    static final String PRICE_BRAND_FOLDERS_KEY = "price_brand_folders";
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
    private static final String FINGERPRINT_UNLOCK_KEY = "fingerprint_unlock_enabled";
    private static final String MESSAGE_HEADER_KEY = "message_header";
    private static final String MESSAGE_FOOTER_KEY = "message_footer";
    private static final String MESSAGE_TEMPLATES_KEY = "message_templates";
    private static final String MESSAGE_DRAFT_KEY = "message_draft";
    private static final String DO_NOT_SEND_KEY = "do_not_send_numbers";
    private static final String LIST_TEMPLATES_KEY = "recipient_list_templates";
    private static final String PAYMENT_REMINDERS_KEY = "payment_reminders";
    private static final String PAYMENT_REMINDER_LISTS_KEY = "payment_reminder_saved_lists";
    private static final String PAYMENT_TEMPLATE_KEY = "payment_reminder_template";
    static final String PAYMENT_HISTORY_KEY = "payment_reminder_history";
    static final String PAYMENT_SCHEDULE_AT = "payment_schedule_at";
    static final String PAYMENT_SCHEDULE_NUMBERS = "payment_schedule_numbers";
    static final String PAYMENT_SCHEDULE_NAMES = "payment_schedule_names";
    static final String PAYMENT_SCHEDULE_MESSAGES = "payment_schedule_messages";
    static final String PAYMENT_SCHEDULE_REPEAT = "payment_schedule_repeat";
    static final String PAYMENT_SCHEDULE_LAST_REPEAT = "payment_schedule_last_repeat";
    static final String LEDGER_SAVED_PARTIES_KEY = "ledger_saved_party_list";
    static final String LEDGER_SAVED_LISTS_KEY = "ledger_saved_named_lists";
    static final String LEDGER_SCHEDULE_LIST_NAME = "ledger_schedule_list_name";
    static final String LEDGER_SCHEDULE_AT = "ledger_schedule_at";
    static final String LEDGER_SCHEDULE_REPEAT = "ledger_schedule_repeat";
    private static final String UPDATE_CHECK_ENABLED_KEY = "update_check_enabled";
    private static final String UPDATE_LAST_CHECK_KEY = "update_last_check";
    private static final String UPDATE_API_URL = "https://api.github.com/repos/jainvikes6270-gif/msgbulk/releases/latest";
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
    private String pendingRuleImageUri = "";
    private String pendingRuleImageType = "image/*";
    private Button pendingRuleImageButton;
    private CheckBox pendingRuleImageEnabled;
    private EditText priceListSearchBox;
    private boolean priceVoiceFromHome=false;
    private String pendingPriceSourceName="",pendingPriceSourceBrand="",pendingPriceSourceCategory="",pendingPriceSourceKeywords="";
    private String pendingPriceReplaceId="";
    private Runnable pendingPriceListRefresh;
    private String pendingFloatingVoiceQuery="";
    private boolean biometricFallbackShown=false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        captureFloatingVoiceIntent(getIntent());
        SubscriptionManager.ensureTrial(this);
        SubscriptionManager.refresh(this,(ok,message)->{});
        if(!SubscriptionManager.hasAccess(this)){
            startActivity(new Intent(this,SubscriptionActivity.class));
            finish();
            return;
        }
        PDFBoxResourceLoader.init(getApplicationContext());
        createNotificationChannel();
        PaymentScheduleReceiver.restoreStoredSchedule(this);
        LedgerScheduleReceiver.restoreStoredSchedule(this);
        requestNotificationPermissionIfNeeded();
        showLoginOrApp();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        captureFloatingVoiceIntent(intent);
        consumeFloatingVoiceQuery();
    }

    private void captureFloatingVoiceIntent(Intent intent){
        if(intent!=null&&ACTION_FLOATING_VOICE_RESULT.equals(intent.getAction()))pendingFloatingVoiceQuery=intent.getStringExtra(EXTRA_FLOATING_VOICE_QUERY)==null?"":intent.getStringExtra(EXTRA_FLOATING_VOICE_QUERY).trim();
    }

    private void consumeFloatingVoiceQuery(){
        if(pendingFloatingVoiceQuery.isEmpty()||messageBox==null)return;
        String query=pendingFloatingVoiceQuery;pendingFloatingVoiceQuery="";
        uiHandler.postDelayed(()->handleMainVoiceCommand(query),180);
    }

    private void showLoginOrApp(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        String pin=p.getString(PIN_KEY,"");
        if(pin.isEmpty()){
            setContentView(buildUi());
            if(pendingFloatingVoiceQuery.isEmpty())uiHandler.postDelayed(this::showCreatePinDialog,300);else consumeFloatingVoiceQuery();
        }else if(p.getBoolean(LOGIN_ENABLED_KEY,true)){
            if(p.getBoolean(FINGERPRINT_UNLOCK_KEY,false))showBiometricUnlock(pin);else showUnlockDialog(pin);
        }else {setContentView(buildUi());consumeFloatingVoiceQuery();}
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
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(savedPin.equals(input.getText().toString().trim())){d.dismiss();setContentView(buildUi());consumeFloatingVoiceQuery();}else input.setError("Wrong PIN");}));
        d.show();
    }

    private boolean fingerprintAvailable(){
        if(Build.VERSION.SDK_INT<28)return false;try{FingerprintManager manager=(FingerprintManager)getSystemService(Context.FINGERPRINT_SERVICE);return manager!=null&&manager.isHardwareDetected()&&manager.hasEnrolledFingerprints();}catch(Exception e){return false;}
    }

    private void showBiometricUnlock(String savedPin){
        if(Build.VERSION.SDK_INT<28||!fingerprintAvailable()){toast("Fingerprint available nahi hai • App PIN use karein");showUnlockDialog(savedPin);return;}biometricFallbackShown=false;try{BiometricPrompt prompt=new BiometricPrompt.Builder(this).setTitle("LATHAEPS SMART").setSubtitle("App open karne ke liye fingerprint lagayein").setDescription("Fingerprint nahi chale to App PIN use karein").setNegativeButton("USE APP PIN",getMainExecutor(),(dialog,which)->{if(!biometricFallbackShown){biometricFallbackShown=true;showUnlockDialog(savedPin);}}).build();prompt.authenticate(new CancellationSignal(),getMainExecutor(),new BiometricPrompt.AuthenticationCallback(){@Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result){super.onAuthenticationSucceeded(result);biometricFallbackShown=true;setContentView(buildUi());consumeFloatingVoiceQuery();toast("Fingerprint unlock successful ✓");}@Override public void onAuthenticationFailed(){super.onAuthenticationFailed();toast("Fingerprint match nahi hua • dobara try karein");}@Override public void onAuthenticationError(int errorCode,CharSequence errString){super.onAuthenticationError(errorCode,errString);if(!biometricFallbackShown){biometricFallbackShown=true;showUnlockDialog(savedPin);}}});}catch(Exception e){showUnlockDialog(savedPin);}
    }

    @Override protected void onResume() {
        super.onResume();
        if(getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean(FloatingMicService.PREF_ENABLED,false)&&Settings.canDrawOverlays(this)){
            try{FloatingMicService.start(this);}catch(Exception ignored){}
        }
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
        checkForAppUpdate(false);
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

        Button mainPriceVoice=button("🎤  VOICE SEARCH");
        mainPriceVoice.setTextSize(16);mainPriceVoice.setTypeface(Typeface.DEFAULT_BOLD);mainPriceVoice.setTextColor(Color.WHITE);mainPriceVoice.setBackground(rounded(Color.rgb(21,73,126),15));
        LinearLayout.LayoutParams priceVoiceLp=new LinearLayout.LayoutParams(-1,dp(48));priceVoiceLp.setMargins(dp(2),dp(3),dp(2),dp(3));root.addView(mainPriceVoice,priceVoiceLp);
        mainPriceVoice.setOnClickListener(v->startPriceVoiceSearch(true));

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
        ScrollView scroll=new ScrollView(this);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(4),dp(14),dp(14));scroll.addView(box);
        TextView tip=new TextView(this);tip.setText("LIVE EDITOR • Text/sticker ko preview par finger se drag karein");tip.setTextSize(12);tip.setTypeface(Typeface.DEFAULT_BOLD);tip.setTextColor(Color.rgb(35,105,175));tip.setGravity(Gravity.CENTER);tip.setPadding(0,0,0,dp(6));box.addView(tip);
        ImageView preview=new ImageView(this);preview.setImageBitmap(original);preview.setAdjustViewBounds(true);preview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);preview.setBackgroundColor(Color.rgb(235,235,235));box.addView(preview,new LinearLayout.LayoutParams(-1,dp(270)));

        TextView cropLabel=new TextView(this);cropLabel.setText("Crop Image");cropLabel.setTypeface(Typeface.DEFAULT_BOLD);cropLabel.setPadding(0,dp(7),0,0);box.addView(cropLabel);
        Spinner crop=new Spinner(this);String[] cropModes={"Original","Square 1:1","Portrait 4:5","Story 9:16","Landscape 16:9"};crop.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cropModes));box.addView(crop,new LinearLayout.LayoutParams(-1,dp(48)));

        EditText overlay=new EditText(this);overlay.setHint("Image ke upar text likhein (2 lines)");overlay.setSingleLine(false);overlay.setMinLines(2);overlay.setMaxLines(2);overlay.setGravity(Gravity.TOP|Gravity.LEFT);overlay.setTextSize(17);overlay.setPadding(dp(12),dp(8),dp(12),dp(8));box.addView(overlay,new LinearLayout.LayoutParams(-1,dp(70)));

        TextView stickerLabel=new TextView(this);stickerLabel.setText("Emoji / Sticker");stickerLabel.setTypeface(Typeface.DEFAULT_BOLD);stickerLabel.setPadding(0,dp(9),0,dp(3));box.addView(stickerLabel);
        String[] stickerValue={""};String[] choices={"NONE","❤️","⭐","⚡","🎉","👍","🙏","✅","🔥","💐","😊","📞","💰"};List<Button> stickerButtons=new ArrayList<>();
        LinearLayout stickers=row();stickers.setPadding(0,0,dp(4),0);android.widget.HorizontalScrollView stickerScroll=new android.widget.HorizontalScrollView(this);stickerScroll.setHorizontalScrollBarEnabled(false);stickerScroll.addView(stickers);
        box.addView(stickerScroll,new LinearLayout.LayoutParams(-1,dp(50)));

        TextView colorLabel=new TextView(this);colorLabel.setText("Text Color");colorLabel.setTypeface(Typeface.DEFAULT_BOLD);colorLabel.setPadding(0,dp(8),0,dp(3));box.addView(colorLabel);
        String[] colorValue={"White"};String[] colors={"White","Yellow","Red","Black","Blue","Green","Pink","Orange"};int[] colorChips={Color.WHITE,Color.YELLOW,Color.RED,Color.BLACK,Color.rgb(30,100,240),Color.rgb(0,155,80),Color.rgb(235,45,130),Color.rgb(255,125,0)};
        LinearLayout colorRow=row();colorRow.setGravity(Gravity.CENTER_VERTICAL);android.widget.HorizontalScrollView colorScroll=new android.widget.HorizontalScrollView(this);colorScroll.setHorizontalScrollBarEnabled(false);colorScroll.addView(colorRow);box.addView(colorScroll,new LinearLayout.LayoutParams(-1,dp(46)));

        Spinner position=new Spinner(this);String[] positions={"Top","Center","Bottom","Manual (drag on image)"};position.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,positions));
        Spinner size=new Spinner(this);String[] sizes={"Medium","Large","Small"};size.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sizes));
        TextView positionLabel=new TextView(this);positionLabel.setText("Text Position                         Text Size");positionLabel.setTypeface(Typeface.DEFAULT_BOLD);positionLabel.setPadding(0,dp(7),0,0);box.addView(positionLabel);
        LinearLayout options=row();options.addView(position,weighted(1f,48));options.addView(size,weighted(1f,48));box.addView(options);

        float[] manualPosition={.5f,.18f};
        Runnable refresh=()->preview.setImageBitmap(renderEditedBitmap(original,overlay.getText().toString().trim(),stickerValue[0],colorValue[0],sizes[size.getSelectedItemPosition()],cropModes[crop.getSelectedItemPosition()],manualPosition[0],manualPosition[1]));
        for(String s:choices){Button b=button(s);b.setTextSize(s.equals("NONE")?10:22);b.setAllCaps(false);stickerButtons.add(b);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(s.equals("NONE")?58:48),dp(44));lp.setMargins(0,0,dp(5),0);stickers.addView(b,lp);b.setOnClickListener(v->{stickerValue[0]=s.equals("NONE")?"":s;for(int i=0;i<stickerButtons.size();i++)stickerButtons.get(i).setAlpha(choices[i].equals(s)?1f:.55f);refresh.run();});}
        stickerButtons.get(0).setAlpha(1f);for(int i=1;i<stickerButtons.size();i++)stickerButtons.get(i).setAlpha(.55f);
        for(int i=0;i<colors.length;i++){final int index=i;Button chip=button(i==0?"✓":"");chip.setTextColor(i==3?Color.WHITE:Color.BLACK);chip.setTextSize(17);chip.setBackground(rounded(colorChips[i],30));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(43),dp(39));lp.setMargins(0,0,dp(8),0);colorRow.addView(chip,lp);chip.setOnClickListener(v->{colorValue[0]=colors[index];for(int j=0;j<colorRow.getChildCount();j++)((Button)colorRow.getChildAt(j)).setText(j==index?"✓":"");refresh.run();});}
        overlay.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh.run();}public void afterTextChanged(Editable e){}});
        position.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){if(pos==0){manualPosition[0]=.5f;manualPosition[1]=.18f;}else if(pos==1){manualPosition[0]=.5f;manualPosition[1]=.52f;}else if(pos==2){manualPosition[0]=.5f;manualPosition[1]=.84f;}refresh.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}});
        android.widget.AdapterView.OnItemSelectedListener redrawListener=new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refresh.run();}public void onNothingSelected(android.widget.AdapterView<?> p){}};size.setOnItemSelectedListener(redrawListener);crop.setOnItemSelectedListener(redrawListener);
        preview.setOnTouchListener((v,event)->{int action=event.getActionMasked();if(action!=android.view.MotionEvent.ACTION_DOWN&&action!=android.view.MotionEvent.ACTION_MOVE)return false;try{android.graphics.Matrix inverse=new android.graphics.Matrix();if(!preview.getImageMatrix().invert(inverse))return true;float[] point={event.getX(),event.getY()};inverse.mapPoints(point);android.graphics.drawable.Drawable drawable=preview.getDrawable();if(!(drawable instanceof android.graphics.drawable.BitmapDrawable))return true;Bitmap shown=((android.graphics.drawable.BitmapDrawable)drawable).getBitmap();manualPosition[0]=Math.max(.06f,Math.min(.94f,point[0]/shown.getWidth()));manualPosition[1]=Math.max(.12f,Math.min(.90f,point[1]/shown.getHeight()));if(position.getSelectedItemPosition()!=3)position.setSelection(3);else refresh.run();}catch(Exception ignored){}return true;});

        AlertDialog d=new AlertDialog.Builder(this).setTitle("Premium Image Editor").setView(scroll).setPositiveButton("APPLY & USE",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{Uri edited=createEditedImage(original,overlay.getText().toString().trim(),stickerValue[0],colorValue[0],sizes[size.getSelectedItemPosition()],cropModes[crop.getSelectedItemPosition()],manualPosition[0],manualPosition[1]);if(edited==null){toast("Image edit save failed");return;}directSendImageUri=edited;if(directSendPreview!=null){directSendPreview.setImageURI(null);directSendPreview.setImageURI(edited);directSendPreview.setVisibility(View.VISIBLE);}if(directSendImageButton!=null)directSendImageButton.setText("CHANGE / EDIT IMAGE ✓");d.dismiss();}));d.show();
    }

    private Bitmap loadScaledBitmap(Uri uri,int maxSide){
        try{BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,bounds);}int sample=1;while(Math.max(bounds.outWidth,bounds.outHeight)/sample>maxSide)sample*=2;BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=Math.max(1,sample);try(InputStream in=getContentResolver().openInputStream(uri)){return BitmapFactory.decodeStream(in,null,options);}}catch(Exception e){return null;}
    }

    private Uri createEditedImage(Bitmap original,String text,String sticker,String colorName,String sizeName,String cropMode,float xPosition,float yPosition){
        try{Bitmap output=renderEditedBitmap(original,text,sticker,colorName,sizeName,cropMode,xPosition,yPosition);
            File dir=new File(getFilesDir(),"edited_images");if(!dir.exists()&&!dir.mkdirs())return null;File out=new File(dir,"edited_"+System.currentTimeMillis()+".jpg");try(FileOutputStream stream=new FileOutputStream(out)){output.compress(Bitmap.CompressFormat.JPEG,92,stream);}return FileProvider.getUriForFile(this,getPackageName()+".fileprovider",out);
        }catch(Exception e){return null;}
    }

    private Bitmap renderEditedBitmap(Bitmap original,String text,String sticker,String colorName,String sizeName,String cropMode,float xPosition,float yPosition){
        Bitmap output=cropBitmapForEditor(original,cropMode);Canvas canvas=new Canvas(output);float scale=output.getWidth()/720f;float textSize=("Large".equals(sizeName)?62:"Small".equals(sizeName)?34:48)*Math.max(.65f,scale);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));paint.setTextSize(textSize);paint.setTextAlign(Paint.Align.CENTER);paint.setColor(editorTextColor(colorName));paint.setShadowLayer(Math.max(3,4*scale),0,Math.max(2,2*scale),Color.BLACK);
        float baseY=output.getHeight()*yPosition;float center=output.getWidth()*xPosition;String safeText=text==null?"":text;
        if(sticker!=null&&!sticker.isEmpty()){Paint sp=new Paint(Paint.ANTI_ALIAS_FLAG);sp.setTextAlign(Paint.Align.CENTER);sp.setTextSize(textSize*1.75f);canvas.drawText(sticker,center,safeText.isEmpty()?baseY:baseY-textSize*1.25f,sp);}
        if(!safeText.isEmpty()){String[] lines=safeText.split("\\n",2);float firstY=baseY-(lines.length-1)*textSize*.58f;for(int i=0;i<lines.length;i++){String line=lines[i];float y=firstY+i*textSize*1.15f;float width=Math.min(output.getWidth()*.92f,paint.measureText(line)+textSize*.55f);Paint bg=new Paint(Paint.ANTI_ALIAS_FLAG);bg.setColor(Color.argb(110,0,0,0));canvas.drawRoundRect(center-width/2,y-textSize*.9f,center+width/2,y+textSize*.25f,textSize*.22f,textSize*.22f,bg);canvas.drawText(line,center,y,paint);}}
        return output;
    }

    private Bitmap cropBitmapForEditor(Bitmap source,String mode){
        if(source==null)return null;float ratio=0f;if("Square 1:1".equals(mode))ratio=1f;else if("Portrait 4:5".equals(mode))ratio=4f/5f;else if("Story 9:16".equals(mode))ratio=9f/16f;else if("Landscape 16:9".equals(mode))ratio=16f/9f;if(ratio<=0f)return source.copy(Bitmap.Config.ARGB_8888,true);
        int width=source.getWidth(),height=source.getHeight(),left=0,top=0,newWidth=width,newHeight=height;float current=width/(float)height;if(current>ratio){newWidth=Math.max(1,Math.round(height*ratio));left=(width-newWidth)/2;}else{newHeight=Math.max(1,Math.round(width/ratio));top=(height-newHeight)/2;}Bitmap cropped=Bitmap.createBitmap(source,left,top,newWidth,newHeight);return cropped.copy(Bitmap.Config.ARGB_8888,true);
    }

    private int editorTextColor(String name){
        if("Yellow".equals(name))return Color.YELLOW;if("Red".equals(name))return Color.RED;if("Black".equals(name))return Color.BLACK;if("Blue".equals(name))return Color.rgb(30,100,240);if("Green".equals(name))return Color.rgb(0,155,80);if("Pink".equals(name))return Color.rgb(235,45,130);if("Orange".equals(name))return Color.rgb(255,125,0);return Color.WHITE;
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
        preview.append("\n\nSafety: Individual chats only • WhatsApp groups blocked");
        new AlertDialog.Builder(this).setTitle("Confirm Auto Send • Individual Only")
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
        if(requestCode==PICK_PRICE_SOURCE_FILE&&resultCode==RESULT_OK&&data!=null)savePriceSourceSelections(data);
        if(requestCode==PICK_PRICE_REPLACE_FILE&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null)replacePriceSourceFile(data.getData());
        if(requestCode==PRICE_VOICE_SEARCH){if(resultCode==RESULT_OK&&data!=null){ArrayList<String> heard=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(heard!=null&&!heard.isEmpty()){String spoken=heard.get(0);if(priceVoiceFromHome)handleMainVoiceCommand(spoken);else if(priceListSearchBox==null)showPriceListScreen(spoken);else{priceListSearchBox.setText(spoken);priceListSearchBox.setSelection(priceListSearchBox.length());toast("Searching: "+spoken);}}}else toast("Voice search cancelled • tap 🎤 and try again");priceVoiceFromHome=false;}
    }
    private void sharePdf(){if(pdfUri==null){toast("Choose PDF first");return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,pdfUri);i.putExtra(Intent.EXTRA_TEXT,buildFinalMessage());i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{i.setPackage("com.whatsapp");startActivity(i);}catch(Exception e){i.setPackage(null);startActivity(Intent.createChooser(i,"Share PDF"));}}

    private void createNotificationChannel(){if(Build.VERSION.SDK_INT>=26){NotificationManager manager=getSystemService(NotificationManager.class);NotificationChannel c=new NotificationChannel(CHANNEL_ID,"Sending progress",NotificationManager.IMPORTANCE_LOW);c.setDescription("Compact queue progress");c.setSound(null,null);manager.createNotificationChannel(c);NotificationChannel updates=new NotificationChannel(UPDATE_CHANNEL_ID,"App updates",NotificationManager.IMPORTANCE_DEFAULT);updates.setDescription("LATHAEPS Smart new version notifications");manager.createNotificationChannel(updates);}}
    private void showProgressNotification(int current,int total,String contact){updateProgressNotification(this,current,total,contact,current>=total);}
    static void updateProgressNotification(Context context,int current,int total,String contact,boolean completed){Intent launch=new Intent(context,MainActivity.class);PendingIntent p=PendingIntent.getActivity(context,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent cancelIntent=new Intent(context,AutoSendCancelReceiver.class).setAction(AutoSendCancelReceiver.ACTION_CANCEL);PendingIntent cancel=PendingIntent.getBroadcast(context,511,cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        int safeTotal=Math.max(1,total),safeCurrent=Math.max(0,Math.min(current,safeTotal));NotificationCompat.Builder b=new NotificationCompat.Builder(context,CHANNEL_ID).setSmallIcon(completed?android.R.drawable.stat_sys_upload_done:android.R.drawable.stat_sys_upload).setContentTitle(completed?"Completed "+total+"/"+total:"Sending "+safeCurrent+"/"+total).setContentText(contact).setOnlyAlertOnce(true).setOngoing(!completed).setPriority(NotificationCompat.PRIORITY_LOW).setProgress(safeTotal,safeCurrent,false).setContentIntent(p).setAutoCancel(completed);
        if(current<total)b.addAction(android.R.drawable.ic_menu_close_clear_cancel,"CANCEL AUTO SEND",cancel);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.notify(NOTIFICATION_ID,b.build());}
    private void cancelProgressNotification(){((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);}

    private void showUpdateSettings(){
        SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        Switch enabled=new Switch(this);enabled.setText("New update notifications");enabled.setTextSize(17);enabled.setChecked(prefs.getBoolean(UPDATE_CHECK_ENABLED_KEY,true));box.addView(enabled,new LinearLayout.LayoutParams(-1,dp(58)));
        TextView note=new TextView(this);note.setText("App daily GitHub Releases check karega. Update install karne ke liye user ko Android ka Install button dabana hoga.");note.setTextSize(13);note.setTextColor(Color.DKGRAY);note.setPadding(0,dp(5),0,dp(8));box.addView(note);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("LATHAEPS Smart Updates").setView(box).setPositiveButton("CHECK NOW",null).setNegativeButton("CLOSE",null).create();
        enabled.setOnCheckedChangeListener((v,on)->{prefs.edit().putBoolean(UPDATE_CHECK_ENABLED_KEY,on).apply();toast(on?"Update notifications ON":"Update notifications OFF");});
        d.setOnShowListener(v->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x->{d.dismiss();checkForAppUpdate(true);}));d.show();
    }

    private void checkForAppUpdate(boolean manual){
        SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);if(!manual&&!prefs.getBoolean(UPDATE_CHECK_ENABLED_KEY,true))return;long now=System.currentTimeMillis(),last=prefs.getLong(UPDATE_LAST_CHECK_KEY,0);if(!manual&&now-last<24L*60L*60L*1000L)return;prefs.edit().putLong(UPDATE_LAST_CHECK_KEY,now).apply();if(manual)toast("Checking latest version…");
        final String currentVersion=appVersion();new Thread(()->{HttpURLConnection connection=null;try{connection=(HttpURLConnection)new URL(UPDATE_API_URL).openConnection();connection.setConnectTimeout(10000);connection.setReadTimeout(10000);connection.setRequestProperty("Accept","application/vnd.github+json");connection.setRequestProperty("X-GitHub-Api-Version","2022-11-28");connection.setRequestProperty("User-Agent","LATHAEPS-Smart-Android");int code=connection.getResponseCode();if(code<200||code>=300)throw new Exception("Server "+code);StringBuilder raw=new StringBuilder();try(BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8))){String line;while((line=reader.readLine())!=null)raw.append(line);}JSONObject release=new JSONObject(raw.toString());String latest=release.optString("tag_name",release.optString("name",""));String pageUrl=release.optString("html_url","https://github.com/jainvikes6270-gif/msgbulk/releases/latest");String downloadUrl=pageUrl;JSONArray assets=release.optJSONArray("assets");if(assets!=null)for(int i=0;i<assets.length();i++){JSONObject asset=assets.optJSONObject(i);if(asset!=null&&asset.optString("name","").toLowerCase(Locale.ROOT).endsWith(".apk")){downloadUrl=asset.optString("browser_download_url",pageUrl);break;}}boolean newer=isNewerVersion(latest,currentVersion);String finalDownloadUrl=downloadUrl;uiHandler.post(()->{if(newer){if(manual||Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)showUpdateAvailableDialog(latest,finalDownloadUrl);else showUpdateAvailableNotification(latest,finalDownloadUrl);}else if(manual)toast("App already latest • v"+currentVersion);});}catch(Exception error){if(manual)uiHandler.post(()->toast("Update check failed • internet check karein"));}finally{if(connection!=null)connection.disconnect();}},"lathaeps-update-check").start();
    }

    private boolean isNewerVersion(String latest,String current){
        String[] a=latest.replaceFirst("^[vV]","").split("[^0-9]+"),b=current.replaceFirst("^[vV]","").split("[^0-9]+");int max=Math.max(a.length,b.length);for(int i=0;i<max;i++){int x=i<a.length&&!a[i].isEmpty()?safeVersionPart(a[i]):0,y=i<b.length&&!b[i].isEmpty()?safeVersionPart(b[i]):0;if(x!=y)return x>y;}return false;
    }
    private int safeVersionPart(String value){try{return Integer.parseInt(value);}catch(Exception ignored){return 0;}}

    private void showUpdateAvailableDialog(String version,String url){
        new AlertDialog.Builder(this)
                .setTitle("LATHAEPS Smart update available")
                .setMessage("New version "+version+" ready hai. DOWNLOAD NOW dabakar signed APK download karein.")
                .setPositiveButton("DOWNLOAD NOW",(dialog,which)->{
                    try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
                    catch(Exception error){toast("Download open nahi hua");}
                })
                .setNegativeButton("LATER",null)
                .show();
    }

    private void showUpdateAvailableNotification(String version,String url){
        Intent open=new Intent(Intent.ACTION_VIEW,Uri.parse(url));PendingIntent update=PendingIntent.getActivity(this,UPDATE_NOTIFICATION_ID,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent laterIntent=new Intent(this,UpdateNotificationReceiver.class).setAction(UpdateNotificationReceiver.ACTION_LATER);PendingIntent later=PendingIntent.getBroadcast(this,UPDATE_NOTIFICATION_ID,laterIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification=new NotificationCompat.Builder(this,UPDATE_CHANNEL_ID).setSmallIcon(android.R.drawable.stat_sys_download_done).setContentTitle("LATHAEPS Smart update available").setContentText("Version "+version+" ready hai • tap karke update karein").setStyle(new NotificationCompat.BigTextStyle().bigText("LATHAEPS Smart ka naya version "+version+" available hai. Update Now se APK download karein.")).setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).setContentIntent(update).addAction(android.R.drawable.stat_sys_download_done,"UPDATE NOW",update).addAction(android.R.drawable.ic_menu_recent_history,"LATER",later);
        NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);if(manager!=null)manager.notify(UPDATE_NOTIFICATION_ID,notification.build());
    }

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
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);Button back=button("‹");back.setTextSize(30);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Settings");title.setTextColor(Color.WHITE);title.setTextSize(24);title.setTypeface(Typeface.DEFAULT_BOLD);title.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(6),0,dp(8),0);head.setBackground(rounded(Color.rgb(0,91,78),18));head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);back.setOnClickListener(v->d.dismiss());title.setOnLongClickListener(v->{startActivity(new Intent(this,AdminActivity.class));return true;});
        ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(0,dp(14),0,dp(20));scroll.addView(list);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        addSettingsButton(list,"◐  Dark / Light Theme",isDark()?"Currently Dark":"Currently Light",v->{getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(DARK_KEY,!isDark()).apply();d.dismiss();recreate();});
        boolean floatingMicOn=getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean(FloatingMicService.PREF_ENABLED,false)&&Settings.canDrawOverlays(this);
        addSettingsButton(list,"🎤  Floating Voice Mic",floatingMicOn?"ON • app ke bahar draggable voice shortcut":"OFF • tap karke floating shortcut ON karein",v->showFloatingMicSettings());
        addSettingsButton(list,"ⓘ  Current Version","LathaBulk v"+appVersion(),v->new AlertDialog.Builder(this).setTitle("Current Version").setMessage("LathaBulk v"+appVersion()+"\nLATHAEPS SMART").setPositiveButton("OK",null).show());
        addSettingsButton(list,"↗  Share App APK","Direct APK share karein • GitHub username ya source code nahi dikhega",v->shareApp());
        addSettingsButton(list,"₹  Subscription & Payment","Plan details, UPI payment & activation",v->startActivity(new Intent(this,SubscriptionActivity.class)));
        addSettingsButton(list,"👥  Contact Settings","Queue controls, Do Not Send & recipient list templates",v->{d.dismiss();showContactSettingsScreen();});
        addSettingsButton(list,"☀  Screen-off Auto Send","Keeps screen awake while bulk sending",v->showScreenOffHelp());
        addSettingsButton(list,"✉  Contact Us","lathaeps@gmail.com",v->contactSupport());
        addSettingsButton(list,"🔒  Login & Forgot PIN","Change PIN, recovery word, login ON/OFF",v->showLoginSettings());
        addSettingsButton(list,"☝  Fingerprint App Unlock",getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean(FINGERPRINT_UNLOCK_KEY,false)?"ON • app fingerprint se open hoga":"OFF • App PIN fallback ke saath",v->showFingerprintSettings());
        addSettingsButton(list,"☁  Drive Backup","Choose Google Drive in the save window",v->createDriveBackupFile());
        addSettingsButton(list,"⬆  App Updates",getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean(UPDATE_CHECK_ENABLED_KEY,true)?"New version notifications ON • tap for update settings":"New version notifications OFF • tap to change",v->showUpdateSettings());
        addSettingsButton(list,"🗑  Clear All Data","Remove contacts, recipient lists, catalogs, rules and files",v->confirmClearAllData(d));
        d.setContentView(page);d.show();
    }

    private void addSettingsButton(LinearLayout parent,String title,String subtitle,View.OnClickListener click){LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(18),dp(13),dp(14),dp(12));card.setBackground(rounded(isDark()?Color.rgb(45,50,54):Color.WHITE,18));TextView a=new TextView(this);a.setText(title);a.setTextSize(18);a.setTypeface(Typeface.DEFAULT_BOLD);a.setTextColor(isDark()?Color.WHITE:Color.rgb(10,65,59));TextView b=new TextView(this);b.setText(subtitle);b.setTextSize(13);b.setTextColor(isDark()?Color.LTGRAY:Color.DKGRAY);b.setPadding(0,dp(4),0,0);card.addView(a);card.addView(b);card.setOnClickListener(click);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(82));lp.setMargins(0,0,0,dp(10));parent.addView(card,lp);}

    private void showFloatingMicSettings(){
        SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(6),dp(18),dp(8));Switch enabled=new Switch(this);enabled.setText("Floating mic shortcut ON");enabled.setTextSize(17);enabled.setChecked(prefs.getBoolean(FloatingMicService.PREF_ENABLED,false)&&Settings.canDrawOverlays(this));box.addView(enabled,new LinearLayout.LayoutParams(-1,dp(58)));TextView help=new TextView(this);help.setText("Mic ko screen par kahin bhi drag karein. Tap karte hi voice search khulega. OFF karne par bubble aur uski notification dono hat jayenge.");help.setTextSize(14);help.setTextColor(Color.DKGRAY);help.setPadding(0,dp(8),0,dp(8));box.addView(help);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Floating Voice Mic").setView(box).setPositiveButton("DONE",null).create();enabled.setOnCheckedChangeListener((button,on)->{if(on){prefs.edit().putBoolean(FloatingMicService.PREF_ENABLED,true).apply();if(!Settings.canDrawOverlays(this)){toast("Display over other apps permission Allow karein");try{startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}}else{try{FloatingMicService.start(this);toast("Floating voice mic ON ✓");}catch(Exception e){toast("Floating mic start nahi hua • permission check karein");}}}else{FloatingMicService.stop(this);toast("Floating voice mic OFF");}});dialog.show();
    }

    private void showFingerprintSettings(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);if(Build.VERSION.SDK_INT<28){new AlertDialog.Builder(this).setTitle("Fingerprint Unlock").setMessage("Is phone Android version par secure fingerprint prompt supported nahi hai. App PIN available rahega.").setPositiveButton("OK",null).show();return;}LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(4),dp(18),dp(8));Switch enabled=new Switch(this);enabled.setText("Open app with fingerprint");enabled.setTextSize(17);enabled.setChecked(p.getBoolean(FINGERPRINT_UNLOCK_KEY,false));box.addView(enabled,new LinearLayout.LayoutParams(-1,dp(58)));TextView note=new TextView(this);note.setText("Fingerprint fail ya cancel hone par existing 4-digit App PIN se login kar sakte hain.");note.setTextSize(14);note.setTextColor(Color.DKGRAY);note.setPadding(0,dp(6),0,dp(8));box.addView(note);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Fingerprint App Unlock").setView(box).setPositiveButton("DONE",null).create();enabled.setOnCheckedChangeListener((button,on)->{if(on){if(p.getString(PIN_KEY,"").isEmpty()){button.setChecked(false);toast("Pehle 4-digit App PIN create karein");return;}if(!fingerprintAvailable()){button.setChecked(false);new AlertDialog.Builder(this).setTitle("Fingerprint not ready").setMessage("Phone Settings me fingerprint add karke phir option ON karein.").setPositiveButton("OPEN SECURITY SETTINGS",(d,w)->{try{startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));}catch(Exception ignored){}}).setNegativeButton("CLOSE",null).show();return;}p.edit().putBoolean(FINGERPRINT_UNLOCK_KEY,true).putBoolean(LOGIN_ENABLED_KEY,true).apply();toast("Fingerprint App Unlock ON ✓");}else{p.edit().putBoolean(FINGERPRINT_UNLOCK_KEY,false).apply();toast("Fingerprint App Unlock OFF • App PIN active");}});dialog.show();
    }

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
    private String appVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "3.23.56";}}

    private void shareApp(){
        try{
            File folder=new File(getCacheDir(),"shared_apk");
            if(!folder.exists()&&!folder.mkdirs())throw new Exception("Share folder unavailable");
            File apk=new File(folder,"LATHAEPS_Smart_v"+appVersion()+".apk");
            try(InputStream in=new FileInputStream(getApplicationInfo().sourceDir);OutputStream out=new FileOutputStream(apk)){
                byte[] buffer=new byte[64*1024];int read;
                while((read=in.read(buffer))!=-1)out.write(buffer,0,read);
            }
            Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",apk);
            Intent share=new Intent(Intent.ACTION_SEND);
            share.setType("application/vnd.android.package-archive");
            share.putExtra(Intent.EXTRA_STREAM,uri);
            share.putExtra(Intent.EXTRA_TEXT,"LATHAEPS Smart App v"+appVersion());
            share.setClipData(android.content.ClipData.newRawUri("LATHAEPS Smart APK",uri));
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share,"Share LATHAEPS Smart App APK"));
        }catch(Exception e){toast("APK share nahi hua: "+e.getMessage());}
    }
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
    private void clearAllUserData(){SharedPreferences main=getSharedPreferences(PREFS,MODE_PRIVATE);String pin=main.getString(PIN_KEY,"");String recovery=main.getString(RECOVERY_KEY,"");boolean login=main.getBoolean(LOGIN_ENABLED_KEY,true);boolean dark=main.getBoolean(DARK_KEY,false);main.edit().clear().putString(PIN_KEY,pin).putString(RECOVERY_KEY,recovery).putBoolean(LOGIN_ENABLED_KEY,login).putBoolean(DARK_KEY,dark).apply();getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().clear().apply();getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).edit().clear().apply();getSharedPreferences("lathaeps_quotation_manager",MODE_PRIVATE).edit().clear().apply();deleteAppFiles(getFilesDir());selectedNumbers.clear();allContacts.clear();visibleContacts.clear();toast("All data cleared • PIN kept safe");}
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
        try{JSONObject root=new JSONObject();root.put("app","LathaBulk");root.put("version",appVersion());root.put("created",System.currentTimeMillis());root.put(PREFS,prefsToJson(PREFS));root.put(AUTO_PREFS,prefsToJson(AUTO_PREFS));root.put(AutoReplyNotificationService.PREFS,prefsToJson(AutoReplyNotificationService.PREFS));root.put("quotation_prefs",prefsToJson("lathaeps_quotation_manager"));JSONObject files=new JSONObject();addFilesToBackup(getFilesDir(),"",files);root.put("files",files);try(OutputStream out=getContentResolver().openOutputStream(uri)){out.write(root.toString(2).getBytes(StandardCharsets.UTF_8));}toast("Backup saved • recipient lists, catalogs, quotations, images, keywords & templates included");}catch(Exception e){toast("Backup failed: "+e.getMessage());}
    }
    private void restoreBackup(Uri uri){
        try{StringBuilder b=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri),StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)b.append(line);}JSONObject root=new JSONObject(b.toString());if(!"LathaBulk".equals(root.optString("app")))throw new Exception("Invalid backup file");jsonToPrefs(PREFS,root.getJSONObject(PREFS));jsonToPrefs(AUTO_PREFS,root.getJSONObject(AUTO_PREFS));jsonToPrefs(AutoReplyNotificationService.PREFS,root.getJSONObject(AutoReplyNotificationService.PREFS));if(root.has("quotation_prefs"))jsonToPrefs("lathaeps_quotation_manager",root.getJSONObject("quotation_prefs"));if(root.has("files"))restoreFiles(root.getJSONObject("files"));toast("Restore complete");uiHandler.postDelayed(this::recreate,600);}catch(Exception e){toast("Restore failed: "+e.getMessage());}
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
            if(pendingRuleImageEnabled!=null){
                pendingRuleImageUri=safe.toString();
                pendingRuleImageType=mime==null?"image/*":mime;
                pendingRuleImageEnabled.setChecked(true);
                if(pendingRuleImageButton!=null)pendingRuleImageButton.setText("Change this rule image ✓");
            }
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
    private void showCatalogScreen(){showCatalogScreen("");}
    private void showCatalogScreen(String initialSearch){
        Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(Color.BLACK);
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(10),0,dp(12),0);head.setBackgroundColor(Color.rgb(28,26,27));Button back=button("‹");back.setTextSize(36);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Catalog");title.setTextSize(25);title.setTypeface(Typeface.DEFAULT_BOLD);title.setTextColor(Color.WHITE);head.addView(back,new LinearLayout.LayoutParams(dp(56),dp(72)));head.addView(title,new LinearLayout.LayoutParams(0,dp(72),1f));page.addView(head);
        EditText search=new EditText(this);search.setHint("Search Catalog name, type or auto-reply word");search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(Color.GRAY);search.setPadding(dp(16),0,dp(16),0);if(initialSearch!=null&&!initialSearch.trim().isEmpty())search.setText(initialSearch);page.addView(search,new LinearLayout.LayoutParams(-1,dp(50)));
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
        Dialog dialog=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(Color.rgb(239,249,248));
        LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(12),dp(8),dp(10),dp(8));header.setBackgroundColor(Color.rgb(0,91,78));
        Button back=button("‹");back.setTextSize(30);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);
        TextView title=new TextView(this);title.setText("Business Files");title.setTextColor(Color.WHITE);title.setTextSize(23);title.setTypeface(Typeface.DEFAULT_BOLD);title.setGravity(Gravity.CENTER_VERTICAL);
        TextView badge=new TextView(this);badge.setText("LEDGER");badge.setTextSize(11);badge.setTextColor(Color.rgb(0,91,78));badge.setGravity(Gravity.CENTER);badge.setTypeface(Typeface.DEFAULT_BOLD);badge.setBackground(rounded(Color.rgb(210,244,238),30));
        header.addView(back,new LinearLayout.LayoutParams(dp(48),dp(54)));header.addView(title,new LinearLayout.LayoutParams(0,dp(54),1f));header.addView(badge,new LinearLayout.LayoutParams(dp(72),dp(32)));page.addView(header);
        back.setOnClickListener(v->dialog.dismiss());
        ScrollView scroll=new ScrollView(this);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(14),dp(16),dp(24));scroll.addView(box);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        Button customers=button("LEDGER PARTY LISTS ("+ledgerCustomerCount()+" CUSTOMERS)");
        Button ledgerFolder=button("📁  LEDGER & CONTACTS");
        Button paymentReminder=button("PAYMENT REMINDER");
        Button quotation=button("BUSINESS QUOTATION MANAGER");
        Button priceList=button("PRICE LIST MANAGER");
        Button[] featureButtons={quotation,priceList,paymentReminder,ledgerFolder,customers};for(Button feature:featureButtons){feature.setTypeface(Typeface.DEFAULT_BOLD);feature.setTextColor(Color.rgb(0,91,78));feature.setBackground(rounded(Color.rgb(210,244,238),16));}
        quotation.setTextColor(Color.WHITE);quotation.setBackground(rounded(Color.rgb(111,45,165),16));
        priceList.setTextColor(Color.WHITE);priceList.setBackground(rounded(Color.rgb(0,91,78),16));paymentReminder.setTextColor(Color.WHITE);paymentReminder.setBackground(rounded(Color.rgb(18,128,78),16));
        box.addView(quotation,new LinearLayout.LayoutParams(-1,dp(52)));box.addView(priceList,new LinearLayout.LayoutParams(-1,dp(50)));box.addView(paymentReminder,new LinearLayout.LayoutParams(-1,dp(50)));box.addView(ledgerFolder,new LinearLayout.LayoutParams(-1,dp(54)));box.addView(customers,new LinearLayout.LayoutParams(-1,dp(46)));
        paymentReminder.setOnClickListener(v->showPaymentReminderScreen());
        quotation.setOnClickListener(v->startActivity(new Intent(this,QuotationActivity.class)));
        priceList.setOnClickListener(v->showPriceListScreen());
        ledgerFolder.setOnClickListener(v->showMasterLedgerFolderDialog());
        customers.setOnClickListener(v->showLedgerListsScreen());
        TextView help=new TextView(this);help.setText("Master PDF, Excel import, file history aur Auto Reply settings ab Master Ledger folder ke andar hain.");help.setTextSize(13);help.setTextColor(Color.DKGRAY);help.setPadding(dp(5),dp(12),dp(5),dp(8));box.addView(help);
        dialog.setContentView(page);dialog.show();
    }

    private void showMasterLedgerFolderDialog(){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);Dialog dialog=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(14),dp(10),dp(14),dp(14));page.setBackgroundColor(Color.rgb(239,249,248));
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.setBackground(rounded(Color.rgb(0,91,78),17));Button back=button("‹");back.setTextSize(31);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Ledger & Contacts");title.setTextSize(21);title.setTextColor(Color.WHITE);title.setTypeface(Typeface.DEFAULT_BOLD);head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);
        ScrollView scroll=new ScrollView(this);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(4),dp(12),dp(4),dp(24));scroll.addView(box);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));String savedLedgerName=p.getString(AutoReplyNotificationService.LEDGER_URI+"_name","");TextView fileLabel=new TextView(this);fileLabel.setText("SAVED MASTER LEDGER PDF");fileLabel.setTextSize(13);fileLabel.setTypeface(Typeface.DEFAULT_BOLD);fileLabel.setTextColor(Color.rgb(12,52,49));fileLabel.setPadding(dp(4),dp(4),dp(4),dp(5));box.addView(fileLabel);
        ledgerFileNameText=new TextView(this);ledgerFileNameText.setText(savedLedgerName.isEmpty()?"No Ledger file saved":savedLedgerName);ledgerFileNameText.setTextSize(17);ledgerFileNameText.setTypeface(Typeface.DEFAULT_BOLD);ledgerFileNameText.setTextColor(savedLedgerName.isEmpty()?Color.rgb(190,45,45):Color.rgb(0,125,70));ledgerFileNameText.setBackground(rounded(savedLedgerName.isEmpty()?Color.rgb(255,235,235):Color.rgb(225,248,235),12));ledgerFileNameText.setPadding(dp(12),dp(10),dp(12),dp(10));box.addView(ledgerFileNameText,new LinearLayout.LayoutParams(-1,-2));TextView current=new TextView(this);current.setPadding(dp(12),dp(10),dp(12),dp(10));current.setText("Customers: "+ledgerCustomerCount()+"\nLast status: "+p.getString("last_business_status","No send attempt yet"));current.setTextColor(Color.rgb(28,28,28));current.setBackground(rounded(Color.WHITE,16));LinearLayout.LayoutParams currentLp=new LinearLayout.LayoutParams(-1,-2);currentLp.setMargins(0,dp(8),0,dp(10));box.addView(current,currentLp);
        Button convertPdf=button("MASTER PDF → PHONE + BALANCE EXCEL");Button uploadPdf=button(savedLedgerName.isEmpty()?"UPLOAD & PREPARE MASTER LEDGER PDF":"UPDATE & PREPARE MASTER LEDGER PDF ✓");Button importExcel=button("IMPORT CONTACT EXCEL");Button history=button("VIEW FILE HISTORY");Button[] folderButtons={convertPdf,uploadPdf,importExcel,history};for(Button b:folderButtons){b.setTypeface(Typeface.DEFAULT_BOLD);b.setTextColor(Color.rgb(0,91,78));b.setBackground(rounded(Color.rgb(210,244,238),15));box.addView(b,new LinearLayout.LayoutParams(-1,dp(48)));}
        EditText ledgerKey=new EditText(this);ledgerKey.setHint("Auto Reply keyword • Example: ledger");ledgerKey.setText(p.getString(AutoReplyNotificationService.LEDGER_KEY,"ledger"));ledgerKey.setSingleLine(true);box.addView(ledgerKey,new LinearLayout.LayoutParams(-1,dp(54)));LinearLayout actions=row();Button save=button("SAVE SETTINGS");Button toggle=button("");save.setTypeface(Typeface.DEFAULT_BOLD);save.setTextColor(Color.WHITE);save.setBackground(rounded(Color.rgb(40,95,175),14));actions.addView(save,weighted(1f,52));actions.addView(toggle,weighted(1.15f,52));box.addView(actions);Runnable updateToggle=()->{boolean enabled=p.getBoolean(AutoReplyNotificationService.ENABLED,false);toggle.setText(enabled?"AUTO REPLY ON ✓":"AUTO REPLY OFF");toggle.setTextColor(Color.WHITE);toggle.setTypeface(Typeface.DEFAULT_BOLD);toggle.setBackground(rounded(enabled?Color.rgb(20,125,70):Color.rgb(185,55,55),14));};updateToggle.run();TextView note=new TextView(this);note.setText("SAVE SETTINGS sirf keyword save karega. AUTO REPLY button se service ON/OFF alag control hogi. Notification Access bhi ON hona chahiye.");note.setTextSize(13);note.setTextColor(Color.DKGRAY);note.setPadding(dp(5),dp(10),dp(5),0);box.addView(note);
        back.setOnClickListener(v->dialog.dismiss());convertPdf.setOnClickListener(v->chooseMasterPdfForExcel());uploadPdf.setOnClickListener(v->pickBusinessFile(PICK_LEDGER_FILE));importExcel.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");startActivityForResult(Intent.createChooser(i,"Select customer Excel"),PICK_LEDGER_CUSTOMERS_XLSX);});history.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("File update history").setMessage(p.getString("file_history","No file updates yet")).setPositiveButton("Close",null).setNeutralButton("Clear",(d,w)->p.edit().remove("file_history").apply()).show());save.setOnClickListener(v->{String keyword=ledgerKey.getText().toString().trim();if(keyword.isEmpty()){ledgerKey.setError("Keyword required");return;}p.edit().putString(AutoReplyNotificationService.LEDGER_KEY,keyword).apply();toast("Ledger settings saved");});toggle.setOnClickListener(v->{boolean enabled=!p.getBoolean(AutoReplyNotificationService.ENABLED,false);p.edit().putBoolean(AutoReplyNotificationService.ENABLED,enabled).apply();updateToggle.run();toast(enabled?"Auto Reply ON":"Auto Reply OFF");});dialog.setContentView(page);dialog.show();
    }

    private JSONArray readPriceList(){try{return new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(PRICE_LIST_ITEMS_KEY,"[]"));}catch(Exception e){return new JSONArray();}}
    private void writePriceList(JSONArray items){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PRICE_LIST_ITEMS_KEY,items.toString()).apply();}

    private void showPriceListScreen(){showPriceListScreen("");}
    private void showPriceListScreen(String initialSearch){
        Dialog d=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(12),dp(10),dp(12),dp(12));page.setBackgroundColor(Color.rgb(244,247,250));
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.setBackground(rounded(Color.rgb(21,73,126),17));
        Button back=button("‹");back.setTextSize(31);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);
        TextView title=new TextView(this);title.setText("Price List Manager");title.setTextSize(23);title.setTextColor(Color.WHITE);title.setTypeface(Typeface.DEFAULT_BOLD);
        head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);
        LinearLayout addRow=row();Button addBrand=button("+ BRAND");Button addSource=button("+ IMAGE / PDF");Button addItem=button("+ PRICE ITEM");addBrand.setTypeface(Typeface.DEFAULT_BOLD);addSource.setTypeface(Typeface.DEFAULT_BOLD);addItem.setTypeface(Typeface.DEFAULT_BOLD);addBrand.setTextColor(Color.rgb(160,82,20));addSource.setTextColor(Color.rgb(21,73,126));addItem.setTextColor(Color.rgb(0,110,65));addRow.addView(addBrand,weighted(.8f,46));addRow.addView(addSource,weighted(1.15f,46));addRow.addView(addItem,weighted(1f,46));page.addView(addRow);
        Button sharePrice=button("SHARE COMPLETE PRICE LIST");sharePrice.setTypeface(Typeface.DEFAULT_BOLD);sharePrice.setTextColor(Color.rgb(160,82,20));page.addView(sharePrice,new LinearLayout.LayoutParams(-1,dp(42)));
        final String[] selectedBrand={"ALL BRANDS"};Button brandFilter=button("BRAND: ALL BRANDS ▼");brandFilter.setTypeface(Typeface.DEFAULT_BOLD);brandFilter.setTextColor(Color.rgb(21,73,126));brandFilter.setBackground(rounded(Color.rgb(225,235,248),14));page.addView(brandFilter,new LinearLayout.LayoutParams(-1,dp(44)));
        LinearLayout searchRow=row();EditText search=new EditText(this);priceListSearchBox=search;search.setHint("AI Smart Search • type or speak");search.setSingleLine(true);search.setTextSize(15);search.setPadding(dp(14),0,dp(8),0);Button voice=button("🎤");voice.setTextSize(22);voice.setContentDescription("Voice search price list");searchRow.addView(search,new LinearLayout.LayoutParams(0,dp(52),1f));searchRow.addView(voice,new LinearLayout.LayoutParams(dp(58),dp(50)));page.addView(searchRow,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView summary=new TextView(this);summary.setTextSize(13);summary.setTextColor(Color.DKGRAY);summary.setPadding(dp(5),dp(5),dp(5),dp(5));page.addView(summary);
        ScrollView scroll=new ScrollView(this);LinearLayout cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);cards.setPadding(0,dp(5),0,dp(30));scroll.addView(cards);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        final Runnable[] refresh={null};refresh[0]=()->{JSONArray all=readPriceList(),sources=readPriceSources();summary.setText(all.length()+" items • "+collectPriceBrands().size()+" brand folders • "+countPriceSourceSections(sources)+" media sections • "+sources.length()+" files");renderPriceListCards(cards,search.getText().toString(),selectedBrand[0],d,brand->{selectedBrand[0]=brand;brandFilter.setText("BRAND: "+brand.toUpperCase(Locale.ROOT)+" ▼");refresh[0].run();});};pendingPriceListRefresh=refresh[0];
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh[0].run();}public void afterTextChanged(Editable e){}});
        brandFilter.setOnClickListener(v->{List<String> brands=collectPriceBrands();String[] choices=new String[brands.size()+1];choices[0]="ALL BRANDS";for(int i=0;i<brands.size();i++)choices[i+1]=brands.get(i);new AlertDialog.Builder(this).setTitle("Select Brand Folder").setSingleChoiceItems(choices,indexOfIgnoreCase(choices,selectedBrand[0]),(x,w)->{selectedBrand[0]=choices[w];brandFilter.setText("BRAND: "+selectedBrand[0].toUpperCase(Locale.ROOT)+" ▼");x.dismiss();refresh[0].run();}).setNegativeButton("CANCEL",null).show();});
        voice.setOnClickListener(v->startPriceVoiceSearch(false));back.setOnClickListener(v->{if(!selectedBrand[0].equalsIgnoreCase("ALL BRANDS")&&search.getText().toString().trim().isEmpty()){selectedBrand[0]="ALL BRANDS";brandFilter.setText("BRAND: ALL BRANDS ▼");refresh[0].run();}else d.dismiss();});addBrand.setOnClickListener(v->showAddPriceBrandDialog(refresh[0]));addSource.setOnClickListener(v->showAddPriceSourceDialog(selectedBrand[0].equalsIgnoreCase("ALL BRANDS")?"":selectedBrand[0],"",""));addItem.setOnClickListener(v->showPriceItemEditor(null,selectedBrand[0].equalsIgnoreCase("ALL BRANDS")?"":selectedBrand[0],refresh[0]));sharePrice.setOnClickListener(v->showPriceShareOptions());d.setOnDismissListener(v->{if(priceListSearchBox==search)priceListSearchBox=null;if(pendingPriceListRefresh==refresh[0])pendingPriceListRefresh=null;});if(initialSearch!=null&&!initialSearch.trim().isEmpty())search.setText(initialSearch);refresh[0].run();d.setContentView(page);d.show();
    }

    private void startPriceVoiceSearch(boolean fromHome){priceVoiceFromHome=fromHome;try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault().toLanguageTag());i.putExtra(RecognizerIntent.EXTRA_PROMPT,fromHome?"Say command • Polycab, Catalog, Business Files":"Say brand and item • Mylinc switch");i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);startActivityForResult(i,PRICE_VOICE_SEARCH);}catch(Exception e){priceVoiceFromHome=false;toast("Google voice search phone me available nahi hai");}}

    private void handleMainVoiceCommand(String spoken){
        String raw=spoken==null?"":spoken.trim(),command=raw.toLowerCase(Locale.ROOT).replace("बिजनेस","business").replace("फाइल्स","files").replace("कैटलॉग","catalog").replace("सेटिंग्स","settings").replace("बैकअप","backup").replace("रिस्टोर","restore").replace("पेमेंट","payment").replace("रिमाइंडर","reminder");
        if(command.contains("business file")){toast("Opening Business Files");showBusinessFilesDialog();return;}
        if(command.contains("auto reply")||command.contains("autoreply")){toast("Opening Auto Reply");showAutoReplyScreen();return;}
        if(command.contains("catalog")||command.contains("catlog")){String q=cleanVoiceCommandQuery(command,"catalog","catlog","open","khol","kholo","dikhao","show");toast(q.isEmpty()?"Opening Catalog":"Catalog search: "+q);showCatalogScreen(q);return;}
        if(command.contains("payment")&&command.contains("reminder")){toast("Opening Payment Reminder");showPaymentReminderScreen();return;}
        if(command.contains("recipient")||command.contains("customer list")||command.contains("my list")){toast("Opening Recipient Lists");showRecipientListsScreen();return;}
        if(command.contains("setting")){toast("Opening Settings");showSettingsScreen();return;}
        if(command.contains("check update")||command.contains("update check")||command.contains("new update")){checkForAppUpdate(true);return;}
        if(command.contains("restore")){toast("Choose backup file to restore");chooseRestoreFile();return;}
        if(command.contains("backup")){toast("Choose location to save backup");createBackupFile();return;}
        if(command.contains("ledger")){String q=cleanVoiceCommandQuery(command,"ledger","customer","search","find","dhundo","dhoondo","dikhao","show","ka","ki","khol","kholo");toast(q.isEmpty()?"Opening Ledger Customers":"Ledger search: "+q);showLedgerCustomersDialog(q);return;}
        if(command.contains("share")||command.contains("send")||command.contains("bhejo")||command.contains("भेजो")){String q=cleanVoiceCommandQuery(command,"share","send","bhejo","भेजो","karo","kar do","please","price","list");if(q.isEmpty())showPriceListScreen();else confirmVoicePriceShare(q);return;}
        toast("Price search: "+raw);showPriceListScreen(raw);
    }

    private String cleanVoiceCommandQuery(String command,String... remove){String q=command;for(String word:remove){if(word.matches("[a-z0-9 ]+"))q=q.replaceAll("\\b"+Pattern.quote(word)+"\\b"," ");else q=q.replace(word," ");}return q.replaceAll("[^a-z0-9.%]+"," ").trim().replaceAll("\\s+"," ");}

    private void confirmVoicePriceShare(String query){
        String normalized=normalizePriceSearch(query);String[] words=normalized.isEmpty()?new String[0]:normalized.split(" ");Map<String,List<JSONObject>> matches=new LinkedHashMap<>();JSONArray all=readPriceSources();for(int i=0;i<all.length();i++){JSONObject source=all.optJSONObject(i);if(source==null)continue;String hay=source.optString("search_text",normalizePriceSearch(source.optString("name")+" "+priceBrand(source)+" "+priceCategory(source)+" "+source.optString("keywords")));boolean match=true;for(String word:words)if(!hay.contains(word)){match=false;break;}if(match)matches.computeIfAbsent(priceBrand(source)+" • "+priceCategory(source),k->new ArrayList<>()).add(source);}
        if(matches.isEmpty()){toast("Share ke liye matching image/PDF section nahi mila");showPriceListScreen(query);return;}String[] categories=matches.keySet().toArray(new String[0]);if(categories.length==1){String category=categories[0];confirmVoicePriceSectionShare(category,matches.get(category));return;}new AlertDialog.Builder(this).setTitle("Which section to share?").setItems(categories,(d,w)->confirmVoicePriceSectionShare(categories[w],matches.get(categories[w]))).setNegativeButton("CANCEL",null).show();
    }
    private void confirmVoicePriceSectionShare(String category,List<JSONObject> files){new AlertDialog.Builder(this).setTitle("Confirm Share").setMessage(category+" ke "+files.size()+" images/PDFs share karne hain?").setPositiveButton("SHARE",(d,w)->sharePriceSourceGroup(category,files)).setNegativeButton("CANCEL",null).show();}

    private String normalizePriceSearch(String value){
        String s=value==null?"":value.toLowerCase(Locale.ROOT).trim();
        s=s.replace("my link","mylinc").replace("my-link","mylinc").replace("mylink","mylinc").replace("my linc","mylinc").replace("माइलिंक","mylinc").replace("मायलिंक","mylinc").replace("माइलिन्क","mylinc").replace("स्विचेस","switch").replace("स्विच","switch").replace("दिखाओ"," ");
        s=s.replaceAll("\\bswitches\\b","switch").replaceAll("\\bwires\\b","wire").replaceAll("\\blights\\b","light");
        s=s.replaceAll("[^a-z0-9.%]+"," ").replaceAll("\\b(show|me|please|price|prices|list|dikhao|batao|ka|ki|ke|wala|wali|items?)\\b"," ");
        return s.trim().replaceAll("\\s+"," ");
    }

    private JSONArray readPriceSources(){try{return new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(PRICE_SOURCE_FILES_KEY,"[]"));}catch(Exception e){return new JSONArray();}}
    private void writePriceSources(JSONArray items){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PRICE_SOURCE_FILES_KEY,items.toString()).apply();}
    private JSONArray readPriceBrandFolders(){try{return new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(PRICE_BRAND_FOLDERS_KEY,"[]"));}catch(Exception e){return new JSONArray();}}
    private void writePriceBrandFolders(JSONArray brands){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PRICE_BRAND_FOLDERS_KEY,brands.toString()).apply();}

    private void showAddPriceBrandDialog(Runnable refresh){
        EditText brand=new EditText(this);brand.setHint("Brand name • Polycab, Finolex, Mylinc");brand.setSingleLine(true);brand.setPadding(dp(18),0,dp(18),0);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Create Brand Folder").setMessage("Is folder ke andar baad mein images, PDFs aur price items add kar sakte hain.").setView(brand).setPositiveButton("CREATE",null).setNegativeButton("CANCEL",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x->{String name=brand.getText().toString().trim();if(name.isEmpty()){brand.setError("Brand name required");return;}for(String old:collectPriceBrands())if(old.equalsIgnoreCase(name)){brand.setError("Brand folder already exists");return;}JSONArray folders=readPriceBrandFolders();folders.put(name);writePriceBrandFolders(folders);toast(name+" brand folder created ✓");dialog.dismiss();refresh.run();}));dialog.show();
    }

    private String priceBrand(JSONObject item){String b=item==null?"":item.optString("brand","").trim();if(b.isEmpty()&&item!=null)b=item.optString("category","Other").trim();return b.isEmpty()?"Other":b;}
    private String priceCategory(JSONObject item){String c=item==null?"":item.optString("category","Other").trim();return c.isEmpty()?"General":c;}
    private boolean priceBrandMatches(String selected,JSONObject item){return selected==null||selected.equalsIgnoreCase("ALL BRANDS")||selected.equalsIgnoreCase(priceBrand(item));}
    private List<String> collectPriceBrands(){java.util.TreeSet<String> brands=new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);JSONArray sources=readPriceSources(),items=readPriceList(),folders=readPriceBrandFolders();for(int i=0;i<folders.length();i++){String name=folders.optString(i,"").trim();if(!name.isEmpty())brands.add(name);}for(int i=0;i<sources.length();i++){JSONObject o=sources.optJSONObject(i);if(o!=null)brands.add(priceBrand(o));}for(int i=0;i<items.length();i++){JSONObject o=items.optJSONObject(i);if(o!=null)brands.add(priceBrand(o));}return new ArrayList<>(brands);}
    private int indexOfIgnoreCase(String[] values,String target){for(int i=0;i<values.length;i++)if(values[i].equalsIgnoreCase(target))return i;return 0;}

    private int countPriceSourceSections(JSONArray sources){
        Set<String> sections=new LinkedHashSet<>();for(int i=0;i<sources.length();i++){JSONObject o=sources.optJSONObject(i);if(o!=null)sections.add((priceBrand(o)+"|"+priceCategory(o)).toLowerCase(Locale.ROOT));}return sections.size();
    }

    private void showAddPriceSourceDialog(){showAddPriceSourceDialog("","","");}
    private void showAddPriceSourceDialog(String initialCategory,String initialKeywords){showAddPriceSourceDialog("",initialCategory,initialKeywords);}
    private void showAddPriceSourceDialog(String initialBrand,String initialCategory,String initialKeywords){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        EditText brand=new EditText(this);brand.setHint("Brand • Mylinc, Polycab, Finolex");brand.setSingleLine(true);brand.setText(initialBrand==null?"":initialBrand);EditText category=new EditText(this);category.setHint("Product Section • Switch, Wire, DB");category.setSingleLine(true);category.setText(initialCategory==null?"":initialCategory);EditText name=new EditText(this);name.setHint("Source name • optional");name.setSingleLine(true);EditText keywords=new EditText(this);keywords.setHint("Search words • switch, socket, plate");keywords.setSingleLine(false);keywords.setMinLines(2);keywords.setText(initialKeywords==null?"":initialKeywords);Button gallery=button("PICTURES FROM PHONE GALLERY");gallery.setTypeface(Typeface.DEFAULT_BOLD);Button pdf=button("PDF FROM FILES");pdf.setTypeface(Typeface.DEFAULT_BOLD);box.addView(brand);box.addView(category);box.addView(name);box.addView(keywords);LinearLayout.LayoutParams pickLp=new LinearLayout.LayoutParams(-1,dp(48));pickLp.setMargins(0,dp(8),0,dp(4));box.addView(gallery,pickLp);box.addView(pdf,new LinearLayout.LayoutParams(-1,dp(48)));
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Add Multiple Images / PDFs").setMessage("Pehle brand aur search words likhein, phir phone Gallery se pictures ya Files se PDFs select karein.").setView(box).setNegativeButton("CANCEL",null).create();
        gallery.setOnClickListener(v->{if(!preparePendingPriceSource(brand,category,name,keywords))return;d.dismiss();launchPriceGalleryPicker();});pdf.setOnClickListener(v->{if(!preparePendingPriceSource(brand,category,name,keywords))return;d.dismiss();launchPricePdfPicker();});d.show();
    }

    private boolean preparePendingPriceSource(EditText brand,EditText category,EditText name,EditText keywords){String b=brand.getText().toString().trim(),c=category.getText().toString().trim();if(b.isEmpty()){brand.setError("Brand required");brand.requestFocus();return false;}if(c.isEmpty()){category.setError("Product section required");category.requestFocus();return false;}pendingPriceSourceBrand=b;pendingPriceSourceCategory=c;pendingPriceSourceName=name.getText().toString().trim();pendingPriceSourceKeywords=keywords.getText().toString().trim();getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("pending_price_source_brand",pendingPriceSourceBrand).putString("pending_price_source_category",pendingPriceSourceCategory).putString("pending_price_source_name",pendingPriceSourceName).putString("pending_price_source_keywords",pendingPriceSourceKeywords).apply();return true;}

    private void launchPriceGalleryPicker(){Intent i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);i.setType("image/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(Intent.createChooser(i,"Select multiple pictures from Phone Gallery"),PICK_PRICE_SOURCE_FILE);}

    private void launchPricePdfPicker(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/pdf");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(Intent.createChooser(i,"Select multiple Price List PDFs"),PICK_PRICE_SOURCE_FILE);}

    private void savePriceSourceSelections(Intent data){
        LinkedHashMap<String,Uri> selected=new LinkedHashMap<>();ClipData clips=data.getClipData();if(clips!=null){for(int i=0;i<clips.getItemCount();i++){Uri u=clips.getItemAt(i).getUri();if(u!=null)selected.put(u.toString(),u);}}Uri single=data.getData();if(single!=null)selected.put(single.toString(),single);if(selected.isEmpty()){toast("Koi image / PDF select nahi hui");return;}
        int saved=0,failed=0,index=0,total=selected.size();String batchId=String.valueOf(System.currentTimeMillis());for(Uri uri:selected.values()){if(savePriceSourceFile(uri,index,total,batchId))saved++;else failed++;index++;}clearPendingPriceSource();if(pendingPriceListRefresh!=null)pendingPriceListRefresh.run();toast(saved+" of "+total+" files saved in section"+(failed>0?" • "+failed+" failed":" ✓"));
    }

    private void clearPendingPriceSource(){getSharedPreferences(PREFS,MODE_PRIVATE).edit().remove("pending_price_source_brand").remove("pending_price_source_category").remove("pending_price_source_name").remove("pending_price_source_keywords").apply();pendingPriceSourceName="";pendingPriceSourceBrand="";pendingPriceSourceCategory="";pendingPriceSourceKeywords="";}

    private boolean savePriceSourceFile(Uri source,int selectedIndex,int selectedTotal,String batchId){
        SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);if(pendingPriceSourceBrand.isEmpty())pendingPriceSourceBrand=prefs.getString("pending_price_source_brand","");if(pendingPriceSourceCategory.isEmpty())pendingPriceSourceCategory=prefs.getString("pending_price_source_category","");if(pendingPriceSourceName.isEmpty())pendingPriceSourceName=prefs.getString("pending_price_source_name","");if(pendingPriceSourceKeywords.isEmpty())pendingPriceSourceKeywords=prefs.getString("pending_price_source_keywords","");
        try{String mime=getContentResolver().getType(source);String original=getDisplayName(source);if(mime==null){String low=original.toLowerCase(Locale.ROOT);mime=low.endsWith(".pdf")?"application/pdf":low.endsWith(".png")?"image/png":low.endsWith(".webp")?"image/webp":"image/jpeg";}if(!mime.contains("pdf")&&!mime.startsWith("image/"))throw new Exception("Only PDF or image allowed");String ext=mime.contains("pdf")?"pdf":mime.contains("png")?"png":mime.contains("webp")?"webp":mime.contains("gif")?"gif":"jpg";File dir=new File(getFilesDir(),"business_files/price_sources");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Price source folder unavailable");long now=System.currentTimeMillis();File target=new File(dir,"price_source_"+now+"_"+selectedIndex+"."+ext);try(InputStream in=getContentResolver().openInputStream(source);OutputStream out=new FileOutputStream(target)){if(in==null)throw new Exception("Source read failed");byte[] buf=new byte[16*1024];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}String extracted="";if(mime.contains("pdf")){try(PDDocument document=PDDocument.load(target)){extracted=new PDFTextStripper().getText(document);if(extracted.length()>40000)extracted=extracted.substring(0,40000);}catch(Exception ignored){}}
            Uri safe=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",target);String display=pendingPriceSourceName.isEmpty()?(original.isEmpty()?target.getName():original):pendingPriceSourceName+(selectedTotal>1?" • "+(selectedIndex+1):"");JSONObject item=new JSONObject();item.put("id",batchId+"_"+selectedIndex);item.put("batch_id",batchId);item.put("name",display);item.put("brand",pendingPriceSourceBrand.isEmpty()?"Other":pendingPriceSourceBrand);item.put("category",pendingPriceSourceCategory.isEmpty()?"General":pendingPriceSourceCategory);item.put("keywords",pendingPriceSourceKeywords);item.put("type",mime);item.put("uri",safe.toString());item.put("path",target.getAbsolutePath());item.put("search_text",normalizePriceSearch(display+" "+pendingPriceSourceBrand+" "+pendingPriceSourceCategory+" "+pendingPriceSourceKeywords+" "+extracted));item.put("updated",now);JSONArray a=readPriceSources();a.put(item);writePriceSources(a);return true;
        }catch(Exception e){return false;}
    }

    private int renderPriceSourceCards(LinearLayout parent,String q,String[] words,String selectedBrand){
        int shown=0;Map<String,Map<String,List<JSONObject>>> brands=new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);JSONArray sources=readPriceSources();
        for(int i=0;i<sources.length();i++){JSONObject source=sources.optJSONObject(i);if(source==null||!priceBrandMatches(selectedBrand,source))continue;String hay=source.optString("search_text",normalizePriceSearch(source.optString("name")+" "+priceBrand(source)+" "+priceCategory(source)+" "+source.optString("keywords")));boolean match=true;for(String word:words)if(!hay.contains(word)){match=false;break;}if(!match)continue;String brand=priceBrand(source),category=priceCategory(source);brands.computeIfAbsent(brand,k->new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER)).computeIfAbsent(category,k->new ArrayList<>()).add(source);}
        if(!brands.isEmpty()){TextView h=new TextView(this);h.setText("BRAND-WISE IMAGE / PDF SECTIONS");h.setTextSize(16);h.setTypeface(Typeface.DEFAULT_BOLD);h.setTextColor(Color.rgb(160,82,20));h.setPadding(dp(5),dp(12),dp(5),dp(6));parent.addView(h);}
        for(Map.Entry<String,Map<String,List<JSONObject>>> brandEntry:brands.entrySet()){
            TextView brandTitle=new TextView(this);brandTitle.setText("◆ "+brandEntry.getKey().toUpperCase(Locale.ROOT));brandTitle.setTextSize(19);brandTitle.setTypeface(Typeface.DEFAULT_BOLD);brandTitle.setTextColor(Color.WHITE);brandTitle.setBackground(rounded(Color.rgb(21,73,126),12));brandTitle.setPadding(dp(12),dp(10),dp(12),dp(10));LinearLayout.LayoutParams brandLp=new LinearLayout.LayoutParams(-1,-2);brandLp.setMargins(0,dp(5),0,dp(5));parent.addView(brandTitle,brandLp);
            for(Map.Entry<String,List<JSONObject>> entry:brandEntry.getValue().entrySet()){String brand=brandEntry.getKey(),category=entry.getKey();List<JSONObject> files=entry.getValue();int imageCount=0,pdfCount=0;for(JSONObject file:files){if(file.optString("type").contains("pdf"))pdfCount++;else imageCount++;}LinearLayout sectionHeader=row();sectionHeader.setGravity(Gravity.CENTER_VERTICAL);TextView sectionTitle=new TextView(this);sectionTitle.setText(category.toUpperCase(Locale.ROOT)+"\n"+imageCount+" images • "+pdfCount+" PDFs");sectionTitle.setTextSize(15);sectionTitle.setTypeface(Typeface.DEFAULT_BOLD);sectionTitle.setTextColor(Color.rgb(21,73,126));Button manage=button("MANAGE");manage.setTextSize(11);manage.setTextColor(Color.rgb(160,82,20));Button shareAll=button("SHARE");shareAll.setTextSize(11);sectionHeader.addView(sectionTitle,new LinearLayout.LayoutParams(0,dp(55),1f));sectionHeader.addView(manage,new LinearLayout.LayoutParams(dp(86),dp(42)));sectionHeader.addView(shareAll,new LinearLayout.LayoutParams(dp(78),dp(42)));parent.addView(sectionHeader);manage.setOnClickListener(v->showManagePriceSection(brand,category,files));shareAll.setOnClickListener(v->sharePriceSourceGroup(brand+" • "+category,files));
                for(JSONObject source:files){shown++;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.HORIZONTAL);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(14),dp(7),dp(6),dp(7));card.setBackground(rounded(Color.rgb(255,248,235),14));TextView details=new TextView(this);boolean pdf=source.optString("type").contains("pdf");details.setText((pdf?"PDF  ":"IMAGE  ")+source.optString("name","Price Source")+(source.optString("keywords").isEmpty()?"":"\n"+source.optString("keywords")));details.setTextSize(15);details.setTextColor(Color.rgb(50,40,28));details.setMaxLines(3);Button more=button("⋮");more.setTextSize(26);more.setTextColor(Color.rgb(160,82,20));more.setBackgroundColor(Color.TRANSPARENT);card.addView(details,new LinearLayout.LayoutParams(0,dp(68),1f));card.addView(more,new LinearLayout.LayoutParams(dp(50),dp(56)));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(84));lp.setMargins(0,0,0,dp(8));parent.addView(card,lp);card.setOnClickListener(v->openPriceSource(source));more.setOnClickListener(v->showPriceSourceMenu(more,source));}}
        }
        return shown;
    }

    private void showManagePriceSection(String brand,String category,List<JSONObject> files){
        int images=0,pdfs=0;String keywords="";for(JSONObject file:files){if(file.optString("type").contains("pdf"))pdfs++;else images++;if(keywords.isEmpty())keywords=file.optString("keywords","");}String savedKeywords=keywords;
        String[] actions={"Edit brand / section / keywords","Add more pictures","Add more PDFs","Delete complete section"};new AlertDialog.Builder(this).setTitle(brand+" • "+category+" • "+images+" images, "+pdfs+" PDFs").setItems(actions,(d,w)->{if(w==0)showEditPriceSection(brand,category,savedKeywords);else if(w==1)beginAddMorePriceFiles(brand,category,savedKeywords,true);else if(w==2)beginAddMorePriceFiles(brand,category,savedKeywords,false);else new AlertDialog.Builder(this).setTitle("Delete complete section?").setMessage(brand+" • "+category+" ke saare "+files.size()+" images/PDFs permanently delete honge.").setPositiveButton("DELETE SECTION",(x,y)->deletePriceSection(brand,category)).setNegativeButton("CANCEL",null).show();}).setNegativeButton("CLOSE",null).show();
    }

    private void beginAddMorePriceFiles(String brand,String category,String keywords,boolean pictures){EditText name=new EditText(this);name.setHint("New file name • optional");name.setSingleLine(true);new AlertDialog.Builder(this).setTitle((pictures?"Add Pictures • ":"Add PDFs • ")+brand+" / "+category).setView(name).setPositiveButton("CHOOSE FILES",(d,w)->{pendingPriceSourceBrand=brand;pendingPriceSourceCategory=category;pendingPriceSourceKeywords=keywords;pendingPriceSourceName=name.getText().toString().trim();getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("pending_price_source_brand",pendingPriceSourceBrand).putString("pending_price_source_category",pendingPriceSourceCategory).putString("pending_price_source_name",pendingPriceSourceName).putString("pending_price_source_keywords",pendingPriceSourceKeywords).apply();if(pictures)launchPriceGalleryPicker();else launchPricePdfPicker();}).setNegativeButton("CANCEL",null).show();}

    private void showEditPriceSection(String oldBrand,String oldCategory,String oldKeywords){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);EditText brand=new EditText(this);brand.setHint("Brand name");brand.setText(oldBrand);brand.setSingleLine(true);EditText category=new EditText(this);category.setHint("Product section");category.setText(oldCategory);category.setSingleLine(true);EditText keywords=new EditText(this);keywords.setHint("Voice and search keywords");keywords.setText(oldKeywords);keywords.setMinLines(2);box.addView(brand);box.addView(category);box.addView(keywords);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Edit Brand Section").setView(box).setPositiveButton("SAVE",null).setNegativeButton("CANCEL",null).create();dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x->{String newBrand=brand.getText().toString().trim(),newCategory=category.getText().toString().trim(),newKeywords=keywords.getText().toString().trim();if(newBrand.isEmpty()){brand.setError("Brand required");return;}if(newCategory.isEmpty()){category.setError("Section name required");return;}try{JSONArray all=readPriceSources();for(int i=0;i<all.length();i++){JSONObject source=all.optJSONObject(i);if(source!=null&&oldBrand.equalsIgnoreCase(priceBrand(source))&&oldCategory.equalsIgnoreCase(priceCategory(source))){source.put("brand",newBrand);source.put("category",newCategory);source.put("keywords",newKeywords);source.put("search_text",normalizePriceSearch(source.optString("name")+" "+newBrand+" "+newCategory+" "+newKeywords+" "+source.optString("search_text")));source.put("updated",System.currentTimeMillis());}}writePriceSources(all);toast("Brand section updated ✓");dialog.dismiss();if(pendingPriceListRefresh!=null)pendingPriceListRefresh.run();}catch(Exception e){toast("Section edit failed");}}));dialog.show();
    }

    private void deletePriceSection(String brand,String category){
        try{JSONArray all=readPriceSources(),out=new JSONArray();int deleted=0;for(int i=0;i<all.length();i++){JSONObject source=all.optJSONObject(i);if(source!=null&&brand.equalsIgnoreCase(priceBrand(source))&&category.equalsIgnoreCase(priceCategory(source))){String path=source.optString("path","");if(!path.isEmpty())new File(path).delete();deleted++;}else out.put(all.get(i));}writePriceSources(out);toast(deleted+" files deleted from "+brand+" • "+category);if(pendingPriceListRefresh!=null)pendingPriceListRefresh.run();}catch(Exception e){toast("Section delete failed");}
    }

    private void sharePriceSourceGroup(String category,List<JSONObject> files){try{ArrayList<Uri> uris=new ArrayList<>();boolean mixed=false;String commonType="";for(JSONObject source:files){Uri uri=Uri.parse(source.optString("uri"));uris.add(uri);String type=source.optString("type","*/*");if(commonType.isEmpty())commonType=type;else if(!commonType.equals(type))mixed=true;}if(uris.isEmpty()){toast("Section me koi file nahi hai");return;}Intent i=new Intent(Intent.ACTION_SEND_MULTIPLE);i.setType(mixed?"*/*":commonType);i.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);i.putExtra(Intent.EXTRA_TEXT,"LATHA EPS • "+category);ClipData clip=ClipData.newRawUri("price source",uris.get(0));for(int n=1;n<uris.size();n++)clip.addItem(new ClipData.Item(uris.get(n)));i.setClipData(clip);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share all • "+category));}catch(Exception e){toast("Section share failed");}}

    private void openPriceSource(JSONObject source){try{Uri uri=Uri.parse(source.optString("uri"));Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(uri,source.optString("type","application/pdf"));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Open Price Source"));}catch(Exception e){toast("Source file open nahi hua");}}
    private void sharePriceSource(JSONObject source){try{Uri uri=Uri.parse(source.optString("uri"));Intent i=new Intent(Intent.ACTION_SEND);i.setType(source.optString("type","application/pdf"));i.putExtra(Intent.EXTRA_STREAM,uri);i.putExtra(Intent.EXTRA_TEXT,"LATHA EPS • "+source.optString("name","Price List"));i.setClipData(android.content.ClipData.newRawUri("price source",uri));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share Price Source"));}catch(Exception e){toast("Source share failed");}}
    private void showPriceSourceMenu(View anchor,JSONObject source){PopupMenu m=new PopupMenu(this,anchor);m.getMenu().add("Open");m.getMenu().add("Share");m.getMenu().add("Edit file details");m.getMenu().add("Replace file");m.getMenu().add("Delete file");m.setOnMenuItemClickListener(x->{String action=x.getTitle().toString();if(action.equals("Open"))openPriceSource(source);else if(action.equals("Share"))sharePriceSource(source);else if(action.startsWith("Edit"))showEditPriceSource(source);else if(action.startsWith("Replace"))launchPriceReplacePicker(source);else new AlertDialog.Builder(this).setTitle("Delete source file?").setMessage(source.optString("name")).setPositiveButton("DELETE",(d,w)->deletePriceSource(source)).setNegativeButton("CANCEL",null).show();return true;});m.show();}

    private void showEditPriceSource(JSONObject source){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);EditText name=new EditText(this);name.setHint("File display name");name.setText(source.optString("name",""));EditText brand=new EditText(this);brand.setHint("Brand");brand.setText(priceBrand(source));EditText category=new EditText(this);category.setHint("Product section");category.setText(priceCategory(source));EditText keywords=new EditText(this);keywords.setHint("Voice and search keywords");keywords.setText(source.optString("keywords",""));keywords.setMinLines(2);box.addView(name);box.addView(brand);box.addView(category);box.addView(keywords);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Edit Price File").setView(box).setPositiveButton("SAVE",null).setNegativeButton("CANCEL",null).create();dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x->{String n=name.getText().toString().trim(),b=brand.getText().toString().trim(),c=category.getText().toString().trim(),k=keywords.getText().toString().trim();if(n.isEmpty()){name.setError("File name required");return;}if(b.isEmpty()){brand.setError("Brand required");return;}if(c.isEmpty()){category.setError("Section required");return;}try{JSONArray all=readPriceSources();String id=source.optString("id");for(int i=0;i<all.length();i++){JSONObject item=all.optJSONObject(i);if(item!=null&&id.equals(item.optString("id"))){item.put("name",n);item.put("brand",b);item.put("category",c);item.put("keywords",k);item.put("search_text",normalizePriceSearch(n+" "+b+" "+c+" "+k+" "+item.optString("search_text")));item.put("updated",System.currentTimeMillis());break;}}writePriceSources(all);toast("File details updated ✓");dialog.dismiss();if(pendingPriceListRefresh!=null)pendingPriceListRefresh.run();}catch(Exception e){toast("File edit failed");}}));dialog.show();
    }

    private void launchPriceReplacePicker(JSONObject source){pendingPriceReplaceId=source.optString("id","");getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("pending_price_replace_id",pendingPriceReplaceId).apply();Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg","image/png","image/webp","image/gif","application/pdf"});startActivityForResult(Intent.createChooser(i,"Choose replacement image or PDF"),PICK_PRICE_REPLACE_FILE);}

    private void replacePriceSourceFile(Uri sourceUri){
        SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);if(pendingPriceReplaceId.isEmpty())pendingPriceReplaceId=prefs.getString("pending_price_replace_id","");if(pendingPriceReplaceId.isEmpty()){toast("Replace target missing");return;}File target=null;try{JSONArray all=readPriceSources();JSONObject old=null;for(int i=0;i<all.length();i++){JSONObject item=all.optJSONObject(i);if(item!=null&&pendingPriceReplaceId.equals(item.optString("id"))){old=item;break;}}if(old==null)throw new Exception("Saved file not found");String mime=getContentResolver().getType(sourceUri),original=getDisplayName(sourceUri);if(mime==null){String low=original.toLowerCase(Locale.ROOT);mime=low.endsWith(".pdf")?"application/pdf":low.endsWith(".png")?"image/png":low.endsWith(".webp")?"image/webp":"image/jpeg";}if(!mime.contains("pdf")&&!mime.startsWith("image/"))throw new Exception("Only image or PDF allowed");String ext=mime.contains("pdf")?"pdf":mime.contains("png")?"png":mime.contains("webp")?"webp":mime.contains("gif")?"gif":"jpg";File dir=new File(getFilesDir(),"business_files/price_sources");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Folder unavailable");target=new File(dir,"price_replace_"+System.currentTimeMillis()+"."+ext);try(InputStream in=getContentResolver().openInputStream(sourceUri);OutputStream out=new FileOutputStream(target)){if(in==null)throw new Exception("Replacement read failed");byte[] buffer=new byte[16*1024];int n;while((n=in.read(buffer))>0)out.write(buffer,0,n);}String extracted="";if(mime.contains("pdf")){try(PDDocument document=PDDocument.load(target)){extracted=new PDFTextStripper().getText(document);if(extracted.length()>40000)extracted=extracted.substring(0,40000);}catch(Exception ignored){}}String oldPath=old.optString("path","");Uri safe=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",target);old.put("type",mime);old.put("uri",safe.toString());old.put("path",target.getAbsolutePath());old.put("original_name",original);old.put("search_text",normalizePriceSearch(old.optString("name")+" "+priceBrand(old)+" "+priceCategory(old)+" "+old.optString("keywords")+" "+extracted));old.put("updated",System.currentTimeMillis());writePriceSources(all);if(!oldPath.isEmpty())new File(oldPath).delete();toast("Price file replaced ✓");if(pendingPriceListRefresh!=null)pendingPriceListRefresh.run();}catch(Exception e){if(target!=null)target.delete();toast("File replace failed: "+e.getMessage());}finally{pendingPriceReplaceId="";prefs.edit().remove("pending_price_replace_id").apply();}
    }
    private void deletePriceSource(JSONObject source){try{JSONArray a=readPriceSources(),out=new JSONArray();String id=source.optString("id");for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null||!id.equals(o.optString("id")))out.put(a.get(i));}writePriceSources(out);String path=source.optString("path");if(!path.isEmpty())new File(path).delete();toast("Price source deleted");if(pendingPriceListRefresh!=null)pendingPriceListRefresh.run();}catch(Exception e){toast("Source delete failed");}}

    private List<JSONObject> sortedPriceItems(){
        List<JSONObject> list=new ArrayList<>();JSONArray a=readPriceList();for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null)list.add(o);}
        Collections.sort(list,(x,y)->{int b=priceBrand(x).compareToIgnoreCase(priceBrand(y));if(b!=0)return b;int c=priceCategory(x).compareToIgnoreCase(priceCategory(y));return c!=0?c:x.optString("name").compareToIgnoreCase(y.optString("name"));});return list;
    }

    private int renderPriceBrandFolders(LinearLayout parent,java.util.function.Consumer<String> openBrand){
        int shown=0;JSONArray sources=readPriceSources(),items=readPriceList();for(String brand:collectPriceBrands()){
            int files=0,priceItems=0,sections=0;Set<String> sectionNames=new LinkedHashSet<>();for(int i=0;i<sources.length();i++){JSONObject source=sources.optJSONObject(i);if(source!=null&&brand.equalsIgnoreCase(priceBrand(source))){files++;sectionNames.add(priceCategory(source).toLowerCase(Locale.ROOT));}}for(int i=0;i<items.length();i++){JSONObject item=items.optJSONObject(i);if(item!=null&&brand.equalsIgnoreCase(priceBrand(item)))priceItems++;}sections=sectionNames.size();
            LinearLayout folder=new LinearLayout(this);folder.setOrientation(LinearLayout.HORIZONTAL);folder.setGravity(Gravity.CENTER_VERTICAL);folder.setPadding(dp(15),dp(10),dp(10),dp(10));folder.setBackground(rounded(Color.WHITE,16));TextView icon=new TextView(this);icon.setText("📁");icon.setTextSize(29);icon.setGravity(Gravity.CENTER);TextView details=new TextView(this);details.setText(brand.toUpperCase(Locale.ROOT)+"\n"+sections+" sections • "+files+" files • "+priceItems+" price items");details.setTextSize(16);details.setTypeface(Typeface.DEFAULT_BOLD);details.setTextColor(Color.rgb(21,73,126));TextView arrow=new TextView(this);arrow.setText("›");arrow.setTextSize(31);arrow.setTextColor(Color.rgb(160,82,20));arrow.setGravity(Gravity.CENTER);folder.addView(icon,new LinearLayout.LayoutParams(dp(52),dp(60)));folder.addView(details,new LinearLayout.LayoutParams(0,dp(62),1f));folder.addView(arrow,new LinearLayout.LayoutParams(dp(42),dp(60)));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(82));lp.setMargins(0,0,0,dp(10));parent.addView(folder,lp);folder.setOnClickListener(v->openBrand.accept(brand));shown++;
        }return shown;
    }

    private void renderPriceListCards(LinearLayout parent,String query,String selectedBrand,Dialog screen,java.util.function.Consumer<String> openBrand){
        parent.removeAllViews();String q=normalizePriceSearch(query);if(q.isEmpty()&&(selectedBrand==null||selectedBrand.equalsIgnoreCase("ALL BRANDS"))){int folders=renderPriceBrandFolders(parent,openBrand);if(folders==0){TextView empty=new TextView(this);empty.setText("No brand folder saved\nTap + BRAND to create one");empty.setGravity(Gravity.CENTER);empty.setTextColor(Color.GRAY);empty.setTextSize(17);parent.addView(empty,new LinearLayout.LayoutParams(-1,dp(180)));}return;}String[] words=q.isEmpty()?new String[0]:q.split(" ");int sourceShown=renderPriceSourceCards(parent,q,words,selectedBrand);int shown=0;String lastBrand="",lastCategory="";
        for(JSONObject item:sortedPriceItems()){
            if(!priceBrandMatches(selectedBrand,item))continue;String brand=priceBrand(item),category=priceCategory(item);String hay=normalizePriceSearch(brand+" "+category+" "+item.optString("name")+" "+item.optString("rate")+" "+item.optString("unit"));boolean match=true;for(String word:words)if(!hay.contains(word)){match=false;break;}if(!match)continue;
            if(!brand.equalsIgnoreCase(lastBrand)){TextView bh=new TextView(this);bh.setText("◆ "+brand.toUpperCase(Locale.ROOT)+" • PRICE ITEMS");bh.setTextSize(18);bh.setTypeface(Typeface.DEFAULT_BOLD);bh.setTextColor(Color.WHITE);bh.setBackground(rounded(Color.rgb(0,110,65),12));bh.setPadding(dp(12),dp(9),dp(12),dp(9));LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,-2);blp.setMargins(0,dp(10),0,dp(4));parent.addView(bh,blp);lastBrand=brand;lastCategory="";}if(!category.equalsIgnoreCase(lastCategory)){TextView h=new TextView(this);h.setText(category.toUpperCase(Locale.ROOT));h.setTextSize(15);h.setTypeface(Typeface.DEFAULT_BOLD);h.setTextColor(Color.rgb(21,73,126));h.setPadding(dp(5),dp(8),dp(5),dp(5));parent.addView(h);lastCategory=category;}
            shown++;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.HORIZONTAL);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(14),dp(8),dp(6),dp(8));card.setBackground(rounded(Color.WHITE,14));
            TextView details=new TextView(this);String discount=item.optString("discount","0"),gst=item.optString("gst","0");String extra=(!discount.isEmpty()&&!"0".equals(discount)?" • Discount "+discount+"%":"")+(!gst.isEmpty()&&!"0".equals(gst)?" • GST "+gst+"%":"");details.setText(item.optString("name","Item")+"\n₹"+item.optString("rate","0")+" / "+item.optString("unit","Nos")+extra);details.setTextSize(16);details.setTextColor(Color.rgb(28,28,28));details.setTypeface(Typeface.DEFAULT_BOLD);details.setMaxLines(3);
            Button more=button("⋮");more.setTextSize(26);more.setTextColor(Color.rgb(21,73,126));more.setBackgroundColor(Color.TRANSPARENT);card.addView(details,new LinearLayout.LayoutParams(0,dp(72),1f));card.addView(more,new LinearLayout.LayoutParams(dp(50),dp(58)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(88));lp.setMargins(0,0,0,dp(8));parent.addView(card,lp);more.setOnClickListener(v->showPriceItemMenu(more,item,()->renderPriceListCards(parent,query,selectedBrand,screen,openBrand)));
        }
        if(shown==0&&sourceShown==0){TextView empty=new TextView(this);empty.setText(readPriceList().length()==0&&readPriceSources().length()==0?"No price data saved\nAdd item or PDF / image source":"No matching price item or source file");empty.setGravity(Gravity.CENTER);empty.setTextColor(Color.GRAY);empty.setTextSize(17);parent.addView(empty,new LinearLayout.LayoutParams(-1,dp(180)));}
    }

    private void showPriceItemEditor(JSONObject existing,Runnable refresh){
        showPriceItemEditor(existing,"",refresh);
    }
    private void showPriceItemEditor(JSONObject existing,String initialBrand,Runnable refresh){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);
        EditText brand=new EditText(this);brand.setHint("Brand • Polycab, Finolex, Mylinc");brand.setSingleLine(true);EditText category=new EditText(this);category.setHint("Product Category • Wire, Switch, DB");category.setSingleLine(true);EditText name=new EditText(this);name.setHint("Item name / size");name.setSingleLine(true);
        EditText rate=new EditText(this);rate.setHint("Rate");rate.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText unit=new EditText(this);unit.setHint("Unit • Nos, Coil, Meter");unit.setSingleLine(true);
        EditText discount=new EditText(this);discount.setHint("Discount % • optional");discount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText gst=new EditText(this);gst.setHint("GST % • optional");gst.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if(existing!=null){brand.setText(priceBrand(existing));category.setText(priceCategory(existing));name.setText(existing.optString("name"));rate.setText(existing.optString("rate"));unit.setText(existing.optString("unit"));discount.setText(existing.optString("discount"));gst.setText(existing.optString("gst"));}else if(initialBrand!=null&&!initialBrand.trim().isEmpty())brand.setText(initialBrand.trim());
        box.addView(brand);box.addView(category);box.addView(name);box.addView(rate);box.addView(unit);box.addView(discount);box.addView(gst);
        AlertDialog d=new AlertDialog.Builder(this).setTitle(existing==null?"Add Price Item":"Edit Price Item").setView(box).setPositiveButton("SAVE",null).setNegativeButton("CANCEL",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String b=brand.getText().toString().trim(),c=category.getText().toString().trim(),n=name.getText().toString().trim(),r=rate.getText().toString().trim(),u=unit.getText().toString().trim();if(b.isEmpty()){brand.setError("Brand required");return;}if(c.isEmpty()){category.setError("Category required");return;}if(n.isEmpty()){name.setError("Item name required");return;}if(r.isEmpty()){rate.setError("Rate required");return;}if(u.isEmpty())u="Nos";try{JSONArray a=readPriceList();String id=existing==null?String.valueOf(System.currentTimeMillis()):existing.optString("id");JSONObject saved=new JSONObject();saved.put("id",id);saved.put("brand",b);saved.put("category",c);saved.put("name",n);saved.put("rate",r);saved.put("unit",u);saved.put("discount",discount.getText().toString().trim());saved.put("gst",gst.getText().toString().trim());saved.put("updated",System.currentTimeMillis());JSONArray out=new JSONArray();boolean replaced=false;for(int i=0;i<a.length();i++){JSONObject old=a.optJSONObject(i);if(old!=null&&id.equals(old.optString("id"))){out.put(saved);replaced=true;}else out.put(a.get(i));}if(!replaced)out.put(saved);writePriceList(out);toast(existing==null?"Price item added ✓":"Price item updated ✓");d.dismiss();refresh.run();}catch(Exception e){toast("Price item save failed");}}));d.show();
    }

    private void showPriceItemMenu(View anchor,JSONObject item,Runnable refresh){PopupMenu m=new PopupMenu(this,anchor);m.getMenu().add("Edit");m.getMenu().add("Delete");m.setOnMenuItemClickListener(x->{if("Edit".equals(x.getTitle().toString()))showPriceItemEditor(item,refresh);else new AlertDialog.Builder(this).setTitle("Delete "+item.optString("name","item")+"?").setPositiveButton("DELETE",(d,w)->{deletePriceItem(item.optString("id"));refresh.run();}).setNegativeButton("CANCEL",null).show();return true;});m.show();}
    private void deletePriceItem(String id){try{JSONArray a=readPriceList(),out=new JSONArray();for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null||!id.equals(o.optString("id")))out.put(a.get(i));}writePriceList(out);toast("Price item deleted");}catch(Exception e){toast("Delete failed");}}

    private String buildPriceListText(){StringBuilder b=new StringBuilder("*LATHA EPS PRICE LIST*\nUpdated: ").append(new java.text.SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(new java.util.Date())).append("\n");String brand="",category="";for(JSONObject o:sortedPriceItems()){String currentBrand=priceBrand(o),c=priceCategory(o);if(!currentBrand.equalsIgnoreCase(brand)){brand=currentBrand;category="";b.append("\n▰ *").append(brand.toUpperCase(Locale.ROOT)).append("*\n");}if(!c.equalsIgnoreCase(category)){category=c;b.append("_").append(c.toUpperCase(Locale.ROOT)).append("_\n");}b.append("• ").append(o.optString("name","Item")).append(" — Rs.").append(o.optString("rate","0")).append("/").append(o.optString("unit","Nos"));String dis=o.optString("discount","0"),gst=o.optString("gst","0");if(!dis.isEmpty()&&!"0".equals(dis))b.append(" | Disc ").append(dis).append("%");if(!gst.isEmpty()&&!"0".equals(gst))b.append(" | GST ").append(gst).append("%");b.append("\n");}b.append("\nRates are subject to confirmation. — LATHA EPS");return b.toString();}
    private void showPriceShareOptions(){if(readPriceList().length()==0){toast("Pehle price items add karein");return;}new AlertDialog.Builder(this).setTitle("Share Price List").setItems(new String[]{"PDF • Professional price list","WhatsApp Text • Quick share"},(d,w)->{if(w==0)sharePriceListPdf();else sharePriceListText();}).setNegativeButton("Cancel",null).show();}
    private void sharePriceListText(){try{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,buildPriceListText());startActivity(Intent.createChooser(i,"Share LATHA EPS Price List"));}catch(Exception e){toast("Price list share failed");}}

    private void sharePriceListPdf(){
        PdfDocument pdf=new PdfDocument();try{Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);int pageNo=1,y=0;PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo).create());Canvas canvas=page.getCanvas();
            paint.setColor(Color.rgb(21,73,126));paint.setTextSize(25);paint.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText("LATHA EPS",36,48,paint);paint.setTextSize(17);canvas.drawText("PRICE LIST",36,75,paint);paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(10);paint.setColor(Color.DKGRAY);canvas.drawText("Updated: "+new java.text.SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(new java.util.Date()),430,68,paint);canvas.drawLine(36,88,559,88,paint);y=116;String brand="",category="";
            for(JSONObject o:sortedPriceItems()){if(y>765){pdf.finishPage(page);pageNo++;page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo).create());canvas=page.getCanvas();paint.setColor(Color.rgb(21,73,126));paint.setTextSize(18);paint.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText("LATHA EPS PRICE LIST • Page "+pageNo,36,45,paint);canvas.drawLine(36,58,559,58,paint);y=86;brand="";category="";}String currentBrand=priceBrand(o),c=priceCategory(o);if(!currentBrand.equalsIgnoreCase(brand)){brand=currentBrand;category="";paint.setColor(Color.rgb(0,110,65));paint.setTextSize(16);paint.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText("BRAND • "+brand.toUpperCase(Locale.ROOT),36,y,paint);y+=25;}if(!c.equalsIgnoreCase(category)){category=c;paint.setColor(Color.rgb(21,73,126));paint.setTextSize(13);paint.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText(c.toUpperCase(Locale.ROOT),42,y,paint);y+=22;}String n=o.optString("name","Item");if(n.length()>42)n=n.substring(0,39)+"...";paint.setColor(Color.BLACK);paint.setTextSize(11);paint.setTypeface(Typeface.DEFAULT);canvas.drawText(n,50,y,paint);String price="Rs."+o.optString("rate","0")+" / "+o.optString("unit","Nos");String dis=o.optString("discount","0"),gst=o.optString("gst","0");if(!dis.isEmpty()&&!"0".equals(dis))price+="  Disc "+dis+"%";if(!gst.isEmpty()&&!"0".equals(gst))price+="  GST "+gst+"%";paint.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText(price,330,y,paint);paint.setColor(Color.LTGRAY);canvas.drawLine(50,y+7,550,y+7,paint);y+=24;}
            paint.setColor(Color.DKGRAY);paint.setTextSize(9);paint.setTypeface(Typeface.DEFAULT);canvas.drawText("Rates are subject to confirmation. Generated by LATHAEPS SMART.",36,820,paint);pdf.finishPage(page);File dir=new File(getFilesDir(),"business_files");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Folder unavailable");File out=new File(dir,"LATHA_EPS_Price_List_"+new java.text.SimpleDateFormat("yyyyMMdd_HHmm",Locale.getDefault()).format(new java.util.Date())+".pdf");try(FileOutputStream stream=new FileOutputStream(out)){pdf.writeTo(stream);}Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",out);Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,uri);i.putExtra(Intent.EXTRA_TEXT,"LATHA EPS Price List");i.setClipData(android.content.ClipData.newRawUri("price list",uri));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share Price List PDF"));
        }catch(Exception e){toast("Price list PDF failed: "+e.getMessage());}finally{try{pdf.close();}catch(Exception ignored){}}
    }

    private JSONArray readPaymentReminders(){try{return new JSONArray(getSharedPreferences(PREFS,MODE_PRIVATE).getString(PAYMENT_REMINDERS_KEY,"[]"));}catch(Exception e){return new JSONArray();}}
    private void writePaymentReminders(JSONArray items){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PAYMENT_REMINDERS_KEY,items.toString()).apply();}
    private Map<String,List<String>> readPaymentReminderLists(){
        Map<String,List<String>> lists=new LinkedHashMap<>();
        try{JSONObject root=new JSONObject(getSharedPreferences(PREFS,MODE_PRIVATE).getString(PAYMENT_REMINDER_LISTS_KEY,"{}"));JSONArray names=root.names();if(names!=null)for(int i=0;i<names.length();i++){String name=names.getString(i);JSONArray numbers=root.optJSONArray(name);List<String> saved=new ArrayList<>();if(numbers!=null)for(int j=0;j<numbers.length();j++){String number=normalize(numbers.optString(j));if(!number.isEmpty()&&!saved.contains(number))saved.add(number);}lists.put(name,saved);}}catch(Exception ignored){}
        return lists;
    }
    private void writePaymentReminderLists(Map<String,List<String>> lists){
        try{JSONObject root=new JSONObject();for(Map.Entry<String,List<String>> entry:lists.entrySet())root.put(entry.getKey(),new JSONArray(entry.getValue()));getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(PAYMENT_REMINDER_LISTS_KEY,root.toString()).apply();}catch(Exception e){toast("Payment reminder list save failed");}
    }
    private void savePaymentReminderList(Set<String> chosen){
        if(chosen.isEmpty()){toast("Select payment customers first");return;}
        EditText input=new EditText(this);input.setHint("Example: Monday Collection");input.setSingleLine(true);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Save Payment Reminder List").setMessage(chosen.size()+" selected parties").setView(input).setPositiveButton("SAVE",null).setNegativeButton("CANCEL",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()){input.setError("List name required");return;}Map<String,List<String>> lists=readPaymentReminderLists();if(lists.containsKey(name)){new AlertDialog.Builder(this).setTitle("Replace saved list?").setMessage(name+" already exists. Selected parties se replace karein?").setPositiveButton("REPLACE",(d,w)->{lists.put(name,new ArrayList<>(chosen));writePaymentReminderLists(lists);toast("Payment list updated: "+name);dialog.dismiss();}).setNegativeButton("CANCEL",null).show();return;}lists.put(name,new ArrayList<>(chosen));writePaymentReminderLists(lists);toast("Payment list saved: "+name);dialog.dismiss();}));dialog.show();
    }
    private void showPaymentReminderListsScreen(){
        Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(Color.BLACK);
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),0,dp(12),0);head.setBackgroundColor(Color.rgb(28,26,27));Button back=button("‹");back.setTextSize(36);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Payment Reminder Lists");title.setTextSize(24);title.setTypeface(Typeface.DEFAULT_BOLD);title.setTextColor(Color.WHITE);head.addView(back,new LinearLayout.LayoutParams(dp(58),dp(72)));head.addView(title,new LinearLayout.LayoutParams(0,dp(72),1f));page.addView(head);
        EditText search=new EditText(this);search.setHint("Search list or party number");search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(Color.GRAY);search.setPadding(dp(16),0,dp(16),0);page.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        Button addList=button("+  ADD NEW LIST");addList.setTextColor(Color.WHITE);addList.setTypeface(Typeface.DEFAULT_BOLD);addList.setBackground(rounded(Color.rgb(20,125,72),12));LinearLayout.LayoutParams addParams=new LinearLayout.LayoutParams(-1,dp(48));addParams.setMargins(dp(16),dp(8),dp(16),dp(4));page.addView(addList,addParams);
        ScrollView scroll=new ScrollView(this);LinearLayout cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);cards.setPadding(dp(16),dp(16),dp(16),dp(90));scroll.addView(cards);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        Runnable refresh=()->renderPaymentReminderListCards(cards,dialog,search.getText().toString());refresh.run();search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){renderPaymentReminderListCards(cards,dialog,s.toString());}public void afterTextChanged(Editable e){}});addList.setOnClickListener(v->createPaymentReminderList(refresh));back.setOnClickListener(v->dialog.dismiss());dialog.setContentView(page);dialog.show();
    }
    private void renderPaymentReminderListCards(LinearLayout parent,Dialog screen,String query){
        parent.removeAllViews();Map<String,List<String>> lists=readPaymentReminderLists();if(lists.isEmpty()){TextView empty=new TextView(this);empty.setText("No saved payment lists\nTap ADD NEW LIST to create one");empty.setTextColor(Color.LTGRAY);empty.setTextSize(17);empty.setGravity(Gravity.CENTER);parent.addView(empty,new LinearLayout.LayoutParams(-1,dp(170)));return;}
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);int shown=0;
        for(Map.Entry<String,List<String>> entry:lists.entrySet()){
            boolean match=q.isEmpty()||entry.getKey().toLowerCase(Locale.ROOT).contains(q);if(!match)for(String number:entry.getValue())if(number.contains(q)||last10Digits(number).contains(q)){match=true;break;}if(!match)continue;
            shown++;String name=entry.getKey();List<String> numbers=new ArrayList<>(entry.getValue());
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(14),dp(10),dp(14),dp(12));card.setBackground(rounded(Color.rgb(42,42,42),14));
            LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);TextView a=new TextView(this);a.setText(name);a.setTextColor(Color.WHITE);a.setTextSize(21);a.setTypeface(Typeface.DEFAULT_BOLD);TextView b=new TextView(this);b.setText(numbers.size()+" selected parties");b.setTextColor(Color.rgb(200,200,205));b.setTextSize(15);words.addView(a);words.addView(b);Button open=button("OPEN");open.setTextSize(12);open.setTextColor(Color.WHITE);open.setBackground(rounded(Color.rgb(25,125,72),12));top.addView(words,new LinearLayout.LayoutParams(0,dp(64),1f));top.addView(open,new LinearLayout.LayoutParams(dp(78),dp(42)));card.addView(top);
            LinearLayout actions=row();Button directSend=button("SEND MESSAGE");Button edit=button("EDIT LIST");Button delete=button("DELETE");directSend.setTextSize(11);directSend.setTextColor(Color.WHITE);directSend.setTypeface(Typeface.DEFAULT_BOLD);directSend.setBackground(rounded(Color.rgb(20,125,70),11));edit.setTextSize(11);edit.setTextColor(Color.WHITE);edit.setBackground(rounded(Color.rgb(40,95,175),11));delete.setTextSize(11);delete.setTextColor(Color.WHITE);delete.setBackground(rounded(Color.rgb(150,48,48),11));actions.addView(directSend,weighted(1.25f,44));actions.addView(edit,weighted(1f,44));actions.addView(delete,weighted(.85f,44));card.addView(actions);
            Runnable refresh=()->renderPaymentReminderListCards(parent,screen,query);View.OnClickListener openList=v->{screen.dismiss();showPaymentReminderScreen(new LinkedHashSet<>(numbers));};open.setOnClickListener(openList);directSend.setOnClickListener(v->showPaymentListDirectMessage(name,numbers));edit.setOnClickListener(v->showPaymentReminderEditOptions(name,refresh));delete.setOnClickListener(v->deletePaymentReminderList(name,refresh));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(14));parent.addView(card,lp);
        }
        if(shown==0){TextView empty=new TextView(this);empty.setText("No payment list found");empty.setTextColor(Color.LTGRAY);empty.setTextSize(16);empty.setGravity(Gravity.CENTER);parent.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));}
    }
    private void createPaymentReminderList(Runnable after){
        EditText input=new EditText(this);input.setHint("List name • example: Monday Collection");input.setSingleLine(true);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Add Payment Reminder List").setView(input).setPositiveButton("CREATE",null).setNegativeButton("CANCEL",null).create();dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()){input.setError("List name required");return;}Map<String,List<String>> lists=readPaymentReminderLists();for(String old:lists.keySet())if(old.equalsIgnoreCase(name)){input.setError("List name already exists");return;}lists.put(name,new ArrayList<>());writePaymentReminderLists(lists);toast("Payment list created: "+name);dialog.dismiss();after.run();editPaymentReminderListParties(name,after);}));dialog.show();
    }
    private void showPaymentReminderEditOptions(String name,Runnable after){new AlertDialog.Builder(this).setTitle("Edit "+name).setItems(new String[]{"Edit list name","Add / remove parties"},(d,which)->{if(which==0)renamePaymentReminderList(name,after);else editPaymentReminderListParties(name,after);}).setNegativeButton("CANCEL",null).show();}
    private void showPaymentReminderListMenu(View anchor,String name,Dialog parent){
        PopupMenu menu=new PopupMenu(this,anchor);menu.getMenu().add("Open selected parties");menu.getMenu().add("Add / remove parties");menu.getMenu().add("Rename list");menu.getMenu().add("Delete list");menu.setOnMenuItemClickListener(item->{String action=item.getTitle().toString();if(action.startsWith("Open")){List<String> numbers=readPaymentReminderLists().get(name);parent.dismiss();showPaymentReminderScreen(new LinkedHashSet<>(numbers==null?new ArrayList<>():numbers));}else if(action.startsWith("Add")){editPaymentReminderListParties(name,()->{parent.dismiss();showPaymentReminderListsScreen();});}else if(action.startsWith("Rename")){renamePaymentReminderList(name,()->{parent.dismiss();showPaymentReminderListsScreen();});}else deletePaymentReminderList(name,()->{parent.dismiss();showPaymentReminderListsScreen();});return true;});menu.show();
    }
    private void renamePaymentReminderList(String oldName,Runnable after){
        EditText input=new EditText(this);input.setText(oldName);input.setSingleLine(true);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Rename Payment List").setView(input).setPositiveButton("RENAME",null).setNegativeButton("CANCEL",null).create();dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()){input.setError("List name required");return;}Map<String,List<String>> lists=readPaymentReminderLists();if(!oldName.equals(name)&&lists.containsKey(name)){input.setError("List name already exists");return;}List<String> numbers=lists.remove(oldName);lists.put(name,numbers==null?new ArrayList<>():numbers);writePaymentReminderLists(lists);toast("Payment list renamed");dialog.dismiss();after.run();}));dialog.show();
    }
    private void deletePaymentReminderList(String name,Runnable after){new AlertDialog.Builder(this).setTitle("Delete payment list?").setMessage(name+" delete hoga. Customer records safe rahenge.").setPositiveButton("DELETE",(d,w)->{Map<String,List<String>> lists=readPaymentReminderLists();lists.remove(name);writePaymentReminderLists(lists);toast("Payment list deleted");after.run();}).setNegativeButton("CANCEL",null).show();}
    private void editPaymentReminderListParties(String name,Runnable after){
        JSONArray all=readPaymentReminders();List<JSONObject> customers=new ArrayList<>();for(int i=0;i<all.length();i++){JSONObject o=all.optJSONObject(i);if(o!=null)customers.add(o);}if(customers.isEmpty()){toast("Pehle payment customers add karein");return;}Map<String,List<String>> lists=readPaymentReminderLists();Set<String> working=new LinkedHashSet<>(lists.containsKey(name)?lists.get(name):new ArrayList<>());List<JSONObject> filtered=new ArrayList<>(customers);Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(12),dp(10),dp(12),dp(10));page.setBackgroundColor(Color.BLACK);TextView title=new TextView(this);title.setText("Edit List • "+name);title.setTextColor(Color.WHITE);title.setTextSize(22);title.setTypeface(Typeface.DEFAULT_BOLD);page.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));EditText find=new EditText(this);find.setHint("Search party name or number");find.setHintTextColor(Color.GRAY);find.setTextColor(Color.WHITE);find.setSingleLine(true);page.addView(find,new LinearLayout.LayoutParams(-1,dp(50)));TextView count=new TextView(this);count.setTextColor(Color.WHITE);count.setTypeface(Typeface.DEFAULT_BOLD);page.addView(count,new LinearLayout.LayoutParams(-1,dp(34)));ListView list=new ListView(this);list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);List<String> rows=new ArrayList<>();ArrayAdapter<String> adapter=paymentDarkChoiceAdapter(rows);list.setAdapter(adapter);page.addView(list,new LinearLayout.LayoutParams(-1,0,1f));Runnable checks=()->{rows.clear();for(JSONObject o:filtered)rows.add(o.optString("name","Customer")+"\n+"+o.optString("phone",""));adapter.notifyDataSetChanged();list.clearChoices();for(int i=0;i<filtered.size();i++)list.setItemChecked(i,working.contains(normalize(filtered.get(i).optString("phone",""))));count.setText("Selected "+working.size()+" • Showing "+filtered.size());};checks.run();list.setOnItemClickListener((p,v,pos,id)->{String number=normalize(filtered.get(pos).optString("phone",""));if(working.contains(number))working.remove(number);else working.add(number);checks.run();});find.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){String q=s.toString().trim().toLowerCase(Locale.ROOT);filtered.clear();for(JSONObject o:customers){String hay=(o.optString("name","")+" "+o.optString("phone","")).toLowerCase(Locale.ROOT);if(q.isEmpty()||hay.contains(q))filtered.add(o);}checks.run();}public void afterTextChanged(Editable e){}});LinearLayout actions=row();Button cancel=button("CANCEL");Button save=button("SAVE CHANGES");save.setTextColor(Color.WHITE);save.setTypeface(Typeface.DEFAULT_BOLD);save.setBackground(rounded(Color.rgb(25,125,72),12));actions.addView(cancel,weighted(1f,50));actions.addView(save,weighted(1.2f,50));page.addView(actions);cancel.setOnClickListener(v->dialog.dismiss());save.setOnClickListener(v->{lists.put(name,new ArrayList<>(working));writePaymentReminderLists(lists);toast("List updated • "+working.size()+" parties");dialog.dismiss();after.run();});dialog.setContentView(page);dialog.show();
    }
    private String defaultPaymentTemplate(){return "Dear {Name}, kindly arrange the pending balance payment. Thank you — LATHA EPS.";}

    private void stylePaymentMessageBox(EditText input){
        input.setTextColor(Color.WHITE);input.setHintTextColor(Color.rgb(175,175,180));input.setCursorVisible(true);input.setSelectAllOnFocus(false);input.setPadding(dp(14),dp(12),dp(14),dp(12));
        GradientDrawable normal=rounded(Color.rgb(43,43,47),12);normal.setStroke(dp(1),Color.rgb(110,110,118));GradientDrawable focused=rounded(Color.rgb(43,43,47),12);focused.setStroke(dp(3),Color.rgb(50,145,255));
        android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();states.addState(new int[]{android.R.attr.state_focused},focused);states.addState(new int[]{},normal);input.setBackground(states);
    }

    private void showPaymentListDirectMessage(String listName,List<String> numbers){
        if(numbers==null||numbers.isEmpty()){toast("Saved list empty • pehle parties add karein");return;}
        List<JSONObject> items=collectPaymentItems(new LinkedHashSet<>(numbers),false);if(items.isEmpty()){toast("Saved list me valid payment customers nahi mile");return;}
        Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(16),dp(12),dp(16),dp(16));page.setBackgroundColor(Color.BLACK);
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);Button back=button("‹");back.setTextSize(31);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Send Message • "+listName);title.setTextSize(21);title.setTextColor(Color.WHITE);title.setTypeface(Typeface.DEFAULT_BOLD);head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);
        TextView count=new TextView(this);count.setText(items.size()+" parties selected • {Name} automatic party name dalega");count.setTextColor(Color.LTGRAY);count.setTextSize(14);count.setPadding(dp(4),dp(4),dp(4),dp(10));page.addView(count,new LinearLayout.LayoutParams(-1,dp(52)));
        EditText message=new EditText(this);message.setHint("Type payment reminder message");message.setGravity(Gravity.TOP|Gravity.START);message.setMinLines(7);message.setTextSize(17);message.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);message.setText(getSharedPreferences(PREFS,MODE_PRIVATE).getString("payment_list_direct_message",defaultPaymentTemplate()));stylePaymentMessageBox(message);page.addView(message,new LinearLayout.LayoutParams(-1,0,1f));
        LinearLayout actions=row();Button cancel=button("CANCEL");Button send=button("SEND NOW");cancel.setTextColor(Color.WHITE);cancel.setBackground(rounded(Color.rgb(55,55,60),12));send.setTextColor(Color.WHITE);send.setTypeface(Typeface.DEFAULT_BOLD);send.setBackground(rounded(Color.rgb(20,125,70),12));actions.addView(cancel,weighted(1f,52));actions.addView(send,weighted(1.2f,52));LinearLayout.LayoutParams actionParams=new LinearLayout.LayoutParams(-1,dp(58));actionParams.setMargins(0,dp(12),0,0);page.addView(actions,actionParams);
        back.setOnClickListener(v->dialog.dismiss());cancel.setOnClickListener(v->dialog.dismiss());send.setOnClickListener(v->{String body=message.getText().toString().trim();if(body.isEmpty()){message.setError("Message required");message.requestFocus();return;}if(!isAccessibilityServiceEnabled()){toast("Pehle Accessibility ON karein");try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(Exception ignored){}return;}getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("payment_list_direct_message",body).apply();String preview="List: "+listName+"\nRecipients: "+items.size()+"\n\nFirst: "+items.get(0).optString("name","Customer")+"\n\n"+body.replace("{Name}",items.get(0).optString("name","Customer")).replace("{name}",items.get(0).optString("name","Customer"));new AlertDialog.Builder(this).setTitle("Confirm Direct Message").setMessage(preview).setPositiveButton("START SEND",(d,w)->{dialog.dismiss();startPaymentListDirectQueue(items,body,listName);}).setNegativeButton("CANCEL",null).show();});dialog.setContentView(page);dialog.show();message.requestFocus();dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void startPaymentListDirectQueue(List<JSONObject> items,String body,String listName){
        try{JSONArray nums=new JSONArray(),names=new JSONArray(),messages=new JSONArray();for(JSONObject o:items){String name=o.optString("name","Customer");nums.put(normalize(o.optString("phone","")));names.put(name);messages.put(body.replace("{Name}",name).replace("{name}",name));o.put("last_sent",System.currentTimeMillis());}JSONArray all=readPaymentReminders();Map<String,JSONObject> sent=new LinkedHashMap<>();for(JSONObject o:items)sent.put(normalize(o.optString("phone","")),o);JSONArray updated=new JSONArray();for(int i=0;i<all.length();i++){JSONObject o=all.optJSONObject(i);if(o==null)continue;JSONObject replacement=sent.get(normalize(o.optString("phone","")));updated.put(replacement==null?o:replacement);}writePaymentReminders(updated);SharedPreferences settings=getSharedPreferences(PREFS,MODE_PRIVATE);getSharedPreferences(AUTO_PREFS,MODE_PRIVATE).edit().putString(AUTO_NUMBERS,nums.toString()).putString(AUTO_NAMES,names.toString()).putString(AUTO_MESSAGES,messages.toString()).putString(AUTO_MESSAGE,"").putString(AUTO_QUEUE_TOKEN,String.valueOf(System.currentTimeMillis())).putString(AUTO_FAILED,"[]").putInt(AUTO_INDEX,0).putInt(AUTO_MIN_DELAY,settings.getInt(AUTO_MIN_DELAY,3)).putInt(AUTO_MAX_DELAY,settings.getInt(AUTO_MAX_DELAY,7)).remove(AUTO_IMAGE_URI).remove(AUTO_IMAGE_TYPE).putBoolean(AUTO_RUNNING,true).apply();String stamp=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date());String old=settings.getString(PAYMENT_HISTORY_KEY,"");settings.edit().putString(PAYMENT_HISTORY_KEY,stamp+" • Direct list: "+listName+" • "+items.size()+" messages"+(old.isEmpty()?"":"\n"+old)).apply();getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);if(sendButton!=null)sendButton.setText("STOP AUTO SENDING");if(miniProgress!=null)miniProgress.setText("Payment List • Starting 1 / "+items.size());showProgressNotification(0,items.size(),"Payment list messages starting");WhatsAppAccessibilityService.openCurrentChat(this);}catch(Exception e){toast("Saved list message queue failed");}
    }

    private void showPaymentReminderScreen(){showPaymentReminderScreen(null);}
    private void showPaymentReminderScreen(Set<String> initialSelection){
        Dialog d=new Dialog(this,android.R.style.Theme_Material_NoActionBar);LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(12),dp(10),dp(12),dp(12));page.setBackgroundColor(Color.BLACK);LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);Button back=button("‹");back.setTextSize(31);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);TextView title=new TextView(this);title.setText("Payment Reminder");title.setTextSize(23);title.setTextColor(Color.WHITE);title.setTypeface(Typeface.DEFAULT_BOLD);head.setBackground(rounded(Color.rgb(28,26,27),17));head.addView(back,new LinearLayout.LayoutParams(dp(50),dp(58)));head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1f));page.addView(head);
        LinearLayout first=row();Button phoneContacts=button("PHONE CONTACTS");Button add=button("+ ADD CUSTOMER");first.addView(phoneContacts,weighted(1.2f,44));first.addView(add,weighted(1f,44));page.addView(first);LinearLayout importRow=row();Button importLedger=button("IMPORT MASTER LEDGER");Button selectVisible=button("SELECT FILTERED");importRow.addView(importLedger,weighted(1.2f,42));importRow.addView(selectVisible,weighted(1f,42));page.addView(importRow);Button savedLists=button("SAVED LISTS  •  ADD / EDIT / DELETE");savedLists.setTextColor(Color.WHITE);savedLists.setBackground(rounded(Color.rgb(40,95,175),12));page.addView(savedLists,new LinearLayout.LayoutParams(-1,dp(46)));LinearLayout second=row();Button clear=button("CLEAR SELECTION");Button template=button("MESSAGE TEMPLATE");second.addView(clear,weighted(1f,42));second.addView(template,weighted(1f,42));page.addView(second);LinearLayout third=row();Button history=button("REMINDER HISTORY");Button scheduled=button(paymentScheduleButtonText());third.addView(history,weighted(1f,42));third.addView(scheduled,weighted(1.2f,42));page.addView(third);Button[] darkButtons={phoneContacts,add,importLedger,selectVisible,clear,template,history,scheduled};for(Button x:darkButtons){x.setTextColor(Color.WHITE);x.setBackground(rounded(Color.rgb(42,42,42),12));}
        String[] filters={"All Customers","Overdue","Due Today","Upcoming","No Due Date"};Spinner filter=new Spinner(this);ArrayAdapter<String> filterAdapter=paymentDarkSpinnerAdapter(filters);filter.setAdapter(filterAdapter);filter.setBackground(rounded(Color.rgb(42,42,42),10));page.addView(filter,new LinearLayout.LayoutParams(-1,dp(48)));TextView summary=new TextView(this);summary.setTextColor(Color.WHITE);summary.setTypeface(Typeface.DEFAULT_BOLD);summary.setPadding(dp(5),dp(5),dp(5),dp(5));page.addView(summary,new LinearLayout.LayoutParams(-1,dp(30)));ListView list=new ListView(this);list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);page.addView(list,new LinearLayout.LayoutParams(-1,0,1f));LinearLayout sendRow=row();Button test=button("TEST FIRST");Button schedule=button("SCHEDULE TIME");Button send=button("SEND NOW");test.setTextColor(Color.WHITE);test.setBackground(rounded(Color.rgb(42,42,42),12));schedule.setTextColor(Color.WHITE);schedule.setBackground(rounded(Color.rgb(42,42,42),12));send.setTextColor(Color.WHITE);send.setBackground(rounded(Color.rgb(20,125,70),12));sendRow.addView(test,weighted(.85f,50));sendRow.addView(schedule,weighted(1.1f,50));sendRow.addView(send,weighted(1f,50));page.addView(sendRow);
        Set<String> chosen=new LinkedHashSet<>();if(initialSelection!=null)for(String number:initialSelection){String normalized=normalize(number);if(!normalized.isEmpty())chosen.add(normalized);}List<JSONObject> visible=new ArrayList<>();List<String> rows=new ArrayList<>();ArrayAdapter<String> paymentRows=paymentDarkChoiceAdapter(rows);list.setAdapter(paymentRows);Runnable[] refresh={null};refresh[0]=()->{visible.clear();rows.clear();JSONArray a=readPaymentReminders();String selectedFilter=filters[filter.getSelectedItemPosition()];for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null||!paymentMatchesFilter(o,selectedFilter))continue;visible.add(o);String due=o.optString("due","");rows.add(o.optString("name","Customer")+"\n+"+o.optString("phone","")+"  •  "+(due.isEmpty()?"No due date":"Due "+due));}paymentRows.notifyDataSetChanged();list.clearChoices();for(int i=0;i<visible.size();i++)list.setItemChecked(i,chosen.contains(normalize(visible.get(i).optString("phone",""))));summary.setText("Showing "+visible.size()+" • Selected "+chosen.size()+" • Long press to edit");};refresh[0].run();
        filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refresh[0].run();}public void onNothingSelected(android.widget.AdapterView<?> p){}});list.setOnItemClickListener((p,v,pos,id)->{String n=normalize(visible.get(pos).optString("phone",""));if(chosen.contains(n))chosen.remove(n);else chosen.add(n);summary.setText("Showing "+visible.size()+" • Selected "+chosen.size()+" • Long press to edit");});list.setOnItemLongClickListener((p,v,pos,id)->{showEditPaymentReminder(visible.get(pos),refresh[0]);return true;});
        phoneContacts.setOnClickListener(v->openPaymentContactPicker(refresh[0]));importLedger.setOnClickListener(v->{importPaymentCustomersFromLedger();refresh[0].run();});add.setOnClickListener(v->showEditPaymentReminder(null,refresh[0]));selectVisible.setOnClickListener(v->{for(JSONObject o:visible){String n=normalize(o.optString("phone",""));if(!n.isEmpty())chosen.add(n);}refresh[0].run();});clear.setOnClickListener(v->{chosen.clear();refresh[0].run();});savedLists.setOnClickListener(v->{d.dismiss();showPaymentReminderListsScreen();});template.setOnClickListener(v->showPaymentTemplateDialog());history.setOnClickListener(v->showPaymentReminderHistory());scheduled.setOnClickListener(v->showPaymentScheduleStatus(scheduled));test.setOnClickListener(v->previewPaymentReminders(chosen,true));schedule.setOnClickListener(v->showPaymentDateTimePicker(chosen,scheduled));send.setOnClickListener(v->previewPaymentReminders(chosen,false));back.setOnClickListener(v->d.dismiss());d.setContentView(page);d.show();
    }

    private ArrayAdapter<String> paymentDarkChoiceAdapter(List<String> rows){return new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,rows){@Override public View getView(int position,View convertView,android.view.ViewGroup parent){View view=super.getView(position,convertView,parent);if(view instanceof TextView){TextView text=(TextView)view;text.setTextColor(Color.WHITE);text.setTextSize(16);text.setPadding(dp(12),dp(10),dp(8),dp(10));}return view;}};}
    private ArrayAdapter<String> paymentDarkSpinnerAdapter(String[] rows){return new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,rows){@Override public View getView(int position,View convertView,android.view.ViewGroup parent){View view=super.getView(position,convertView,parent);if(view instanceof TextView){((TextView)view).setTextColor(Color.WHITE);((TextView)view).setPadding(dp(14),0,dp(8),0);}return view;}@Override public View getDropDownView(int position,View convertView,android.view.ViewGroup parent){View view=super.getDropDownView(position,convertView,parent);if(view instanceof TextView){((TextView)view).setTextColor(Color.BLACK);((TextView)view).setBackgroundColor(Color.WHITE);}return view;}};}

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
                String selectedDisplay=getDisplayName(source);final String display=selectedDisplay.isEmpty()?"Master Ledger PDF":selectedDisplay;
                String history=p.getString("file_history","");String line=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date())+" • Master Ledger prepared • "+display;
                p.edit().putString(AutoReplyNotificationService.LEDGER_URI,safe.toString()).putString(AutoReplyNotificationService.LEDGER_URI+"_type","application/pdf").putString(AutoReplyNotificationService.LEDGER_URI+"_name",display).putLong(AutoReplyNotificationService.LEDGER_URI+"_updated",System.currentTimeMillis()).putString("file_history",line+(history.isEmpty()?"":"\n"+history)).apply();
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
        getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).edit().putString(AutoReplyNotificationService.LEDGER_CUSTOMERS,customers.toString()).putInt("ledger_source_pages",result.totalPages).putInt("ledger_skipped_pages",result.skippedPages).putLong("ledger_index_updated",System.currentTimeMillis()).apply();
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

    private JSONArray readLedgerLists(){
        SharedPreferences prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        try{
            JSONArray lists=new JSONArray(prefs.getString(LEDGER_SAVED_LISTS_KEY,"[]"));
            if(lists.length()>0)return lists;
            JSONArray old=new JSONArray(prefs.getString(LEDGER_SAVED_PARTIES_KEY,"[]"));
            if(old.length()>0){JSONObject migrated=new JSONObject();migrated.put("name","My Ledger List");migrated.put("phones",old);lists.put(migrated);writeLedgerLists(lists);}
            return lists;
        }catch(Exception e){return new JSONArray();}
    }

    private void writeLedgerLists(JSONArray lists){getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(LEDGER_SAVED_LISTS_KEY,lists.toString()).apply();}

    private Set<String> ledgerListPhones(JSONObject list){
        Set<String> phones=new LinkedHashSet<>();if(list==null)return phones;
        JSONArray a=list.optJSONArray("phones");if(a!=null)for(int i=0;i<a.length();i++){String p=normalize(a.optString(i));if(!p.isEmpty())phones.add(p);}return phones;
    }

    private String ledgerPartyLabel(String phone){
        String normalized=normalize(phone),name="";
        try{JSONArray a=new JSONArray(getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&normalized.equals(normalize(o.optString("phone","")))){name=o.optString("name","").trim();break;}}}catch(Exception ignored){}
        return name.isEmpty()?normalized:name+"  •  "+normalized;
    }

    private String ledgerListPreview(JSONObject list){
        Set<String> phones=ledgerListPhones(list);if(phones.isEmpty())return "No parties added yet";
        StringBuilder out=new StringBuilder();int shown=0;for(String phone:phones){if(shown==4){out.append("\n+").append(phones.size()-shown).append(" more parties");break;}if(shown>0)out.append("\n");out.append("• ").append(ledgerPartyLabel(phone));shown++;}return out.toString();
    }

    private void showLedgerListsScreen(){
        final Dialog dialog=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(Color.rgb(239,249,248));
        LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(12),dp(8),dp(10),dp(8));header.setBackgroundColor(Color.rgb(0,91,78));
        Button back=button("‹");back.setTextSize(30);back.setTextColor(Color.WHITE);back.setBackgroundColor(Color.TRANSPARENT);
        TextView title=new TextView(this);title.setText("Ledger Party Lists");title.setTextColor(Color.WHITE);title.setTextSize(23);title.setTypeface(Typeface.DEFAULT_BOLD);title.setGravity(Gravity.CENTER_VERTICAL);
        Button customers=button("PEOPLE");customers.setTextSize(11);customers.setTextColor(Color.WHITE);customers.setBackgroundColor(Color.TRANSPARENT);
        header.addView(back,new LinearLayout.LayoutParams(dp(48),dp(54)));header.addView(title,new LinearLayout.LayoutParams(0,dp(54),1f));header.addView(customers,new LinearLayout.LayoutParams(dp(72),dp(54)));page.addView(header);
        TextView help=new TextView(this);help.setText("Separate lists बनाएं, party names देखें और हर list से ledgers send या schedule करें।");help.setTextSize(14);help.setTextColor(Color.rgb(12,52,49));help.setPadding(dp(18),dp(14),dp(18),dp(10));page.addView(help);
        TextView listTitle=new TextView(this);listTitle.setText("Saved Lists");listTitle.setTextSize(22);listTitle.setTypeface(Typeface.DEFAULT_BOLD);listTitle.setTextColor(Color.rgb(12,52,49));listTitle.setPadding(dp(20),dp(4),0,dp(6));page.addView(listTitle);
        ScrollView scroll=new ScrollView(this);LinearLayout cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);cards.setPadding(dp(16),0,dp(16),dp(92));scroll.addView(cards);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        Button plus=button("+");plus.setTextSize(38);plus.setTextColor(Color.WHITE);plus.setBackground(rounded(Color.rgb(0,91,78),50));LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);bottom.setPadding(0,0,dp(20),dp(10));bottom.addView(plus,new LinearLayout.LayoutParams(dp(72),dp(72)));page.addView(bottom,new LinearLayout.LayoutParams(-1,dp(82)));
        Runnable refresh=()->renderLedgerLists(cards,dialog);back.setOnClickListener(v->dialog.dismiss());customers.setOnClickListener(v->showLedgerCustomersDialog());plus.setOnClickListener(v->showCreateLedgerList(refresh));refresh.run();dialog.setContentView(page);dialog.show();
    }

    private void renderLedgerLists(LinearLayout cards,Dialog parent){
        cards.removeAllViews();JSONArray lists=readLedgerLists();
        if(lists.length()==0){TextView empty=new TextView(this);empty.setText("No Ledger Lists saved\nTap + to create your first list");empty.setGravity(Gravity.CENTER);empty.setTextSize(17);empty.setTextColor(Color.DKGRAY);empty.setPadding(0,dp(70),0,0);cards.addView(empty,new LinearLayout.LayoutParams(-1,dp(190)));return;}
        for(int i=0;i<lists.length();i++){
            final int index=i;JSONObject item=lists.optJSONObject(i);if(item==null)continue;String listName=item.optString("name","Ledger List "+(i+1));int count=ledgerListPhones(item).size();
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(12),dp(12),dp(12));card.setBackground(rounded(Color.WHITE,20));
            LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);TextView name=new TextView(this);name.setText(listName);name.setTextSize(20);name.setTypeface(Typeface.DEFAULT_BOLD);name.setTextColor(Color.rgb(0,91,78));TextView badge=new TextView(this);badge.setText(count+" PARTIES");badge.setTextSize(11);badge.setTypeface(Typeface.DEFAULT_BOLD);badge.setTextColor(Color.rgb(0,91,78));badge.setGravity(Gravity.CENTER);badge.setBackground(rounded(Color.rgb(220,245,241),30));Button more=button("⋮");more.setTextSize(23);more.setTextColor(Color.rgb(0,91,78));more.setBackgroundColor(Color.TRANSPARENT);top.addView(name,new LinearLayout.LayoutParams(0,dp(46),1f));top.addView(badge,new LinearLayout.LayoutParams(dp(86),dp(32)));top.addView(more,new LinearLayout.LayoutParams(dp(42),dp(46)));card.addView(top);
            TextView parties=new TextView(this);parties.setText(ledgerListPreview(item));parties.setTextSize(14);parties.setTextColor(Color.rgb(45,45,45));parties.setPadding(dp(4),dp(2),dp(4),dp(8));card.addView(parties);
            LinearLayout actions=row();Button open=button("OPEN / EDIT");Button send=button("SEND LEDGERS");open.setTextColor(Color.rgb(0,91,78));send.setTextColor(Color.WHITE);send.setBackground(rounded(Color.rgb(18,128,78),13));actions.addView(open,weighted(1f,44));actions.addView(send,weighted(1f,44));card.addView(actions);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(5),0,dp(9));cards.addView(card,cp);
            Runnable refresh=()->renderLedgerLists(cards,parent);open.setOnClickListener(v->showLedgerListEditor(index,refresh));send.setOnClickListener(v->sendSelectedCustomerLedgers(ledgerListPhones(item)));more.setOnClickListener(v->showLedgerListMenu(more,index,refresh));
        }
    }

    private void showCreateLedgerList(Runnable refresh){
        EditText input=new EditText(this);input.setHint("List name • Weekly Parties");input.setSingleLine(true);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Add Ledger List").setView(input).setPositiveButton("CREATE",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()){input.setError("List name required");return;}try{JSONArray lists=readLedgerLists();for(int i=0;i<lists.length();i++)if(name.equalsIgnoreCase(lists.optJSONObject(i).optString("name"))){input.setError("List name already exists");return;}JSONObject item=new JSONObject();item.put("name",name);item.put("phones",new JSONArray());lists.put(item);writeLedgerLists(lists);int index=lists.length()-1;d.dismiss();refresh.run();showLedgerListEditor(index,refresh);}catch(Exception e){toast("List create failed");}}));d.show();
    }

    private void showLedgerListEditor(int index,Runnable after){
        JSONArray lists=readLedgerLists();JSONObject saved=lists.optJSONObject(index);if(saved==null){toast("Ledger list not found");return;}Set<String> chosen=new LinkedHashSet<>(ledgerListPhones(saved));
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(12),0,dp(12),0);EditText search=new EditText(this);search.setHint("Search party name or phone");TextView selected=new TextView(this);selected.setTypeface(Typeface.DEFAULT_BOLD);selected.setPadding(dp(4),dp(6),dp(4),dp(6));ListView list=new ListView(this);list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);Button addCustomer=button("+ ADD NEW CUSTOMER");addCustomer.setTextColor(Color.rgb(0,91,78));box.addView(search);box.addView(selected);box.addView(list,new LinearLayout.LayoutParams(-1,dp(360)));box.addView(addCustomer,new LinearLayout.LayoutParams(-1,dp(44)));
        ArrayList<String> rows=new ArrayList<>();ArrayList<JSONObject> objects=new ArrayList<>();Runnable update=()->selected.setText(chosen.size()+" parties selected");
        Runnable refresh=()->{rows.clear();objects.clear();String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);try{JSONArray customers=new JSONArray(getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));for(int i=0;i<customers.length();i++){JSONObject o=customers.optJSONObject(i);if(o==null)continue;String phone=normalize(o.optString("phone",""));String name=o.optString("name","").trim();String row=(name.isEmpty()?phone:name+"\n"+phone)+(o.optString("ledger_uri","").isEmpty()?"\n⚠ Ledger PDF missing":"\n✓ Ledger ready");if(q.isEmpty()||row.toLowerCase(Locale.ROOT).contains(q)){rows.add(row);objects.add(o);}}}catch(Exception ignored){}list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,rows));for(int i=0;i<objects.size();i++)list.setItemChecked(i,chosen.contains(normalize(objects.get(i).optString("phone",""))));update.run();};
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh.run();}public void afterTextChanged(Editable e){}});list.setOnItemClickListener((a,v,pos,id)->{String phone=normalize(objects.get(pos).optString("phone",""));if(list.isItemChecked(pos))chosen.add(phone);else chosen.remove(phone);update.run();});addCustomer.setOnClickListener(v->showEditLedgerCustomer(null,refresh));
        AlertDialog d=new AlertDialog.Builder(this).setTitle(saved.optString("name","Ledger List")).setView(box).setPositiveButton("SAVE LIST",null).setNeutralButton("SEND",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{JSONArray current=readLedgerLists();JSONObject item=current.optJSONObject(index);if(item==null){toast("List not found");return;}JSONArray phones=new JSONArray();for(String phone:chosen)phones.put(phone);item.put("phones",phones);writeLedgerLists(current);toast("Ledger list saved • "+chosen.size()+" parties");d.dismiss();after.run();}catch(Exception e){toast("List save failed");}});d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->sendSelectedCustomerLedgers(chosen));});refresh.run();d.show();
    }

    private void showLedgerListMenu(View anchor,int index,Runnable refresh){
        PopupMenu menu=new PopupMenu(this,anchor);menu.getMenu().add("Weekly / Monthly schedule");menu.getMenu().add("Rename list");menu.getMenu().add("Delete list");menu.setOnMenuItemClickListener(item->{String action=item.getTitle().toString();JSONObject list=readLedgerLists().optJSONObject(index);if(list==null)return true;if(action.startsWith("Weekly")){Set<String> phones=ledgerListPhones(list);if(phones.isEmpty())toast("List empty hai");else showLedgerScheduleRepeat(phones,list.optString("name","Ledger List"));}else if(action.startsWith("Rename"))renameLedgerList(index,refresh);else deleteLedgerList(index,refresh);return true;});menu.show();
    }

    private void renameLedgerList(int index,Runnable refresh){JSONObject old=readLedgerLists().optJSONObject(index);if(old==null)return;EditText input=new EditText(this);input.setText(old.optString("name","Ledger List"));input.setSelectAllOnFocus(true);new AlertDialog.Builder(this).setTitle("Rename Ledger List").setView(input).setPositiveButton("SAVE",(d,w)->{String name=input.getText().toString().trim();if(name.isEmpty())return;try{JSONArray lists=readLedgerLists();lists.getJSONObject(index).put("name",name);writeLedgerLists(lists);refresh.run();toast("List renamed");}catch(Exception e){toast("Rename failed");}}).setNegativeButton("Cancel",null).show();}

    private void deleteLedgerList(int index,Runnable refresh){JSONObject item=readLedgerLists().optJSONObject(index);if(item==null)return;new AlertDialog.Builder(this).setTitle("Delete Ledger List?").setMessage(item.optString("name","Ledger List")+" delete होगी। Customer और Ledger PDFs safe रहेंगे।").setPositiveButton("DELETE",(d,w)->{try{JSONArray old=readLedgerLists(),out=new JSONArray();for(int i=0;i<old.length();i++)if(i!=index)out.put(old.get(i));writeLedgerLists(out);refresh.run();toast("Ledger list deleted");}catch(Exception e){toast("Delete failed");}}).setNegativeButton("Cancel",null).show();}

    private void showLedgerCustomersDialog(){showLedgerCustomersDialog("");}
    private void showLedgerCustomersDialog(String initialSearch){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(12),0,dp(12),0);
        EditText search=new EditText(this);search.setHint("Search phone or customer name");if(initialSearch!=null&&!initialSearch.trim().isEmpty())search.setText(initialSearch);
        TextView selected=new TextView(this);selected.setTypeface(Typeface.DEFAULT_BOLD);selected.setPadding(dp(4),dp(6),dp(4),dp(6));
        LinearLayout actions=row();Button selectAll=button("SELECT ALL");Button clear=button("CLEAR");Button send=button("SEND SELECTED LEDGERS");send.setTextColor(Color.WHITE);send.setBackgroundColor(Color.rgb(0,125,70));actions.addView(selectAll,weighted(1f,44));actions.addView(clear,weighted(.75f,44));
        LinearLayout savedActions=row();Button saveList=button("SAVE AS LIST");Button loadList=button("OPEN LISTS");Button scheduleList=button("WEEKLY / MONTHLY");savedActions.addView(saveList,weighted(1f,44));savedActions.addView(loadList,weighted(1f,44));savedActions.addView(scheduleList,weighted(1.1f,44));
        ListView list=new ListView(this);list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);box.addView(search);box.addView(selected);box.addView(actions);box.addView(savedActions);box.addView(list,new LinearLayout.LayoutParams(-1,dp(265)));box.addView(send,new LinearLayout.LayoutParams(-1,dp(50)));
        final ArrayList<String> rows=new ArrayList<>();final ArrayList<JSONObject> objects=new ArrayList<>();
        final Set<String> chosen=new LinkedHashSet<>();
        Runnable updateSelected=()->selected.setText(chosen.size()+" parties selected");
        Runnable refresh=()->{rows.clear();objects.clear();try{JSONArray a=new JSONArray(p.getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;String ph=normalize(o.optString("phone",""));String nm=o.optString("name","");String lf=o.optString("ledger_file","");String uri=o.optString("ledger_uri","");String row=(nm.isEmpty()?ph:nm+"\n"+ph)+(uri.isEmpty()?"\n⚠ Ledger PDF missing":"\n✓ Ledger ready"+(lf.isEmpty()?"":" • "+lf));if(q.isEmpty()||row.toLowerCase(Locale.ROOT).contains(q)){rows.add(row);objects.add(o);}}}catch(Exception ignored){}list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,rows));for(int i=0;i<objects.size();i++)list.setItemChecked(i,chosen.contains(normalize(objects.get(i).optString("phone",""))));updateSelected.run();};
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){refresh.run();}public void afterTextChanged(Editable e){}});
        list.setOnItemClickListener((a,v,pos,id)->{String phone=normalize(objects.get(pos).optString("phone",""));if(list.isItemChecked(pos))chosen.add(phone);else chosen.remove(phone);updateSelected.run();});
        list.setOnItemLongClickListener((a,v,pos,id)->{showEditLedgerCustomer(objects.get(pos),refresh);return true;});
        selectAll.setOnClickListener(v->{for(JSONObject o:objects)if(!o.optString("ledger_uri","").isEmpty())chosen.add(normalize(o.optString("phone","")));refresh.run();});
        clear.setOnClickListener(v->{chosen.clear();refresh.run();});
        saveList.setOnClickListener(v->{if(chosen.isEmpty()){toast("Pehle parties select karein");return;}showSaveChosenLedgerList(chosen);});
        loadList.setOnClickListener(v->showLedgerListsScreen());
        scheduleList.setOnClickListener(v->{if(chosen.isEmpty()){toast("Pehle parties select karein");return;}showLedgerScheduleRepeat(chosen);});
        send.setOnClickListener(v->sendSelectedCustomerLedgers(chosen));refresh.run();
        new AlertDialog.Builder(this).setTitle("Ledger Customers • "+ledgerCustomerCount()).setView(box).setPositiveButton("Add customer",(d,w)->showEditLedgerCustomer(null,()->showLedgerCustomersDialog())).setNeutralButton("Clear all",(d,w)->p.edit().remove(AutoReplyNotificationService.LEDGER_CUSTOMERS).apply()).setNegativeButton("Close",null).show();
    }

    private void showSaveChosenLedgerList(Set<String> chosen){
        EditText input=new EditText(this);input.setHint("List name • Monthly Ledgers");input.setSingleLine(true);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Save New Ledger List").setMessage(chosen.size()+" parties selected").setView(input).setPositiveButton("SAVE",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()){input.setError("List name required");return;}try{JSONArray lists=readLedgerLists();for(int i=0;i<lists.length();i++)if(name.equalsIgnoreCase(lists.optJSONObject(i).optString("name"))){input.setError("List name already exists");return;}JSONArray phones=new JSONArray();for(String phone:chosen)phones.put(phone);JSONObject item=new JSONObject();item.put("name",name);item.put("phones",phones);lists.put(item);writeLedgerLists(lists);toast("Ledger list saved • "+chosen.size()+" parties");d.dismiss();}catch(Exception e){toast("List save failed");}}));d.show();
    }

    private void sendSelectedCustomerLedgers(Set<String> selectedPhones){
        if(selectedPhones==null||selectedPhones.isEmpty()){toast("Pehle parties select karein");return;}
        if(!isAccessibilityServiceEnabled()){toast("Pehle Accessibility ON karein");try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(Exception ignored){}return;}
        ArrayList<Uri> files=new ArrayList<>();ArrayList<String> phones=new ArrayList<>(),names=new ArrayList<>();int skipped=0;
        try{JSONArray all=new JSONArray(getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE).getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));for(int i=0;i<all.length();i++){JSONObject o=all.optJSONObject(i);if(o==null)continue;String phone=normalize(o.optString("phone",""));if(!selectedPhones.contains(phone))continue;String uri=o.optString("ledger_uri","");if(phone.length()<10||uri.isEmpty()||isDoNotSend(phone)){skipped++;continue;}files.add(Uri.parse(uri));phones.add(phone);names.add(o.optString("name","Customer"));}}catch(Exception e){toast("Ledger list read failed");return;}
        if(files.isEmpty()){toast("Selected parties ke Ledger PDFs missing hain");return;}
        final int skippedCount=skipped;new AlertDialog.Builder(this).setTitle("Send Ledgers to "+files.size()+" parties?").setMessage("Har selected party ko uska own Ledger PDF jayega."+(skippedCount>0?"\n\n"+skippedCount+" missing/blocked party skipped.":"")).setPositiveButton("START SEND",(d,w)->{if(AutoReplyNotificationService.prepareLedgerBatchQueue(this,files,phones,names))toast("Ledger sending started • "+files.size()+" parties");else toast("Ledger queue start failed");}).setNegativeButton("Cancel",null).show();
    }

    private void showLedgerScheduleRepeat(Set<String> selectedPhones){showLedgerScheduleRepeat(selectedPhones,"Selected Parties");}
    private void showLedgerScheduleRepeat(Set<String> selectedPhones,String listName){
        final String[] repeats={"Weekly","Monthly"};final Set<String> copy=new LinkedHashSet<>(selectedPhones);
        new AlertDialog.Builder(this).setTitle(listName+" • Schedule").setSingleChoiceItems(repeats,0,null).setPositiveButton("NEXT",(dialog,which)->{int pos=((AlertDialog)dialog).getListView().getCheckedItemPosition();showLedgerScheduleDateTime(copy,repeats[pos<0?0:pos],listName);}).setNegativeButton("Cancel",null).show();
    }

    private void showLedgerScheduleDateTime(Set<String> selectedPhones,String repeat,String listName){
        java.util.Calendar chosen=java.util.Calendar.getInstance();chosen.add(java.util.Calendar.MINUTE,2);
        new android.app.DatePickerDialog(this,(dateView,year,month,day)->{chosen.set(java.util.Calendar.YEAR,year);chosen.set(java.util.Calendar.MONTH,month);chosen.set(java.util.Calendar.DAY_OF_MONTH,day);new android.app.TimePickerDialog(this,(timeView,hour,minute)->{chosen.set(java.util.Calendar.HOUR_OF_DAY,hour);chosen.set(java.util.Calendar.MINUTE,minute);chosen.set(java.util.Calendar.SECOND,0);chosen.set(java.util.Calendar.MILLISECOND,0);scheduleLedgerPartyList(selectedPhones,repeat,chosen.getTimeInMillis(),listName);},chosen.get(java.util.Calendar.HOUR_OF_DAY),chosen.get(java.util.Calendar.MINUTE),false).show();},chosen.get(java.util.Calendar.YEAR),chosen.get(java.util.Calendar.MONTH),chosen.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void scheduleLedgerPartyList(Set<String> selectedPhones,String repeat,long at,String listName){
        if(at<=System.currentTimeMillis()){toast("Future date/time select karein");return;}
        try{android.app.AlarmManager manager=(android.app.AlarmManager)getSystemService(ALARM_SERVICE);if(manager==null)throw new Exception("Alarm unavailable");if(Build.VERSION.SDK_INT>=31&&!manager.canScheduleExactAlarms()){try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception ignored){startActivity(new Intent(Settings.ACTION_SETTINGS));}toast("Alarms & reminders Allow karke schedule dobara karein");return;}JSONArray saved=new JSONArray();for(String phone:selectedPhones)saved.put(phone);getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(LEDGER_SAVED_PARTIES_KEY,saved.toString()).putLong(LEDGER_SCHEDULE_AT,at).putString(LEDGER_SCHEDULE_REPEAT,repeat).putString(LEDGER_SCHEDULE_LIST_NAME,listName).apply();if(!LedgerScheduleReceiver.scheduleNext(this,at))throw new Exception("Schedule failed");LedgerScheduleReceiver.showScheduledNotification(this,saved.length(),at,repeat,listName);String when=new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new java.util.Date(at));toast(listName+" • "+repeat+" scheduled • "+when);}catch(Exception e){toast("Ledger schedule save failed");}
    }

    private void showEditLedgerCustomer(JSONObject existing,Runnable after){
        SharedPreferences p=getSharedPreferences(AutoReplyNotificationService.PREFS,MODE_PRIVATE);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText phone=new EditText(this);phone.setHint("10-digit phone number");phone.setInputType(InputType.TYPE_CLASS_PHONE);EditText name=new EditText(this);name.setHint("Customer / WhatsApp name");
        String oldPhone="";if(existing!=null){oldPhone=normalize(existing.optString("phone",""));phone.setText(oldPhone);name.setText(existing.optString("name",""));}final String original=oldPhone;box.addView(phone);box.addView(name);
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle(existing==null?"Add customer":"Edit customer").setView(box).setPositiveButton("Save",(d,w)->{String ph=normalize(phone.getText().toString());if(ph.length()<10){toast("Valid phone number required");return;}try{JSONArray a=new JSONArray(p.getString(AutoReplyNotificationService.LEDGER_CUSTOMERS,"[]"));JSONArray out=new JSONArray();for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&!normalize(o.optString("phone","")).equals(original)&&!normalize(o.optString("phone","")).equals(ph))out.put(o);}JSONObject n=existing==null?new JSONObject():new JSONObject(existing.toString());n.put("phone",ph);n.put("name",name.getText().toString().trim());out.put(n);p.edit().putString(AutoReplyNotificationService.LEDGER_CUSTOMERS,out.toString()).apply();toast("Customer saved • Ledger PDF mapping preserved");after.run();}catch(Exception e){toast("Save failed");}}).setNegativeButton("Cancel",null);
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
                boolean hasRuleImage=!rule.optString("image","").isEmpty();String sendSummary=reply.isEmpty()?(hasRuleImage?"🖼 Image":"—"):reply+(hasRuleImage?" + 🖼 Image":"");
                TextView summary=new TextView(this);summary.setText("Received Msg : "+key+"\nSend Msg : "+sendSummary+"\n"+mode+(rule.optBoolean("case",false)?" • Case sensitive":""));summary.setTextSize(15);summary.setTextColor(Color.rgb(28,28,28));summary.setMaxLines(4);summary.setEllipsize(TextUtils.TruncateAt.END);summary.setPadding(dp(5),0,dp(4),0);
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
        pendingRuleImageUri=old==null?"":old.optString("image","");
        pendingRuleImageType=old==null?"image/*":old.optString("type","image/*");
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText keyword=new EditText(this);keyword.setHint("Received message / keyword");Spinner match=new Spinner(this);String[] modes={"Contains","Exact match","Starts with","Ends with"};match.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));
        EditText reply=new EditText(this);reply.setHint("Reply text / image caption");reply.setMinLines(2);CheckBox caseSensitive=new CheckBox(this);caseSensitive.setText("Case sensitive");
        if(old!=null){keyword.setText(old.optString("keyword"));match.setSelection(old.optInt("match",0));reply.setText(old.optString("reply"));caseSensitive.setChecked(old.optBoolean("case",false));}
        CheckBox sendImage=new CheckBox(this);sendImage.setText("Send image with this rule");sendImage.setChecked(!pendingRuleImageUri.isEmpty());
        Button image=button(pendingRuleImageUri.isEmpty()?"Choose image for this rule (optional)":"Change this rule image ✓");
        pendingRuleImageEnabled=sendImage;pendingRuleImageButton=image;
        box.addView(keyword);box.addView(match);box.addView(reply);box.addView(caseSensitive);box.addView(sendImage);box.addView(image,new LinearLayout.LayoutParams(-1,dp(46)));image.setOnClickListener(v->openPhoneGalleryFirst());
        AlertDialog d=new AlertDialog.Builder(this).setTitle(editIndex<0?"Add Auto Reply Rule":"Edit Rule "+(editIndex+1)).setView(box).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String k=keyword.getText().toString().trim();if(k.isEmpty()){keyword.setError("Keyword required");return;}if(sendImage.isChecked()&&pendingRuleImageUri.isEmpty()){toast("Image choose karein ya Send image option OFF karein");return;}try{JSONArray arr=new JSONArray(p.getString("rules","[]"));JSONObject o=new JSONObject();o.put("keyword",k);o.put("match",match.getSelectedItemPosition());o.put("case",caseSensitive.isChecked());o.put("reply",reply.getText().toString().trim());o.put("image",sendImage.isChecked()?pendingRuleImageUri:"");o.put("type",sendImage.isChecked()?pendingRuleImageType:"image/*");if(editIndex>=0){JSONArray out=new JSONArray();for(int i=0;i<arr.length();i++)out.put(i==editIndex?o:arr.get(i));arr=out;}else arr.put(o);p.edit().putString("rules",arr.toString()).putBoolean(AutoReplyNotificationService.ENABLED,true).apply();toast(editIndex<0?"Rule added • Auto reply ON":"Rule updated");d.dismiss();refresh.run();}catch(Exception e){toast("Rule save failed");}}));
        d.setOnDismissListener(x->{pendingRuleImageEnabled=null;pendingRuleImageButton=null;pendingRuleImageUri="";pendingRuleImageType="image/*";});d.show();
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
