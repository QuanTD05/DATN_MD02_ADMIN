package com.example.datn_md02_admim;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.Model.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.UUID;

public class AddEditFurnitureActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgSelected;
    private Spinner spinnerType;
    private EditText edtName, edtDescription, edtPrice;
    private Button btnDone;

    private Uri imageUri;
    private boolean isEdit = false;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_furniture);

        imgSelected = findViewById(R.id.img_selected);
        spinnerType = findViewById(R.id.spinner_type);
        edtName = findViewById(R.id.edt_name);
        edtDescription = findViewById(R.id.edt_description);
        edtPrice = findViewById(R.id.edt_price);
        btnDone = findViewById(R.id.btn_done);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Bàn", "Ghế", "Tủ", "Giường", "Sofa"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        imgSelected.setOnClickListener(v -> chooseImage());

        if (getIntent().hasExtra("product")) {
            isEdit = true;
            product = (Product) getIntent().getSerializableExtra("product");
            populateFields(product);
        }

        btnDone.setOnClickListener(v -> {
            if (validateFields()) {
                if (isEdit) {
                    updateProduct();
                } else {
                    uploadNewProduct();
                }
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgSelected);
        }
    }

    private boolean validateFields() {
        if (edtName.getText().toString().trim().isEmpty() ||
                edtDescription.getText().toString().trim().isEmpty() ||
                edtPrice.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void populateFields(Product p) {
        edtName.setText(p.getName());
        edtDescription.setText(p.getDescription());
        edtPrice.setText(String.valueOf(p.getPrice()));

        Glide.with(this).load(p.getImageUrl()).into(imgSelected);
        imageUri = Uri.parse(p.getImageUrl());
    }

    private int getIndexFromType(String type) {
        for (int i = 0; i < spinnerType.getCount(); i++) {
            if (spinnerType.getItemAtPosition(i).toString().equalsIgnoreCase(type)) {
                return i;
            }
        }
        return 0;
    }

    private void uploadNewProduct() {
        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + UUID.randomUUID().toString());
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            Product newProduct = new Product();
            newProduct.setName(edtName.getText().toString().trim());
            newProduct.setDescription(edtDescription.getText().toString().trim());
            newProduct.setPrice(Double.parseDouble(edtPrice.getText().toString().trim()));

            newProduct.setImageUrl(uri.toString());

            DatabaseReference refDb = FirebaseDatabase.getInstance().getReference("product");
            String id = refDb.push().getKey();
            newProduct.setProductId(id);
            refDb.child(id).setValue(newProduct);

            Toast.makeText(this, "Đã thêm sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }));
    }

    private void updateProduct() {
        product.setName(edtName.getText().toString().trim());
        product.setDescription(edtDescription.getText().toString().trim());
        product.setPrice(Double.parseDouble(edtPrice.getText().toString().trim()));


        if (imageUri != null && !imageUri.toString().equals(product.getImageUrl())) {
            StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + UUID.randomUUID().toString());
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                product.setImageUrl(uri.toString());
                updateProductToFirebase();
            }));
        } else {
            updateProductToFirebase();
        }
    }

    private void updateProductToFirebase() {
        DatabaseReference refDb = FirebaseDatabase.getInstance().getReference("product");
        refDb.child(product.getProductId()).setValue(product);
        Toast.makeText(this, "Đã cập nhật sản phẩm", Toast.LENGTH_SHORT).show();
        finish();
    }
}
