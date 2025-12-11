package com.example.baitap1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
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

    // ⭐ HẰNG SỐ MỚI: Dùng để nhận kết quả từ CreateNewTaskActivity ⭐
    private static final int ADD_TASK_REQUEST = 1000;

    private Button btnCreate;
    private Button btnOpenAppData;
    private Button btnRecurring;
    private Button btnStatistics;
    private Button btnViewChart;
    private ImageButton btnNotification;

    private ListView lvTasks;
    private ArrayAdapter<Expense> adapter;
    private AppData appData;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ⭐ ĐÃ SỬA: Lấy AppData bằng Context (BẮT BUỘC) ⭐
        appData = AppData.getInstance(getApplicationContext());

        // --- 1. Gọi hàm kiểm tra chi tiêu định kỳ ngay khi mở App ---
        appData.checkAndAddRecurringExpenses();

        // Ánh xạ View
        btnCreate = findViewById(R.id.btnCreate);
        btnOpenAppData = findViewById(R.id.btnOpenAppData);
        btnRecurring = findViewById(R.id.btnRecurring);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnViewChart = findViewById(R.id.btnViewChart);
        btnNotification = findViewById(R.id.btnNotification);
        lvTasks = findViewById(R.id.lvTasks);

        // Adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appData.taskList);
        lvTasks.setAdapter(adapter);

        // --- Sự kiện Click ---
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateNewTaskActivity.class);
            // ⭐ ĐÃ SỬA: Dùng startActivityForResult để chờ tín hiệu làm mới ⭐
            startActivityForResult(intent, ADD_TASK_REQUEST);
        });

        btnOpenAppData.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppDataActivity.class);
            startActivity(intent);
        });

        btnRecurring.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecurringExpenseActivity.class);
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

        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        btnNotification.setOnLongClickListener(v -> {
            showSetLimitDialog();
            return true;
        });

        // ⭐ BỔ SUNG LOGIC: TRUYỀN ID CHI TIÊU THAY VÌ CHUỖI CONTENT ⭐
        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, editTask.class);

            // Lấy đối tượng Expense từ danh sách
            Expense selectedExpense = appData.taskList.get(position);

            // ⭐ TRUYỀN ID CỦA CHI TIÊU VÀO INTENT ⭐
            intent.putExtra("EXPENSE_ID", selectedExpense.getId());

            // Giữ lại vị trí nếu editTask.java vẫn cần nó
            intent.putExtra("TASK_POSITION", position);

            startActivityForResult(intent, AppData.REQUEST_EDIT_TASK);
        });
        // -------------------------------------------------------------
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ⭐ BƯỚC QUAN TRỌNG: LUÔN TẢI DỮ LIỆU MỚI NHẤT TỪ DB VÀO taskList ⭐
        appData.loadTasksFromDatabase();

        appData.checkAndAddRecurringExpenses();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        long totalSpending = calculateTotalSpending();
        checkSpendingLimit(totalSpending);
    }

    // ⭐ PHƯƠNG THỨC ĐÃ SỬA: Xử lý kết quả trả về từ Activity khác ⭐
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 1. Xử lý logic cập nhật sau khi chỉnh sửa
        if (requestCode == AppData.REQUEST_EDIT_TASK && resultCode == Activity.RESULT_OK && data != null) {
            int position = data.getIntExtra("TASK_POSITION", -1);
            String updatedContent = data.getStringExtra("UPDATED_TASK_CONTENT");
            if (position != -1 && updatedContent != null) {
                Expense newExpense = parseExpenseFromString(updatedContent);
                if (newExpense != null) {
                    appData.taskList.set(position, newExpense);
                    // onResume sẽ gọi notifyDataSetChanged() sau đó
                } else {
                    Toast.makeText(this, "Lỗi cập nhật chi tiêu!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // 2. Xử lý logic sau khi THÊM MỚI (Từ CreateNewTaskActivity)
        if (requestCode == ADD_TASK_REQUEST && resultCode == Activity.RESULT_OK) {
            // Chỉ cần báo hiệu vì onResume() sẽ chạy ngay sau hàm này và gọi loadTasksFromDatabase()
            Toast.makeText(this, "Chi tiêu mới đã được thêm thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    // --- CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN ---

    private long calculateTotalSpending() {
        long total = 0;
        for (Expense expense : appData.taskList) {
            total += (long) expense.getQuantity() * expense.getAmount();
        }
        return total;
    }

    private Expense parseExpenseFromString(String expenseString) {
        // LƯU Ý: Nếu Expense Model có thay đổi (thêm ID, Path), logic này cần cập nhật.
        try {
            String description = expenseString.split(" - SL: ")[0].replace("Tên: ", "").trim();
            String quantityStr = expenseString.split(" - SL: ")[1].split(" - Giá:")[0].trim();
            String priceStr = expenseString.split(" - Giá: ")[1].split(" - DM: ")[0].trim();
            String category = expenseString.split(" - DM: ")[1].trim();

            int quantity = Integer.parseInt(quantityStr);
            long price = Long.parseLong(priceStr);

            // Trả về constructor 4 tham số (giữ nguyên logic cũ)
            return new Expense(description, quantity, price, category);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
                saveLimit(limit);
                Toast.makeText(MainActivity.this, "Đã đặt hạn mức: " + limit, Toast.LENGTH_SHORT).show();
                checkSpendingLimit(calculateTotalSpending());
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
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

    private void checkSpendingLimit(long currentDailyTotal) {
        long limit = getLimit();
        if (limit == 0) return;
        long warningThreshold = (long) (limit * 0.9);
        if (currentDailyTotal >= limit) {
            sendNotification("CẢNH BÁO CHI TIÊU!", "Đã vượt hạn mức ("+limit+")!");
        } else if (currentDailyTotal >= warningThreshold) {
            sendNotification("Nhắc nhở", "Sắp hết hạn mức (" + currentDailyTotal + "/" + limit + ")");
        }
    }

    private void sendNotification(String title, String message) {
        String channelId = "expense_limit_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Hạn mức chi tiêu", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
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