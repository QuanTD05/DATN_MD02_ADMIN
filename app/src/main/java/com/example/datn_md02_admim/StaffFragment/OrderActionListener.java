package com.example.datn_md02_admim.StaffFragment;

public interface OrderActionListener {
    void onOrderStatusChanged(String orderId, String oldStatus, String newStatus);
}
