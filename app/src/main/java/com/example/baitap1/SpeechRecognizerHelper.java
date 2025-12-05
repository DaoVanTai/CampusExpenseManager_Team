package com.example.baitap1;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import java.util.Locale;

public class SpeechRecognizerHelper {

    public static final int SPEECH_REQUEST_CODE = 100;
    private Activity activity;

    public SpeechRecognizerHelper(Activity activity) {
        this.activity = activity;
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói theo cú pháp: [Tên] [Số tiền] [Danh mục]");

        try {
            activity.startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(activity, "Thiết bị không hỗ trợ giọng nói", Toast.LENGTH_SHORT).show();
        }
    }
}