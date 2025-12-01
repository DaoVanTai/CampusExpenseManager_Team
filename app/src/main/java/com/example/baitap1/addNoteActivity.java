package com.example.baitap1;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class addNoteActivity extends AppCompatActivity {

    EditText editTitle, editContent;
    Button btnSave;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        db = new DatabaseHelper(this);
        editTitle = findViewById(R.id.editTextTitle);
        editContent = findViewById(R.id.editTextContent);
        btnSave = findViewById(R.id.buttonSaveNote);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString();
                String content = editContent.getText().toString();

                if(title.isEmpty()) {
                    Toast.makeText(addNoteActivity.this, "Chưa nhập tiêu đề!", Toast.LENGTH_SHORT).show();
                } else {
                    boolean check = db.addNote(title, content);
                    if(check) {
                        Toast.makeText(addNoteActivity.this, "Đã lưu!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng màn hình này để quay lại danh sách
                    } else {
                        Toast.makeText(addNoteActivity.this, "Lỗi lưu trữ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}