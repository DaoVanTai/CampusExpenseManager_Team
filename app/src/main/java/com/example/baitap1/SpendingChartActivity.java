package com.example.baitap1;

import android.graphics.Color; // Thêm import màu
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView; // Thêm import
import android.widget.ArrayAdapter; // Thêm import
import android.widget.Spinner; // Thêm import

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

// Import đầy đủ thư viện Chart
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class SpendingChartActivity extends AppCompatActivity {

    // 1. KHAI BÁO BIẾN Ở ĐÂY (QUAN TRỌNG)
    private LineChart lineChart;
    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spending_chart);

        // 2. Ánh xạ
        lineChart = findViewById(R.id.lineChart);
        spinnerFilter = findViewById(R.id.spinnerFilter);

        // Cấu hình Spinner
        String[] filters = {"Ngày", "Tuần", "Tháng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        // Bắt sự kiện chọn Spinner
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        showDailyChart();
                        break;
                    case 1:
                        showWeeklyChart();
                        break;
                    case 2:
                        showMonthlyChart();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void showDailyChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        // Giả lập dữ liệu ngày
        float[] spending = {50, 70, 20, 100, 40, 60, 30};
        for (int i = 0; i < spending.length; i++) {
            entries.add(new Entry(i, spending[i]));
        }
        drawChart(entries, "Chi tiêu hằng ngày");
    }

    private void showWeeklyChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        // Giả lập dữ liệu tuần
        float[] spending = {300, 450, 250, 500};
        for (int i = 0; i < spending.length; i++) {
            entries.add(new Entry(i, spending[i]));
        }
        drawChart(entries, "Chi tiêu theo tuần");
    }

    private void showMonthlyChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        // Giả lập dữ liệu tháng
        float[] spending = {1000, 1200, 900, 1500, 1300, 1600, 1400, 1800, 1100, 1700, 1900, 2000};
        for (int i = 0; i < spending.length; i++) {
            entries.add(new Entry(i, spending[i]));
        }
        drawChart(entries, "Chi tiêu theo tháng");
    }

    private void drawChart(ArrayList<Entry> entries, String label) {
        LineDataSet dataSet = new LineDataSet(entries, label);

        // Trang trí đường kẻ
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setColor(Color.BLUE); // Màu đường
        dataSet.setCircleColor(Color.RED); // Màu chấm tròn
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Đường cong mềm mại

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // 3. SỬA LỖI DESCRIPTION (Cách chuẩn cho v3.1.0)
        Description description = new Description();
        description.setText(label);
        description.setTextColor(Color.BLACK);
        description.setTextSize(12f);
        lineChart.setDescription(description);

        // Refresh biểu đồ
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
        lineChart.animateX(1000); // Thêm hiệu ứng chạy chạy cho đẹp
    }
}