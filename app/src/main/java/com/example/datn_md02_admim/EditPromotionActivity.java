
package com.example.datn_md02_admim;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.datn_md02_admim.Model.Promotion;
import com.google.firebase.database.*;

import java.util.*;

public class EditPromotionActivity extends AppCompatActivity {

    EditText edtCode, edtDesc, edtDiscount, edtStartDate, edtEndDate;
    Switch switchActive, switchApplyAll;
    Button btnUpdate, btnDelete, btnSelectProducts;
    List<String> selectedProductIds = new ArrayList<>();
    String promoCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_promotion);

        edtCode = findViewById(R.id.edtCode);
        edtDesc = findViewById(R.id.edtDesc);
        edtDiscount = findViewById(R.id.edtDiscount);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        switchActive = findViewById(R.id.switchActive);
        switchApplyAll = findViewById(R.id.switchApplyAll);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnSelectProducts = findViewById(R.id.btnSelectProducts);

        edtStartDate.setOnClickListener(v -> showDatePicker(edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePicker(edtEndDate));

        promoCode = getIntent().getStringExtra("promo_code");
        loadPromotion(promoCode);

        btnSelectProducts.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectProductActivity.class);
            startActivityForResult(i, 101);
        });

        btnUpdate.setOnClickListener(v -> updatePromotion());
        btnDelete.setOnClickListener(v -> deletePromotion());
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

    private void loadPromotion(String code) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("promotions").child(code);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Promotion p = snapshot.getValue(Promotion.class);
                if (p != null) {
                    edtCode.setText(p.code);
                    edtCode.setEnabled(false);
                    edtDesc.setText(p.description);
                    edtDiscount.setText(String.valueOf(p.discount));
                    edtStartDate.setText(p.start_date);
                    edtEndDate.setText(p.end_date);
                    switchActive.setChecked(p.is_active);
                    switchApplyAll.setChecked(p.apply_to_all);
                    if (p.apply_to_product_ids != null)
                        selectedProductIds = p.apply_to_product_ids;
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void updatePromotion() {
        String desc = edtDesc.getText().toString().trim();
        double discount = Double.parseDouble(edtDiscount.getText().toString());
        String start = edtStartDate.getText().toString().trim();
        String end = edtEndDate.getText().toString().trim();
        boolean isActive = switchActive.isChecked();
        boolean applyAll = switchApplyAll.isChecked();

        Promotion p = new Promotion();
        p.code = promoCode;
        p.description = desc;
        p.discount = discount;
        p.start_date = start;
        p.end_date = end;
        p.is_active = isActive;
        p.apply_to_all = applyAll;
        if (!applyAll) p.apply_to_product_ids = selectedProductIds;

        FirebaseDatabase.getInstance().getReference("promotions")
                .child(promoCode).setValue(p)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void deletePromotion() {
        FirebaseDatabase.getInstance().getReference("promotions")
                .child(promoCode).removeValue()
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Đã xoá", Toast.LENGTH_SHORT).show();
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
