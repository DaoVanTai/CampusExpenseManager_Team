package com.example.baitap1;

import android.Manifest;
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
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class editTask extends AppCompatActivity {

    private static final String TAG = "EditTaskActivity";
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int CAMERA_REQUEST_CODE = 201;

    private DatabaseHelper dbHelper;

    private EditText edtEditTaskContent, edtEditTaskQuantity, edtEditTaskPrice, edtEditTaskDate;
    private Spinner spinnerEditCategory;
    private Button btnSubmitEdit, btnRetakePhoto, btnDeletePhoto;
    private ImageView ivReceiptImage;
    private TextView tvReceiptLabel;

    private int currentExpenseId = -1;
    private String currentReceiptPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ UI
        btnSubmitEdit = findViewById(R.id.btnSubmitEdit);
        edtEditTaskContent = findViewById(R.id.editTaskContent);
        edtEditTaskQuantity = findViewById(R.id.edtEditTaskQuantity);
        edtEditTaskPrice = findViewById(R.id.edtEditTaskPrice);
        edtEditTaskDate = findViewById(R.id.edtEditTaskDate);
        spinnerEditCategory = findViewById(R.id.spinnerEditCategory);

        ivReceiptImage = findViewById(R.id.ivReceiptImage);
        tvReceiptLabel = findViewById(R.id.tvReceiptLabel);
        btnRetakePhoto = findViewById(R.id.btnRetakePhoto);
        btnDeletePhoto = findViewById(R.id.btnDeletePhoto);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditCategory.setAdapter(adapter);

        currentExpenseId = getIntent().getIntExtra("EXPENSE_ID", -1);

        if (currentExpenseId != -1) {
            loadExpenseDetails(currentExpenseId);
        } else {
            // ⭐ THAY THẾ CHUỖI CỨNG
            Toast.makeText(this, getString(R.string.error_id_not_found), Toast.LENGTH_SHORT).show();
            finish();
        }

        // --- CÁC SỰ KIỆN ---
        btnSubmitEdit.setOnClickListener(v -> {
            hideKeyboard();
            saveEditedTask();
        });

        edtEditTaskDate.setOnClickListener(v -> showDatePicker());

        btnRetakePhoto.setOnClickListener(v -> checkAndRequestPermissions());

        btnDeletePhoto.setOnClickListener(v -> {
            currentReceiptPath = null;
            ivReceiptImage.setVisibility(View.GONE);
            ivReceiptImage.setImageBitmap(null);
            // ⭐ THAY THẾ CHUỖI CỨNG
            Toast.makeText(this, getString(R.string.msg_photo_deleted), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadExpenseDetails(int id) {
        Expense expense = dbHelper.getExpenseById(id);
        if (expense != null) {
            edtEditTaskContent.setText(expense.getDescription());
            edtEditTaskQuantity.setText(String.valueOf(expense.getQuantity()));
            edtEditTaskPrice.setText(String.valueOf(expense.getAmount()));

            String date = expense.getDate();
            if (date == null || date.isEmpty()) {
                date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            }
            edtEditTaskDate.setText(date);

            setSpinnerToValue(spinnerEditCategory, expense.getCategory());

            currentReceiptPath = expense.getReceiptPath();
            if (ivReceiptImage != null) ivReceiptImage.post(() -> displayReceipt(currentReceiptPath));
        }
    }

    private void displayReceipt(String photoPath) {
        if (photoPath != null && !photoPath.isEmpty()) {
            ivReceiptImage.setVisibility(View.VISIBLE);
            try {
                int targetW = ivReceiptImage.getWidth();
                int targetH = ivReceiptImage.getHeight();
                if (targetW <= 0) targetW = 300;
                if (targetH <= 0) targetH = 150;
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(photoPath, bmOptions);
                int scaleFactor = Math.max(1, Math.min(bmOptions.outWidth / targetW, bmOptions.outHeight / targetH));
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
                ivReceiptImage.setImageBitmap(bitmap);
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            ivReceiptImage.setVisibility(View.GONE);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        try {
            String[] parts = edtEditTaskDate.getText().toString().split("-");
            if (parts.length == 3) cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        } catch (Exception e) {}
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
            edtEditTaskDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void saveEditedTask() {
        String name = edtEditTaskContent.getText().toString().trim();
        String quantityStr = edtEditTaskQuantity.getText().toString().trim();
        String priceStr = edtEditTaskPrice.getText().toString().trim();
        String category = spinnerEditCategory.getSelectedItem().toString();
        String date = edtEditTaskDate.getText().toString().trim();

        if (name.isEmpty() || currentExpenseId == -1) {
            // ⭐ THAY THẾ CHUỖI CỨNG
            Toast.makeText(this, getString(R.string.error_data_invalid), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int quantity = Integer.parseInt(quantityStr);
            long price = Long.parseLong(priceStr);

            boolean isUpdated = dbHelper.updateExpense(
                    currentExpenseId,
                    name,
                    quantity,
                    price,
                    category,
                    date,
                    currentReceiptPath
            );

            if (isUpdated) {
                // ⭐ THAY THẾ CHUỖI CỨNG
                Toast.makeText(this, getString(R.string.msg_update_success), Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                // ⭐ THAY THẾ CHUỖI CỨNG
                Toast.makeText(this, getString(R.string.error_save_db), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // ⭐ THAY THẾ CHUỖI CỨNG
            Toast.makeText(this, getString(R.string.error_number_invalid), Toast.LENGTH_SHORT).show();
        }
    }

    // --- LOGIC CAMERA ---
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        } else { dispatchTakePictureIntent(); }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            // ⭐ THAY THẾ CHUỖI CỨNG
            Toast.makeText(this, getString(R.string.perm_camera_required), Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent take = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (take.resolveActivity(getPackageManager()) != null) {
            File f = null;
            try {
                String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                f = File.createTempFile("RECEIPT_" + time + "_", ".jpg", dir);
                currentReceiptPath = f.getAbsolutePath();
            } catch (IOException e) {}
            if (f != null) {
                Uri uri = FileProvider.getUriForFile(this, "com.example.baitap1.fileprovider", f);
                take.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(take, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            displayReceipt(currentReceiptPath);
        }
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                    spinner.setSelection(i); return;
                }
            }
        }
    }
}