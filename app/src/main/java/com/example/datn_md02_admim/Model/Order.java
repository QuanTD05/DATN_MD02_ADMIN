package com.example.datn_md02_admim.Model;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private String receiverName;
    private String receiverAddress;
    private String status;
    private double totalAmount;
    private long timestamp;
    private List<CartItem> items;

    public Order() {}

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
