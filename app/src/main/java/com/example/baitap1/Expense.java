package com.example.baitap1;

// (Tạo file mới)
public class Expense {
    private String description;
    private int quantity;
    private long amount; // Giá đơn vị
    private String category; // Mới: Thêm trường phân loại

    // Constructor
    public Expense(String description, int quantity, long amount, String category) {
        this.description = description;
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
    }

    // Getters
    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    // Phương thức chuyển đổi về String để hiển thị trong ListView
    // (Giữ format cũ để các activity khác không cần sửa nhiều)
    @Override
    public String toString() {
        return String.format("Tên: %s - SL: %d - Giá: %d - DM: %s",
                description, quantity, amount, category);
    }
}