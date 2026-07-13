package com.lathaeps.lathabulk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int CONTACT_PERMISSION = 101;
    private static final int PICK_PDF = 102;

    private final List<ContactItem> contacts = new ArrayList<>();
    private final Set<Integer> selected = new LinkedHashSet<>();
    private final List<ContactItem> queue = new ArrayList<>();
    private ArrayAdapter<ContactItem> adapter;
    private ListView listView;
    private TextView statusText;
    private TextView pdfText;
    private EditText messageBox;
    private Button nextButton;
    private Uri pdfUri;
    private int queueIndex = 0;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(buildUi());
    }

    private View buildUi() {
        int pad = dp(16);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(Color.rgb(248, 246, 240));

        TextView title = new TextView(this);
        title.setText("LATHA BULK");
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.rgb(30, 30, 30));
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        TextView note = new TextView(this);
        note.setText("Select contacts and open WhatsApp chats one by one. You must tap Send in WhatsApp.");
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(6), 0, dp(12));
        root.addView(note, matchWrap());

        Button load = button("1. Load Phone Contacts");
        load.setOnClickListener(v -> requestContacts());
        root.addView(load, matchWrap());

        statusText = new TextView(this);
        statusText.setText("Selected: 0");
        statusText.setPadding(0, dp(8), 0, dp(5));
        statusText.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(statusText, matchWrap());

        listView = new ListView(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, contacts);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((p, v, position, id) -> {
            if (listView.isItemChecked(position)) selected.add(position); else selected.remove(position);
            statusText.setText("Selected: " + selected.size());
        });
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1f));

        messageBox = new EditText(this);
        messageBox.setHint("Type WhatsApp message here...");
        messageBox.setMinLines(3);
        messageBox.setGravity(Gravity.TOP);
        root.addView(messageBox, new LinearLayout.LayoutParams(-1, dp(100)));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button pdf = button("Choose PDF");
        Button share = button("Share PDF");
        row.addView(pdf, new LinearLayout.LayoutParams(0, dp(52), 1f));
        row.addView(share, new LinearLayout.LayoutParams(0, dp(52), 1f));
        pdf.setOnClickListener(v -> choosePdf());
        share.setOnClickListener(v -> sharePdf());
        root.addView(row, matchWrap());

        pdfText = new TextView(this);
        pdfText.setText("No PDF selected");
        pdfText.setPadding(0, dp(5), 0, dp(5));
        root.addView(pdfText, matchWrap());

        nextButton = button("2. Start Selected Chats");
        nextButton.setOnClickListener(v -> startOrNext());
        root.addView(nextButton, matchWrap());
        return root;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(16);
        return b;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }

    private void requestContacts() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) loadContacts();
        else requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == CONTACT_PERMISSION && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) loadContacts();
        else Toast.makeText(this, "Contacts permission required", Toast.LENGTH_LONG).show();
    }

    private void loadContacts() {
        contacts.clear(); selected.clear(); listView.clearChoices();
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
                    contacts.add(new ContactItem(name == null ? "Unknown" : name, number));
                }
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
        statusText.setText("Selected: 0 | Contacts: " + contacts.size());
    }

    private String normalize(String raw) {
        if (raw == null) return "";
        String d = raw.replaceAll("[^0-9]", "");
        if (d.startsWith("91") && d.length() == 12) return d;
        if (d.length() == 10) return "91" + d;
        return "";
    }

    private void startOrNext() {
        if (queue.isEmpty()) {
            if (selected.isEmpty()) { toast("Select contacts first"); return; }
            if (messageBox.getText().toString().trim().isEmpty()) { toast("Type a message first"); return; }
            for (Integer i : selected) if (i >= 0 && i < contacts.size()) queue.add(contacts.get(i));
            queueIndex = 0;
        }
        openNext();
    }

    private void openNext() {
        if (queueIndex >= queue.size()) {
            toast("Completed " + queue.size() + " chats");
            queue.clear(); queueIndex = 0;
            nextButton.setText("2. Start Selected Chats");
            return;
        }
        ContactItem item = queue.get(queueIndex++);
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
                i.setPackage(null);
                startActivity(i);
            }
        }
        nextButton.setText(queueIndex < queue.size() ? "Open Next Chat (" + (queueIndex + 1) + "/" + queue.size() + ")" : "Finish");
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
            getContentResolver().takePersistableUriPermission(pdfUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfText.setText("PDF selected");
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

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private static class ContactItem {
        final String name, number;
        ContactItem(String n, String p) { name = n; number = p; }
        @Override public String toString() { return name + "  •  +" + number; }
    }
}
