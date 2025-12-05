package com.example.baitap1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class editTask extends AppCompatActivity {
    private Button btnSubmitEdit;
    private EditText editTextTaskContent;
    private int taskPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        btnSubmitEdit = findViewById(R.id.btnSubmitEdit);
        editTextTaskContent = findViewById(R.id.editTaskContent);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TASK_CONTENT")) {
            String taskContent = intent.getStringExtra("TASK_CONTENT");
            taskPosition = intent.getIntExtra("TASK_POSITION", -1);
            editTextTaskContent.setText(taskContent);
        }
        btnSubmitEdit.setOnClickListener(v -> {
            String editedContent = editTextTaskContent.getText().toString().trim();
            if (editedContent.isEmpty()) {
                Toast.makeText(this, "Nội dung không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra("UPDATED_TASK_CONTENT", editedContent);
            resultIntent.putExtra("TASK_POSITION", taskPosition);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }
}