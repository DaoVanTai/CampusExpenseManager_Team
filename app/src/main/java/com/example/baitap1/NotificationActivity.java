package com.example.baitap1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSetLimit;
    private Button btnSetCategoryLimit; // ⭐ Khai báo nút mới
    private ListView lvNotifications;
    private CardView cardNewMessage;
    private TextView tvNewMessageContent;

    private ArrayList<String> notificationList;
    private ArrayAdapter<String> adapter;
    private DatabaseHelper dbHelper; // ⭐ Thêm DatabaseHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ
        btnBack = findViewById(R.id.btnBackNoti);
        btnSetLimit = findViewById(R.id.btnSetLimitNoti);
        btnSetCategoryLimit = findViewById(R.id.btnSetCategoryLimit); // ⭐ Ánh xạ nút mới
        lvNotifications = findViewById(R.id.lvNotifications);
        cardNewMessage = findViewById(R.id.cardNewMessage);
        tvNewMessageContent = findViewById(R.id.tvNewMessageContent);

        btnBack.setOnClickListener(v -> finish());

        // Sự kiện nút Hạn mức Ngày (Cũ)
        btnSetLimit.setOnClickListener(v -> showSetLimitDialog());

        // ⭐ Sự kiện nút Hạn mức Danh mục (Mới)
        btnSetCategoryLimit.setOnClickListener(v -> showCategoryLimitDialog());

        // Setup ListView
        notificationList = new ArrayList<>();
        long currentLimit = getLimit();
        if (currentLimit > 0) {
            notificationList.add("Hạn mức NGÀY: " + currentLimit + " VNĐ");
        } else {
            notificationList.add("Chưa thiết lập hạn mức ngày.");
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationList);
        lvNotifications.setAdapter(adapter);

        // Nhận dữ liệu từ MainActivity
        String message = getIntent().getStringExtra("NOTI_MESSAGE");
        if (message != null && !message.isEmpty()) {
            cardNewMessage.setVisibility(View.VISIBLE);
            tvNewMessageContent.setText(message);
            notificationList.add(0, "🔴 " + message);
            adapter.notifyDataSetChanged();
        }
    }

    // ⭐ HỘP THOẠI CÀI ĐẶT HẠN MỨC DANH MỤC ⭐
    private void showCategoryLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt hạn mức cho Danh mục");

        // Layout chứa Spinner và EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // 1. Spinner chọn danh mục (Lấy từ arrays.xml)
        final Spinner spinner = new Spinner(this);
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinAdapter);
        layout.addView(spinner);

        // 2. Ô nhập tiền
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập giới hạn (VD: 2000000)");
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String category = spinner.getSelectedItem().toString();
            String amountStr = input.getText().toString();

            if (!amountStr.isEmpty()) {
                long limit = Long.parseLong(amountStr);
                // Lưu vào Database
                dbHelper.setCategoryLimit(category, limit);

                Toast.makeText(this, "Đã đặt giới hạn cho " + category + ": " + limit, Toast.LENGTH_SHORT).show();
                notificationList.add(0, "✅ Đã đặt hạn mức " + category + ": " + limit);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // --- CÁC HÀM CŨ (GIỮ NGUYÊN) ---
    private void showSetLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hạn mức chi tiêu TOÀN BỘ/ngày");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        long currentLimit = getLimit();
        if (currentLimit > 0) input.setText(String.valueOf(currentLimit));
        builder.setView(input);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                long limit = Long.parseLong(text);
                saveLimit(limit);
                Toast.makeText(this, "Đã cập nhật hạn mức ngày: " + limit, Toast.LENGTH_SHORT).show();
                notificationList.add(0, "✅ Đã thay đổi hạn mức ngày: " + limit);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void saveLimit(long limit) {
        SharedPreferences prefs = getSharedPreferences("ExpensePrefs", MODE_PRIVATE);
        prefs.edit().putLong("DAILY_LIMIT", limit).apply();
    }

    private long getLimit() {
        SharedPreferences prefs = getSharedPreferences("ExpensePrefs", MODE_PRIVATE);
        return prefs.getLong("DAILY_LIMIT", 0);
    }
}