package com.example.datn_md02_admim.Model;

import java.io.Serializable;

public class Variant implements Serializable {
    private String color;
    private String size;
    private double length;
    private double width;
    private double height;
    private double price;
    private int quantity;
    private String imageUrl;

    public Variant() {}

    public Variant(String color, String size, double length, double width, double height) {
        this.color = color;
        this.size = size;
        this.length = length;
        this.width = width;
        this.height = height;
    }

    // Getter và Setter cho color
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    // Getter và Setter cho size
    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }

    // Getter và Setter cho length
    public double getLength() {
        return length;
    }
    public void setLength(double length) {
        this.length = length;
    }

    // Getter và Setter cho width
    public double getWidth() {
        return width;
    }
    public void setWidth(double width) {
        this.width = width;
    }

    // Getter và Setter cho height
    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }

    // Getter và Setter cho price
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    // Getter và Setter cho quantity
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Getter và Setter cho imageUrl
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
