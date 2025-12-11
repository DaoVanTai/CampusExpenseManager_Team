package com.example.baitap1;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

// Đã chỉnh sửa file AppDataActivity.java
public class AppDataActivity extends AppCompatActivity {
    private Button btnSave, btnDelete, btnDeleteAll, btnAdd;
    private ListView lvAppDataTasks;
    private AppData appData;
    // THAY ĐỔI 1: Đổi kiểu dữ liệu của ArrayAdapter thành Expense
    private ArrayAdapter<Expense> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appdata);
        appData = AppData.getInstance();
        btnSave = findViewById(R.id .btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnAdd = findViewById(R.id.btnAdd);
        lvAppDataTasks = findViewById(R.id.lvAppDataTasks);

        // THAY ĐỔI 2: Khởi tạo ArrayAdapter với kiểu Expense
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, appData.taskList);
        lvAppDataTasks.setAdapter(adapter);

        btnSave.setOnClickListener(v -> finish());

        // THAY ĐỔI 3: Thay thế logic showAddTaskDialog bằng thông báo (vì cần nhiều trường)
        btnAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Vui lòng sử dụng nút 'Thêm mới' trên màn hình chính để nhập chi tiêu chi tiết (bao gồm Danh mục).", Toast.LENGTH_LONG).show();
        });

        btnDeleteAll.setOnClickListener(v -> {
            appData.taskList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã xóa toàn bộ!", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            SparseBooleanArray checked = lvAppDataTasks.getCheckedItemPositions();
            // THAY ĐỔI 4: Đổi kiểu dữ liệu của toRemove thành Expense
            ArrayList<Expense> toRemove = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) {
                if (checked.valueAt(i)) {
                    // THAY ĐỔI 5: Lấy item là đối tượng Expense
                    toRemove.add(adapter.getItem(checked.keyAt(i)));
                }
            }
            if (toRemove.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn mục để xóa", Toast.LENGTH_SHORT).show();
                return;
            }
            appData.taskList.removeAll(toRemove);
            lvAppDataTasks.clearChoices();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã xóa mục đã chọn", Toast.LENGTH_SHORT).show();
        });
    }


}