package com.example.baitap1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageButton btnSetLimit; // Khai báo nút mới
    private ListView lvNotifications;
    private CardView cardNewMessage;
    private TextView tvNewMessageContent;

    private ArrayList<String> notificationList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // 1. Ánh xạ View
        btnBack = findViewById(R.id.btnBackNoti);
        btnSetLimit = findViewById(R.id.btnSetLimitNoti); // Ánh xạ nút cài đặt
        lvNotifications = findViewById(R.id.lvNotifications);
        cardNewMessage = findViewById(R.id.cardNewMessage);
        tvNewMessageContent = findViewById(R.id.tvNewMessageContent);

        // 2. Xử lý nút Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 3. Xử lý nút Đặt hạn mức (Hiện Dialog)
        btnSetLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetLimitDialog();
            }
        });

        // 4. Setup ListView
        notificationList = new ArrayList<>();
        // Lấy hạn mức hiện tại để hiển thị cho người dùng biết
        long currentLimit = getLimit();
        if (currentLimit > 0) {
            notificationList.add("Hạn mức hiện tại: " + currentLimit + " VNĐ");
        } else {
            notificationList.add("Bạn chưa thiết lập hạn mức chi tiêu.");
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationList);
        lvNotifications.setAdapter(adapter);

        // 5. Nhận dữ liệu từ MainActivity
        String message = getIntent().getStringExtra("NOTI_MESSAGE");
        if (message != null && !message.isEmpty()) {
            cardNewMessage.setVisibility(View.VISIBLE);
            tvNewMessageContent.setText(message);
            notificationList.add(0, "🔴 " + message);
            adapter.notifyDataSetChanged();
        }
    }

    // --- CÁC HÀM XỬ LÝ HẠN MỨC (Copy logic từ MainActivity sang) ---

    private void showSetLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thay đổi hạn mức chi tiêu");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Hiển thị hạn mức cũ lên ô nhập để tiện sửa
        long currentLimit = getLimit();
        if (currentLimit > 0) {
            input.setText(String.valueOf(currentLimit));
        } else {
            input.setHint("Nhập số tiền (VD: 500000)");
        }

        builder.setView(input);

        builder.setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if (!text.isEmpty()) {
                    long limit = Long.parseLong(text);
                    if (limit < 100000 || limit > 1000000) {
                        Toast.makeText(NotificationActivity.this, "Vui lòng nhập từ 100k - 1 triệu!", Toast.LENGTH_SHORT).show();
                    } else {
                        saveLimit(limit);
                        Toast.makeText(NotificationActivity.this, "Đã cập nhật hạn mức: " + limit, Toast.LENGTH_SHORT).show();

                        // Cập nhật lại dòng thông báo trong list để người dùng thấy ngay
                        notificationList.add(0, "✅ Đã thay đổi hạn mức mới: " + limit);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Lưu vào cùng một file "ExpensePrefs" để MainActivity cũng đọc được
    private void saveLimit(long limit) {
        SharedPreferences prefs = getSharedPreferences("ExpensePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("DAILY_LIMIT", limit);
        editor.apply();
    }

    private long getLimit() {
        SharedPreferences prefs = getSharedPreferences("ExpensePrefs", MODE_PRIVATE);
        return prefs.getLong("DAILY_LIMIT", 0);
    }
}