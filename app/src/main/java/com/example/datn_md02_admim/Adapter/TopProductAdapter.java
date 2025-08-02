package com.example.datn_md02_admim.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.Model.ProductStat;
import com.example.datn_md02_admim.R;

import java.util.List;

public class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.TopProductViewHolder> {
    private Context context;
    private List<ProductStat> productList;

    public TopProductAdapter(Context context, List<ProductStat> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public TopProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_product, parent, false);
        return new TopProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopProductViewHolder holder, int position) {
        ProductStat product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvVariant.setText("Biến thể: " + product.getVariant());
        holder.tvQuantity.setText("Số lượng: " + product.getTotalQuantity());
        holder.tvRevenue.setText("Doanh thu: ₫" + String.format("%,.0f", product.getTotalRevenue()));
        Glide.with(context).load(product.getImage()).into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class TopProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvVariant, tvQuantity, tvRevenue;
        ImageView imgProduct;

        public TopProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvVariant = itemView.findViewById(R.id.tvVariant);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
}