package com.example.datn_md02_admim;

import android.app.AlertDialog;
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
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddPromotionActivity extends AppCompatActivity {

    EditText edtCode, edtDesc, edtDiscount, edtStartDate, edtEndDate;
    Switch switchActive, switchApplyAll;
    Button btnSave, btnSelectProducts;

    List<String> selectedProductIds = new ArrayList<>();
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private boolean isSaving = false;

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

        SDF.setLenient(false);

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

        btnSave.setOnClickListener(v -> validateAndSavePromotion());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // Hiện nút back
        getSupportActionBar().setTitle("Thêm ưu đãi");

        toolbar.setNavigationOnClickListener(v -> finish()); // Quay lại khi bấm
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // đảm bảo format yyyy-MM-dd với padding
            String mm = String.format(Locale.US, "%02d", month + 1);
            String dd = String.format(Locale.US, "%02d", dayOfMonth);
            String date = year + "-" + mm + "-" + dd;
            target.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void validateAndSavePromotion() {
        if (isSaving) return; // tránh bấm liên tiếp
        // reset lỗi
        edtCode.setError(null);
        edtDiscount.setError(null);
        edtStartDate.setError(null);
        edtEndDate.setError(null);

        String code = edtCode.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String discountStr = edtDiscount.getText().toString().trim();
        String start = edtStartDate.getText().toString().trim();
        String end = edtEndDate.getText().toString().trim();
        boolean isActive = switchActive.isChecked();
        boolean applyAll = switchApplyAll.isChecked();

        boolean valid = true;

        // code
        if (code.isEmpty()) {
            edtCode.setError("Mã khuyến mãi không được để trống");
            valid = false;
        } else if (!code.matches("^[A-Za-z0-9_-]{1,30}$")) {
            edtCode.setError("Mã chỉ gồm chữ/số/_/- (tối đa 30 ký tự)");
            valid = false;
        }

        // discount
        double discount = 0;
        if (discountStr.isEmpty()) {
            edtDiscount.setError("Phần trăm giảm giá không được để trống");
            valid = false;
        } else {
            try {
                discount = Double.parseDouble(discountStr);
                if (discount < 0 || discount > 100) {
                    edtDiscount.setError("Giảm giá phải trong khoảng 0 đến 100");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                edtDiscount.setError("Giảm giá phải là số hợp lệ");
                valid = false;
            }
        }

        // dates
        Date startDate = null, endDate = null;
        if (start.isEmpty()) {
            edtStartDate.setError("Chọn ngày bắt đầu");
            valid = false;
        } else {
            try {
                startDate = SDF.parse(start);
            } catch (ParseException e) {
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
            } catch (ParseException e) {
                edtEndDate.setError("Định dạng ngày kết thúc phải là yyyy-MM-dd");
                valid = false;
            }
        }

        if (startDate != null && endDate != null) {
            if (endDate.before(startDate)) {
                edtEndDate.setError("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
                valid = false;
            }
        }

        if (!applyAll) {
            if (selectedProductIds == null || selectedProductIds.isEmpty()) {
                Toast.makeText(this, "Phải chọn ít nhất 1 sản phẩm nếu không áp dụng cho tất cả", Toast.LENGTH_LONG).show();
                valid = false;
            }
        }

        if (!valid) return;

        // chuẩn bị object
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
        } else {
            p.apply_to_product_ids = null; // hoặc để trống tuỳ model
        }

        // kiểm tra tồn tại mã trước
        DatabaseReference promoRef = FirebaseDatabase.getInstance().getReference("promotions").child(code);
        isSaving = true;
        btnSave.setEnabled(false);
        promoRef.get().addOnCompleteListener(checkTask -> {
            if (!checkTask.isSuccessful()) {
                Toast.makeText(this, "Lỗi kiểm tra tồn tại: " + Objects.requireNonNull(checkTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
                resetSavingState();
                return;
            }

            boolean exists = checkTask.getResult().exists();
            if (exists) {
                new AlertDialog.Builder(this)
                        .setTitle("Mã đã tồn tại")
                        .setMessage("Mã này đã có. Bạn có muốn ghi đè lên khuyến mãi cũ?")
                        .setPositiveButton("Ghi đè", (d, w) -> writePromotion(promoRef, p))
                        .setNegativeButton("Hủy", (d, w) -> resetSavingState())
                        .show();
            } else {
                writePromotion(promoRef, p);
            }
        });
    }

    private void writePromotion(DatabaseReference ref, Promotion p) {
        ref.setValue(p)
                .addOnCompleteListener(saveTask -> {
                    if (saveTask.isSuccessful()) {
                        Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Lưu thất bại: " + (saveTask.getException() != null ? saveTask.getException().getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                        resetSavingState();
                    }
                });
    }

    private void resetSavingState() {
        isSaving = false;
        btnSave.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> ids = data.getStringArrayListExtra("selected_ids");
            if (ids != null) selectedProductIds = ids;
        }
    }
}
