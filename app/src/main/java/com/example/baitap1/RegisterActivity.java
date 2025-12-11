package com.example.baitap1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUser, etPass, etRePass;
    private Button btnRegister, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUser = findViewById(R.id.etRegUser);
        etPass = findViewById(R.id.etRegPass);
        etRePass = findViewById(R.id.etRegRePass);
        btnRegister = findViewById(R.id.btnConfirmRegister);
        btnBack = findViewById(R.id.btnBackToLogin);

        // Xử lý nút Đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUser.getText().toString().trim();
                String pass = etPass.getText().toString().trim();
                String rePass = etRePass.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    // ⭐ THAY ĐỔI 1: Dùng resource string
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_input_empty), Toast.LENGTH_SHORT).show();
                } else if (!pass.equals(rePass)) {
                    // ⭐ THAY ĐỔI 2: Dùng resource string
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show();
                } else {
                    // Mã hóa mật khẩu
                    String hashedPassword = SecurityUtils.hashPassword(pass);

                    if (hashedPassword != null) {
                        // Lưu vào SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("UserAuth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("USERNAME", user);
                        editor.putString("PASSWORD", hashedPassword);
                        editor.apply();

                        // ⭐ THAY ĐỔI 3: Dùng resource string
                        Toast.makeText(RegisterActivity.this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Thay thế luôn thông báo lỗi hệ thống cho đồng bộ
                        Toast.makeText(RegisterActivity.this, getString(R.string.error_system_hash), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }
}