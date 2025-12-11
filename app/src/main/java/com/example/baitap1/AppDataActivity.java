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
import androidx.core.content.FileProvider; // ⭐ IMPORT QUAN TRỌNG CHO CHIA SẺ FILE

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class AppDataActivity extends AppCompatActivity {

    // Khai báo các nút
    private Button btnSave, btnDelete, btnDeleteAll, btnAdd;
    private Button btnExportExcel; // ⭐ KHAI BÁO NÚT MỚI

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

        // ⭐ Ánh xạ nút Xuất Excel (ID phải khớp với file XML vừa sửa)
        btnExportExcel = findViewById(R.id.btnExportExcel);

        // Khởi tạo Adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, appData.taskList);
        lvAppDataTasks.setAdapter(adapter);

        // --- CÁC SỰ KIỆN CŨ (GIỮ NGUYÊN) ---
        btnSave.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Vui lòng sử dụng nút 'Thêm mới' trên màn hình chính để nhập chi tiêu chi tiết (bao gồm Danh mục).", Toast.LENGTH_LONG).show();
        });

        btnDeleteAll.setOnClickListener(v -> {
            appData.taskList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã xóa toàn bộ (trên RAM)!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Vui lòng chọn mục để xóa", Toast.LENGTH_SHORT).show();
                return;
            }
            appData.taskList.removeAll(toRemove);
            lvAppDataTasks.clearChoices();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã xóa mục đã chọn", Toast.LENGTH_SHORT).show();
        });

        // --- ⭐ SỰ KIỆN MỚI: XUẤT EXCEL ⭐ ---
        btnExportExcel.setOnClickListener(v -> exportToCSV());
    }

    // --- HÀM XỬ LÝ LOGIC XUẤT FILE ---
    private void exportToCSV() {
        if (appData.taskList.isEmpty()) {
            Toast.makeText(this, "Danh sách trống, không có dữ liệu để xuất!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 1. Xây dựng nội dung file CSV
            StringBuilder sb = new StringBuilder();

            // Thêm ký tự BOM (\uFEFF) để Excel trên Windows hiển thị đúng Tiếng Việt
            sb.append("\uFEFF");

            // Tiêu đề các cột
            sb.append("ID,Mô tả chi tiêu,Số lượng,Đơn giá,Thành tiền,Danh mục\n");

            // Duyệt qua từng chi tiêu để ghi vào file
            for (Expense e : appData.taskList) {
                // Xử lý dấu phẩy trong tên (nếu có) để tránh lỗi cột
                String description = "\"" + e.getDescription().replace("\"", "\"\"") + "\"";
                long total = e.getQuantity() * e.getAmount();

                sb.append(e.getId()).append(",");
                sb.append(description).append(",");
                sb.append(e.getQuantity()).append(",");
                sb.append(e.getAmount()).append(",");
                sb.append(total).append(","); // Cột tự tính thêm
                sb.append(e.getCategory()).append("\n");
            }

            // 2. Tạo và Lưu file
            // Tên file theo thời gian thực để không bị trùng
            String fileName = "ChiTieu_" + System.currentTimeMillis() + ".csv";

            // Lưu vào thư mục riêng của ứng dụng (không cần xin quyền bộ nhớ ở Android mới)
            File file = new File(getExternalFilesDir(null), fileName);

            FileOutputStream out = new FileOutputStream(file);
            out.write(sb.toString().getBytes("UTF-8")); // Ghi theo chuẩn UTF-8
            out.close();

            // 3. Gọi hàm chia sẻ file
            shareFile(file);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(File file) {
        // Sử dụng FileProvider để tạo đường dẫn an toàn (content://...)
        // "com.example.baitap1.fileprovider" phải khớp với android:authorities trong AndroidManifest.xml
        Uri uri = FileProvider.getUriForFile(this, "com.example.baitap1.fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv"); // Hoặc "application/vnd.ms-excel"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Báo cáo Chi Tiêu");
        intent.putExtra(Intent.EXTRA_TEXT, "Gửi bạn file danh sách chi tiêu từ ứng dụng.");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        // Cấp quyền đọc tạm thời cho ứng dụng nhận (Zalo, Gmail...)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Chia sẻ file Excel qua:"));
    }
}