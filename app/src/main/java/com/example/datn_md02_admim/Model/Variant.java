package com.example.datn_md02_admim.Model;

import java.io.Serializable;

public class Variant implements Serializable {
    private int quantity;
    private double price;
    private String image;

    // Bắt buộc constructor rỗng cho Firebase
    public Variant() {}

    // Constructor đầy đủ
    public Variant(int quantity, double price, String image) {
        this.quantity = quantity;
        this.price = price;
        this.image = image;
    }

    // Getter & Setter
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
