package com.example.baitap1;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddNoteActivity extends AppCompatActivity {

    private EditText editTitle, editContent;
    private Button btnSave;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Kích hoạt chế độ tràn viền
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_add_note);

        // 2. Xử lý Insets (Tránh bị thanh trạng thái che mất nội dung)
        // Yêu cầu: File XML activity_add_note.xml phải có ID root là "main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo Database
        db = new DatabaseHelper(this);

        // Ánh xạ View
        editTitle = findViewById(R.id.editTextTitle);
        editContent = findViewById(R.id.editTextContent);
        btnSave = findViewById(R.id.buttonSaveNote);

        // Sự kiện bấm nút Lưu
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(AddNoteActivity.this, "Vui lòng nhập tiêu đề!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean check = db.addNote(title, content);
        if (check) {
            Toast.makeText(AddNoteActivity.this, "Đã lưu ghi chú!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng màn hình này để quay lại danh sách
        } else {
            Toast.makeText(AddNoteActivity.this, "Lỗi khi lưu trữ, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}