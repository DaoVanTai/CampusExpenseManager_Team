package com.example.baitap1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button btnLogin;
    private TextView tvRegister; // Nút chuyển trang đăng ký

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvGoToRegister);

        // 1. Xử lý nút Chuyển sang Đăng ký
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 2. Xử lý Đăng nhập (Lấy dữ liệu đã lưu để so sánh)
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputUser = etUser.getText().toString().trim();
                String inputPass = etPass.getText().toString().trim();

                // Lấy dữ liệu mật khẩu đã lưu trong máy ra
                SharedPreferences prefs = getSharedPreferences("UserAuth", MODE_PRIVATE);
                String savedUser = prefs.getString("USERNAME", ""); // Mặc định là rỗng nếu chưa có
                String savedPass = prefs.getString("PASSWORD", "");

                if (savedUser.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Chưa có tài khoản nào! Vui lòng đăng ký trước.", Toast.LENGTH_LONG).show();
                } else {
                    // So sánh dữ liệu nhập vào với dữ liệu đã lưu
                    if (inputUser.equals(savedUser) && inputPass.equals(savedPass)) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        // Chuyển vào MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Đóng Login lại để không back về được
                    } else {
                        Toast.makeText(LoginActivity.this, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}