package com.example.baitap1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddRecurringActivity extends AppCompatActivity {

    private EditText etDesc, etAmount, etCategory, etDay;
    private Button btnSave;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nhớ tạo layout activity_add_recurring.xml bên dưới
        setContentView(R.layout.activity_add_recurring);

        dbHelper = new DatabaseHelper(this);

        etDesc = findViewById(R.id.etRecDesc);
        etAmount = findViewById(R.id.etRecAmount);
        etCategory = findViewById(R.id.etRecCategory);
        etDay = findViewById(R.id.etRecDay);
        btnSave = findViewById(R.id.btnRecSave);

        btnSave.setOnClickListener(v -> {
            try {
                String desc = etDesc.getText().toString();
                double amount = Double.parseDouble(etAmount.getText().toString());
                String cat = etCategory.getText().toString();
                int day = Integer.parseInt(etDay.getText().toString());

                if (day < 1 || day > 31) {
                    Toast.makeText(this, "Ngày từ 1-31", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dbHelper.addRecurring(desc, amount, cat, day)) {
                    Toast.makeText(this, "Đã lưu cài đặt!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi nhập liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}