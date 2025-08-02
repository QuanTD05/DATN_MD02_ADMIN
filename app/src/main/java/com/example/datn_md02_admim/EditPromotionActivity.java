package com.example.datn_md02_admim;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.datn_md02_admim.Model.Promotion;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EditPromotionActivity extends AppCompatActivity {

    EditText edtCode, edtDesc, edtDiscount, edtStartDate, edtEndDate;
    Switch switchActive, switchApplyAll;
    Button btnUpdate, btnDelete, btnSelectProducts;
    List<String> selectedProductIds = new ArrayList<>();
    String promoCode;

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_promotion);

        SDF.setLenient(false);

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
        if (promoCode == null || promoCode.trim().isEmpty()) {
            Toast.makeText(this, "Mã khuyến mãi không hợp lệ", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        loadPromotion(promoCode);

        btnSelectProducts.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectProductActivity.class);
            startActivityForResult(i, 101);
        });

        btnUpdate.setOnClickListener(v -> updatePromotion());
        btnDelete.setOnClickListener(v -> confirmDeletePromotion());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // Hiện nút back
        getSupportActionBar().setTitle("Chỉnh sửa ưu đãi");
        toolbar.setNavigationOnClickListener(v -> finish()); // Quay lại khi bấm
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String mm = String.format(Locale.US, "%02d", month + 1);
            String dd = String.format(Locale.US, "%02d", dayOfMonth);
            String date = year + "-" + mm + "-" + dd;
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
                    switchActive.setChecked(Boolean.TRUE.equals(p.is_active));
                    switchApplyAll.setChecked(Boolean.TRUE.equals(p.apply_to_all));
                    if (p.apply_to_product_ids != null)
                        selectedProductIds = new ArrayList<>(p.apply_to_product_ids);
                } else {
                    Toast.makeText(EditPromotionActivity.this, "Không tìm thấy khuyến mãi", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditPromotionActivity.this,
                        "Tải khuyến mãi thất bại: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updatePromotion() {
        if (isProcessing) return;
        // reset errors
        edtDiscount.setError(null);
        edtStartDate.setError(null);
        edtEndDate.setError(null);

        String desc = edtDesc.getText().toString().trim();
        String discountStr = edtDiscount.getText().toString().trim();
        String start = edtStartDate.getText().toString().trim();
        String end = edtEndDate.getText().toString().trim();
        boolean isActive = switchActive.isChecked();
        boolean applyAll = switchApplyAll.isChecked();

        boolean valid = true;

        double discount = 0;
        if (discountStr.isEmpty()) {
            edtDiscount.setError("Phần trăm giảm giá không được để trống");
            valid = false;
        } else {
            try {
                discount = Double.parseDouble(discountStr);
                if (discount < 0 || discount > 100) {
                    edtDiscount.setError("Giảm giá phải nằm trong 0–100");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                edtDiscount.setError("Giảm giá không hợp lệ");
                valid = false;
            }
        }

        Date startDate = null, endDate = null;
        if (start.isEmpty()) {
            edtStartDate.setError("Chọn ngày bắt đầu");
            valid = false;
        } else {
            try {
                startDate = SDF.parse(start);
            } catch (Exception e) {
                edtStartDate.setError("Định dạng ngày bắt đầu phải là yyyy-MM-dd");
                valid = false;
            }
        }

        if (end.isEmpty()) {
            edtEndDate.setError("Chọn ngày kết thúc");
            valid = false;
        } else {
            try {
                endDate = SDF.parse(end);
            } catch (Exception e) {
                edtEndDate.setError("Định dạng ngày kết thúc phải là yyyy-MM-dd");
                valid = false;
            }
        }

        if (startDate != null && endDate != null && endDate.before(startDate)) {
            edtEndDate.setError("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
            valid = false;
        }

        if (!applyAll) {
            if (selectedProductIds == null || selectedProductIds.isEmpty()) {
                Toast.makeText(this, "Phải chọn ít nhất 1 sản phẩm nếu không áp dụng cho tất cả", Toast.LENGTH_LONG).show();
                valid = false;
            }
        }

        if (!valid) return;

        Promotion p = new Promotion();
        p.code = promoCode;
        p.description = desc;
        p.discount = discount;
        p.start_date = start;
        p.end_date = end;
        p.is_active = isActive;
        p.apply_to_all = applyAll;
        if (!applyAll) p.apply_to_product_ids = selectedProductIds;
        else p.apply_to_product_ids = null;

        isProcessing = true;
        btnUpdate.setEnabled(false);
        DatabaseReference promoRef = FirebaseDatabase.getInstance().getReference("promotions").child(promoCode);
        promoRef.setValue(p)
                .addOnCompleteListener(task -> {
                    isProcessing = false;
                    btnUpdate.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Cập nhật thất bại: " + (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void confirmDeletePromotion() {
        new AlertDialog.Builder(this)
                .setTitle("Xoá khuyến mãi")
                .setMessage("Bạn có chắc muốn xoá khuyến mãi này?")
                .setPositiveButton("Xoá", (dialog, which) -> deletePromotion())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deletePromotion() {
        if (isProcessing) return;
        isProcessing = true;
        btnDelete.setEnabled(false);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("promotions")
                .child(promoCode);
        ref.removeValue()
                .addOnCompleteListener(task -> {
                    isProcessing = false;
                    btnDelete.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đã xoá", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Xoá thất bại: " + (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            List<String> ids = data.getStringArrayListExtra("selected_ids");
            if (ids != null) selectedProductIds = ids;
        }
    }
}
