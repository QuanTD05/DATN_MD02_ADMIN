package com.example.datn_md02_admim.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.Model.Banner;
import com.example.datn_md02_admim.R;


import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    public interface OnBannerAction {
        void onEdit(Banner banner);
        void onDelete(Banner banner);
    }

    private List<Banner> list;
    private OnBannerAction listener;

    public BannerAdapter(List<Banner> list, OnBannerAction listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner1, parent, false);
        return new BannerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = list.get(position);
        Glide.with(holder.itemView.getContext()).load(banner.getImageUrl()).into(holder.ivBanner);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(banner));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(banner));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setData(List<Banner> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        Button btnEdit, btnDelete;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}