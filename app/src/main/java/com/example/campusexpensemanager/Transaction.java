package com.example.campusexpensemanager;

public class Transaction {
    private int transactionId;      // ID của giao dịch
    private int userId;             // ID của người dùng
    private int accountId;          // ID tài khoản liên kết
    private double amount;          // Số tiền của giao dịch
    private String transactionDate; // Ngày giao dịch
    private String type;            // Loại giao dịch (Income/Expense)

    // Getter và setter cho các thuộc tính
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
