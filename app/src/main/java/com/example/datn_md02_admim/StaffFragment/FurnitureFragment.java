
// FurnitureFragment.java - Corrected version with safe image handling during edit

package com.example.datn_md02_admim.StaffFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
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
import com.google.firebase.storage.StorageReference;

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
    private String currentProductImageUrl; // to retain image if not updated
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    interface OnUploadCompleteListener {
        void onComplete(String imageUrl);
    }

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
            @Override public void onView(Product product) { }
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

        fabAdd.setOnClickListener(v -> showAddDialog());
        loadProductData();
        return view;
    }

    private void uploadImageToStorage(Uri uri, String fileName, boolean isMainImage, OnUploadCompleteListener listener) {
        if (uri == null) {
            listener.onComplete("");
            return;
        }
        StorageReference ref = storage.getReference().child("product_images/" + fileName + (isMainImage ? "_main" : "_variant") + ".jpg");
        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri1 -> listener.onComplete(uri1.toString())))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    listener.onComplete("");
                });
    }

    private void saveProductToFirebase(Product product, LinearLayout layoutVariants, Uri imageUri, boolean isEditMode) {
        Map<String, Map<String, Variant>> variantsMap = new HashMap<>();
        int variantCount = layoutVariants.getChildCount();
        if (variantCount == 0) {
            Toast.makeText(getContext(), "Cần ít nhất một biến thể", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < variantCount; i++) {
            View row = layoutVariants.getChildAt(i);
            String size = ((EditText) row.findViewById(R.id.edt_size)).getText().toString().trim();
            String color = ((EditText) row.findViewById(R.id.edt_color)).getText().toString().trim();
            int quantity = Integer.parseInt(((EditText) row.findViewById(R.id.edt_quantity)).getText().toString().trim());
            double variantPrice = Double.parseDouble(((EditText) row.findViewById(R.id.edt_variant_price)).getText().toString().trim());
            Object tag = ((ImageView) row.findViewById(R.id.img_variant)).getTag();
            Uri variantImageUri = tag instanceof Uri ? (Uri) tag : null;
            String existingVariantUrl = tag instanceof String ? (String) tag : null;

            String variantKey = UUID.randomUUID().toString();
            int finalI = i;
            uploadImageToStorage(variantImageUri, variantKey, false, variantImageUrl -> {
                Variant variant = new Variant(quantity, variantPrice,
                        variantImageUrl.isEmpty() ? existingVariantUrl : variantImageUrl);

                variantsMap.computeIfAbsent(size, k -> new HashMap<>()).put(color, variant);

                if (finalI == variantCount - 1) {
                    if (imageUri != null) {
                        uploadImageToStorage(imageUri, UUID.randomUUID().toString(), true, mainImageUrl -> {
                            product.setImageUrl(mainImageUrl);
                            product.setVariants(variantsMap);
                            saveOrUpdateProduct(product, isEditMode);
                        });
                    } else {
                        product.setImageUrl(currentProductImageUrl); // giữ ảnh cũ
                        product.setVariants(variantsMap);
                        saveOrUpdateProduct(product, isEditMode);
                    }
                }
            });
        }
    }

    private void saveOrUpdateProduct(Product product, boolean isEditMode) {
        if (isEditMode && product.getProductId() != null) {
            productRef.child(product.getProductId()).setValue(product);
        } else {
            String key = productRef.push().getKey();
            product.setProductId(key);
            productRef.child(key).setValue(product);
        }
        Toast.makeText(getContext(), "Đã lưu sản phẩm", Toast.LENGTH_SHORT).show();
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
            Glide.with(getContext()).load(product.getImageUrl()).into(imgProduct);
            currentProductImageUrl = product.getImageUrl();

            // Set selected spinner position
            int index = Arrays.asList(types).indexOf(mapCategoryIdToType(product.getCategoryId()));
            spinnerType.setSelection(Math.max(0, index - 1));

            // ⬇️ THÊM LẠI CÁC BIẾN THỂ
            Map<String, Map<String, Variant>> variants = product.getVariants();
            if (variants != null) {
                for (String size : variants.keySet()) {
                    Map<String, Variant> colorMap = variants.get(size);
                    if (colorMap != null) {
                        for (String color : colorMap.keySet()) {
                            Variant v = colorMap.get(color);
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
            product.setName(edtName.getText().toString().trim());
            product.setDescription(edtDescription.getText().toString().trim());
            product.setPrice(Double.parseDouble(edtPrice.getText().toString().trim()));
            product.setCategoryId(spinnerType.getSelectedItem().toString().toLowerCase());
            saveProductToFirebase(product, layoutVariants, selectedProductImageUri, product.getProductId() != null);
        });

        builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (currentImageView != null && uri != null) {
                currentImageView.setImageURI(uri);
                currentImageView.setTag(uri);
                if (currentImageView.getId() == R.id.img_selected) {
                    selectedProductImageUri = uri;
                }
            }
        }
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
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddDialog() {
        Product product = new Product();
        product.setVariants(new HashMap<>());
        showEditDialog(product);
    }

    private void showDeleteConfirmDialog(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá sản phẩm này không?")
                .setPositiveButton("Xoá", (dialog, which) -> productRef.child(product.getProductId()).removeValue())
                .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void filterByType(String type) {
        productList.clear();
        if (type.equals("Tất cả")) {
            productList.addAll(allProductList);
        } else {
            for (Product item : allProductList) {
                if (type.equalsIgnoreCase(item.getCategoryId())) {
                    productList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    private String mapCategoryIdToType(String categoryId) {
        if (categoryId == null) return "Khác";
        switch (categoryId.toLowerCase()) {
            case "ban": return "Bàn";
            case "ghe": return "Ghế";
            case "tu": return "Tủ";
            case "giuong": return "Giường";
            case "ke": return "Kệ";
            default: return "Khác";
        }
    }
}
