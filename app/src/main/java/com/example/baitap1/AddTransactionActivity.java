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
import com.example.baitap1.models.Category;
import com.example.baitap1.models.Transaction;
import com.example.baitap1.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText etAmount, etDescription;
    private Spinner spinnerCategory;
    private Button btnSelectDate, btnSaveTransaction;
    private TextView tvDate;

    private TransactionViewModel transactionViewModel;
    private List<Category> allCategories = new ArrayList<>();
    private long selectedTimestamp = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kích hoạt chế độ tràn viền
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_add_transaction);

        // 2. Xử lý Insets để tránh nội dung bị che bởi thanh trạng thái
        // Yêu cầu: File XML activity_add_transaction.xml phải có ID root là "main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 3. Ánh xạ View
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnSaveTransaction = findViewById(R.id.btn_save_transaction);
        tvDate = findViewById(R.id.tv_date);

        // Hiển thị ngày hiện tại mặc định
        updateDateTextView(selectedTimestamp);

        // 4. Khởi tạo ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // 5. Load Danh mục (Category)
        loadCategories();

        // 6. Thiết lập sự kiện
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void loadCategories() {
        transactionViewModel.allCategories.observe(this, categories -> {
            allCategories = categories;
            List<String> categoryNames = new ArrayList<>();
            for (Category category : categories) {
                categoryNames.add(category.getName());
            }

            // Dùng ArrayAdapter để hiển thị danh sách danh mục lên Spinner
            // Sử dụng layout dropdown chuẩn của Android để đẹp hơn
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    categoryNames
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);

            // Khởi tạo một vài danh mục mẫu nếu DB trống (Tùy chọn)
            if (categories.isEmpty()) {
                initDefaultCategories();
            }
        });
    }

    // Hàm khởi tạo danh mục mặc định (chỉ nên chạy 1 lần hoặc khi DB trống)
    private void initDefaultCategories() {
        // Lưu ý: Việc insert này nên được xử lý ở DatabaseHelper hoặc ViewModel khi khởi tạo DB thì tốt hơn
        transactionViewModel.insertCategory(new Category("Ăn uống", 1));
        transactionViewModel.insertCategory(new Category("Đi lại", 1));
        transactionViewModel.insertCategory(new Category("Học tập", 1));
        // Thêm các danh mục khác...
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (selectedTimestamp != 0) {
            calendar.setTimeInMillis(selectedTimestamp);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(y, m, d, 0, 0, 0); // Đặt giờ về 0 để lưu ngày chuẩn
                    selectedTimestamp = c.getTimeInMillis();
                    updateDateTextView(selectedTimestamp);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void updateDateTextView(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        // Định dạng ngày theo kiểu dd/MM/yyyy
        String dateString = String.format(Locale.getDefault(), "Ngày: %02d/%02d/%d",
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.YEAR));
        tvDate.setText(dateString);
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr) || spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng điền đủ Số tiền và chọn Danh mục.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            int selectedPosition = spinnerCategory.getSelectedItemPosition();

            // Kiểm tra tính hợp lệ của danh mục được chọn
            if (selectedPosition < 0 || selectedPosition >= allCategories.size()) {
                Toast.makeText(this, "Danh mục không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            int categoryId = allCategories.get(selectedPosition).getId();

            // Tạo đối tượng Transaction mới
            Transaction newTransaction = new Transaction(
                    amount,
                    description.isEmpty() ? "Không có mô tả" : description,
                    selectedTimestamp,
                    categoryId
            );

            // Thêm vào cơ sở dữ liệu qua ViewModel
            transactionViewModel.insertTransaction(newTransaction);
            Toast.makeText(this, "Đã lưu giao dịch thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity sau khi lưu

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền nhập vào không hợp lệ.", Toast.LENGTH_SHORT).show();
        }
    }
}