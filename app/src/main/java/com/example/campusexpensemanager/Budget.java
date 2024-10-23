
    package com.example.campusexpensemanager;

    public class Budget {
        private int id;
        private int userId;
        private int categoryId;  // Nếu bạn sử dụng categoryId thay vì categoryName
        private float amount;
        private String startDate;
        private String endDate;

        // Constructor
        public Budget(int id, int userId, int categoryId, float amount, String startDate, String endDate) {
            this.id = id;
            this.userId = userId;
            this.categoryId = categoryId;
            this.amount = amount;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Getters
        public int getId() {
            return id;
        }

        public int getUserId() {
            return userId;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public float getAmount() {
            return amount;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        // Setters
        public void setId(int id) {
            this.id = id;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public void setCategoryId(int categoryId) {
            this.categoryId = categoryId;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
