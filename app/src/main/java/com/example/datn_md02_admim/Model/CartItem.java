package com.example.datn_md02_admim.Model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String productName;
    private String productImage;
    private String variant;
    private int quantity;
    private double price;

    public CartItem() {}

    public String getProductName() {
        return productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public String getVariant() {
        return variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
