package com.example.baitap1;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.baitap1.viewmodel.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddExpenseByVoiceActivity extends AppCompatActivity {

    // Khai báo các biến UI
    private TextView tvRecognizedText;
    private EditText etAmount, etCategory, etDescription, etDate;
    private ImageButton btnMic;
    private Button btnSave;

    // ViewModel để xử lý database (nếu cần dùng)
    private TransactionViewModel transactionViewModel;

    // Mã request cho Intent giọng nói
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kích hoạt chế độ tràn viền
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_add_expense_by_voice);

        // 2. Xử lý Insets (Tránh bị tai thỏ/camera che mất)
        // Yêu cầu: File XML activity_add_expense_by_voice.xml phải có ID root là "main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo ViewModel (chuẩn bị cho việc lưu DB)
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Ánh xạ View
        initViews();

        // Tự động điền ngày hiện tại
        etDate.setText(getCurrentDate());

        // Sự kiện bấm nút Mic
        btnMic.setOnClickListener(v -> startVoiceInput());

        // Sự kiện bấm nút Lưu
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void initViews() {
        tvRecognizedText = findViewById(R.id.tvRecognizedText);
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        btnMic = findViewById(R.id.btnMic);
        btnSave = findViewById(R.id.btnSave);
    }

    // Hàm gọi Google Voice Input
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); // Tự động lấy ngôn ngữ máy (thường là vi-VN)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói chi tiêu của bạn (VD: Ăn sáng 30 nghìn)");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Thiết bị không hỗ trợ nhận diện giọng nói", Toast.LENGTH_SHORT).show();
        }
    }

    // Nhận kết quả trả về từ Google Voice
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String spokenText = result.get(0);
                    tvRecognizedText.setText("Bạn đã nói: " + spokenText);

                    // Xử lý thông minh: Tự động điền form dựa trên câu nói
                    processVoiceCommand(spokenText);
                }
            }
        }
    }

    // LOGIC THÔNG MINH: Phân tích câu nói để tách Tiền và Danh mục
    private void processVoiceCommand(String text) {
        String lowerText = text.toLowerCase();

        // 1. Tự động điền mô tả là toàn bộ câu nói
        etDescription.setText(text);

        // 2. Xử lý số tiền (Tìm các số trong chuỗi)
        long amount = extractAmount(lowerText);
        if (amount > 0) {
            // Định dạng số nguyên cho đẹp (bỏ .0)
            etAmount.setText(String.valueOf(amount));
        }

        // 3. Xử lý danh mục (Dựa trên từ khóa)
        String category = detectCategory(lowerText);
        etCategory.setText(category);
    }

    private long extractAmount(String text) {
        // Regex tìm chuỗi số liên tiếp
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(text);

        long value = 0;
        // Tìm số đầu tiên hoặc số hợp lý nhất
        if (m.find()) {
            try {
                value = Long.parseLong(m.group());

                // Xử lý đơn vị tiền tệ việt nam nói tắt
                if (text.contains("nghìn") || text.contains(" nghìn") || text.matches(".*\\d+k.*")) {
                    value *= 1000;
                } else if (text.contains("trăm")) {
                    value *= 100;
                } else if (text.contains("triệu")) {
                    value *= 1000000;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    private String detectCategory(String text) {
        if (text.contains("ăn") || text.contains("uống") || text.contains("cafe") || text.contains("cơm") || text.contains("phở")) {
            return "Ăn uống";
        } else if (text.contains("xe") || text.contains("xăng") || text.contains("grab") || text.contains("bus") || text.contains("gửi")) {
            return "Đi lại";
        } else if (text.contains("nhà") || text.contains("điện") || text.contains("nước") || text.contains("wifi") || text.contains("net")) {
            return "Sinh hoạt phí";
        } else if (text.contains("học") || text.contains("sách") || text.contains("vở") || text.contains("bút")) {
            return "Học tập";
        } else if (text.contains("áo") || text.contains("quần") || text.contains("giày") || text.contains("mua")) {
            return "Mua sắm";
        }
        return "Khác"; // Mặc định nếu không tìm thấy từ khóa
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Lưu vào Database
    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String categoryName = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        // String date = etDate.getText().toString().trim(); // Bạn có thể cần parse date này sang Timestamp nếu Model yêu cầu

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            // --- KHU VỰC DATABASE (Tương tự AddBudgetActivity) ---
            // Bạn cần tạo đối tượng Transaction ở đây và gọi ViewModel

            // Ví dụ mẫu (Bạn hãy bỏ comment và chỉnh sửa cho đúng Constructor của nhóm bạn):
            /*
            Transaction newTransaction = new Transaction(
                amount,
                categoryName, // Hoặc categoryId nếu bạn quản lý theo ID
                description,
                System.currentTimeMillis() // Hoặc parse từ etDate
            );
            transactionViewModel.insertTransaction(newTransaction);
            */

            Toast.makeText(this, "Đã lưu: " + categoryName + " - " + amountStr, Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}