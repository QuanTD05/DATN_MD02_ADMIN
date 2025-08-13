// app/src/main/java/com/example/datn_md02_admim/SelectProductActivity.java
package com.example.datn_md02_admim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.SelectProductAdapter;
import com.example.datn_md02_admim.Model.Product;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SelectProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnConfirm;

    private final List<Product> productList = new ArrayList<>();
    private final ArrayList<String> selectedIds = new ArrayList<>();
    private SelectProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product);

        recyclerView = findViewById(R.id.recyclerViewSelect);
        btnConfirm = findViewById(R.id.btnConfirmSelect);

        // Nhận danh sách đã chọn từ trước (nếu có) để giữ trạng thái khi quay lại
        ArrayList<String> preSelected = getIntent().getStringArrayListExtra("pre_selected_ids");
        if (preSelected != null) selectedIds.addAll(preSelected);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectProductAdapter(productList, selectedIds);
        recyclerView.setAdapter(adapter);

        btnConfirm.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putStringArrayListExtra("selected_ids", selectedIds);
            setResult(RESULT_OK, data);
            finish();
        });

        loadProducts();
    }

    private void loadProducts() {
        // 🔧 lưu ý: node là "product" (số ít)
        FirebaseDatabase.getInstance().getReference("product")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Product p = s.getValue(Product.class);
                            if (p != null && p.getProductId() != null) {
                                productList.add(p);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(DatabaseError error) { }
                });
    }
}
