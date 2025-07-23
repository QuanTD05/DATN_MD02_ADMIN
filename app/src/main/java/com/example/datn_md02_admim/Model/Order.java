package com.example.datn_md02_admim.Model;

public class Order {
    private String customerName;
    private String orderTime;
    private String totalAmount;
    private String status;

    public Order(String customerName, String orderTime, String totalAmount, String status) {
        this.customerName = customerName;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }
}
