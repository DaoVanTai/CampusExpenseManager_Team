package com.example.baitap1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Khai báo đầy đủ 5 nút bấm
    private Button btnCreate;
    private Button btnOpenAppData;
    private Button btnStatistics;      // Nút Thống kê (Mới)
    private Button btnRecurring;       // Nút Định kỳ (Mới)
    private Button btnVoiceExpense;    // Nút Giọng nói

    private ListView lvTasks;
    private ArrayAdapter<String> adapter;
    private AppData appData;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Liên kết với giao diện XML

        // 1. Khởi tạo Database và chạy kiểm tra định kỳ ngay khi mở app
        dbHelper = new DatabaseHelper(this);
        dbHelper.checkAndAddRecurring(); // Hàm này tự động thêm chi phí nếu đến ngày

        appData = AppData.getInstance();

        // 2. Ánh xạ (Tìm nút bấm trong XML bằng ID chính xác)
        btnCreate = findViewById(R.id.btnCreate);
        btnOpenAppData = findViewById(R.id.btnOpenAppData);
        btnStatistics = findViewById(R.id.btnStatistics);       // ID này phải có trong XML
        btnRecurring = findViewById(R.id.btnRecurring);         // ID này phải có trong XML
        btnVoiceExpense = findViewById(R.id.button_voice_expense); // ID này phải có trong XML
        lvTasks = findViewById(R.id.lvTasks);

        // 3. Thiết lập List View (Code cũ)
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appData.taskList);
        lvTasks.setAdapter(adapter);

        // 4. Cài đặt sự kiện bấm nút (Liên kết chức năng)

        // Nút Thêm Task (Code cũ)
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateNewTaskActivity.class);
            startActivity(intent);
        });

        // Nút Quản lý AppData (Code cũ)
        btnOpenAppData.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppDataActivity.class);
            startActivity(intent);
        });

        // ⭐ Nút Thống Kê (MỚI) - Mở màn hình StatisticsActivity
        btnStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        // ⭐ Nút Cài Đặt Định Kỳ (MỚI) - Mở màn hình AddRecurringActivity
        btnRecurring.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddRecurringActivity.class);
            startActivity(intent);
        });

        // Sự kiện click vào item trong list (Sửa Task - Code cũ)
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
        // Gọi lại kiểm tra định kỳ khi quay lại màn hình chính
        if (dbHelper != null) {
            dbHelper.checkAndAddRecurring();
        }
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
}