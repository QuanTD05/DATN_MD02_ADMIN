package com.example.datn_md02_admim.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.datn_md02_admim.Model.Product;
import com.example.datn_md02_admim.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public interface OnProductClickListener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onView(Product product);
    }

    private OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product item = productList.get(position);

        holder.tvName.setText(item.getName() != null ? item.getName() : "-");
        holder.tvCategory.setText("Loại: " + mapCategoryIdToType(item.getCategoryId()));

        // Format tiền Việt
        String priceText = "-";
        if (item.getPrice() >= 0) {
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            priceText = nf.format(item.getPrice()) + " ₫";
        }
        holder.tvPrice.setText("Giá: " + priceText);


        // Load ảnh nếu có url
        if (!TextUtils.isEmpty(item.getImageUrl())) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Gán sự kiện click, check listener null tránh crash
        if (listener != null) {
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
            holder.btnView.setOnClickListener(v -> listener.onView(item));
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnEdit, btnDelete, btnView;
        TextView tvName, tvCategory, tvPrice, tvCreated;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgFurniture);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }

    private String mapCategoryIdToType(String categoryId) {
        if (categoryId == null) return "Khác";
        switch (categoryId.toLowerCase()) {
            case "ban": return "Bàn";
            case "ghe": return "Ghế";
            case "tu": return "Tủ";
            case "giuong": return "Giường";
            default: return "Khác";
        }
    }

}
