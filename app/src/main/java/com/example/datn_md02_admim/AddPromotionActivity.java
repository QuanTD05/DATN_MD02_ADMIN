package com.example.datn_md02_admim;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;


import com.example.datn_md02_admim.Model.Promotion;
import com.google.firebase.database.FirebaseDatabase;

import java.util.*;

public class AddPromotionActivity extends AppCompatActivity {

    EditText edtCode, edtDesc, edtDiscount, edtStartDate, edtEndDate;
    Switch switchActive, switchApplyAll;
    Button btnSave, btnSelectProducts;

    List<String> selectedProductIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_promotion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtCode = findViewById(R.id.edtCode);
        edtDesc = findViewById(R.id.edtDesc);
        edtDiscount = findViewById(R.id.edtDiscount);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        switchActive = findViewById(R.id.switchActive);
        switchApplyAll = findViewById(R.id.switchApplyAll);
        btnSave = findViewById(R.id.btnSave);
        btnSelectProducts = findViewById(R.id.btnSelectProducts);

        edtStartDate.setOnClickListener(v -> showDatePicker(edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePicker(edtEndDate));

        btnSelectProducts.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectProductActivity.class);
            startActivityForResult(i, 101);
        });

        btnSave.setOnClickListener(v -> savePromotion());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện nút back
        getSupportActionBar().setTitle("Thêm ưu đãi");

        toolbar.setNavigationOnClickListener(v -> finish()); // Quay lại khi bấm
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = year + "-" + (month + 1) + "-" + dayOfMonth;
            target.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void savePromotion() {
        String code = edtCode.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        double discount = Double.parseDouble(edtDiscount.getText().toString());
        String start = edtStartDate.getText().toString().trim();
        String end = edtEndDate.getText().toString().trim();
        boolean isActive = switchActive.isChecked();
        boolean applyAll = switchApplyAll.isChecked();

        Promotion p = new Promotion();
        p.code = code;
        p.description = desc;
        p.discount = discount;
        p.start_date = start;
        p.end_date = end;
        p.is_active = isActive;
        p.apply_to_all = applyAll;
        if (!applyAll) {
            p.apply_to_product_ids = selectedProductIds;
        }

        FirebaseDatabase.getInstance().getReference("promotions")
                .child(code).setValue(p)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            selectedProductIds = data.getStringArrayListExtra("selected_ids");
        }
    }
}
