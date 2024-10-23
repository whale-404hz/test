package com.example.campusexpensemanager;

public class Transaction_Notification {
    public static class Transaction {
        private int transactionId;      // ID của giao dịch
        private int userId;             // ID của người dùng
        private int accountId;          // ID tài khoản liên kết
        private double amount;          // Số tiền của giao dịch
        private String transactionDate; // Ngày giao dịch
        private String type;            // Loại giao dịch (Income/Expense)
        private String sourceOrCategory; // Dùng cho source (Income) hoặc category (Expense)

        // Constructor với tất cả thuộc tính
        public Transaction(int transactionId, int userId, int accountId, double amount, String transactionDate, String type, String sourceOrCategory) {
            this.transactionId = transactionId;
            this.userId = userId;
            this.accountId = accountId;
            this.amount = amount;
            this.transactionDate = transactionDate;
            this.type = type;
            this.sourceOrCategory = sourceOrCategory;
        }

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

        public String getSourceOrCategory() {
            return sourceOrCategory;
        }

        public void setSourceOrCategory(String sourceOrCategory) {
            this.sourceOrCategory = sourceOrCategory;
        }
    }
}
