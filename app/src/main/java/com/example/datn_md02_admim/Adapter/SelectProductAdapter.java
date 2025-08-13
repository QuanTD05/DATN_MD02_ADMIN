// app/src/main/java/com/example/datn_md02_admim/Adapter/SelectProductAdapter.java
package com.example.datn_md02_admim.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.Model.Product;
import com.example.datn_md02_admim.R;

import java.util.List;

public class SelectProductAdapter extends RecyclerView.Adapter<SelectProductAdapter.VH> {

    private final List<Product> items;
    private final List<String> selectedIds;

    public SelectProductAdapter(List<Product> items, List<String> selectedIds) {
        this.items = items;
        this.selectedIds = selectedIds;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);
        String pid = p.getProductId();

        h.tvName.setText(p.getName() != null ? p.getName() : "(Không tên)");
        h.tvId.setText(pid != null ? pid : "(No ID)");

        Glide.with(h.itemView.getContext())
                .load(p.getImageUrl())
                .into(h.img);

        boolean checked = pid != null && selectedIds.contains(pid);
        h.cb.setChecked(checked);

        View.OnClickListener toggle = v -> {
            if (pid == null) return;
            boolean nowChecked = !h.cb.isChecked();
            h.cb.setChecked(nowChecked);
            if (nowChecked) {
                if (!selectedIds.contains(pid)) selectedIds.add(pid);
            } else {
                selectedIds.remove(pid);
            }
        };

        h.itemView.setOnClickListener(toggle);
        h.cb.setOnClickListener(toggle);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvId;
        CheckBox cb;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvId = v.findViewById(R.id.tvId);
            cb = v.findViewById(R.id.cbSelect);
        }
    }
}
