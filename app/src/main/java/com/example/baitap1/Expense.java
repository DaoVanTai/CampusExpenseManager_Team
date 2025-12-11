package com.example.baitap1;

public class Expense {
    // Khai báo các trường
    private int id;
    private String description;
    private int quantity;
    private long amount;
    private String category;
    private String receiptPath; // Đường dẫn biên lai


    public Expense(int id, String description, int quantity, long amount, String category, String receiptPath) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
        this.receiptPath = receiptPath;
    }


    public Expense(String description, int quantity, long amount, String category, String receiptPath) {
        this.id = 0; // Đặt ID là 0 hoặc -1 vì DB chưa tạo
        this.description = description;
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
        this.receiptPath = receiptPath;
    }

    // ⭐ 3. CONSTRUCTOR TẠO MỚI KHÔNG CÓ BIÊN LAI (4 THAM SỐ) ⭐
    // Dùng cho logic cũ và các chi tiêu không có ảnh.
    public Expense(String description, int quantity, long amount, String category) {
        this(description, quantity, amount, category, null); // Gọi constructor 5 tham số với Path là null
    }


    // Getters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public long getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getReceiptPath() { return receiptPath; }


    // Phương thức chuyển đổi về String để hiển thị trong ListView
    @Override
    public String toString() {
        String receiptStatus = receiptPath != null && !receiptPath.isEmpty() ? " (Có biên lai)" : "";
        return String.format("Tên: %s - SL: %d - Giá: %d - DM: %s%s",
                description, quantity, amount, category, receiptStatus);
    }
}