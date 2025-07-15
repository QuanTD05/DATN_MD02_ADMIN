package com.example.datn_md02_admim.StaffFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.Adapter.ProductAdapter;
import com.example.datn_md02_admim.Model.Product;
import com.example.datn_md02_admim.Model.Review;
import com.example.datn_md02_admim.Model.Variant;
import com.example.datn_md02_admim.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;

import java.text.NumberFormat;
import java.util.*;

public class FurnitureFragment extends Fragment {

    private RecyclerView recyclerFurniture;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> allProductList = new ArrayList<>();
    private DatabaseReference productRef;
    private EditText editSearch;
    private FloatingActionButton fabAdd;
    private Spinner spinnerFilter;
    private final String[] types = {"Tất cả", "Bàn", "Ghế", "Tủ", "Giường"};

    private static final int PICK_IMAGE_PRODUCT = 1;
    private static final int PICK_IMAGE_VARIANT = 2;
    private ImageView currentImageView;
    private Uri selectedProductImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_furniture, container, false);

        recyclerFurniture = view.findViewById(R.id.recycler_furniture);
        editSearch = view.findViewById(R.id.edit_search);
        fabAdd = view.findViewById(R.id.fab_add);
        spinnerFilter = view.findViewById(R.id.spinner_filter);

        recyclerFurniture.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new ProductAdapter(getContext(), productList, new ProductAdapter.OnProductClickListener() {
            @Override public void onEdit(Product product) { showEditDialog(product); }
            @Override public void onDelete(Product product) { showDeleteConfirmDialog(product); }
            @Override public void onView(Product product) { showProductDetailsDialog(product); }
        });

        recyclerFurniture.setAdapter(adapter);
        productRef = FirebaseDatabase.getInstance().getReference("product");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, types);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterByType(types[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByName(s.toString());
            }
        });

        fabAdd.setOnClickListener(v -> showAddDialog());

        loadProductData();
        return view;
    }

    private void loadProductData() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                allProductList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product item = data.getValue(Product.class);
                    if (item != null) {
                        item.setProductId(data.getKey());
                        productList.add(item);
                        allProductList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        Product product = new Product();
        product.setVariants(new HashMap<>());
        showEditDialog(product);
    }

    private void showEditDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_furniture, null);
        builder.setView(view);

        EditText edtName = view.findViewById(R.id.edt_name);
        EditText edtDescription = view.findViewById(R.id.edt_description);
        EditText edtPrice = view.findViewById(R.id.edt_price);
        Spinner spinnerType = view.findViewById(R.id.spinner_type);
        ImageView imgProduct = view.findViewById(R.id.img_selected);
        LinearLayout layoutVariants = view.findViewById(R.id.layout_variants_container);
        Button btnAddVariant = view.findViewById(R.id.btn_add_variant);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, Arrays.copyOfRange(types, 1, types.length));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        if (product.getName() != null) {
            edtName.setText(product.getName());
            edtDescription.setText(product.getDescription());
            edtPrice.setText(String.valueOf(product.getPrice()));
            selectedProductImageUri = Uri.parse(product.getImageUrl());
            Glide.with(getContext()).load(product.getImageUrl()).into(imgProduct);
            int index = Arrays.asList(types).indexOf(mapCategoryIdToType(product.getCategoryId()));
            spinnerType.setSelection(Math.max(0, index - 1));

            // Hiển thị biến thể nếu có
            Map<String, Map<String, Variant>> variants = product.getVariants();
            if (variants != null) {
                for (String size : variants.keySet()) {
                    Map<String, Variant> colors = variants.get(size);
                    if (colors != null) {
                        for (String color : colors.keySet()) {
                            Variant v = colors.get(color);
                            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_variant_row, layoutVariants, false);
                            ((EditText) row.findViewById(R.id.edt_size)).setText(size);
                            ((EditText) row.findViewById(R.id.edt_color)).setText(color);
                            ((EditText) row.findViewById(R.id.edt_quantity)).setText(String.valueOf(v.getQuantity()));
                            ((EditText) row.findViewById(R.id.edt_variant_price)).setText(String.valueOf(v.getPrice()));
                            ImageView imgV = row.findViewById(R.id.img_variant);
                            Glide.with(getContext()).load(v.getImage()).into(imgV);
                            imgV.setTag(v.getImage());
                            imgV.setOnClickListener(v2 -> {
                                currentImageView = imgV;
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, PICK_IMAGE_VARIANT);
                            });
                            layoutVariants.addView(row);
                        }
                    }
                }
            }
        }

        imgProduct.setOnClickListener(v -> {
            currentImageView = imgProduct;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_PRODUCT);
        });

        btnAddVariant.setOnClickListener(v -> {
            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_variant_row, layoutVariants, false);
            ImageView imgVariant = row.findViewById(R.id.img_variant);
            imgVariant.setOnClickListener(v2 -> {
                currentImageView = imgVariant;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_VARIANT);
            });
            layoutVariants.addView(row);
        });

        builder.setTitle(product.getProductId() == null ? "Thêm sản phẩm" : "Sửa sản phẩm");
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            // Lưu logic giữ nguyên
        });
        builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showDeleteConfirmDialog(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá sản phẩm này không?")
                .setPositiveButton("Xoá", (dialog, which) -> productRef.child(product.getProductId()).removeValue())
                .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void showProductDetailsDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_furniture_detail, null);
        builder.setView(view);

        ImageView imgDetail = view.findViewById(R.id.img_detail);
        TextView tvName = view.findViewById(R.id.tv_detail_name);
        TextView tvType = view.findViewById(R.id.tv_detail_type);
        TextView tvPrice = view.findViewById(R.id.tv_detail_price);
        TextView tvRating = view.findViewById(R.id.tv_detail_rating);
        TextView tvDescription = view.findViewById(R.id.tv_detail_description);
        LinearLayout variantContainer = view.findViewById(R.id.variant_container);

        tvName.setText(product.getName());
        tvType.setText("Loại: " + mapCategoryIdToType(product.getCategoryId()));
        tvPrice.setText("Giá: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(product.getPrice()) + " ₫");
        tvDescription.setText(product.getDescription() != null ? product.getDescription() : "Không có mô tả");

        double avgRating = 0;
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            double total = 0;
            for (Review r : product.getReviews()) {
                total += r.getRating();
            }
            avgRating = total / product.getReviews().size();
        }
        tvRating.setText("Đánh giá: " + String.format(Locale.getDefault(), "%.1f sao", avgRating));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(getContext()).load(product.getImageUrl()).into(imgDetail);
        } else {
            imgDetail.setImageResource(R.drawable.ic_image_placeholder);
        }

        variantContainer.removeAllViews();
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (String color : product.getVariants().keySet()) {
                Map<String, Variant> sizeMap = product.getVariants().get(color);
                if (sizeMap != null) {
                    for (String size : sizeMap.keySet()) {
                        Variant v = sizeMap.get(size);
                        TextView tv = new TextView(getContext());
                        tv.setText("Màu: " + color + " - Size: " + size + " - SL: " + v.getQuantity() +
                                " - Giá: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(v.getPrice()) + " ₫");
                        tv.setPadding(0, 4, 0, 4);
                        variantContainer.addView(tv);
                    }
                }
            }
        } else {
            TextView tv = new TextView(getContext());
            tv.setText("Không có biến thể.");
            variantContainer.addView(tv);
        }

        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    private void filterByType(String type) {
        productList.clear();
        if (type.equals("Tất cả")) {
            productList.addAll(allProductList);
        } else {
            for (Product item : allProductList) {
                if (type.equalsIgnoreCase(mapCategoryIdToType(item.getCategoryId()))) {
                    productList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void filterByName(String keyword) {
        String selectedType = spinnerFilter.getSelectedItem().toString();
        List<Product> filtered = new ArrayList<>();
        for (Product item : allProductList) {
            boolean matchType = selectedType.equals("Tất cả") || selectedType.equalsIgnoreCase(mapCategoryIdToType(item.getCategoryId()));
            boolean matchName = item.getName() != null && item.getName().toLowerCase().contains(keyword.toLowerCase());
            if (matchType && matchName) {
                filtered.add(item);
            }
        }
        productList.clear();
        productList.addAll(filtered);
        adapter.notifyDataSetChanged();
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
