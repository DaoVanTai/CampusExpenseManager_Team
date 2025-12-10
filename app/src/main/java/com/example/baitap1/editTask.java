package com.example.baitap1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter; // Thêm import
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner; // Thêm import
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class editTask extends AppCompatActivity {
    private Button btnSubmitEdit;

    // Đổi tên biến để phản ánh việc chỉnh sửa nội dung chi tiết
    private EditText edtEditTaskContent; // Dùng lại ID cũ, nhưng giờ sẽ chứa tên/mô tả
    private EditText edtEditTaskQuantity; // Mới: Số lượng
    private EditText edtEditTaskPrice; // Mới: Giá
    private Spinner spinnerEditCategory; // Mới: Danh mục

    private int taskPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // Ánh xạ View (Bạn cần đảm bảo file activity_edit_task.xml có các ID này)
        btnSubmitEdit = findViewById(R.id.btnSubmitEdit);
        edtEditTaskContent = findViewById(R.id.editTaskContent);
        edtEditTaskQuantity = findViewById(R.id.edtEditTaskQuantity); // Cần thêm vào layout XML
        edtEditTaskPrice = findViewById(R.id.edtEditTaskPrice);       // Cần thêm vào layout XML
        spinnerEditCategory = findViewById(R.id.spinnerEditCategory); // Cần thêm vào layout XML

        // Cấu hình Spinner Danh mục
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.expense_categories, // Lấy từ strings.xml đã tạo trước đó
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditCategory.setAdapter(adapter);

        // 1. Lấy dữ liệu cũ để hiển thị
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TASK_CONTENT")) {
            String taskContent = intent.getStringExtra("TASK_CONTENT");
            taskPosition = intent.getIntExtra("TASK_POSITION", -1);

            // MỚI: Tách chuỗi String thành các trường dữ liệu và điền vào form
            Expense currentExpense = parseExpenseFromString(taskContent);
            if (currentExpense != null) {
                edtEditTaskContent.setText(currentExpense.getDescription());
                edtEditTaskQuantity.setText(String.valueOf(currentExpense.getQuantity()));
                edtEditTaskPrice.setText(String.valueOf(currentExpense.getAmount()));

                // Đặt giá trị Spinner dựa trên Category
                setSpinnerToValue(spinnerEditCategory, currentExpense.getCategory());
            } else {
                Toast.makeText(this, "Lỗi đọc dữ liệu chi tiêu!", Toast.LENGTH_SHORT).show();
            }
        }

        // 2. Xử lý nút Lưu/Submit
        btnSubmitEdit.setOnClickListener(v -> {
            String name = edtEditTaskContent.getText().toString().trim();
            String quantityStr = edtEditTaskQuantity.getText().toString().trim();
            String priceStr = edtEditTaskPrice.getText().toString().trim();
            String category = spinnerEditCategory.getSelectedItem().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Kiểm tra tính hợp lệ của số
                int quantity = Integer.parseInt(quantityStr);
                long price = Long.parseLong(priceStr);

                // Tạo chuỗi mới theo format đã thống nhất
                String updatedContent = String.format("Tên: %s - SL: %d - Giá: %d - DM: %s", name, quantity, price, category);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("UPDATED_TASK_CONTENT", updatedContent);
                resultIntent.putExtra("TASK_POSITION", taskPosition);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số lượng hoặc giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- HÀM HỖ TRỢ ---

    // Hàm chuyển đổi chuỗi String (lưu trong Intent) thành đối tượng Expense
    private Expense parseExpenseFromString(String expenseString) {
        try {
            // Chuỗi ví dụ: "Tên: Sản Phẩm A - SL: 5 - Giá: 10000 - DM: Thực phẩm"
            String description = expenseString.split(" - SL: ")[0].replace("Tên: ", "").trim();
            String quantityStr = expenseString.split(" - SL: ")[1].split(" - Giá:")[0].trim();
            String priceStr = expenseString.split(" - Giá: ")[1].split(" - DM: ")[0].trim();
            String category = expenseString.split(" - DM: ")[1].trim();

            int quantity = Integer.parseInt(quantityStr);
            long price = Long.parseLong(priceStr);

            return new Expense(description, quantity, price, category);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hàm tìm và chọn giá trị trong Spinner
    private void setSpinnerToValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }
}