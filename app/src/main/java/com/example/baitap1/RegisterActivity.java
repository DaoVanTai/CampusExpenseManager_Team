package com.example.baitap1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    EditText edtEmailReg, edtPasswordReg, edtConfirmPassword;
    Button btnRegister, btnBackLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        edtEmailReg = findViewById(R.id.edtEmailReg);
        edtPasswordReg = findViewById(R.id.edtPasswordReg);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackLogin = findViewById(R.id.btnBackLogin);

        btnBackLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmailReg.getText().toString().trim();
                String pass = edtPasswordReg.getText().toString().trim();
                String confirm = edtConfirmPassword.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pass.equals(confirm)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lưu tài khoản
                SharedPreferences prefs = getSharedPreferences("USER_DATA", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", email);
                editor.putString("password", pass);
                editor.apply();

                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish(); // quay về login
            }
        });
    }
}