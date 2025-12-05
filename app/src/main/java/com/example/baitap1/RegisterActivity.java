package com.example.baitap1; // Sửa lại package của bạn

 code-moi-cua-toi
import androidx.appcompat.app.AppCompatActivity;

code-SoDo
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
main
 main
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

 code-moi-cua-toi

code-SoDo
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUser, etPass, etRePass;
    private Button btnRegister, btnBack;

 main
public class RegisterActivity extends AppCompatActivity {

    EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    Button buttonRegister;
    DatabaseHelper db;
 code-moi-cua-toi

main
 main

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 code-moi-cua-toi

code-SoDo
        EdgeToEdge.enable(this);
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

 main
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        editTextUsername = findViewById(R.id.editTextRegUsername);
        editTextPassword = findViewById(R.id.editTextRegPassword);
        editTextConfirmPassword = findViewById(R.id.editTextRegConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = editTextUsername.getText().toString();
                String pass = editTextPassword.getText().toString();
                String confirmPass = editTextConfirmPassword.getText().toString();

                if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.equals(confirmPass)) {
                        if (db.checkUsernameExists(user)) {
                            Toast.makeText(RegisterActivity.this, "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
                        } else {
                            boolean isAdded = db.addUser(user, pass);
                            if (isAdded) {
                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                    }
                }
 code-moi-cua-toi

main
 main
            }
        });

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }
}