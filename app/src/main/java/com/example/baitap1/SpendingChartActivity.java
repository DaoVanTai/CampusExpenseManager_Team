package com.example.baitap1;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class SpendingChartActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RadioGroup radioGroupTime;
    private BarChart barChart;


    private LineChart lineChart;
    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spending_chart);

        // 1. Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        radioGroupTime = findViewById(R.id.radioGroupTime);
        barChart = findViewById(R.id.chartSpending);

        // 2. Xử lý nút Quay lại MainActivity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đóng Activity này để quay về màn hình trước (MainActivity)
                finish();
            }
        });

        // 3. Cấu hình giao diện biểu đồ ban đầu
        setupChartConfig();

        // 4. Load dữ liệu mặc định (Theo Ngày)
        loadChartData(0);

        // 5. Xử lý sự kiện chọn Ngày/Tháng/Năm
        radioGroupTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbDay) {
                    loadChartData(0); // 0: Ngày
                } else if (checkedId == R.id.rbMonth) {
                    loadChartData(1); // 1: Tháng
                } else if (checkedId == R.id.rbYear) {
                    loadChartData(2); // 2: Năm
                }
            }
        });
    }

    // --- CẤU HÌNH BIỂU ĐỒ ---
    private void setupChartConfig() {
        if (barChart == null) return;

        barChart.getDescription().setEnabled(false); // Tắt dòng mô tả nhỏ
        barChart.setDrawGridBackground(false);
        barChart.animateY(1000); // Hiệu ứng chạy lên trong 1 giây (Sửa lại lệnh này cho đúng thư viện)

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Đưa chữ xuống đáy
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
    }

    // --- HÀM LOAD DỮ LIỆU ---
    private void loadChartData(int type) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] labels; // Nhãn trục hoành (T2, T3...)

        if (type == 0) {
            // --- THEO NGÀY ---
            entries.add(new BarEntry(0, 50000));
            entries.add(new BarEntry(1, 80000));
            entries.add(new BarEntry(2, 30000));
            labels = new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        } else if (type == 1) {
            // --- THEO THÁNG ---
            entries.add(new BarEntry(0, 1500000));
            entries.add(new BarEntry(1, 2000000));
            labels = new String[]{"T1", "T2", "T3", "T4", "T5", "T6"};
        } else {
            // --- THEO NĂM ---
            entries.add(new BarEntry(0, 12000000));
            entries.add(new BarEntry(1, 15000000));
            labels = new String[]{"2023", "2024", "2025"};
        }

        // Tạo DataSet và đổ dữ liệu
        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu (VNĐ)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Màu sắc
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        // Gán nhãn cho trục X (Nếu mảng labels ngắn hơn dữ liệu thì coi chừng lỗi Index)
        // Code an toàn: Chỉ gán formatter nếu số lượng label khớp
        if (labels.length > 0) {
            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        }

        barChart.invalidate(); // Vẽ lại biểu đồ
    }
}