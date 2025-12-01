package com.example.baitap1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
 feature/giong-noi
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// ⭐ ĐẢM BẢO DÒNG IMPORT NÀY KHÔNG BỊ LỖI ⭐


public class MainActivity extends AppCompatActivity { // Đây là định nghĩa lớp chính

// import android.widget.Toast; // Không dùng có thể bỏ qua
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  main

    private Button btnCreate;
    private Button btnOpenAppData;
    private Button btnStatistics;
 feature/giong-noi

    // Khai báo Button mới cho chức năng Voice Expense
    private Button btnVoiceExpense;

    private Button btnViewChart; // <--- 1. KHAI BÁO BIẾN MỚI
 main

    private ListView lvTasks;
    private ArrayAdapter<String> adapter;
    private AppData appData;

 feature/giong-noi

    @SuppressLint("MissingInflatedId")
 main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appData = AppData.getInstance();

 feature/giong-noi
        // Ánh xạ các Button cũ
        btnCreate = findViewById(R.id.btnCreate);
        btnOpenAppData = findViewById(R.id.btnOpenAppData);
        btnStatistics = findViewById(R.id.btnStatistics);

        // Ánh xạ Button mới (Đảm bảo ID này tồn tại trong activity_main.xml)
        btnVoiceExpense = findViewById(R.id.button_voice_expense);


        // Ánh xạ View
        btnCreate = findViewById(R.id.btnCreate);
        btnOpenAppData = findViewById(R.id.btnOpenAppData);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnViewChart = findViewById(R.id.btnViewChart); // <--- 2. ÁNH XẠ ID TỪ XML
 main
        lvTasks = findViewById(R.id.lvTasks);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appData.taskList);
        lvTasks.setAdapter(adapter);

 feature/giong-noi
        // Thiết lập Listener cho các nút cũ (Giữ nguyên)

        // --- Các sự kiện cũ ---
 main
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

 feature/giong-noi
        // ⭐ LISTENER MỚI ĐÃ SỬA LỖI INTENT TARGET ⭐
        btnVoiceExpense.setOnClickListener(v -> {
            // Chuyển sang Activity xử lý ghi âm và lưu chi tiêu
            // Mục tiêu phải là Activity (AddExpenseByVoiceActivity) chứ không phải Helper Class
            Intent intent = new Intent(MainActivity.this, AddExpenseByVoiceActivity.class);
            startActivity(intent);
        });

        // Thiết lập Listener cho ListView cũ (Giữ nguyên)

        // --- 3. SỰ KIỆN MỚI: CHUYỂN HƯỚNG ĐẾN BIỂU ĐỒ ---
        btnViewChart.setOnClickListener(v -> {
            // Chuyển sang màn hình SpendingChartActivity chúng ta vừa tạo
            Intent intent = new Intent(MainActivity.this, SpendingChartActivity.class);
            startActivity(intent);
        });
        // ------------------------------------------------

 main
        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, editTask.class);
            intent.putExtra("TASK_CONTENT", appData.taskList.get(position));
            intent.putExtra("TASK_POSITION", position);
            startActivityForResult(intent, AppData.REQUEST_EDIT_TASK);
        });
    }

 feature/giong-noi
    // onResume() cũ (Giữ nguyên)

 main
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

 feature/giong-noi
    // onActivityResult() cũ (Giữ nguyên)

 main
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