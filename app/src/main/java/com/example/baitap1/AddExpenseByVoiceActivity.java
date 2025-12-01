package com.example.baitap1; // ⭐ Đảm bảo package này là đúng cho dự án của bạn ⭐

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// SỬ DỤNG LẠI DatabaseHelper từ package chính
// SỬ DỤNG LẠI SpeechRecognizerHelper từ package của nó
import com.example.baitap1.SpeechRecognizerHelper; // ⭐ Thay bằng package đúng nếu SpeechRecognizerHelper ở package khác ⭐

import java.util.ArrayList;

public class AddExpenseByVoiceActivity extends AppCompatActivity {

    private static final String TAG = "VoiceExpenseActivity";

    // UI Components
    private TextView resultTextView;
    private Button micButton;
    private Button saveButton;

    // Helpers
    private SpeechRecognizerHelper speechHelper;
    private DatabaseHelper dbHelper;

    // Biến tạm lưu trữ văn bản đã được nhận dạng
    private String recognizedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ⭐ KẾT NỐI GIAO DIỆN XML ⭐
        setContentView(R.layout.activity_speech_expense);

        // 1. Khởi tạo UI
        resultTextView = findViewById(R.id.result_text_view);
        micButton = findViewById(R.id.mic_button);
        saveButton = findViewById(R.id.button_save_expense);

        // 2. Khởi tạo Helpers
        speechHelper = new SpeechRecognizerHelper(this);
        dbHelper = new DatabaseHelper(this);

        // 3. Listener cho nút GHI ÂM (Bắt đầu quá trình STT)
        micButton.setOnClickListener(v -> {
            recognizedText = "";
            resultTextView.setText("");
            saveButton.setEnabled(false);
            saveButton.setAlpha(0.5f);

            speechHelper.startListening(); // Gọi helper để gửi Intent ghi âm
        });

        // 4. Listener cho nút LƯU (Xử lý và lưu vào DB)
        saveButton.setOnClickListener(v -> {
            if (!recognizedText.isEmpty()) {
                processAndSaveExpense(recognizedText);
            } else {
                Toast.makeText(this, "Vui lòng ghi âm chi tiêu trước.", Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setEnabled(false);
        saveButton.setAlpha(0.5f);
    }

    // ------------------------------------------------------------------
    // ⭐ PHƯƠNG THỨC BẮT KẾT QUẢ GHI ÂM (STT) ⭐
    // ------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SpeechRecognizerHelper.SPEECH_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (results != null && !results.isEmpty()) {
                    String spokenText = results.get(0);

                    recognizedText = spokenText;
                    resultTextView.setText(recognizedText);

                    saveButton.setEnabled(true);
                    saveButton.setAlpha(1.0f);

                } else {
                    resultTextView.setText("Không nhận dạng được văn bản.");
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Đã hủy ghi âm.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ------------------------------------------------------------------
    // ⭐ PHƯƠNG THỨC PHÂN TÍCH VÀ LƯU VÀO DATABASE ⭐
    // ------------------------------------------------------------------

    private void processAndSaveExpense(String textToParse) {
        // Giả định cú pháp: Mô_tả Số_tiền Danh_mục
        String[] parts = textToParse.split(" ");

        if (parts.length >= 3) {
            String description = parts[0];
            String category = parts[2];
            double amount;

            try {
                amount = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "LƯU LỖI: Số tiền không hợp lệ. Hãy kiểm tra lại văn bản.", Toast.LENGTH_LONG).show();
                return;
            }

            // Gọi hàm thêm vào DatabaseHelper (Đã được cập nhật ở bước trước)
            boolean success = dbHelper.addExpense(description, amount, category);

            if (success) {
                Toast.makeText(this, "✅ ĐÃ LƯU: " + description + " (" + amount + ")", Toast.LENGTH_LONG).show();
                // Reset UI
                resultTextView.setText("Chi tiêu đã được lưu. Hãy ghi âm khoản mới.");
                recognizedText = "";
                saveButton.setEnabled(false);
                saveButton.setAlpha(0.5f);
            } else {
                Toast.makeText(this, "❌ LỖI LƯU CHI TIÊU VÀO DATABASE.", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "LƯU LỖI: Cú pháp không đủ 3 phần (Mô tả - Số tiền - Danh mục).", Toast.LENGTH_LONG).show();
        }
    }
}