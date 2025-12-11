package com.example.baitap1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class AppDataActivity extends AppCompatActivity {

    private Button btnSave, btnDelete, btnDeleteAll, btnAdd;
    private Button btnExportExcel;

    private ListView lvAppDataTasks;
    private AppData appData;
    private ArrayAdapter<Expense> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appdata);

        appData = AppData.getInstance();

        // --- ÁNH XẠ VIEW ---
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnAdd = findViewById(R.id.btnAdd);
        lvAppDataTasks = findViewById(R.id.lvAppDataTasks);
        btnExportExcel = findViewById(R.id.btnExportExcel);

        // Khởi tạo Adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, appData.taskList);
        lvAppDataTasks.setAdapter(adapter);

        // --- CÁC SỰ KIỆN ---
        btnSave.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Vui lòng sử dụng nút 'Thêm mới' trên màn hình chính.", Toast.LENGTH_LONG).show();
        });

        btnDeleteAll.setOnClickListener(v -> {
            appData.taskList.clear();
            adapter.notifyDataSetChanged();
            // ⭐ THAY THẾ CHUỖI CỨNG
            Toast.makeText(this, getString(R.string.msg_delete_all), Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            SparseBooleanArray checked = lvAppDataTasks.getCheckedItemPositions();
            ArrayList<Expense> toRemove = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) {
                if (checked.valueAt(i)) {
                    toRemove.add(adapter.getItem(checked.keyAt(i)));
                }
            }
            if (toRemove.isEmpty()) {
                // ⭐ THAY THẾ CHUỖI CỨNG
                Toast.makeText(this, getString(R.string.msg_select_to_delete), Toast.LENGTH_SHORT).show();
                return;
            }
            appData.taskList.removeAll(toRemove);
            lvAppDataTasks.clearChoices();
            adapter.notifyDataSetChanged();
            // ⭐ THAY THẾ CHUỖI CỨNG
            Toast.makeText(this, getString(R.string.msg_delete_selected), Toast.LENGTH_SHORT).show();
        });

        // Sự kiện xuất Excel
        btnExportExcel.setOnClickListener(v -> exportToCSV());
    }

    // --- HÀM XUẤT FILE CSV ---
    private void exportToCSV() {
        if (appData.taskList.isEmpty()) {
            // ⭐ THAY THẾ CHUỖI CỨNG
            Toast.makeText(this, getString(R.string.error_list_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("\uFEFF"); // BOM cho tiếng Việt
            sb.append("ID,Mô tả chi tiêu,Số lượng,Đơn giá,Thành tiền,Danh mục,Ngày\n");

            for (Expense e : appData.taskList) {
                String description = "\"" + e.getDescription().replace("\"", "\"\"") + "\"";
                long total = e.getQuantity() * e.getAmount();
                // Lấy ngày (nếu có)
                String date = (e.getDate() != null) ? e.getDate() : "";

                sb.append(e.getId()).append(",");
                sb.append(description).append(",");
                sb.append(e.getQuantity()).append(",");
                sb.append(e.getAmount()).append(",");
                sb.append(total).append(",");
                sb.append(e.getCategory()).append(",");
                sb.append(date).append("\n");
            }

            String fileName = "ChiTieu_" + System.currentTimeMillis() + ".csv";
            File file = new File(getExternalFilesDir(null), fileName);

            FileOutputStream out = new FileOutputStream(file);
            out.write(sb.toString().getBytes("UTF-8"));
            out.close();

            shareFile(file);

        } catch (Exception e) {
            e.printStackTrace();
            // ⭐ THAY THẾ CHUỖI CỨNG (Dùng format %s cho lỗi)
            Toast.makeText(this, getString(R.string.msg_export_error, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, "com.example.baitap1.fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            // ⭐ THAY THẾ CHUỖI CỨNG
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // ⭐ THAY THẾ CHUỖI CỨNG
            startActivity(Intent.createChooser(intent, getString(R.string.chooser_title)));
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}