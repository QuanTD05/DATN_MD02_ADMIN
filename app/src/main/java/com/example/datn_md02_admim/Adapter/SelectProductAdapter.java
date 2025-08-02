// SelectProductAdapter.java
package com.example.datn_md02_admim.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Model.Product;
import com.example.datn_md02_admim.R;

import java.util.List;

public class SelectProductAdapter extends RecyclerView.Adapter<SelectProductAdapter.ViewHolder> {

    private final List<Product> productList;
    private final List<String> selectedIds;

    public SelectProductAdapter(List<Product> productList, List<String> selectedIds) {
        this.productList = productList;
        this.selectedIds = selectedIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.txtName.setText(product.getName());
        holder.checkBox.setChecked(selectedIds.contains(product.getProductId()));

        holder.itemView.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                holder.checkBox.setChecked(false);
                selectedIds.remove(product.getProductId());
            } else {
                holder.checkBox.setChecked(true);
                if (!selectedIds.contains(product.getProductId())) {
                    selectedIds.add(product.getProductId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtProductName);
            checkBox = itemView.findViewById(R.id.chkProduct);
        }
    }
}
