package com.example.baitap1;

import android.os.Bundle;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
public class AppDataActivity extends AppCompatActivity {
    private Button btnSave, btnDelete, btnDeleteAll, btnAdd;
    private ListView lvAppDataTasks;
    private AppData appData;
    private ArrayAdapter<String> adapter;
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

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, appData.taskList);
        lvAppDataTasks.setAdapter(adapter);

        btnSave.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showAddTaskDialog());

        btnDeleteAll.setOnClickListener(v -> {
            appData.taskList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã xóa toàn bộ!", Toast.LENGTH_SHORT).show();
        });
        btnDelete.setOnClickListener(v -> {
            SparseBooleanArray checked = lvAppDataTasks.getCheckedItemPositions();
            ArrayList<String> toRemove = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) {
                if (checked.valueAt(i)) {
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

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm công việc mới");
        final EditText input = new EditText(this);
        input.setHint("Nhập tên công việc...");
        builder.setView(input);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String task = input.getText().toString().trim();
            if (!task.isEmpty()) {
                appData.taskList.add(task);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}