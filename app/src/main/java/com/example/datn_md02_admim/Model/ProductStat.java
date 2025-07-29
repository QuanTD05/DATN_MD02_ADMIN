package com.example.datn_md02_admim.Model;

public class ProductStat {
    private String name;
    private String variant;
    private String image;
    private int totalQuantity;
    private double totalRevenue;

    public ProductStat() {}

    public ProductStat(String name, String variant, String image) {
        this.name = name;
        this.variant = variant;
        this.image = image;
        this.totalQuantity = 0;
        this.totalRevenue = 0;
    }

    public void add(int quantity, double price) {
        this.totalQuantity += quantity;
        this.totalRevenue += price * quantity;
    }

    public String getName() {
        return name;
    }

    public String getVariant() {
        return variant;
    }

    public String getImage() {
        return image;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}