  package com.example.baitap1;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {
    private TextView tvTotalQuantity, tvTotalAmount;
    private Button btnBack;
    private ListView lvBreakdown; // Thêm ListView mới
    private DatabaseHelper dbHelper;
    private final double MONTHLY_BUDGET = 5000000; // Ngân sách giả định 5 triệu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        dbHelper = new DatabaseHelper(this);

        tvTotalQuantity = findViewById(R.id.tvTotalQuantity);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnBack = findViewById(R.id.btnBack);

        // Bạn cần thêm ListView vào file XML, nếu chưa có thì code dưới sẽ bị null, xem bước 3
        lvBreakdown = findViewById(R.id.lvCategoryBreakdown);

        calculateStatistics();

        btnBack.setOnClickListener(v -> finish());
    }

    private void calculateStatistics() {
        // 1. Lấy tháng hiện tại
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String currentMonth = sdf.format(new Date());

        // 2. Lấy tổng chi từ Database (Thay vì parse string cũ)
        double totalSpent = dbHelper.getMonthlyTotal(currentMonth);
        double remaining = MONTHLY_BUDGET - totalSpent;

        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

        // 3. Cập nhật giao diện
        // Tận dụng tvTotalAmount để hiện Tổng chi
        tvTotalAmount.setText(currencyFormatter.format(totalSpent));

        // Tận dụng tvTotalQuantity để hiện Số dư (Thay vì số lượng)
        tvTotalQuantity.setText(currencyFormatter.format(remaining));

        // Đổi màu nếu âm tiền
        if (remaining < 0) {
            tvTotalQuantity.setTextColor(Color.RED);
        } else {
            tvTotalQuantity.setTextColor(Color.parseColor("#4CAF50")); // Green
        }

        // 4. Hiển thị danh sách phân loại (Breakdown)
        if (lvBreakdown != null) {
            ArrayList<String> list = new ArrayList<>();
            Cursor cursor = dbHelper.getCategoryBreakdown(currentMonth);
            if (cursor.moveToFirst()) {
                do {
                    String cat = cursor.getString(0);
                    double amount = cursor.getDouble(1);
                    list.add(cat + ": " + currencyFormatter.format(amount));
                } while (cursor.moveToNext());
            }
            cursor.close();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
            lvBreakdown.setAdapter(adapter);
        }
    }
}