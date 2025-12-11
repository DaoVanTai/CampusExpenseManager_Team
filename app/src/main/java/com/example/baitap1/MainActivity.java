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
import android.view.View; // ⭐ IMPORT ĐỂ LẤY VIEW HIỆN TẠI
import android.view.inputmethod.InputMethodManager; // ⭐ IMPORT ĐỂ ẨN BÀN PHÍM
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.widget.SearchView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_TASK_REQUEST = 1000;

    private Button btnCreate;
    private Button btnOpenAppData;
    private Button btnRecurring;
    private Button btnStatistics;
    private Button btnViewChart;
    private ImageButton btnNotification;

    private SearchView searchView;

    private ListView lvTasks;
    private ArrayAdapter<Expense> adapter;
    private AppData appData;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appData = AppData.getInstance(getApplicationContext());

        // Gọi hàm kiểm tra chi tiêu định kỳ ngay khi mở App
        appData.checkAndAddRecurringExpenses();

        // --- ÁNH XẠ VIEW ---
        btnCreate = findViewById(R.id.btnCreate);
        btnOpenAppData = findViewById(R.id.btnOpenAppData);
        btnRecurring = findViewById(R.id.btnRecurring);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnViewChart = findViewById(R.id.btnViewChart);
        btnNotification = findViewById(R.id.btnNotification);
        lvTasks = findViewById(R.id.lvTasks);

        // Ánh xạ thanh tìm kiếm
        searchView = findViewById(R.id.searchView);

        // Adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appData.taskList);
        lvTasks.setAdapter(adapter);

        // --- SỰ KIỆN CLICK CÁC NÚT CHỨC NĂNG ---
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateNewTaskActivity.class);
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

        // Xử lý click vào item để sửa
        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, editTask.class);
            Expense selectedExpense = appData.taskList.get(position);

            // Truyền ID sang màn hình sửa
            intent.putExtra("EXPENSE_ID", selectedExpense.getId());
            intent.putExtra("TASK_POSITION", position);

            startActivityForResult(intent, AppData.REQUEST_EDIT_TASK);
        });

        // --- SỰ KIỆN TÌM KIẾM ---
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query); // Tìm khi bấm Enter
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText); // Tìm ngay khi gõ (Real-time)
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load lại dữ liệu mỗi khi quay lại màn hình chính
        appData.loadTasksFromDatabase();
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

        // Xử lý cập nhật sau khi sửa
        if (requestCode == AppData.REQUEST_EDIT_TASK && resultCode == Activity.RESULT_OK && data != null) {
            // onResume sẽ tự động load lại dữ liệu mới từ DB
        }

        // Xử lý sau khi thêm mới
        if (requestCode == ADD_TASK_REQUEST && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Chi tiêu mới đã được thêm thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    // ⭐ HÀM THỰC HIỆN TÌM KIẾM (ĐÃ THÊM LOGIC ẨN BÀN PHÍM) ⭐
    private void performSearch(String keyword) {
        // 1. Ẩn bàn phím để người dùng nhìn thấy kết quả rõ hơn
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // 2. Logic tìm kiếm
        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu từ khóa rỗng, tải lại toàn bộ danh sách
            appData.loadTasksFromDatabase();
        } else {
            // Nếu có từ khóa, gọi DatabaseHelper để tìm
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            List<Expense> results = dbHelper.searchExpenses(keyword.trim());

            // Cập nhật danh sách hiển thị
            appData.taskList.clear();
            appData.taskList.addAll(results);
        }

        // 3. Cập nhật giao diện ListView
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // --- CÁC HÀM HỖ TRỢ KHÁC (Giữ nguyên) ---

    private long calculateTotalSpending() {
        long total = 0;
        for (Expense expense : appData.taskList) {
            total += (long) expense.getQuantity() * expense.getAmount();
        }
        return total;
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