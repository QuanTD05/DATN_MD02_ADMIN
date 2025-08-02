package com.example.datn_md02_admim.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.EditPromotionActivity;
import com.example.datn_md02_admim.Model.Promotion;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.ViewHolder> {
    private final List<Promotion> promoList;

    public PromotionAdapter(List<Promotion> promoList, Context context) {
        this.promoList = new ArrayList<>();
        if (promoList != null) this.promoList.addAll(promoList);
    }

    // method để cập nhật lại dữ liệu (dùng lại adapter)
    public void updateData(List<Promotion> newList) {
        promoList.clear();
        if (newList != null) promoList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Promotion promo = promoList.get(position);
        Context ctx = holder.itemView.getContext();

        if (promo == null) {
            holder.tvCode.setText("Mã: -");
            holder.tvDesc.setText("Mô tả: -");
            holder.tvDiscount.setText("Giảm: 0%");
            holder.tvStatus.setText("Không xác định");
            holder.btnMore.setVisibility(View.GONE);
            return;
        }

        // Validate / fallback từng trường trước khi hiển thị
        String code = promo.code != null ? promo.code : "";
        String desc = promo.description != null ? promo.description : "";
        String discountText;
        if (promo.discount >= 0 && promo.discount <= 100) {
            discountText = promo.discount + "%";
        } else {
            discountText = "0%"; // fallback nếu dữ liệu lệch
        }
        boolean active = Boolean.TRUE.equals(promo.is_active);

        holder.tvCode.setText("Mã: " + (code.isEmpty() ? "-" : code));
        holder.tvDesc.setText("Mô tả: " + (desc.isEmpty() ? "-" : desc));
        holder.tvDiscount.setText("Giảm: " + discountText);
        holder.tvStatus.setText(active ? "Đang hoạt động" : "Đã tắt");

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ctx, holder.btnMore);
            popup.inflate(R.menu.menu_promo_item);
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_edit) {
                    if (code.isEmpty()) {
                        Toast.makeText(ctx, "Mã khuyến mãi không hợp lệ, không thể sửa", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    Intent i = new Intent(ctx, EditPromotionActivity.class);
                    i.putExtra("promo_code", code);
                    ctx.startActivity(i);
                    return true;

                } else if (itemId == R.id.menu_delete) {
                    if (code.isEmpty()) {
                        Toast.makeText(ctx, "Mã khuyến mãi không hợp lệ, không thể xoá", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    new AlertDialog.Builder(ctx)
                            .setTitle("Xoá khuyến mãi")
                            .setMessage("Bạn chắc chắn muốn xoá khuyến mãi \"" + code + "\"?")
                            .setPositiveButton("Xoá", (dialog, which) -> {
                                FirebaseDatabase.getInstance()
                                        .getReference("promotions")
                                        .child(code)
                                        .removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(ctx, "Xoá thành công", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ctx, "Xoá thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            })
                            .setNegativeButton("Huỷ", null)
                            .show();
                    return true;
                }

                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return promoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvDesc, tvDiscount, tvStatus;
        ImageView btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
