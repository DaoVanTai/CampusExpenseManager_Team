// File: com.example.baitap1.AddBudgetActivity.java
package com.example.baitap1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.example.baitap1.models.Budget;
import com.example.baitap1.models.Category;
import com.example.baitap1.viewmodel.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddBudgetActivity extends AppCompatActivity {

    private TextInputEditText etLimitAmount;
    private Spinner spinnerCategory;
    private Button btnSelectStartDate, btnSelectEndDate, btnSaveBudget;
    private TextView tvStartDate, tvEndDate; // tvStartDate hiển thị ngày đã chọn

    private TransactionViewModel transactionViewModel;
    private List<Category> allCategories = new ArrayList<>();

    // Khởi tạo timestamp mặc định là đầu tháng hiện tại và cuối tháng hiện tại
    private long startDateTimestamp = 0;
    private long endDateTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // *** SỬA LỖI: Sử dụng layout mới đã tạo cho Budget ***
        setContentView(R.layout.activity_add_budget);

        // 1. Ánh xạ View (Sử dụng ID từ activity_add_budget.xml)
        etLimitAmount = findViewById(R.id.et_limit_amount);
        spinnerCategory = findViewById(R.id.spinner_budget_category);
        btnSelectStartDate = findViewById(R.id.btn_select_start_date);
        btnSelectEndDate = findViewById(R.id.btn_select_end_date);
        btnSaveBudget = findViewById(R.id.btn_save_budget);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);

        // 2. Khởi tạo ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // 3. Khởi tạo ngày mặc định và hiển thị
        initDefaultDates();

        // 4. Load Danh mục
        loadCategories();

        // 5. Thiết lập sự kiện chọn ngày
        btnSelectStartDate.setOnClickListener(v -> showDatePickerDialog(true)); // true: ngày bắt đầu
        btnSelectEndDate.setOnClickListener(v -> showDatePickerDialog(false)); // false: ngày kết thúc

        // 6. Thiết lập sự kiện Lưu Ngân sách
        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void initDefaultDates() {
        // Đặt ngày bắt đầu là đầu tháng hiện tại
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        startDateTimestamp = start.getTimeInMillis();
        updateDateTextView(startDateTimestamp, true);

        // Đặt ngày kết thúc là cuối tháng hiện tại
        Calendar end = Calendar.getInstance();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        // Đặt thời gian là cuối ngày để bao gồm cả ngày đó (23:59:59.999)
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        endDateTimestamp = end.getTimeInMillis();
        updateDateTextView(endDateTimestamp, false);
    }

    private void loadCategories() {
        transactionViewModel.allCategories.observe(this, categories -> {
            allCategories = categories;
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("TỔNG (Ngân sách chung)"); // Lựa chọn 0
            for (Category category : categories) {
                categoryNames.add(category.getName()); // Lựa chọn 1...n
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    categoryNames
            );
            spinnerCategory.setAdapter(adapter);
        });
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        long initialTimestamp = isStartDate ? startDateTimestamp : endDateTimestamp;

        if (initialTimestamp != 0) {
            calendar.setTimeInMillis(initialTimestamp);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(y, m, d);

                    // Thiết lập thời gian (đầu ngày cho StartDate, cuối ngày cho EndDate)
                    if (isStartDate) {
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);
                        startDateTimestamp = c.getTimeInMillis();
                    } else {
                        c.set(Calendar.HOUR_OF_DAY, 23);
                        c.set(Calendar.MINUTE, 59);
                        c.set(Calendar.SECOND, 59);
                        c.set(Calendar.MILLISECOND, 999);
                        endDateTimestamp = c.getTimeInMillis();
                    }
                    updateDateTextView(c.getTimeInMillis(), isStartDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    // Hàm cập nhật TextView hiển thị ngày
    private void updateDateTextView(long timestamp, boolean isStartDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateString = sdf.format(new java.util.Date(timestamp));

        if (isStartDate) {
            tvStartDate.setText(dateString);
        } else {
            tvEndDate.setText(dateString);
        }
    }

    private void saveBudget() {
        String amountStr = etLimitAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr) || spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng điền Giới hạn và chọn Danh mục/Tổng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Kiểm tra ngày hợp lệ
        if (startDateTimestamp == 0 || endDateTimestamp == 0) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ Ngày bắt đầu và Ngày kết thúc.", Toast.LENGTH_SHORT).show();
            return;
        }

        // So sánh: Ngày Bắt đầu phải nhỏ hơn hoặc bằng Ngày Kết thúc
        if (startDateTimestamp >= endDateTimestamp) {
            Toast.makeText(this, "Ngày Bắt đầu phải trước Ngày Kết thúc.", Toast.LENGTH_LONG).show();
            return;
        }

        double limit = Double.parseDouble(amountStr);
        int selectedPosition = spinnerCategory.getSelectedItemPosition();

        int categoryId = 0; // Mặc định 0 là Ngân sách TỔNG
        if (selectedPosition > 0) {
            // Lấy ID của Category, vì vị trí 0 là "TỔNG"
            categoryId = allCategories.get(selectedPosition - 1).getId();
        }

        Budget newBudget = new Budget(
                limit,
                startDateTimestamp,
                endDateTimestamp,
                categoryId
        );

        // 2. Thêm vào cơ sở dữ liệu qua ViewModel
        // *** LƯU Ý: Đảm bảo đã thêm hàm insertBudget(Budget budget) vào TransactionViewModel ***
        transactionViewModel.insertBudget(newBudget);

        Toast.makeText(this, "Đã lưu kế hoạch ngân sách thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}