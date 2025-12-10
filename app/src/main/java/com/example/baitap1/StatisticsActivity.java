package com.example.baitap1;

import android.content.SharedPreferences; // Thêm import
import android.graphics.Color; // Thêm import màu sắc
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {
    // MỚI: Thêm TextView hiển thị ngân sách còn lại (Bạn cần thêm vào XML nếu muốn hiện lên màn hình)
    // Hiện tại mình sẽ gộp hiển thị vào một TextView có sẵn hoặc Toast để bạn dễ hình dung logic.
    private TextView tvTotalQuantity, tvTotalAmount, tvRemaining;
    private ListView lvCategoryStats;
    private Button btnBack;
    private AppData appData;

    private ArrayAdapter<String> categoryStatsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tvTotalQuantity = findViewById(R.id.tvTotalQuantity);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        // GIẢ SỬ: Bạn thêm 1 TextView có ID tvRemaining trong XML.
        // Nếu chưa có, bạn có thể bỏ qua dòng này, nhưng logic tính toán bên dưới vẫn cần thiết.
        // tvRemaining = findViewById(R.id.tvRemaining);

        lvCategoryStats = findViewById(R.id.lvCategoryStats);
        btnBack = findViewById(R.id.btnBack);

        appData = AppData.getInstance();

        categoryStatsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvCategoryStats.setAdapter(categoryStatsAdapter);

        calculateStatistics();

        btnBack.setOnClickListener(v -> finish());
    }

    private void calculateStatistics() {
        int totalQuantity = 0;
        long totalAmount = 0;

        Map<String, Long> categoryTotals = new HashMap<>();
        ArrayList<Expense> taskList = appData.taskList;

        for (Expense expense : taskList) {
            try {
                int quantity = expense.getQuantity();
                long price = expense.getAmount();
                long itemTotal = (long) quantity * price;

                totalQuantity += quantity;
                totalAmount += itemTotal;

                String category = expense.getCategory();
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0L) + itemTotal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);

        tvTotalQuantity.setText(String.valueOf(totalQuantity));
        tvTotalAmount.setText(currencyFormatter.format(totalAmount));

        // --- MỚI: TÍNH TOÁN NGÂN SÁCH CÒN LẠI (REMAINING BUDGET) ---
        // 1. Lấy hạn mức từ SharedPreferences (Đã cài ở MainActivity)
        SharedPreferences prefs = getSharedPreferences("ExpensePrefs", MODE_PRIVATE);
        long dailyLimit = prefs.getLong("DAILY_LIMIT", 0);

        // 2. Tính còn lại & Hiển thị vào danh sách thống kê luôn
        List<String> statsList = new ArrayList<>();

        if (dailyLimit > 0) {
            long remaining = dailyLimit - totalAmount;
            String status = remaining >= 0 ? "Còn dư" : "Vượt mức";
            // Thêm dòng đầu tiên vào ListView là thông tin Ngân sách
            statsList.add("--- TÌNH TRẠNG NGÂN SÁCH ---");
            statsList.add("Hạn mức: " + currencyFormatter.format(dailyLimit));
            statsList.add("Đã chi: " + currencyFormatter.format(totalAmount));
            statsList.add("Còn lại: " + currencyFormatter.format(remaining) + " (" + status + ")");
            statsList.add("--------------------------------");
        } else {
            statsList.add("⚠️ Chưa cài đặt hạn mức chi tiêu");
            statsList.add("--------------------------------");
        }
        // -----------------------------------------------------------

        for (Map.Entry<String, Long> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            long amount = entry.getValue();
            String formattedAmount = currencyFormatter.format(amount);
            double percentage = (totalAmount > 0) ? ((double) amount / totalAmount) * 100 : 0;

            statsList.add(String.format("%s: %s (%.1f%%)", category, formattedAmount, percentage));
        }

        categoryStatsAdapter.clear();
        categoryStatsAdapter.addAll(statsList);
        categoryStatsAdapter.notifyDataSetChanged();
    }
}