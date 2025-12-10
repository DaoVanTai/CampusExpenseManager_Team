package com.example.baitap1;

public class Expense {
    // Thêm trường ID (cần cho DB) và receiptPath
    private int id;
    private String description;
    private int quantity;
    private long amount;
    private String category;
    private String receiptPath; // ⭐ TRƯỜNG MỚI: ĐƯỜNG DẪN BIÊN LAI ⭐

    // Constructor MỚI (Thêm ID và receiptPath)
    public Expense(String description, int quantity, long amount, String category) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
        this.receiptPath = receiptPath;
    }

    // Constructor DÙNG KHI TẠO MỚI (Không có ID)
    public Expense(String description, int quantity, long amount, String category, String receiptPath) {
        this.description = description;
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
        this.receiptPath = receiptPath;
    }


    // Getters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public long getAmount() { return amount; }
    public String getCategory() { return category; }
    // ⭐ GETTER MỚI ⭐
    public String getReceiptPath() { return receiptPath; }


    // Phương thức chuyển đổi về String để hiển thị trong ListView
    @Override
    public String toString() {
        // Thêm trạng thái biên lai vào chuỗi hiển thị
        String receiptStatus = receiptPath != null && !receiptPath.isEmpty() ? " (Có biên lai)" : "";
        return String.format("Tên: %s - SL: %d - Giá: %d - DM: %s%s",
                description, quantity, amount, category, receiptStatus);
    }
}