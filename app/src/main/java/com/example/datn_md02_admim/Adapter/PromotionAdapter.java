package com.example.datn_md02_admim.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.EditPromotionActivity;
import com.example.datn_md02_admim.Model.Promotion;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.ViewHolder> {
    private List<Promotion> promoList;
    private Context context;

    public PromotionAdapter(List<Promotion> promoList, Context context) {
        this.promoList = promoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Promotion promo = promoList.get(position);
        holder.tvCode.setText("Mã: " + promo.code);
        holder.tvDesc.setText("Mô tả: " + promo.description);
        holder.tvDiscount.setText("Giảm: " + promo.discount + "%");
        holder.tvStatus.setText(promo.is_active ? "Đang hoạt động" : "Đã tắt");

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMore);
            popup.inflate(R.menu.menu_promo_item);
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_edit) {
                    Intent i = new Intent(context, EditPromotionActivity.class);
                    i.putExtra("promo_code", promo.code);
                    context.startActivity(i);
                    return true;

                } else if (itemId == R.id.menu_delete) {
                    new AlertDialog.Builder(context)
                            .setTitle("Xoá khuyến mãi")
                            .setMessage("Bạn chắc chắn muốn xoá?")
                            .setPositiveButton("Xoá", (dialog, which) -> {
                                FirebaseDatabase.getInstance().getReference("promotions")
                                        .child(promo.code).removeValue();
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
