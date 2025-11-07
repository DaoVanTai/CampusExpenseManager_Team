package com.example.baitap1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {
    private TextView tvTotalQuantity, tvTotalAmount;
    private Button btnBack;
    private AppData appData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        tvTotalQuantity = findViewById(R.id.tvTotalQuantity);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnBack = findViewById(R.id.btnBack);
        appData = AppData.getInstance();
        calculateStatistics();
        btnBack.setOnClickListener(v -> finish());
    }
    private void calculateStatistics() {
        int totalQuantity = 0;
        long totalAmount = 0;

        ArrayList<String> taskList = appData.taskList;

        for (String taskString : taskList) {
            try {
                String quantityStr = taskString.split("- SL: ")[1].split(" - Giá:")[0].trim();
                String priceStr = taskString.split("- Giá: ")[1].trim();
                int quantity = Integer.parseInt(quantityStr);
                long price = Long.parseLong(priceStr);
                totalQuantity += quantity;
                totalAmount += (quantity * price);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        tvTotalQuantity.setText(String.valueOf(totalQuantity));
        tvTotalAmount.setText(currencyFormatter.format(totalAmount));
    }
}