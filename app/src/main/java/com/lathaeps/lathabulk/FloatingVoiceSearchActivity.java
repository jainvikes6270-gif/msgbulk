package com.lathaeps.lathabulk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/** Small transparent bridge that lets the overlay receive a speech result. */
public class FloatingVoiceSearchActivity extends Activity {
    private static final int REQUEST_SPEECH = 901;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null) showLanguageChooser();
    }

    private void showLanguageChooser() {
        String[] languages = {"हिंदी  •  Hindi words", "HINGLISH / ENGLISH"};
        new AlertDialog.Builder(this)
                .setTitle("Voice language चुनें")
                .setItems(languages, (dialog, which) -> startSpeech(which == 0 ? "hi-IN" : "en-IN"))
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    private void startSpeech(String language) {
        try {
            Intent speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
            speech.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
            speech.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            speech.putExtra(RecognizerIntent.EXTRA_PROMPT, "hi-IN".equals(language) ? "हिंदी में ब्रांड और सामान बोलिए" : "Speak brand and product in Hinglish / English");
            speech.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
            startActivityForResult(speech, REQUEST_SPEECH);
        } catch (Exception error) {
            Toast.makeText(this, "Google Speech Services में Hindi voice उपलब्ध नहीं है", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> heard = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (heard != null && !heard.isEmpty()) {
                Intent open = new Intent(this, MainActivity.class)
                        .setAction(MainActivity.ACTION_FLOATING_VOICE_RESULT)
                        .putExtra(MainActivity.EXTRA_FLOATING_VOICE_QUERY, heard.get(0))
                        .putStringArrayListExtra(MainActivity.EXTRA_FLOATING_VOICE_ALTERNATIVES, heard)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(open);
            }
        }
        finish();
    }
}
