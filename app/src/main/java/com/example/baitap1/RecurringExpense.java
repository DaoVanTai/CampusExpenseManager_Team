package com.example.baitap1;

public class RecurringExpense {
    private String name;
    private long amount;
    private String category;
    private String startDate; // Format: dd/MM/yyyy
    private String endDate;   // Format: dd/MM/yyyy

    public RecurringExpense(String name, long amount, String category, String startDate, String endDate) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getName() { return name; }
    public long getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }

    @Override
    public String toString() {
        return String.format("%s - %d VNĐ\n(%s) | %s đến %s", name, amount, category, startDate, endDate);
    }
}