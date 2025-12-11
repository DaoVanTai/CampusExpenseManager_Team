package com.example.baitap1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class editTask extends AppCompatActivity {

    private static final String TAG = "EditTaskActivity";

    // Khai báo Database Helper
    private DatabaseHelper dbHelper;

    // UI elements
    private EditText edtEditTaskContent, edtEditTaskQuantity, edtEditTaskPrice;
    private Spinner spinnerEditCategory;
    private Button btnSubmitEdit;

    // UI elements cho Biên Lai
    private ImageView ivReceiptImage;
    private TextView tvReceiptLabel;

    // Biến quan trọng
    private int currentExpenseId = -1;
    private int taskPosition = -1;
    private String currentReceiptPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        dbHelper = new DatabaseHelper(this);

        // --- ÁNH XẠ UI ---
        btnSubmitEdit = findViewById(R.id.btnSubmitEdit);
        edtEditTaskContent = findViewById(R.id.editTaskContent);
        edtEditTaskQuantity = findViewById(R.id.edtEditTaskQuantity);
        edtEditTaskPrice = findViewById(R.id.edtEditTaskPrice);
        spinnerEditCategory = findViewById(R.id.spinnerEditCategory);

        // Ánh xạ UI cho Biên Lai
        ivReceiptImage = findViewById(R.id.ivReceiptImage);
        tvReceiptLabel = findViewById(R.id.tvReceiptLabel);

        // Cấu hình Spinner Danh mục
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditCategory.setAdapter(adapter);

        // 1. Lấy ID và Tải dữ liệu từ Intent
        currentExpenseId = getIntent().getIntExtra("EXPENSE_ID", -1);
        taskPosition = getIntent().getIntExtra("TASK_POSITION", -1);

        if (currentExpenseId != -1) {
            loadExpenseDetails(currentExpenseId); // Tải dữ liệu từ DB lên giao diện
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID chi tiêu.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 2. Xử lý nút Lưu/Submit
        btnSubmitEdit.setOnClickListener(v -> saveEditedTask());

        // Đặt mặc định ẩn ảnh biên lai lúc đầu
        ivReceiptImage.setVisibility(View.GONE);
        if (tvReceiptLabel != null) tvReceiptLabel.setVisibility(View.GONE);
    }

    // --- PHƯƠNG THỨC TẢI CHI TIẾT TỪ DATABASE ---
    private void loadExpenseDetails(int id) {
        Expense expense = dbHelper.getExpenseById(id);

        if (expense != null) {
            // Hiển thị các trường dữ liệu cũ
            edtEditTaskContent.setText(expense.getDescription());
            edtEditTaskQuantity.setText(String.valueOf(expense.getQuantity()));
            edtEditTaskPrice.setText(String.valueOf(expense.getAmount()));

            // Đặt giá trị Spinner đúng với dữ liệu cũ
            setSpinnerToValue(spinnerEditCategory, expense.getCategory());

            // Lưu đường dẫn ảnh hiện tại vào biến tạm
            currentReceiptPath = expense.getReceiptPath();

            // Hiển thị ảnh (nếu có)
            if (ivReceiptImage != null) {
                ivReceiptImage.post(() -> displayReceipt(currentReceiptPath));
            }

        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy chi tiêu với ID này.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayReceipt(String photoPath) {
        if (photoPath != null && !photoPath.isEmpty()) {
            if (tvReceiptLabel != null) tvReceiptLabel.setVisibility(View.VISIBLE);
            ivReceiptImage.setVisibility(View.VISIBLE);

            try {
                int targetW = ivReceiptImage.getWidth();
                int targetH = ivReceiptImage.getHeight();

                if (targetW <= 0) targetW = 300;
                if (targetH <= 0) targetH = 150;

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(photoPath, bmOptions);

                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;
                int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
                ivReceiptImage.setImageBitmap(bitmap);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi tải ảnh biên lai: " + e.getMessage());
                ivReceiptImage.setVisibility(View.GONE);
                Toast.makeText(this, "Không thể tải ảnh (File có thể đã bị xóa).", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (tvReceiptLabel != null) tvReceiptLabel.setVisibility(View.GONE);
            ivReceiptImage.setVisibility(View.GONE);
        }
    }

    // --- HÀM LƯU CHỈNH SỬA (Đã cập nhật logic gọi DB) ---
    private void saveEditedTask() {
        String name = edtEditTaskContent.getText().toString().trim();
        String quantityStr = edtEditTaskQuantity.getText().toString().trim();
        String priceStr = edtEditTaskPrice.getText().toString().trim();
        String category = spinnerEditCategory.getSelectedItem().toString();

        // Kiểm tra dữ liệu hợp lệ
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên chi tiêu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentExpenseId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được ID chi tiêu!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            long price = Long.parseLong(priceStr);

            // ⭐ GỌI HÀM UPDATE TRONG DATABASE HELPER ⭐
            // Ta truyền lại currentReceiptPath (ảnh cũ) vì tính năng sửa ảnh chưa có
            boolean isUpdated = dbHelper.updateExpense(
                    currentExpenseId,
                    name,
                    quantity,
                    price,
                    category,
                    currentReceiptPath
            );

            if (isUpdated) {
                Toast.makeText(this, "Đã cập nhật thành công!", Toast.LENGTH_SHORT).show();

                // Trả kết quả về MainActivity để làm mới danh sách
                Intent resultIntent = new Intent();
                resultIntent.putExtra("UPDATED_TASK_CONTENT",
                        String.format("Tên: %s - SL: %d - Giá: %d - DM: %s", name, quantity, price, category));

                setResult(Activity.RESULT_OK, resultIntent);
                finish(); // Đóng Activity
            } else {
                Toast.makeText(this, "Lỗi: Không thể cập nhật vào cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng hoặc giá phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Đã xảy ra lỗi không mong muốn.", Toast.LENGTH_SHORT).show();
        }
    }

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