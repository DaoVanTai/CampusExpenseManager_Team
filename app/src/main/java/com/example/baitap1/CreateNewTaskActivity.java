/**
 * CreateNewTaskActivity.java
 * Đã tích hợp logic chụp biên lai, quản lý quyền run-time, và lưu vào DatabaseHelper
 * với cấu trúc dữ liệu Quantity/UnitPrice/ReceiptPath mới.
 */
package com.example.baitap1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNewTaskActivity extends AppCompatActivity {

    // HẰNG SỐ YÊU CẦU CAMERA & QUYỀN
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final String TAG = "CreateNewTaskActivity";

    // BIẾN QUAN TRỌNG
    private String currentPhotoPath = null;

    private long loggedInUserId = 1L;

    // UI ELEMENTS
    private EditText edtTaskName, edtTaskQuantity, edtTaskPrice;
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

        // KHỞI TẠO CÁC ID GỐC
        edtTaskName = findViewById(R.id.edtTaskName);
        edtTaskQuantity = findViewById(R.id.edtTaskQuantity);
        edtTaskPrice = findViewById(R.id.edtTaskPrice);
        btnSubmitCreate = findViewById(R.id.btnSubmitCreate);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        // ⭐ ĐÃ SỬA: Ánh xạ ID mới KHỚP với XML: btn_capture_receipt và iv_receipt_preview ⭐
        btnCapture = findViewById(R.id.btn_capture_receipt);
        ivReceiptPreview = findViewById(R.id.iv_receipt_preview);

        // Thiết lập Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // ĐẶT SỰ KIỆN
        btnCapture.setOnClickListener(v -> checkAndRequestPermissions());
        btnSubmitCreate.setOnClickListener(v -> saveExpense());

        ivReceiptPreview.setVisibility(View.GONE);
    }


    private void checkAndRequestPermissions() {
        // Chỉ kiểm tra quyền CAMERA (vì Storage không cần cho thư mục ứng dụng từ API 29+)
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
                dispatchTakePictureIntent(); // Quyền được cấp, gọi camera
            } else {
                Toast.makeText(this, "Không thể chụp biên lai. Cần quyền truy cập Camera!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "RECEIPT_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Tạo file ảnh
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try { photoFile = createImageFile(); } catch (IOException ex) { Log.e(TAG, "Lỗi tạo file: ", ex); }

            if (photoFile != null) {
                // Sử dụng FileProvider để tạo URI an toàn
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.baitap1.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng Camera hệ thống.", Toast.LENGTH_SHORT).show();
        }
    }

    // =================================================================
    // 2. XỬ LÝ KẾT QUẢ VÀ HIỂN THỊ ẢNH
    // =================================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Đã chụp biên lai thành công!", Toast.LENGTH_SHORT).show();
                setReducedImagePreview(); // ⭐ HIỂN THỊ ẢNH ĐÃ GIẢM KÍCH THƯỚC ⭐
            } else if (resultCode == RESULT_CANCELED) {
                // Xóa tệp ảnh nếu người dùng hủy
                if (currentPhotoPath != null) {
                    new File(currentPhotoPath).delete();
                }
                currentPhotoPath = null;
                ivReceiptPreview.setVisibility(View.GONE);
                Toast.makeText(this, "Đã hủy chụp biên lai.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setReducedImagePreview() {
        if (currentPhotoPath == null) return;

        int targetW = ivReceiptPreview.getWidth();
        int targetH = ivReceiptPreview.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if (targetW > 0 && targetH > 0) {
            // Tính toán tỷ lệ giảm kích thước
            scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        ivReceiptPreview.setImageBitmap(bitmap);
        ivReceiptPreview.setVisibility(View.VISIBLE);
    }

    // =================================================================
    // 3. LOGIC LƯU CHI TIÊU VÀO DB
    // =================================================================

    private void saveExpense() {
        String description = edtTaskName.getText().toString().trim();
        String quantityStr = edtTaskQuantity.getText().toString().trim();
        String priceStr = edtTaskPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        // Lấy ngày hiện tại
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        int quantity;
        long unitPrice;

        if (description.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên Vật Phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Phân tích Số lượng và Đơn giá
        try {
            quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);
            unitPrice = priceStr.isEmpty() ? 0 : Long.parseLong(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số Lượng và Đơn Giá phải là số hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // GỌI DATABASE HELPER ĐỂ LƯU 7 THAM SỐ
        boolean isInserted = dbHelper.addExpense(
                loggedInUserId,
                description,
                quantity,           // Quantity
                unitPrice,          // Unit Price
                category,
                date,
                (currentPhotoPath != null) ? currentPhotoPath : "" // Receipt Path
        );

        if (isInserted) {
            Toast.makeText(this, "Đã lưu giao dịch thành công!", Toast.LENGTH_LONG).show();
            // Cài đặt RESULT_OK để Activity gọi có thể làm mới dữ liệu
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu giao dịch vào DB.", Toast.LENGTH_LONG).show();
        }
    }
}