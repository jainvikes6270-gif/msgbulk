package com.lathaeps.lathabulk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
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
    private TextView statusText;
    private TextView pdfText;
    private TextView miniProgress;
    private EditText messageBox;
    private EditText searchBox;
    private Button sendButton;
    private Uri pdfUri;
    private int queueIndex = 0;
    private String activeGroup = "";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        createNotificationChannel();
        requestNotificationPermissionIfNeeded();
        setContentView(buildUi());
    }

    private View buildUi() {
        int pad = dp(12);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, dp(10), pad, dp(10));
        root.setBackgroundColor(Color.rgb(248, 246, 240));

        TextView title = new TextView(this);
        title.setText("LATHA BULK v2");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.rgb(30, 30, 30));
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        Button load = button("Load Contacts");
        Button selectAll = button("Select All");
        Button clear = button("Clear");
        topRow.addView(load, weighted(1.25f, dp(48)));
        topRow.addView(selectAll, weighted(1f, dp(48)));
        topRow.addView(clear, weighted(.8f, dp(48)));
        root.addView(topRow, matchWrap());

        load.setOnClickListener(v -> requestContacts());
        selectAll.setOnClickListener(v -> selectAllVisible());
        clear.setOnClickListener(v -> clearSelection());

        searchBox = new EditText(this);
        searchBox.setHint("Search phone contacts by name or number");
        searchBox.setSingleLine(true);
        searchBox.setTextSize(15);
        searchBox.setPadding(dp(12), 0, dp(12), 0);
        root.addView(searchBox, new LinearLayout.LayoutParams(-1, dp(48)));
        searchBox.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { filterContacts(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });

        LinearLayout groupRow = new LinearLayout(this);
        groupRow.setOrientation(LinearLayout.HORIZONTAL);
        Button saveGroup = button("Save Group");
        Button openGroup = button("My Groups");
        Button deleteGroup = button("Delete Group");
        groupRow.addView(saveGroup, weighted(1f, dp(46)));
        groupRow.addView(openGroup, weighted(1f, dp(46)));
        groupRow.addView(deleteGroup, weighted(1f, dp(46)));
        root.addView(groupRow, matchWrap());
        saveGroup.setOnClickListener(v -> showSaveGroupDialog());
        openGroup.setOnClickListener(v -> showGroupsDialog(false));
        deleteGroup.setOnClickListener(v -> showGroupsDialog(true));

        statusText = new TextView(this);
        statusText.setText("Selected: 0 | Contacts: 0");
        statusText.setPadding(dp(2), dp(6), dp(2), dp(4));
        statusText.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(statusText, matchWrap());

        listView = new ListView(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, visibleContacts);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((p, v, position, id) -> {
            ContactItem item = visibleContacts.get(position);
            if (selectedNumbers.contains(item.number)) selectedNumbers.remove(item.number);
            else selectedNumbers.add(item.number);
            refreshChecks();
        });
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1f));

        messageBox = new EditText(this);
        messageBox.setHint("Type WhatsApp message...");
        messageBox.setMinLines(2);
        messageBox.setMaxLines(3);
        messageBox.setGravity(Gravity.TOP);
        messageBox.setTextSize(15);
        root.addView(messageBox, new LinearLayout.LayoutParams(-1, dp(76)));

        LinearLayout fileRow = new LinearLayout(this);
        fileRow.setOrientation(LinearLayout.HORIZONTAL);
        Button pdf = button("Choose PDF");
        Button share = button("Share PDF");
        fileRow.addView(pdf, weighted(1f, dp(44)));
        fileRow.addView(share, weighted(1f, dp(44)));
        pdf.setOnClickListener(v -> choosePdf());
        share.setOnClickListener(v -> sharePdf());
        root.addView(fileRow, matchWrap());

        pdfText = new TextView(this);
        pdfText.setText("No PDF selected");
        pdfText.setTextSize(12);
        pdfText.setPadding(dp(3), dp(2), dp(3), dp(2));
        root.addView(pdfText, matchWrap());

        miniProgress = new TextView(this);
        miniProgress.setText("Ready");
        miniProgress.setTextSize(12);
        miniProgress.setGravity(Gravity.CENTER);
        miniProgress.setPadding(dp(6), dp(3), dp(6), dp(3));
        miniProgress.setBackgroundColor(Color.rgb(235, 232, 222));
        root.addView(miniProgress, matchWrap());

        sendButton = button("SEND TO SELECTED CONTACTS");
        sendButton.setTextSize(17);
        sendButton.setTypeface(Typeface.DEFAULT_BOLD);
        sendButton.setTextColor(Color.WHITE);
        sendButton.setBackgroundColor(Color.rgb(25, 110, 60));
        sendButton.setOnClickListener(v -> startOrNext());
        LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(-1, dp(58));
        sendLp.topMargin = dp(5);
        root.addView(sendButton, sendLp);

        return root;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setPadding(dp(4), 0, dp(4), 0);
        return b;
    }

    private LinearLayout.LayoutParams weighted(float weight, int height) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, height, weight);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        return lp;
    }

    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(-1, -2); }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    private void requestContacts() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) loadContacts();
        else requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_PERMISSION);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == CONTACT_PERMISSION) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) loadContacts();
            else toast("Contacts permission required");
        }
    }

    private void loadContacts() {
        allContacts.clear();
        visibleContacts.clear();
        selectedNumbers.clear();
        Cursor c = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        Set<String> seen = new LinkedHashSet<>();
        if (c != null) {
            int ni = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int pi = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (c.moveToNext()) {
                String number = normalize(c.getString(pi));
                if (!number.isEmpty() && seen.add(number)) {
                    String name = c.getString(ni);
                    allContacts.add(new ContactItem(name == null ? "Unknown" : name, number));
                }
            }
            c.close();
        }
        Collections.sort(allContacts, Comparator.comparing(a -> a.name.toLowerCase(Locale.ROOT)));
        filterContacts(searchBox.getText().toString());
        toast(allContacts.size() + " phone contacts loaded");
    }

    private String normalize(String raw) {
        if (raw == null) return "";
        String d = raw.replaceAll("[^0-9]", "");
        if (d.startsWith("91") && d.length() == 12) return d;
        if (d.startsWith("0") && d.length() == 11) d = d.substring(1);
        if (d.length() == 10) return "91" + d;
        return "";
    }

    private void filterContacts(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        visibleContacts.clear();
        for (ContactItem item : allContacts) {
            if (q.isEmpty() || item.name.toLowerCase(Locale.ROOT).contains(q) || item.number.contains(q)) visibleContacts.add(item);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            refreshChecks();
        }
    }

    private void refreshChecks() {
        listView.clearChoices();
        for (int i = 0; i < visibleContacts.size(); i++) {
            listView.setItemChecked(i, selectedNumbers.contains(visibleContacts.get(i).number));
        }
        statusText.setText("Selected: " + selectedNumbers.size() + " | Showing: " + visibleContacts.size() + " / " + allContacts.size()
                + (activeGroup.isEmpty() ? "" : " | Group: " + activeGroup));
    }

    private void selectAllVisible() {
        for (ContactItem item : visibleContacts) selectedNumbers.add(item.number);
        refreshChecks();
    }

    private void clearSelection() {
        selectedNumbers.clear();
        queue.clear();
        queueIndex = 0;
        activeGroup = "";
        sendButton.setText("SEND TO SELECTED CONTACTS");
        miniProgress.setText("Ready");
        cancelProgressNotification();
        refreshChecks();
    }

    private Map<String, List<String>> readGroups() {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        try {
            String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(GROUPS_KEY, "{}");
            JSONObject root = new JSONObject(raw);
            JSONArray names = root.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    String name = names.getString(i);
                    JSONArray arr = root.getJSONArray(name);
                    List<String> nums = new ArrayList<>();
                    for (int j = 0; j < arr.length(); j++) nums.add(arr.getString(j));
                    groups.put(name, nums);
                }
            }
        } catch (Exception ignored) {}
        return groups;
    }

    private void writeGroups(Map<String, List<String>> groups) {
        try {
            JSONObject root = new JSONObject();
            for (Map.Entry<String, List<String>> e : groups.entrySet()) root.put(e.getKey(), new JSONArray(e.getValue()));
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(GROUPS_KEY, root.toString()).apply();
        } catch (Exception e) { toast("Could not save group"); }
    }

    private void showSaveGroupDialog() {
        if (selectedNumbers.isEmpty()) { toast("Select contacts first"); return; }
        EditText input = new EditText(this);
        input.setHint("Example: Dealers");
        input.setSingleLine(true);
        new AlertDialog.Builder(this)
                .setTitle("Save contact group")
                .setMessage(selectedNumbers.size() + " contacts selected")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) { toast("Enter group name"); return; }
                    Map<String, List<String>> groups = readGroups();
                    groups.put(name, new ArrayList<>(selectedNumbers));
                    writeGroups(groups);
                    activeGroup = name;
                    refreshChecks();
                    toast("Group saved: " + name);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showGroupsDialog(boolean deleteMode) {
        Map<String, List<String>> groups = readGroups();
        if (groups.isEmpty()) { toast("No saved groups"); return; }
        String[] names = groups.keySet().toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(deleteMode ? "Delete group" : "Open saved group")
                .setItems(names, (d, which) -> {
                    String name = names[which];
                    if (deleteMode) {
                        groups.remove(name);
                        writeGroups(groups);
                        if (name.equals(activeGroup)) activeGroup = "";
                        toast("Deleted: " + name);
                        refreshChecks();
                    } else {
                        selectedNumbers.clear();
                        selectedNumbers.addAll(groups.get(name));
                        activeGroup = name;
                        searchBox.setText("");
                        refreshChecks();
                        toast(name + " opened");
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void startOrNext() {
        if (queue.isEmpty()) {
            if (selectedNumbers.isEmpty()) { toast("Select contacts or open a saved group"); return; }
            if (messageBox.getText().toString().trim().isEmpty()) { toast("Type a message first"); return; }
            for (ContactItem item : allContacts) if (selectedNumbers.contains(item.number)) queue.add(item);
            if (queue.isEmpty()) { toast("Reload contacts, then open the group again"); return; }
            queueIndex = 0;
        }
        openNext();
    }

    private void openNext() {
        if (queueIndex >= queue.size()) {
            int total = queue.size();
            miniProgress.setText("Completed: " + total + " / " + total);
            showProgressNotification(total, total, "Completed");
            toast("Completed " + total + " chats");
            queue.clear();
            queueIndex = 0;
            sendButton.setText("SEND TO SELECTED CONTACTS");
            return;
        }

        ContactItem item = queue.get(queueIndex);
        int current = queueIndex + 1;
        miniProgress.setText("Opening " + current + "/" + queue.size() + " • " + item.name);
        showProgressNotification(current, queue.size(), item.name);
        queueIndex++;

        String encoded = URLEncoder.encode(messageBox.getText().toString().trim(), StandardCharsets.UTF_8);
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + item.number + "?text=" + encoded));
        try {
            i.setPackage("com.whatsapp");
            startActivity(i);
        } catch (Exception first) {
            try {
                i.setPackage("com.whatsapp.w4b");
                startActivity(i);
            } catch (Exception second) {
                try {
                    i.setPackage(null);
                    startActivity(i);
                } catch (Exception third) {
                    toast("WhatsApp not found");
                }
            }
        }
        sendButton.setText(queueIndex < queue.size()
                ? "OPEN NEXT (" + (queueIndex + 1) + "/" + queue.size() + ")"
                : "FINISH");
    }

    private void choosePdf() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/pdf");
        startActivityForResult(i, PICK_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();
            try { getContentResolver().takePersistableUriPermission(pdfUri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
            catch (Exception ignored) {}
            pdfText.setText("PDF selected: " + pdfUri.getLastPathSegment());
        }
    }

    private void sharePdf() {
        if (pdfUri == null) { toast("Choose PDF first"); return; }
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("application/pdf");
        i.putExtra(Intent.EXTRA_STREAM, pdfUri);
        i.putExtra(Intent.EXTRA_TEXT, messageBox.getText().toString().trim());
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            i.setPackage("com.whatsapp");
            startActivity(i);
        } catch (Exception e) {
            i.setPackage(null);
            startActivity(Intent.createChooser(i, "Share PDF"));
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Sending progress", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Small WhatsApp queue progress notification");
            channel.setSound(null, null);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void showProgressNotification(int current, int total, String contact) {
        Intent launch = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(current >= total ? "Latha Bulk completed" : "Latha Bulk " + current + "/" + total)
                .setContentText(contact)
                .setOnlyAlertOnce(true)
                .setOngoing(current < total)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(total, Math.min(current, total), false)
                .setContentIntent(pending);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, b.build());
    }

    private void cancelProgressNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private static class ContactItem {
        final String name, number;
        ContactItem(String n, String p) { name = n; number = p; }
        @Override public String toString() { return name + "  •  +" + number; }
    }
}
