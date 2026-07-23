package com.lathaeps.lathabulk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/** Receives a WhatsApp voice note via Share, transcribes it and routes it safely. */
public class VoiceNoteRouterActivity extends Activity implements RecognitionListener {
    private TextView status;
    private EditText transcript;
    private ProgressBar progress;
    private Uri audioUri;
    private SpeechRecognizer recognizer;
    private ParcelFileDescriptor recognitionAudio;
    private File decodedPcm;
    private String language = "hi-IN";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(buildScreen());
        receive(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        receive(intent);
    }

    private View buildScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(14), dp(14), dp(14));
        page.setBackgroundColor(Color.rgb(244, 247, 252));

        TextView title = text("🎤  Customer Voice Note", 22, Color.WHITE, true);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(18), 0, dp(12), 0);
        title.setBackground(rounded(Color.rgb(20, 74, 132), 18));
        page.addView(title, new LinearLayout.LayoutParams(-1, dp(62)));

        status = text("WhatsApp voice note receive हो रहा है…", 14,
                Color.rgb(45, 56, 72), true);
        status.setPadding(dp(12), dp(12), dp(12), dp(8));
        page.addView(status);

        progress = new ProgressBar(this);
        page.addView(progress, new LinearLayout.LayoutParams(-1, dp(42)));

        transcript = new EditText(this);
        transcript.setHint("Voice transcript यहाँ आएगा • जरूरत हो तो edit करें");
        transcript.setTextSize(16);
        transcript.setGravity(Gravity.TOP);
        transcript.setMinLines(6);
        transcript.setPadding(dp(14), dp(12), dp(14), dp(12));
        transcript.setBackground(rounded(Color.WHITE, 16));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(transcript);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout languageRow = row();
        Button hinglish = button("Hindi / Hinglish");
        Button english = button("English");
        languageRow.addView(hinglish, weighted(1f, 44));
        languageRow.addView(english, weighted(1f, 44));
        page.addView(languageRow);
        hinglish.setOnClickListener(v -> { language = "hi-IN"; transcribe(); });
        english.setOnClickListener(v -> { language = "en-IN"; transcribe(); });

        Button smart = button("SMART ROUTE");
        smart.setTextColor(Color.WHITE);
        smart.setTypeface(Typeface.DEFAULT_BOLD);
        smart.setBackground(rounded(Color.rgb(0, 128, 92), 15));
        page.addView(smart, new LinearLayout.LayoutParams(-1, dp(50)));
        smart.setOnClickListener(v -> smartRoute());

        LinearLayout manual = row();
        Button quote = button("Quotation Manager");
        Button price = button("Price List Manager");
        manual.addView(quote, weighted(1f, 48));
        manual.addView(price, weighted(1f, 48));
        page.addView(manual);
        quote.setOnClickListener(v -> routeQuotation());
        price.setOnClickListener(v -> routePriceList());
        return page;
    }

    private void receive(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) {
            showError("WhatsApp में voice note long-press करके Share → Business Dost चुनें");
            return;
        }
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        Uri stream = null;
        if (Build.VERSION.SDK_INT >= 33)
            stream = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class);
        else
            stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (stream == null && sharedText != null && !sharedText.trim().isEmpty()) {
            transcript.setText(sharedText.trim());
            setReady("Shared voice transcript ready • check करके Smart Route दबाएँ");
            return;
        }
        if (stream == null) {
            showError("Audio file receive नहीं हुई • WhatsApp voice note को Share करें");
            return;
        }
        audioUri = stream;
        transcribe();
    }

    private void transcribe() {
        if (audioUri == null) {
            showError("Voice note दुबारा Share करें");
            return;
        }
        if (Build.VERSION.SDK_INT < 33) {
            showError("Audio transcription के लिए Android 13 या नया चाहिए • transcript manually लिखें");
            return;
        }
        stopRecognition();
        status.setText("Voice note पढ़ रहा हूँ…");
        progress.setVisibility(View.VISIBLE);
        transcript.setText("");
        new Thread(() -> {
            try {
                File pcm = AudioPcmDecoder.decode(this, audioUri);
                runOnUiThread(() -> startRecognition(pcm));
            } catch (Exception error) {
                runOnUiThread(() -> showError("Voice पढ़ नहीं पाया: " + safe(error.getMessage())
                        + " • transcript manually edit कर सकते हैं"));
            }
        }, "customer-voice-decoder").start();
    }

    private void startRecognition(File pcm) {
        if (isFinishing()) return;
        decodedPcm = pcm;
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            showError("Google Speech Services उपलब्ध नहीं है • transcript manually लिखें");
            return;
        }
        try {
            recognitionAudio = ParcelFileDescriptor.open(pcm, ParcelFileDescriptor.MODE_READ_ONLY);
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(this);
            Intent request = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            request.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            request.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            request.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            request.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            request.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, recognitionAudio);
            request.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, 1);
            request.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING,
                    android.media.AudioFormat.ENCODING_PCM_16BIT);
            request.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, 16000);
            request.putStringArrayListExtra(RecognizerIntent.EXTRA_BIASING_STRINGS,
                    new ArrayList<>(Arrays.asList("quotation", "quote", "price list",
                            "rate list", "Polycab", "Finolex", "Anchor Roma", "coil",
                            "meter", "discount", "GST")));
            if (Build.VERSION.SDK_INT >= 34) {
                request.putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_DETECTION, true);
                request.putStringArrayListExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_DETECTION_ALLOWED_LANGUAGES,
                        new ArrayList<>(Arrays.asList("hi-IN", "en-IN")));
            }
            status.setText("Voice को text में बदल रहा हूँ…");
            recognizer.startListening(request);
        } catch (Exception error) {
            showError("Speech recognition start नहीं हुआ • transcript manually लिखें");
        }
    }

    private void smartRoute() {
        String value = value();
        if (value.isEmpty()) return;
        String low = normalize(value);
        boolean quote = low.contains("quote") || low.contains("quotation")
                || low.contains("estimate") || low.contains("कोटेशन")
                || low.contains("क्वोटेशन");
        boolean price = low.contains("price list") || low.contains("rate list")
                || low.contains("प्राइस लिस्ट") || low.contains("रेट लिस्ट")
                || low.contains("polycab") || low.contains("finolex")
                || low.contains("anchor") || low.contains("roma");
        if (quote && !price) routeQuotation();
        else if (price && !quote) routePriceList();
        else if (quote) {
            // "Polycab quotation" is still a quotation, not a price-list request.
            routeQuotation();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Voice किस काम के लिए है?")
                    .setItems(new String[]{"Quotation Manager", "Price List Manager"},
                            (d, which) -> { if (which == 0) routeQuotation(); else routePriceList(); })
                    .setNegativeButton("CANCEL", null).show();
        }
    }

    private void routeQuotation() {
        String value = value();
        if (value.isEmpty()) return;
        Intent route = new Intent(this, QuotationActivity.class);
        route.putExtra(QuotationActivity.EXTRA_VOICE_QUOTATION,
                value.replaceAll("(?i)\\b(?:quote|quotation|estimate)\\b", " ").trim());
        startActivity(route);
        finish();
    }

    private void routePriceList() {
        String value = value();
        if (value.isEmpty()) return;
        String routed = value;
        String low = normalize(routed);
        if (!low.contains("price list") && !low.contains("rate list")
                && !low.contains("प्राइस लिस्ट") && !low.contains("रेट लिस्ट"))
            routed = routed + " price list";
        Intent route = new Intent(this, MainActivity.class)
                .setAction(MainActivity.ACTION_FLOATING_VOICE_RESULT)
                .putExtra(MainActivity.EXTRA_FLOATING_VOICE_QUERY, routed)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(route);
        finish();
    }

    private String value() {
        String value = transcript.getText().toString().trim();
        if (value.isEmpty()) transcript.setError("Voice text check करें या manually लिखें");
        return value;
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private String chooseBest(ArrayList<String> results) {
        String best = results.get(0);
        int bestScore = score(best);
        for (String candidate : results) {
            int score = score(candidate);
            if (score > bestScore) { best = candidate; bestScore = score; }
        }
        return best;
    }

    private int score(String candidate) {
        String low = normalize(candidate);
        int score = candidate.length();
        for (String word : new String[]{"quotation", "quote", "price", "list", "rate",
                "polycab", "finolex", "anchor", "roma", "coil", "meter", "discount"})
            if (low.contains(word)) score += 30;
        return score;
    }

    private void setReady(String message) {
        progress.setVisibility(View.GONE);
        status.setText(message);
    }

    private void showError(String message) {
        stopRecognition();
        setReady(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "audio format issue" : value.trim();
    }

    @Override public void onReadyForSpeech(Bundle params) {}
    @Override public void onBeginningOfSpeech() {}
    @Override public void onRmsChanged(float rmsdB) {}
    @Override public void onBufferReceived(byte[] buffer) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onEvent(int eventType, Bundle params) {}

    @Override public void onPartialResults(Bundle partialResults) {
        ArrayList<String> results = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (results != null && !results.isEmpty()) transcript.setText(chooseBest(results));
    }

    @Override public void onResults(Bundle resultsBundle) {
        ArrayList<String> results = resultsBundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (results == null || results.isEmpty()) {
            showError("Voice text नहीं मिला • English/Hinglish button से retry करें");
            return;
        }
        String result = chooseBest(results);
        transcript.setText(result);
        transcript.setSelection(transcript.length());
        setReady("Transcript ready ✓ • check करके Smart Route दबाएँ");
        finishRecognition();
    }

    @Override public void onError(int error) {
        String reason;
        switch (error) {
            case SpeechRecognizer.ERROR_NO_MATCH: reason = "voice साफ समझ नहीं आई"; break;
            case SpeechRecognizer.ERROR_NETWORK:
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: reason = "speech service network issue"; break;
            case SpeechRecognizer.ERROR_AUDIO: reason = "audio format read नहीं हुआ"; break;
            default: reason = "speech service error " + error;
        }
        showError(reason + " • language बदलकर retry या transcript manually लिखें");
    }

    private void stopRecognition() {
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) {}
            try { recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        if (recognitionAudio != null) {
            try { recognitionAudio.close(); } catch (Exception ignored) {}
            recognitionAudio = null;
        }
    }

    private void finishRecognition() {
        if (recognizer != null) {
            try { recognizer.destroy(); } catch (Exception ignored) {}
            recognizer = null;
        }
        if (recognitionAudio != null) {
            try { recognitionAudio.close(); } catch (Exception ignored) {}
            recognitionAudio = null;
        }
    }

    @Override protected void onDestroy() {
        stopRecognition();
        if (decodedPcm != null) decodedPcm.delete();
        super.onDestroy();
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(Color.rgb(21, 73, 126));
        return button;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        if (bold) text.setTypeface(Typeface.DEFAULT_BOLD);
        return text;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private LinearLayout.LayoutParams weighted(float weight, int height) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(height), weight);
        params.setMargins(dp(2), dp(3), dp(2), dp(3));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
