package com.example.datn_md02_admim.AdminFragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.StaffAdapter;
import com.example.datn_md02_admim.Model.User;
import com.example.datn_md02_admim.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Locale;

public class StaffFragment extends Fragment {

    private RecyclerView staffRecyclerView;
    private EditText searchEmail;
    private FloatingActionButton btnAddStaff;
    private ArrayList<User> userList, filteredList;
    private StaffAdapter adapter;
    private DatabaseReference userRef;
    private FirebaseAuth auth;

    public StaffFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_staff, container, false);

        staffRecyclerView = view.findViewById(R.id.staffRecyclerView);
        searchEmail = view.findViewById(R.id.searchEmail);
        btnAddStaff = view.findViewById(R.id.btnAddStaff);

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new StaffAdapter(getContext(), filteredList);

        staffRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        staffRecyclerView.setAdapter(adapter);

        userRef = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();

        loadData();

        btnAddStaff.setOnClickListener(v -> showAddDialog());

        searchEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEmail(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && "staff".equalsIgnoreCase(user.getRole())) {
                        user.setUser_id(data.getKey());
                        userList.add(user);
                    }
                }
                filterEmail(searchEmail.getText().toString());
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Không tải được danh sách nhân viên: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterEmail(String email) {
        filteredList.clear();
        String query = email != null ? email.toLowerCase(Locale.ROOT) : "";
        for (User user : userList) {
            if (user.getEmail() != null && user.getEmail().toLowerCase(Locale.ROOT).contains(query)) {
                filteredList.add(user);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_user_form, null);
        EditText inputName = view.findViewById(R.id.inputName);
        EditText inputEmail = view.findViewById(R.id.inputEmail);
        EditText inputPassword = view.findViewById(R.id.inputPassword);
        EditText inputPhone = view.findViewById(R.id.inputPhone);
        EditText inputRole = view.findViewById(R.id.inputRole);
        EditText inputAdminPassword = new EditText(requireContext()); // thêm để re-authenticate admin

        inputRole.setText("staff");
        inputRole.setVisibility(View.GONE);

        // tạo container để chèn thêm field admin password dưới form nếu cần
        // giả sử layout dialog_user_form là LinearLayout, ta thêm phía dưới
        if (view instanceof ViewGroup) {
            inputAdminPassword.setHint("Mật khẩu admin để đăng nhập lại");
            inputAdminPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            // bọc riêng label nếu muốn
            ((ViewGroup) view).addView(inputAdminPassword);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Thêm nhân viên")
                .setView(view)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Thêm", null) // override sau
                .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addBtn.setOnClickListener(v -> {
                String name = inputName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString();
                String phone = inputPhone.getText().toString().trim();
                String adminPassword = inputAdminPassword.getText().toString();

                boolean valid = true;

                if (name.isEmpty()) {
                    inputName.setError("Tên không được để trống");
                    valid = false;
                }

                if (email.isEmpty()) {
                    inputEmail.setError("Email không được để trống");
                    valid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    inputEmail.setError("Email không hợp lệ");
                    valid = false;
                }

                if (password.isEmpty()) {
                    inputPassword.setError("Mật khẩu không được để trống");
                    valid = false;
                } else if (password.length() < 6) {
                    inputPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                    valid = false;
                }

                if (phone.isEmpty()) {
                    inputPhone.setError("Số điện thoại không được để trống");
                    valid = false;
                } else if (!isValidVietnamPhone(phone)) {
                    inputPhone.setError("Số điện thoại không hợp lệ");
                    valid = false;
                }

                if (adminPassword.isEmpty()) {
                    inputAdminPassword.setError("Cần nhập mật khẩu admin để đăng nhập lại");
                    valid = false;
                }

                if (!valid) return;

                FirebaseUser currentAdmin = auth.getCurrentUser();
                if (currentAdmin == null || currentAdmin.getEmail() == null) {
                    Toast.makeText(getContext(), "Không có admin đang đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }
                String adminEmail = currentAdmin.getEmail();

                addBtn.setEnabled(false);
                addBtn.setText("Đang tạo...");

                // Tạo tài khoản staff mới
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(createTask -> {
                            if (createTask.isSuccessful()) {
                                FirebaseUser newUser = auth.getCurrentUser(); // giờ đang là staff mới
                                if (newUser != null) {
                                    String uid = newUser.getUid();
                                    // Lưu user (không lưu mật khẩu thô)
                                    User user = new User(uid, name, email, "", phone, "staff");
                                    userRef.child(uid).setValue(user)
                                            .addOnCompleteListener(writeTask -> {
                                                if (writeTask.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Đã tạo nhân viên", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getContext(),
                                                            "Lưu thông tin nhân viên thất bại: " +
                                                                    (writeTask.getException() != null ? writeTask.getException().getMessage() : ""),
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                // Đăng xuất staff mới rồi đăng nhập lại admin
                                                auth.signOut();
                                                reauthenticateAdmin(adminEmail, adminPassword, dialog);
                                            });
                                } else {
                                    Toast.makeText(getContext(), "Lỗi nội bộ: không lấy được user mới", Toast.LENGTH_LONG).show();
                                    // Cố gắng đăng nhập lại admin
                                    auth.signOut();
                                    reauthenticateAdmin(adminEmail, adminPassword, dialog);
                                }
                            } else {
                                String err = createTask.getException() != null ?
                                        createTask.getException().getMessage() : "Lỗi tạo tài khoản";
                                Toast.makeText(getContext(), "Tạo tài khoản thất bại: " + err, Toast.LENGTH_LONG).show();
                                addBtn.setEnabled(true);
                                addBtn.setText("Thêm");
                            }
                        });
            });
        });

        dialog.show();
    }

    private void reauthenticateAdmin(String email, String password, AlertDialog parentDialog) {
        if (email == null || password == null) {
            Toast.makeText(getContext(), "Không thể đăng nhập lại admin tự động", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Quay lại tài khoản admin", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Không đăng nhập lại được admin. Vui lòng đăng nhập thủ công: " +
                                        (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                    }
                    parentDialog.dismiss();
                });
    }

    private boolean isValidVietnamPhone(String phone) {
        // Ví dụ: 10 chữ số, bắt đầu 03,05,07,08,09
        return phone.matches("^(0(3|5|7|8|9)\\d{8})$");
    }
}
