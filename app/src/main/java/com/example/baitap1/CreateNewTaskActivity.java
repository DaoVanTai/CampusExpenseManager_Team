package com.example.baitap1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class CreateNewTaskActivity extends AppCompatActivity {
    private EditText edtTaskName, edtTaskQuantity, edtTaskPrice;
    private Button btnSubmitCreate;
    private AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_task);
        appData = AppData.getInstance();
        edtTaskName = findViewById(R.id.edtTaskName);
        edtTaskQuantity = findViewById(R.id.edtTaskQuantity);
        edtTaskPrice = findViewById(R.id.edtTaskPrice);
        btnSubmitCreate = findViewById(R.id.btnSubmitCreate);
        btnSubmitCreate.setOnClickListener(v -> {
            String taskName = edtTaskName.getText().toString().trim();
            String taskQuantity = edtTaskQuantity.getText().toString().trim();
            String taskPrice = edtTaskPrice.getText().toString().trim();
            if (taskName.isEmpty()) {
                Toast.makeText(this, "Tên món ăn không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            String quantity = taskQuantity.isEmpty() ? "1" : taskQuantity;
            String price = taskPrice.isEmpty() ? "0" : taskPrice;
            String fullTask = String.format("Tên: %s - SL: %s - Giá: %s", taskName, quantity, price);
            appData.taskList.add(fullTask);
            Toast.makeText(this, "Đã thêm thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}