package com.example.baitap1;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class AddBudgetActivity extends AppCompatActivity {

    private TextInputEditText etLimitAmount;
    private Spinner spinnerCategory;
    private Button btnSelectStartDate, btnSelectEndDate, btnSaveBudget;
    private TextView tvStartDate, tvEndDate;

    private TransactionViewModel transactionViewModel;
    private List<Category> allCategories = new ArrayList<>();

    private long startDateTimestamp = 0;
    private long endDateTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kích hoạt chế độ tràn viền
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_add_budget);

        // 2. Xử lý Insets để tránh nội dung bị che bởi thanh trạng thái/điều hướng
        // Lưu ý: Layout gốc trong file activity_add_budget.xml cần có android:id="@+id/main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ View
        etLimitAmount = findViewById(R.id.et_limit_amount);
        spinnerCategory = findViewById(R.id.spinner_budget_category);
        btnSelectStartDate = findViewById(R.id.btn_select_start_date);
        btnSelectEndDate = findViewById(R.id.btn_select_end_date);
        btnSaveBudget = findViewById(R.id.btn_save_budget);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);

        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        initDefaultDates();
        loadCategories();

        btnSelectStartDate.setOnClickListener(v -> showDatePickerDialog(true)); // true: ngày bắt đầu
        btnSelectEndDate.setOnClickListener(v -> showDatePickerDialog(false)); // false: ngày kết thúc

        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void initDefaultDates() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        startDateTimestamp = start.getTimeInMillis();
        updateDateTextView(startDateTimestamp, true);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
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
            categoryNames.add("TỔNG (Ngân sách chung)");
            for (Category category : categories) {
                categoryNames.add(category.getName());
            }

            // Sử dụng layout dropdown chuẩn của Android để đẹp hơn
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    categoryNames
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        if (startDateTimestamp == 0 || endDateTimestamp == 0) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ Ngày bắt đầu và Ngày kết thúc.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDateTimestamp >= endDateTimestamp) {
            Toast.makeText(this, "Ngày Bắt đầu phải trước Ngày Kết thúc.", Toast.LENGTH_LONG).show();
            return;
        }

        double limit;
        try {
            limit = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = spinnerCategory.getSelectedItemPosition();

        int categoryId = 0; // Mặc định là 0 (Tổng)
        if (selectedPosition > 0 && !allCategories.isEmpty()) {
            // Kiểm tra index để tránh IndexOutOfBoundsException
            if (selectedPosition - 1 < allCategories.size()) {
                categoryId = allCategories.get(selectedPosition - 1).getId();
            }
        }

        Budget newBudget = new Budget(
                limit,
                startDateTimestamp,
                endDateTimestamp,
                categoryId
        );

        transactionViewModel.insertBudget(newBudget);

        Toast.makeText(this, "Đã lưu kế hoạch ngân sách thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}