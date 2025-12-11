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
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_TASK_REQUEST = 1000;

    private Button btnCreate, btnOpenAppData, btnRecurring, btnStatistics, btnViewChart;
    private ImageButton btnNotification;
    private SearchView searchView;
    private ListView lvTasks;

    // Sử dụng Adapter tùy chỉnh (đã tối ưu hiển thị ảnh)
    private ExpenseAdapter adapter;
    private AppData appData;

    // Bộ công cụ xử lý đa luồng (Tránh giật lag UI)
    private ExecutorService executorService;
    private Handler mainHandler;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo luồng
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        appData = AppData.getInstance(getApplicationContext());

        // Kiểm tra chi tiêu định kỳ ngay khi mở app
        appData.checkAndAddRecurringExpenses();

        // --- ÁNH XẠ VIEW ---
        btnCreate = findViewById(R.id.btnCreate);
        btnOpenAppData = findViewById(R.id.btnOpenAppData);
        btnRecurring = findViewById(R.id.btnRecurring);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnViewChart = findViewById(R.id.btnViewChart);
        btnNotification = findViewById(R.id.btnNotification);
        lvTasks = findViewById(R.id.lvTasks);
        searchView = findViewById(R.id.searchView);

        // Khởi tạo Adapter
        adapter = new ExpenseAdapter(this, R.layout.item_expense, appData.taskList);
        lvTasks.setAdapter(adapter);

        // --- SỰ KIỆN CLICK ---
        btnCreate.setOnClickListener(v -> startActivityForResult(new Intent(this, CreateNewTaskActivity.class), ADD_TASK_REQUEST));
        btnOpenAppData.setOnClickListener(v -> startActivity(new Intent(this, AppDataActivity.class)));
        btnRecurring.setOnClickListener(v -> startActivity(new Intent(this, RecurringExpenseActivity.class)));
        btnStatistics.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        btnViewChart.setOnClickListener(v -> startActivity(new Intent(this, SpendingChartActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

        btnNotification.setOnLongClickListener(v -> {
            showSetLimitDialog();
            return true;
        });

        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, editTask.class);
            Expense selectedExpense = appData.taskList.get(position);
            intent.putExtra("EXPENSE_ID", selectedExpense.getId());
            intent.putExtra("TASK_POSITION", position);
            startActivityForResult(intent, AppData.REQUEST_EDIT_TASK);
        });

        // Sự kiện tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load dữ liệu dưới nền để không làm đơ màn hình
        loadDataInBackground();
    }

    private void loadDataInBackground() {
        executorService.execute(() -> {
            // 1. Luồng phụ: Tải dữ liệu nặng
            appData.loadTasksFromDatabase();
            appData.checkAndAddRecurringExpenses();
            long total = calculateTotalSpending();

            // 2. Luồng chính: Cập nhật giao diện
            mainHandler.post(() -> {
                if (adapter != null) adapter.notifyDataSetChanged();
                checkSpendingLimit(total);
            });
        });
    }

    private void performSearch(String keyword) {
        // Ẩn bàn phím
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        executorService.execute(() -> {
            // Tìm kiếm dưới nền
            if (keyword == null || keyword.trim().isEmpty()) {
                appData.loadTasksFromDatabase();
            } else {
                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                List<Expense> results = dbHelper.searchExpenses(keyword.trim());
                appData.taskList.clear();
                appData.taskList.addAll(results);
            }

            mainHandler.post(() -> {
                if (adapter != null) adapter.notifyDataSetChanged();
            });
        });
    }

    private long calculateTotalSpending() {
        long total = 0;
        for (Expense expense : appData.taskList) {
            total += (long) expense.getQuantity() * expense.getAmount();
        }
        return total;
    }

    private void showSetLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // ⭐ Thay thế chuỗi cứng bằng Resource String
        builder.setTitle(getString(R.string.dialog_limit_title));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        // ⭐ Thay thế chuỗi cứng
        input.setHint(getString(R.string.dialog_limit_hint));

        long current = getLimit();
        if (current > 0) input.setText(String.valueOf(current));

        builder.setView(input);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                long limit = Long.parseLong(text);
                saveLimit(limit);
                // ⭐ Thay thế chuỗi cứng (Dùng format string %d)
                String msg = getString(R.string.msg_limit_updated, limit); // Cần đảm bảo strings.xml có dòng này hoặc dùng chuỗi ghép
                // Nếu chưa có %d trong strings.xml cho msg_limit_updated, bạn có thể dùng tạm: "Đã đặt hạn mức: " + limit
                Toast.makeText(MainActivity.this, "Đã đặt hạn mức: " + limit, Toast.LENGTH_SHORT).show();

                checkSpendingLimit(calculateTotalSpending());
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

    private void checkSpendingLimit(long currentDailyTotal) {
        long limit = getLimit();
        if (limit == 0) return;
        long warningThreshold = (long) (limit * 0.9);

        if (currentDailyTotal >= limit) {
            // ⭐ Thay thế chuỗi cứng: Cảnh báo vượt mức
            String msg = getString(R.string.noti_limit_msg_exceed, limit);
            sendNotification(getString(R.string.noti_limit_title), msg);
        } else if (currentDailyTotal >= warningThreshold) {
            // ⭐ Thay thế chuỗi cứng: Cảnh báo sắp hết
            String msg = getString(R.string.noti_limit_msg_warning, currentDailyTotal, limit);
            sendNotification("Nhắc nhở", msg);
        }
    }

    private void sendNotification(String title, String message) {
        String channelId = "expense_limit_channel";
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Hạn mức", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
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
        nm.notify(1, builder.build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TASK_REQUEST && resultCode == Activity.RESULT_OK) {
            // ⭐ Thay thế chuỗi cứng
            Toast.makeText(this, getString(R.string.add_task_success), Toast.LENGTH_SHORT).show();
        }
    }
}