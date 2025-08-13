// app/src/main/java/com/example/datn_md02_admim/AddPromotionActivity.java
package com.example.datn_md02_admim;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.SelectedProductAdapter;
import com.example.datn_md02_admim.Model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPromotionActivity extends AppCompatActivity {

    private static final int REQ_SELECT_PRODUCTS = 101;

    private EditText edtCode, edtDesc, edtDiscount, edtStartDate, edtEndDate;
    private Switch switchActive, switchApplyAll;
    private Button btnSelectProducts, btnSave;

    // Danh sách ID đã chọn và object để hiển thị
    private final ArrayList<String> selectedProductIds = new ArrayList<>();
    private final List<Product> selectedProductsList = new ArrayList<>();

    // RecyclerView hiển thị sản phẩm đã chọn
    private RecyclerView rvSelectedProducts;
    private SelectedProductAdapter selectedProductAdapter;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion);

        bindViews();
        setupDatePickers();
        setupSelectedProductsList();
        setupApplyAllSwitch();
        setupActions();
    }

    private void bindViews() {
        edtCode = findViewById(R.id.edtCode);
        edtDesc = findViewById(R.id.edtDesc);
        edtDiscount = findViewById(R.id.edtDiscount);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        switchActive = findViewById(R.id.switchActive);
        switchApplyAll = findViewById(R.id.switchApplyAll);
        btnSelectProducts = findViewById(R.id.btnSelectProducts);
        btnSave = findViewById(R.id.btnSave);
        rvSelectedProducts = findViewById(R.id.rvSelectedProducts);
    }

    private void setupDatePickers() {
        edtStartDate.setOnClickListener(v -> showCalendarDialog(edtStartDate));
        edtEndDate.setOnClickListener(v -> showCalendarDialog(edtEndDate));
    }

    private void showCalendarDialog(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(Calendar.YEAR, year);
                    picked.set(Calendar.MONTH, month);
                    picked.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    target.setText(sdf.format(picked.getTime()));
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dlg.show();
    }

    private void setupSelectedProductsList() {
        rvSelectedProducts.setLayoutManager(new LinearLayoutManager(this));
        selectedProductAdapter = new SelectedProductAdapter(selectedProductsList);
        rvSelectedProducts.setAdapter(selectedProductAdapter);
    }

    private void setupApplyAllSwitch() {
        // Bật "Áp dụng cho tất cả" -> disable nút chọn và mờ list
        switchApplyAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnSelectProducts.setEnabled(!isChecked);
            rvSelectedProducts.setAlpha(isChecked ? 0.4f : 1f);
            rvSelectedProducts.setClickable(!isChecked);
        });
    }

    private void setupActions() {
        btnSelectProducts.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectProductActivity.class);
            i.putStringArrayListExtra("pre_selected_ids", new ArrayList<>(selectedProductIds));
            startActivityForResult(i, REQ_SELECT_PRODUCTS);
        });

        btnSave.setOnClickListener(v -> savePromotion());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SELECT_PRODUCTS && resultCode == RESULT_OK && data != null) {
            ArrayList<String> ids = data.getStringArrayListExtra("selected_ids");
            selectedProductIds.clear();
            if (ids != null) selectedProductIds.addAll(ids);
            loadSelectedProductsForPreview();
        }
    }

    private void loadSelectedProductsForPreview() {
        selectedProductsList.clear();
        selectedProductAdapter.notifyDataSetChanged();
        if (selectedProductIds.isEmpty()) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("product");
        for (String id : selectedProductIds) {
            ref.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot snapshot) {
                    Product p = snapshot.getValue(Product.class);
                    if (p != null) {
                        selectedProductsList.add(p);
                        selectedProductAdapter.notifyItemInserted(selectedProductsList.size() - 1);
                    }
                }
                @Override public void onCancelled(DatabaseError error) { }
            });
        }
    }

    private void savePromotion() {
        String code = edtCode.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String discountStr = edtDiscount.getText().toString().trim();
        String startDate = edtStartDate.getText().toString().trim();
        String endDate = edtEndDate.getText().toString().trim();
        boolean isActive = switchActive.isChecked();
        boolean applyAll = switchApplyAll.isChecked();

        if (TextUtils.isEmpty(code)) { toast("Vui lòng nhập mã khuyến mãi"); return; }
        if (TextUtils.isEmpty(discountStr)) { toast("Vui lòng nhập phần trăm giảm"); return; }

        int discount;
        try {
            discount = (int) Math.round(Double.parseDouble(discountStr));
            if (discount < 0 || discount > 100) { toast("Phần trăm giảm phải 0–100"); return; }
        } catch (Exception e) { toast("Phần trăm giảm không hợp lệ"); return; }

        if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
            toast("Vui lòng chọn ngày bắt đầu/kết thúc");
            return;
        }
        if (!isStartBeforeOrEqual(startDate, endDate)) {
            toast("Ngày kết thúc phải ≥ ngày bắt đầu");
            return;
        }

        if (!applyAll && (selectedProductIds.isEmpty())) {
            toast("Hãy chọn ít nhất 1 sản phẩm hoặc bật 'Áp dụng cho tất cả'");
            return;
        }

        // Ghi promotions/{code}
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference promoRef = root.child("promotions").child(code);

        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("description", desc);
        data.put("discount", discount);                 // thống nhất field 'discount'
        data.put("start_date", startDate);
        data.put("end_date", endDate);
        data.put("is_active", isActive);
        data.put("apply_to_all", applyAll);
        data.put("apply_to_product_ids", applyAll ? null : new ArrayList<>(selectedProductIds));

        promoRef.setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Gửi thông báo chi tiết khuyến mãi
                sendPromotionNotification(
                        root, code, desc, discount, applyAll,
                        new ArrayList<>(selectedProductIds), startDate, endDate
                );
                toast("Lưu khuyến mãi thành công");
                finish();
            } else {
                toast("Lỗi lưu khuyến mãi: " +
                        (task.getException() != null ? task.getException().getMessage() : ""));
            }
        });
    }

    private boolean isStartBeforeOrEqual(String start, String end) {
        try {
            return !sdf.parse(end).before(sdf.parse(start));
        } catch (ParseException e) {
            return true;
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /** Gửi thông báo khuyến mãi có kèm phạm vi áp dụng & thời gian hiệu lực */
    private void sendPromotionNotification(
            DatabaseReference root,
            String code,
            String desc,
            int discount,
            boolean applyAll,
            List<String> productIds,
            String startDate,
            String endDate
    ) {
        if (applyAll) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("title", "🎁 Khuyến mãi mới!");
            notif.put("message",
                    "Mã: " + code +
                            " - " + desc +
                            " (" + discount + "%)\n" +
                            "Áp dụng: TẤT CẢ sản phẩm\n" +
                            "Hiệu lực: " + startDate + " → " + endDate
            );
            notif.put("type", "promo");
            notif.put("timestamp", System.currentTimeMillis());

            // Field cấu trúc cho client hiển thị đẹp
            notif.put("code", code);
            notif.put("discount", discount);
            notif.put("apply_to_all", true);
            notif.put("apply_to_product_ids", null);
            notif.put("start_date", startDate);
            notif.put("end_date", endDate);
            notif.put("product_names", null);
            notif.put("product_names_text", "TẤT CẢ sản phẩm");

            root.child("notifications").push().setValue(notif);
            return;
        }

        if (productIds == null || productIds.isEmpty()) productIds = Collections.emptyList();
        final List<String> finalProductIds = new ArrayList<>(productIds); // ✅ biến final để dùng trong inner class
        final List<String> names = new ArrayList<>();
        final int total = finalProductIds.size();

        if (total == 0) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("title", "🎁 Khuyến mãi mới!");
            notif.put("message",
                    "Mã: " + code +
                            " - " + desc +
                            " (" + discount + "%)\n" +
                            "Áp dụng: Một số sản phẩm được chọn\n" +
                            "Hiệu lực: " + startDate + " → " + endDate
            );
            notif.put("type", "promo");
            notif.put("timestamp", System.currentTimeMillis());
            notif.put("code", code);
            notif.put("discount", discount);
            notif.put("apply_to_all", false);
            notif.put("apply_to_product_ids", finalProductIds);
            notif.put("start_date", startDate);
            notif.put("end_date", endDate);
            notif.put("product_names", null);
            notif.put("product_names_text", "Một số sản phẩm");
            root.child("notifications").push().setValue(notif);
            return;
        }

        DatabaseReference prodRef = FirebaseDatabase.getInstance().getReference("product");
        final int[] done = {0};

        for (String pid : finalProductIds) {
            prodRef.child(pid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    String name = snap.child("name").getValue(String.class);
                    names.add(name != null ? name : pid);

                    if (++done[0] == total) {
                        pushPromoNotif(root, code, desc, discount, finalProductIds, startDate, endDate, names);
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {
                    if (++done[0] == total) {
                        pushPromoNotif(root, code, desc, discount, finalProductIds, startDate, endDate, names);
                    }
                }
            });
        }
    }

    private void pushPromoNotif(
            DatabaseReference root, String code, String desc, int discount,
            List<String> productIds, String startDate, String endDate, List<String> names
    ) {
        String productNamesText;
        if (names == null || names.isEmpty()) {
            productNamesText = "Một số sản phẩm";
        } else if (names.size() <= 3) {
            productNamesText = String.join(", ", names);
        } else {
            productNamesText = String.join(", ", names.subList(0, 3)) + " +" + (names.size() - 3) + " sp";
        }

        Map<String, Object> notif = new HashMap<>();
        notif.put("title", "🎁 Khuyến mãi mới!");
        notif.put("message",
                "Mã: " + code +
                        " - " + desc +
                        " (" + discount + "%)\n" +
                        "Áp dụng: " + productNamesText + "\n" +
                        "Hiệu lực: " + startDate + " → " + endDate
        );
        notif.put("type", "promo");
        notif.put("timestamp", System.currentTimeMillis());

        // Field cấu trúc
        notif.put("code", code);
        notif.put("discount", discount);
        notif.put("apply_to_all", false);
        notif.put("apply_to_product_ids", productIds);
        notif.put("start_date", startDate);
        notif.put("end_date", endDate);
        notif.put("product_names", names);
        notif.put("product_names_text", productNamesText);

        root.child("notifications").push().setValue(notif);
    }
}
