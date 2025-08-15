package com.example.datn_md02_admim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.datn_md02_admim.Util.BgInit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView txtSignup, txtForgot;
    private RadioGroup roleGroup;
    private RadioButton rbAdmin, rbStaff;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Kiểm tra đã đăng nhập chưa
        SharedPreferences prefs = getSharedPreferences("USER_PREF", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String role = prefs.getString("role", "");
            navigateToRoleScreen(role);
            finish(); // Không quay lại màn đăng nhập
            return;
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        txtSignup = findViewById(R.id.txt_signup);
        txtForgot = findViewById(R.id.txtForgot);
        roleGroup = findViewById(R.id.roleGroup);
        rbAdmin = findViewById(R.id.rb_admin);
        rbStaff = findViewById(R.id.rb_staff);

        // Màu viền RadioButton
        rbAdmin.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_tint));
        rbStaff.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_tint));

        // Đổi màu chữ khi chọn vai trò
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_admin) {
                rbAdmin.setTextColor(ContextCompat.getColor(this, R.color.green));
                rbStaff.setTextColor(ContextCompat.getColor(this, R.color.gray));
            } else if (checkedId == R.id.rb_staff) {
                rbStaff.setTextColor(ContextCompat.getColor(this, R.color.green));
                rbAdmin.setTextColor(ContextCompat.getColor(this, R.color.gray));
            }
        });

        loginButton.setOnClickListener(v -> loginUser());

        txtSignup.setOnClickListener(v -> Toast.makeText(this, "Liên hệ admin để tạo tài khoản", Toast.LENGTH_SHORT).show());
        txtForgot.setOnClickListener(v -> Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());

        // Đổi màu text khi người dùng nhập
        emailEditText.addTextChangedListener(new SimpleTextWatcher(() ->
                emailEditText.setTextColor(ContextCompat.getColor(this, R.color.black))));

        passwordEditText.addTextChangedListener(new SimpleTextWatcher(() ->
                passwordEditText.setTextColor(ContextCompat.getColor(this, R.color.black))));
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        int selectedRoleId = roleGroup.getCheckedRadioButtonId();

        if (selectedRoleId == -1) {
            Toast.makeText(this, "Vui lòng chọn vai trò", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRoleBtn = findViewById(selectedRoleId);
        String selectedRole = selectedRoleBtn.getTag().toString().toLowerCase();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String registeredRole = snapshot.child("role").getValue(String.class);
                                        if (registeredRole != null && registeredRole.equalsIgnoreCase(selectedRole)) {
                                            SharedPreferences.Editor editor = getSharedPreferences("USER_PREF", MODE_PRIVATE).edit();
                                            editor.putString("user_id", uid);
                                            editor.putString("fullName", snapshot.child("fullName").getValue(String.class));
                                            editor.putString("email", snapshot.child("email").getValue(String.class));
                                            editor.putString("phone", snapshot.child("phone").getValue(String.class));
                                            editor.putString("password", snapshot.child("password").getValue(String.class));
                                            editor.putString("role", registeredRole);
                                            editor.putBoolean("isLoggedIn", true);
                                            editor.apply();

                                            // ✅ Cập nhật status online
                                            usersRef.child(uid).child("status").setValue(true);

                                            navigateToRoleScreen(registeredRole);
                                        }else {
                                            Toast.makeText(LoginActivity.this, "Vai trò không khớp với tài khoản", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(LoginActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToRoleScreen(String role) {
        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            BgInit.startAll(this);
            intent = new Intent(this, AdminActivity.class);
        } else if ("staff".equalsIgnoreCase(role)) {
            BgInit.startAll(this);
            intent = new Intent(this, StaffActivity.class);
        } else {
            Toast.makeText(this, "Quyền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đăng nhập thành công với vai trò: " + role, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;
        public SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChange.run();
        }
        @Override public void afterTextChanged(Editable s) {}
    }
}