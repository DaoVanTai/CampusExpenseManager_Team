package com.example.baitap1;

import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = edtEmail.getText().toString().trim();
                String pass = edtPassword.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter full information", Toast.LENGTH_SHORT).show();
                }
                else if (email.equals("admin@gmail.com") && pass.equals("123456")) {
                    // Login success → chuyển sang HomeActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // ko quay lại login
                }
                else {
                    Toast.makeText(LoginActivity.this, "Wrong email or password!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}


    