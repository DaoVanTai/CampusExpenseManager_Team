package com.example.baitap1;

import android.app.AlertDialog;
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
    private Button btnSetCategoryLimit;
    private ListView lvNotifications;
    private CardView cardNewMessage;
    private TextView tvNewMessageContent;

    private ArrayList<String> notificationList;
    private ArrayAdapter<String> adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBackNoti);
        btnSetLimit = findViewById(R.id.btnSetLimitNoti);
        btnSetCategoryLimit = findViewById(R.id.btnSetCategoryLimit);
        lvNotifications = findViewById(R.id.lvNotifications);
        cardNewMessage = findViewById(R.id.cardNewMessage);
        tvNewMessageContent = findViewById(R.id.tvNewMessageContent);

        btnBack.setOnClickListener(v -> finish());

        // Sự kiện các nút cài đặt
        btnSetLimit.setOnClickListener(v -> showSetLimitDialog());
        btnSetCategoryLimit.setOnClickListener(v -> showCategoryLimitDialog());

        // Setup ListView & Hiển thị trạng thái hiện tại
        notificationList = new ArrayList<>();
        long currentLimit = getLimit();

        if (currentLimit > 0) {
            // ⭐ THAY THẾ CHUỖI CỨNG: Dùng format string (%d)
            notificationList.add(getString(R.string.text_limit_current, currentLimit));
        } else {
            // ⭐ THAY THẾ CHUỖI CỨNG
            notificationList.add(getString(R.string.text_limit_not_set));
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationList);
        lvNotifications.setAdapter(adapter);

        // Nhận thông báo từ MainActivity (Nếu có)
        String message = getIntent().getStringExtra("NOTI_MESSAGE");
        if (message != null && !message.isEmpty()) {
            cardNewMessage.setVisibility(View.VISIBLE);
            tvNewMessageContent.setText(message);
            // Thêm vào đầu danh sách lịch sử
            notificationList.add(0, "🔴 " + message);
            adapter.notifyDataSetChanged();
        }
    }

    // --- HỘP THOẠI CÀI ĐẶT HẠN MỨC DANH MỤC ---
    private void showCategoryLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt hạn mức cho Danh mục"); // Có thể thêm vào strings.xml nếu muốn

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        final Spinner spinner = new Spinner(this);
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinAdapter);
        layout.addView(spinner);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        // ⭐ THAY THẾ CHUỖI CỨNG (Tái sử dụng hint cũ)
        input.setHint(getString(R.string.dialog_limit_hint));
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton(getString(R.string.action_continue), (dialog, which) -> {
            String category = spinner.getSelectedItem().toString();
            String amountStr = input.getText().toString();

            if (!amountStr.isEmpty()) {
                long limit = Long.parseLong(amountStr);
                dbHelper.setCategoryLimit(category, limit);

                // Hiển thị thông báo Toast
                String msg = "Đã đặt giới hạn cho " + category + ": " + limit;
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                // Cập nhật vào list
                notificationList.add(0, "✅ " + msg);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.action_cancel), null);
        builder.show();
    }

    // --- HỘP THOẠI CÀI ĐẶT HẠN MỨC NGÀY ---
    private void showSetLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // ⭐ THAY THẾ CHUỖI CỨNG
        builder.setTitle(getString(R.string.dialog_limit_title));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        long currentLimit = getLimit();
        if (currentLimit > 0) input.setText(String.valueOf(currentLimit));

        // ⭐ THAY THẾ CHUỖI CỨNG
        input.setHint(getString(R.string.dialog_limit_hint));

        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                long limit = Long.parseLong(text);
                saveLimit(limit);

                // ⭐ THAY THẾ CHUỖI CỨNG (Dùng format %d từ strings.xml)
                String msg = getString(R.string.msg_limit_updated, limit);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                notificationList.add(0, "✅ " + msg);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.action_cancel), null);
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