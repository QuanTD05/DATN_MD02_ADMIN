package com.example.datn_md02_admim.Model;

import java.util.List;

public class Promotion {
    public String code;
    public String description;
    public double discount;
    public String start_date;
    public String end_date;
    public boolean is_active;
    public boolean apply_to_all;
    public List<String> apply_to_product_ids;

    public Promotion() {} // Firebase yêu cầu constructor rỗng
}
