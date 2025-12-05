package com.example.baitap1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baitap1.R;
import com.example.baitap1.model.Budget; // Giả sử bạn đã có class này
import com.example.baitap1.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private Context context;
    private List<Budget> budgetList;
    private TransactionViewModel viewModel;

    public BudgetAdapter(Context context, TransactionViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        this.budgetList = new ArrayList<>();
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgetList = budgets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Bạn cần tạo layout item_budget.xml cho từng dòng
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);

        // Giả sử Budget có các trường: categoryId, limitAmount
        // Bạn cần logic để lấy tên Category từ ID (có thể cần truy vấn DB hoặc list category có sẵn)
        holder.tvCategoryName.text = "Danh mục ID: " + budget.getCategoryId();
        holder.tvLimit.setText(String.format("Hạn mức: %,.0f VND", budget.getLimitAmount()));

        // TÍNH TOÁN TIẾN ĐỘ (LOGIC KHÓ NHẤT)
        // Cách 1 (Đơn giản): Gọi hàm tính toán trong ViewModel (Lưu ý: hàm này nên trả về LiveData hoặc chạy background)
        // Ở đây mình ví dụ set cứng, thực tế bạn nên lấy tổng chi tiêu của danh mục này từ ViewModel

        double spentAmount = 0; // TODO: Lấy từ ViewModel: viewModel.getSpentAmountByCategory(budget.getCategoryId())

        holder.tvSpent.setText(String.format("Đã chi: %,.0f VND", spentAmount));

        int progress = (int) ((spentAmount / budget.getLimitAmount()) * 100);
        holder.progressBar.setProgress(progress);

        // Đổi màu nếu vượt quá ngân sách
        if (progress > 100) {
            holder.progressBar.getProgressDrawable().setColorFilter(
                    context.getResources().getColor(android.R.color.holo_red_dark), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvSpent, tvLimit;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ view từ layout item_budget.xml
            tvCategoryName = itemView.findViewById(R.id.tv_budget_category);
            tvSpent = itemView.findViewById(R.id.tv_budget_spent);
            tvLimit = itemView.findViewById(R.id.tv_budget_limit);
            progressBar = itemView.findViewById(R.id.pb_budget);
        }
    }
}