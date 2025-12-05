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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;

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
main
 main
 main
