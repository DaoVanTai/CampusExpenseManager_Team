package com.example.baitap1;

import android.os.Bundle;
import android.widget.ArrayAdapter; // Thêm import
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner; // Thêm import
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Sửa đổi file CreateNewTaskActivity.java
public class CreateNewTaskActivity extends AppCompatActivity {
    private EditText edtTaskName, edtTaskQuantity, edtTaskPrice;
    private Spinner spinnerCategory; // Mới: Thêm Spinner
    private Button btnSubmitCreate;
    private AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_task);
        appData = AppData.getInstance();
        edtTaskName = findViewById(R.id.edtTaskName);
        edtTaskQuantity = findViewById(R.id.edtTaskQuantity);
        edtTaskPrice = findViewById(R.id.edtTaskPrice);
        btnSubmitCreate = findViewById(R.id.btnSubmitCreate);
        spinnerCategory = findViewById(R.id.spinnerCategory); // Ánh xạ Spinner

        // Thiết lập Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_categories, // Bạn cần tạo mảng này trong strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnSubmitCreate.setOnClickListener(v -> {
            String taskName = edtTaskName.getText().toString().trim();
            String quantityStr = edtTaskQuantity.getText().toString().trim();
            String priceStr = edtTaskPrice.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString(); // Lấy danh mục

            if (taskName.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);
                long price = priceStr.isEmpty() ? 0 : Long.parseLong(priceStr);

                // MỚI: Thêm đối tượng Expense vào taskList
                Expense newExpense = new Expense(taskName, quantity, price, category);
                appData.taskList.add(newExpense);

                Toast.makeText(this, "Đã thêm thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số lượng hoặc giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}