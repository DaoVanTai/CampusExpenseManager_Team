package com.example.baitap1; // Sửa lại package của bạn

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
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (!pass.equals(rePass)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show();
                } else {
                    // --- LƯU TÀI KHOẢN VÀO MÁY ---
                    SharedPreferences prefs = getSharedPreferences("UserAuth", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("USERNAME", user);
                    editor.putString("PASSWORD", pass);
                    editor.apply(); // Lưu xong

                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình đăng ký để quay về Login
                }
            }
        });

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }
}