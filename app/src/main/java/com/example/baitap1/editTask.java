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

    // ⭐ UI elements mới cho Biên Lai ⭐
    private ImageView ivReceiptImage;
    private TextView tvReceiptLabel;

    // Biến quan trọng mới
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

        // ⭐ Ánh xạ UI mới cho Biên Lai ⭐
        ivReceiptImage = findViewById(R.id.ivReceiptImage);
        tvReceiptLabel = findViewById(R.id.tvReceiptLabel);

        // Cấu hình Spinner Danh mục
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditCategory.setAdapter(adapter);

        // 1. Lấy ID và Tải dữ liệu
        currentExpenseId = getIntent().getIntExtra("EXPENSE_ID", -1);
        taskPosition = getIntent().getIntExtra("TASK_POSITION", -1);

        if (currentExpenseId != -1) {
            loadExpenseDetails(currentExpenseId); // ⭐ TẢI DỮ LIỆU TỪ DB ⭐
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID chi tiêu.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 2. Xử lý nút Lưu/Submit
        btnSubmitEdit.setOnClickListener(v -> saveEditedTask());

        // Đặt mặc định ẩn
        ivReceiptImage.setVisibility(View.GONE);
        if (tvReceiptLabel != null) tvReceiptLabel.setVisibility(View.GONE);
    }

    // ⭐ PHƯƠNG THỨC TẢI CHI TIẾT TỪ DATABASE ⭐
    private void loadExpenseDetails(int id) {
        Expense expense = dbHelper.getExpenseById(id);

        if (expense != null) {
            // Hiển thị các trường cũ
            edtEditTaskContent.setText(expense.getDescription());
            edtEditTaskQuantity.setText(String.valueOf(expense.getQuantity()));
            edtEditTaskPrice.setText(String.valueOf(expense.getAmount()));

            // Đặt giá trị Spinner
            setSpinnerToValue(spinnerEditCategory, expense.getCategory());

            // Lưu đường dẫn ảnh hiện tại
            currentReceiptPath = expense.getReceiptPath();

            // ⭐ SỬA LỖI Ở ĐÂY: Trì hoãn việc gọi hàm hiển thị ảnh cho đến khi View có kích thước ⭐
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
            // Hiển thị label/preview
            if (tvReceiptLabel != null) tvReceiptLabel.setVisibility(View.VISIBLE);
            ivReceiptImage.setVisibility(View.VISIBLE);

            try {
                // Logic giảm kích thước ảnh Bitmap
                int targetW = ivReceiptImage.getWidth();
                int targetH = ivReceiptImage.getHeight();

                // LƯU Ý: Nếu targetW/H vẫn là 0 (ví dụ ViewTreeObserver không hoạt động),
                // ta sẽ dùng kích thước cố định để tránh lỗi chia cho 0.
                if (targetW <= 0) targetW = 300;
                if (targetH <= 0) targetH = 150; // Kích thước đặt trong XML là 150dp

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
            // Ẩn nếu không có ảnh
            if (tvReceiptLabel != null) tvReceiptLabel.setVisibility(View.GONE);
            ivReceiptImage.setVisibility(View.GONE);
        }
    }


    private void saveEditedTask() {
        String name = edtEditTaskContent.getText().toString().trim();
        String quantityStr = edtEditTaskQuantity.getText().toString().trim();
        String priceStr = edtEditTaskPrice.getText().toString().trim();
        String category = spinnerEditCategory.getSelectedItem().toString();

        if (name.isEmpty() || currentExpenseId == -1) {
            Toast.makeText(this, "Dữ liệu không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            long price = Long.parseLong(priceStr);

            // ⭐ BỔ SUNG LOGIC UPDATE DATABASE BẰNG ID TẠI ĐÂY ⭐
            // Ví dụ: boolean success = dbHelper.updateExpense(currentExpenseId, ...);

            // Hiện tại, chỉ báo hiệu thành công và kết thúc
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            Toast.makeText(this, "Đã lưu chỉnh sửa.", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng hoặc giá không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm tìm và chọn giá trị trong Spinner (Dùng lại)
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