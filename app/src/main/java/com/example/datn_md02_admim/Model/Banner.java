package com.example.datn_md02_admim.Model;

public class Banner {
    private String id;
    private String imageUrl;

    public Banner() { }

    public Banner(String id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}