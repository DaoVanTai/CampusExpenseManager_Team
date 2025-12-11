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
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvGoToRegister);

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputUser = etUser.getText().toString().trim();
                String inputPass = etPass.getText().toString().trim();

                SharedPreferences prefs = getSharedPreferences("UserAuth", MODE_PRIVATE);
                String savedUser = prefs.getString("USERNAME", "");
                String savedPassHash = prefs.getString("PASSWORD", "");

                if (savedUser.isEmpty()) {
                    // ⭐ THAY ĐỔI 1: Dùng getString(R.string.account_not_found)
                    Toast.makeText(LoginActivity.this, getString(R.string.account_not_found), Toast.LENGTH_LONG).show();
                } else {
                    String inputPassHash = SecurityUtils.hashPassword(inputPass);

                    if (inputPassHash != null) {
                        if (inputUser.equals(savedUser) && inputPassHash.equals(savedPassHash)) {
                            // ⭐ THAY ĐỔI 2: Dùng getString(R.string.login_success)
                            Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // ⭐ THAY ĐỔI 3: Dùng getString(R.string.login_fail)
                            Toast.makeText(LoginActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // ⭐ THAY ĐỔI 4: Dùng getString(R.string.error_system_hash)
                        Toast.makeText(LoginActivity.this, getString(R.string.error_system_hash), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}