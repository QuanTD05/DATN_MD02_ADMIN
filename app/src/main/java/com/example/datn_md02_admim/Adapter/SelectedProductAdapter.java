// app/src/main/java/com/example/datn_md02_admim/Adapter/SelectedProductAdapter.java
package com.example.datn_md02_admim.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.Model.Product;
import com.example.datn_md02_admim.R;

import java.util.List;

public class SelectedProductAdapter extends RecyclerView.Adapter<SelectedProductAdapter.VH> {

    private final List<Product> items;

    public SelectedProductAdapter(List<Product> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);
        h.tvName.setText(p.getName());
        h.tvId.setText(p.getProductId());
        Glide.with(h.itemView.getContext()).load(p.getImageUrl()).into(h.imgProduct);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvId;
        VH(@NonNull View v) {
            super(v);
            imgProduct = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvId = v.findViewById(R.id.tvId);
        }
    }
}
