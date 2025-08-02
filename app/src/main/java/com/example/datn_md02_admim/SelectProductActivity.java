package com.example.datn_md02_admim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private List<Product> productList = new ArrayList<>();
    private List<String> selectedIds = new ArrayList<>();
    private SelectProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_product);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewSelect);
        btnConfirm = findViewById(R.id.btnConfirmSelect);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectProductAdapter(productList, selectedIds);
        recyclerView.setAdapter(adapter);

        btnConfirm.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putStringArrayListExtra("selected_ids", new ArrayList<>(selectedIds));
            setResult(RESULT_OK, data);
            finish();
        });

        loadProducts();
    }

    private void loadProducts() {
        FirebaseDatabase.getInstance().getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Product p = snap.getValue(Product.class);
                            if (p != null) productList.add(p);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }
}
