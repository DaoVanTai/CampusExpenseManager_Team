package com.example.baitap1;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Button btnCreate;
    private Button btnOpenAppData;
    private Button btnStatistics;
    private Button btnViewChart;
    private ImageButton btnNotification;

    private ListView lvTasks;
    private ArrayAdapter<String> adapter;
    private AppData appData;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appData = AppData.getInstance();

        // Ánh xạ View
        btnCreate = findViewById(R.id.btnCreate);
        btnOpenAppData = findViewById(R.id.btnOpenAppData);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnViewChart = findViewById(R.id.btnViewChart);
        btnNotification = findViewById(R.id.btnNotification);
        lvTasks = findViewById(R.id.lvTasks);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appData.taskList);
        lvTasks.setAdapter(adapter);

        // --- CÁC SỰ KIỆN CHUYỂN TRANG (Code cũ) ---
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateNewTaskActivity.class);
            startActivity(intent);
        });

        btnOpenAppData.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppDataActivity.class);
            startActivity(intent);
        });

        btnStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        btnViewChart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SpendingChartActivity.class);
            startActivity(intent);
        });

        // Click thường: Mở trang thông báo
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        // --- MỚI: SỰ KIỆN NHẤN GIỮ (LONG CLICK) ĐỂ CÀI ĐẶT HẠN MỨC ---
        btnNotification.setOnLongClickListener(v -> {
            showSetLimitDialog();
            return true;
        });

        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, editTask.class);
            intent.putExtra("TASK_CONTENT", appData.taskList.get(position));
            intent.putExtra("TASK_POSITION", position);
            startActivityForResult(intent, AppData.REQUEST_EDIT_TASK);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // --- MỚI: TỰ ĐỘNG KIỂM TRA HẠN MỨC KHI QUAY LẠI MÀN HÌNH NÀY ---
        // (Ví dụ: Vừa thêm mới chi tiêu xong, quay lại đây thì check ngay)
        long totalSpending = calculateTotalSpending();
        checkSpendingLimit(totalSpending);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppData.REQUEST_EDIT_TASK && resultCode == Activity.RESULT_OK && data != null) {
            int position = data.getIntExtra("TASK_POSITION", -1);
            String updatedContent = data.getStringExtra("UPDATED_TASK_CONTENT");
            if (position != -1 && updatedContent != null) {
                appData.taskList.set(position, updatedContent);
                adapter.notifyDataSetChanged();
            }
        }
    }


    // 1. Hiển thị hộp thoại nhập số tiền giới hạn
    private void showSetLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cài đặt hạn mức chi tiêu/ngày");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập số tiền (VD: 500000)");
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                long limit = Long.parseLong(text);
                // Validate: Giới hạn từ 100k đến 1 triệu
                if (limit < 100000 || limit > 1000000) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập từ 100k - 1 triệu!", Toast.LENGTH_SHORT).show();
                } else {
                    saveLimit(limit);
                    Toast.makeText(MainActivity.this, "Đã đặt hạn mức: " + limit, Toast.LENGTH_SHORT).show();
                    // Check lại ngay lập tức sau khi lưu
                    checkSpendingLimit(calculateTotalSpending());
                }
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // 2. Lưu hạn mức vào bộ nhớ máy (SharedPreferences)
    private void saveLimit(long limit) {
        SharedPreferences prefs = getSharedPreferences("ExpensePrefs", MODE_PRIVATE);
        prefs.edit().putLong("DAILY_LIMIT", limit).apply();
    }

    // 3. Lấy hạn mức ra
    private long getLimit() {
        SharedPreferences prefs = getSharedPreferences("ExpensePrefs", MODE_PRIVATE);
        return prefs.getLong("DAILY_LIMIT", 0);
    }

    // 4. Tính tổng tiền đang có trong danh sách (Giả lập)
    // Vì danh sách của bạn là String, mình sẽ tìm các con số trong chuỗi để cộng lại
    private long calculateTotalSpending() {
        long total = 0;
        // Duyệt qua từng dòng trong ListView
        for (String task : appData.taskList) {
            // Dùng Regex để tìm số trong chuỗi (Ví dụ: "Ăn trưa 30000" -> lấy được 30000)
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(task);
            while (m.find()) {
                try {
                    // Lấy con số tìm được cộng vào tổng
                    total += Long.parseLong(m.group());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return total;
    }

    // 5. Kiểm tra và bắn thông báo
    private void checkSpendingLimit(long currentDailyTotal) {
        long limit = getLimit();
        if (limit == 0) return; // Chưa cài đặt thì thôi

        long warningThreshold = (long) (limit * 0.9); // 90% là cảnh báo

        if (currentDailyTotal >= limit) {
            // Vượt quá -> Cảnh báo ĐỎ
            sendNotification("CẢNH BÁO CHI TIÊU!", "Bạn đã đạt đến hạn mức ("+limit+"). Vui lòng ngừng chi tiêu!");
        } else if (currentDailyTotal >= warningThreshold) {
            // Sắp đến -> Cảnh báo VÀNG
            sendNotification("Nhắc nhở", "Bạn sắp hết hạn mức hôm nay (" + currentDailyTotal + "/" + limit + ")");
        }
    }

    // 6. Hàm tạo thông báo hệ thống
    private void sendNotification(String title, String message) {
        String channelId = "expense_limit_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Hạn mức chi tiêu", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // Dùng icon mặc định hoặc thay bằng R.drawable.ic_notification
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("NOTI_MESSAGE", message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);
        notificationManager.notify(1, builder.build());
    }
}