package com.example.baitap1;

 code-moi-cua-toi
import androidx.appcompat.app.AppCompatActivity;


 feature/login-ui-update
import androidx.appcompat.app.AppCompatActivity;


code-SoDo

import androidx.appcompat.app.AppCompatActivity;

main
 main
 main
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
 code-moi-cua-toi
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {


 feature/login-ui-update
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    EditText editTextUsername;
    EditText editTextPassword;
    Button buttonLogin;

code-SoDo

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button btnLogin;
    private TextView tvRegister; // Nút chuyển trang đăng ký

import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
 main
    EditText editTextUsername;
    EditText editTextPassword;
    Button buttonLogin;
    TextView textViewRegisterLink;
    DatabaseHelper db;
 code-moi-cua-toi

main
 main
 main

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

 code-moi-cua-toi

 feature/login-ui-update
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

code-SoDo
        EdgeToEdge.enable(this);

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

}

    

 main
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink); // ID này phải có trong file activity_login.xml

 code-moi-cua-toi

 main
 main

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

 code-moi-cua-toi

 feature/login-ui-update
                if (username.equals("admin") && password.equals("123")) {

 main
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isAuthenticated = db.checkUser(username, password);

                if (isAuthenticated) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

 code-moi-cua-toi

 main
 main
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Tên đăng nhập hoặc mật khẩu sai!", Toast.LENGTH_SHORT).show();
                }
            }
        });
 code-moi-cua-toi

 feature/login-ui-update
    }
}

 main
        textViewRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
 code-moi-cua-toi
}

}



