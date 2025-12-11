package com.example.baitap1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateNewTaskActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final String TAG = "CreateNewTaskActivity";

    private String currentPhotoPath = null;
    private long loggedInUserId = 1L;

    private EditText edtTaskName, edtTaskQuantity, edtTaskPrice, edtTaskDate; // ⭐ Thêm edtTaskDate
    private Spinner spinnerCategory;
    private Button btnSubmitCreate, btnCapture;
    private ImageView ivReceiptPreview;
    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_task);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ
        edtTaskName = findViewById(R.id.edtTaskName);
        edtTaskQuantity = findViewById(R.id.edtTaskQuantity);
        edtTaskPrice = findViewById(R.id.edtTaskPrice);
        edtTaskDate = findViewById(R.id.edtTaskDate); // ⭐ Ánh xạ ô ngày
        btnSubmitCreate = findViewById(R.id.btnSubmitCreate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnCapture = findViewById(R.id.btn_capture_receipt);
        ivReceiptPreview = findViewById(R.id.iv_receipt_preview);

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // ⭐ Set ngày mặc định là hôm nay
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        edtTaskDate.setText(today);

        // ⭐ Sự kiện chọn ngày
        edtTaskDate.setOnClickListener(v -> showDatePicker());

        btnCapture.setOnClickListener(v -> checkAndRequestPermissions());
        btnSubmitCreate.setOnClickListener(v -> {
            hideKeyboard(); // ⭐ Ẩn bàn phím trước khi xử lý
            checkLimitAndSave();
        });

        ivReceiptPreview.setVisibility(View.GONE);
    }

    // ⭐ HÀM HIỆN LỊCH CHỌN NGÀY ⭐
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        // Lấy ngày hiện tại đang hiển thị trong ô (nếu có) để set cho lịch
        try {
            String[] parts = edtTaskDate.getText().toString().split("-");
            if (parts.length == 3) {
                cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            }
        } catch (Exception e) { e.printStackTrace(); }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Format thành yyyy-MM-dd để lưu vào DB chuẩn
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    edtTaskDate.setText(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // ⭐ HÀM ẨN BÀN PHÍM (YC C) ⭐
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void checkLimitAndSave() {
        String description = edtTaskName.getText().toString().trim();
        String quantityStr = edtTaskQuantity.getText().toString().trim();
        String priceStr = edtTaskPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        // ⭐ Lấy ngày từ ô nhập liệu thay vì new Date()
        String date = edtTaskDate.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên Vật Phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);
            long unitPrice = priceStr.isEmpty() ? 0 : Long.parseLong(priceStr);
            long newAmount = quantity * unitPrice;

            long limit = dbHelper.getCategoryLimit(category);
            if (limit > 0) {
                long currentTotal = dbHelper.getTotalSpentByCategory(category);
                if (currentTotal + newAmount > limit) {
                    new AlertDialog.Builder(this)
                            .setTitle("⚠️ VƯỢT HẠN MỨC DANH MỤC")
                            .setMessage("Cảnh báo: Chi tiêu này vượt quá giới hạn " + limit + " VNĐ.")
                            .setPositiveButton("Vẫn lưu", (dialog, which) -> saveExpenseDirectly(description, quantity, unitPrice, category, date))
                            .setNegativeButton("Hủy", null)
                            .show();
                    return;
                }
            }
            saveExpenseDirectly(description, quantity, unitPrice, category, date);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số liệu không hợp lệ.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveExpenseDirectly(String description, int quantity, long unitPrice, String category, String date) {
        // Không gọi new Date() nữa, dùng biến date truyền vào
        boolean isInserted = dbHelper.addExpense(
                loggedInUserId,
                description,
                quantity,
                unitPrice,
                category,
                date,
                (currentPhotoPath != null) ? currentPhotoPath : ""
        );

        if (isInserted) {
            Toast.makeText(this, "Đã lưu giao dịch!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Lỗi lưu DB.", Toast.LENGTH_LONG).show();
        }
    }

    // --- CÁC HÀM CAMERA (GIỮ NGUYÊN) ---
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Cần quyền Camera!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try { photoFile = createImageFile(); } catch (IOException ex) { Log.e(TAG, "Lỗi tạo file", ex); }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.baitap1.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("RECEIPT_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setReducedImagePreview();
            } else {
                currentPhotoPath = null;
            }
        }
    }

    private void setReducedImagePreview() {
        if (currentPhotoPath == null) return;
        int targetW = ivReceiptPreview.getWidth();
        int targetH = ivReceiptPreview.getHeight();
        if (targetW <= 0) targetW = 300;
        if (targetH <= 0) targetH = 300;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int scaleFactor = Math.max(1, Math.min(bmOptions.outWidth / targetW, bmOptions.outHeight / targetH));
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        ivReceiptPreview.setImageBitmap(bitmap);
        ivReceiptPreview.setVisibility(View.VISIBLE);
    }
}