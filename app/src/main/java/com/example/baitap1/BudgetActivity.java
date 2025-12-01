package com.example.baitap1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.baitap1.adapter.BudgetAdapter; // Bạn cần tạo BudgetAdapter
import com.example.baitap1.viewmodel.TransactionViewModel; // Dùng chung ViewModel

import java.util.Calendar;

public class BudgetActivity extends AppCompatActivity {

    private RecyclerView rvBudgets;
    private TextView tvNoBudget;
    private FloatingActionButton fabAddBudget;
    private BudgetAdapter budgetAdapter;
    private TransactionViewModel transactionViewModel; // Tạm dùng ViewModel này

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn đã tạo file layout activity_budget.xml
        setContentView(R.layout.activity_budget);

        // Ánh xạ View
        rvBudgets = findViewById(R.id.rv_budgets);
        tvNoBudget = findViewById(R.id.tv_no_budget);
        fabAddBudget = findViewById(R.id.fab_add_budget);

        // Khởi tạo ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Cài đặt RecyclerView
        budgetAdapter = new BudgetAdapter(this, transactionViewModel); // Truyền ViewModel vào Adapter để tính toán chi tiêu
        rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        rvBudgets.setAdapter(budgetAdapter);

        // Lấy danh sách Ngân sách đang hoạt động (ví dụ: trong tháng hiện tại)
        long currentTime = System.currentTimeMillis();

        // --- Chú ý: Bạn cần thêm hàm get/observe Budget vào TransactionViewModel hoặc tạo BudgetViewModel riêng ---
        // Giả sử bạn đã thêm hàm getCurrentBudgets(long) vào Repository và ViewModel.

        // Tạm thời, tôi sẽ giả định có một LiveData<List<Budget>> trong ViewModel
        // LƯU Ý: Nếu bạn tạo BudgetViewModel riêng, hãy dùng nó.
        // Ví dụ: transactionViewModel.getCurrentBudgets(currentTime).observe(this, budgets -> {
        //     budgetAdapter.setBudgets(budgets);
        //     tvNoBudget.setVisibility(budgets.isEmpty() ? View.VISIBLE : View.GONE);
        // });


        // Thiết lập sự kiện FAB (Thêm Ngân sách mới)
        fabAddBudget.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetActivity.this, AddBudgetActivity.class);
            startActivity(intent);
        });
    }
}
