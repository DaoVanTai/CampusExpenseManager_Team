package com.example.baitap1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends ArrayAdapter<Expense> {

    private Context context;
    private int resource;
    private List<Expense> objects;

    public ExpenseAdapter(@NonNull Context context, int resource, @NonNull List<Expense> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        // Lấy dữ liệu
        Expense expense = objects.get(position);

        // Ánh xạ View (Đã bỏ ImageView)
        TextView tvName = convertView.findViewById(R.id.tvItemName);
        TextView tvCategory = convertView.findViewById(R.id.tvItemCategory);
        TextView tvPrice = convertView.findViewById(R.id.tvItemPrice);

        // Đổ dữ liệu chữ
        tvName.setText(expense.getDescription());

        // Format tiền tệ
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        long total = expense.getQuantity() * expense.getAmount();
        tvPrice.setText(currencyFormatter.format(total));

        // Hiển thị Danh mục + Ngày
        String date = (expense.getDate() != null) ? expense.getDate() : "N/A";
        tvCategory.setText(expense.getCategory() + " • " + date);

        // ⭐ ĐÃ XÓA PHẦN LOAD ẢNH GLIDE TẠI ĐÂY ⭐
        // Vì danh sách chỉ cần hiện chữ cho gọn.

        return convertView;
    }
}