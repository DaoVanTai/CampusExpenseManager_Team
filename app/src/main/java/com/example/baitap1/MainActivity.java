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

    private Button btnCreate;
    private Button btnOpenAppData;
    private Button btnRecurring; // MỚI: Nút Định kỳ
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

        appData = AppData.getInstance();

        // --- 1. Gọi hàm kiểm tra chi tiêu định kỳ ngay khi mở App ---
        appData.checkAndAddRecurringExpenses();

        // Ánh xạ View
        btnCreate = findViewById(R.id.btnCreate);
        btnOpenAppData = findViewById(R.id.btnOpenAppData);
        btnRecurring = findViewById(R.id.btnRecurring); // MỚI: Ánh xạ nút
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
            startActivity(intent);
        });

        btnOpenAppData.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppDataActivity.class);
            startActivity(intent);
        });

        // MỚI: Sự kiện mở màn hình RecurringExpenseActivity
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

        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, editTask.class);
            intent.putExtra("TASK_CONTENT", appData.taskList.get(position).toString());
            intent.putExtra("TASK_POSITION", position);
            startActivityForResult(intent, AppData.REQUEST_EDIT_TASK);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // MỚI: Kiểm tra lại chi tiêu định kỳ khi quay lại màn hình này
        // (đề phòng trường hợp vừa thêm quy tắc mới xong quay lại)
        appData.checkAndAddRecurringExpenses();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

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
                Expense newExpense = parseExpenseFromString(updatedContent);
                if (newExpense != null) {
                    appData.taskList.set(position, newExpense);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Lỗi cập nhật chi tiêu!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private long calculateTotalSpending() {
        long total = 0;
        for (Expense expense : appData.taskList) {
            total += (long) expense.getQuantity() * expense.getAmount();
        }
        return total;
    }

    private Expense parseExpenseFromString(String expenseString) {
        try {
            String description = expenseString.split(" - SL: ")[0].replace("Tên: ", "").trim();
            String quantityStr = expenseString.split(" - SL: ")[1].split(" - Giá:")[0].trim();
            String priceStr = expenseString.split(" - Giá: ")[1].split(" - DM: ")[0].trim();
            String category = expenseString.split(" - DM: ")[1].trim();

            int quantity = Integer.parseInt(quantityStr);
            long price = Long.parseLong(priceStr);

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