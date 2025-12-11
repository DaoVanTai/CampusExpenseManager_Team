package com.example.baitap1;

public class Expense {
    private int id;
    private String description;
    private int quantity;
    private long amount;
    private String category;
    private String date; // ⭐ THÊM TRƯỜNG NGÀY
    private String receiptPath;

    // Constructor đầy đủ (Dùng khi đọc từ DB)
    public Expense(int id, String description, int quantity, long amount, String category, String date, String receiptPath) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
        this.date = date; // ⭐
        this.receiptPath = receiptPath;
    }

    // Constructor thêm mới (Dùng khi tạo mới)
    public Expense(String description, int quantity, long amount, String category, String date, String receiptPath) {
        this.id = 0;
        this.description = description;
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
        this.date = date; // ⭐
        this.receiptPath = receiptPath;
    }

    // Constructor tương thích cũ (để không lỗi AppData)
    public Expense(String description, int quantity, long amount, String category) {
        this.id = 0;
        this.description = description;
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
        this.date = null;
        this.receiptPath = null;
    }

    // Getters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public long getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDate() { return date; } // ⭐ HÀM GET DATE MỚI
    public String getReceiptPath() { return receiptPath; }

    @Override
    public String toString() {
        return String.format("Tên: %s - SL: %d - Giá: %d - DM: %s", description, quantity, amount, category);
    }
}