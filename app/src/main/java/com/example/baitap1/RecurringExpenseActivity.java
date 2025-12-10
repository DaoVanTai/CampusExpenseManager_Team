package com.example.baitap1;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class RecurringExpenseActivity extends AppCompatActivity {
    private ListView lvRecurring;
    private Button btnAddRecurring;
    private Button btnBack; // 1. Khai báo biến nút Back
    private AppData appData;
    private ArrayAdapter<RecurringExpense> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

        appData = AppData.getInstance();

        // Ánh xạ View
        lvRecurring = findViewById(R.id.lvRecurring);
        btnAddRecurring = findViewById(R.id.btnAddRecurring);
        btnBack = findViewById(R.id.btnBackRecur); // 2. Ánh xạ nút Back (ID phải trùng với XML)

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appData.recurringList);
        lvRecurring.setAdapter(adapter);

        // Sự kiện thêm mới
        btnAddRecurring.setOnClickListener(v -> showAddDialog());

        // 3. Sự kiện nút Back -> Đóng Activity để quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm Chi Tiêu Định Kỳ");

        // Layout code Java
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        final EditText edtName = new EditText(this);
        edtName.setHint("Tên khoản chi (VD: Tiền nhà)");

        final EditText edtAmount = new EditText(this);
        edtAmount.setHint("Số tiền (VD: 5000000)");
        edtAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        final Spinner spnCategory = new Spinner(this);
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(this, R.array.expense_categories, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(catAdapter);

        final EditText edtStart = new EditText(this);
        edtStart.setHint("Ngày bắt đầu (dd/MM/yyyy)");
        edtStart.setFocusable(false); // Không cho nhập tay, chỉ chọn lịch

        final EditText edtEnd = new EditText(this);
        edtEnd.setHint("Ngày kết thúc (dd/MM/yyyy)");
        edtEnd.setFocusable(false); // Không cho nhập tay, chỉ chọn lịch

        // Sự kiện click vào EditText ngày -> Hiện lịch
        edtStart.setOnClickListener(v -> showDatePicker(edtStart));
        edtEnd.setOnClickListener(v -> showDatePicker(edtEnd));

        layout.addView(edtName);
        layout.addView(edtAmount);
        layout.addView(spnCategory);
        layout.addView(edtStart);
        layout.addView(edtEnd);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String amountStr = edtAmount.getText().toString().trim();
            String category = spnCategory.getSelectedItem().toString();
            String start = edtStart.getText().toString().trim();
            String end = edtEnd.getText().toString().trim();

            if (!name.isEmpty() && !amountStr.isEmpty() && !start.isEmpty() && !end.isEmpty()) {
                try {
                    long amount = Long.parseLong(amountStr);
                    RecurringExpense item = new RecurringExpense(name, amount, category, start, end);
                    appData.recurringList.add(item);
                    adapter.notifyDataSetChanged();

                    // Chạy kiểm tra ngay lập tức xem hôm nay có trùng ngày không để thêm vào luôn
                    appData.checkAndAddRecurringExpenses();

                    Toast.makeText(this, "Đã thêm quy tắc định kỳ!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi nhập liệu!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showDatePicker(EditText edt) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Format ngày thành dd/MM/yyyy (ví dụ: 05/01/2025)
            String date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
            edt.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}