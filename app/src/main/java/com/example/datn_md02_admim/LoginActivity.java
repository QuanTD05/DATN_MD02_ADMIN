package com.example.datn_md02_admim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView txtSignup, txtForgot;
    private RadioGroup roleGroup;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_ROLE = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra người dùng đã đăng nhập chưa
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkSavedRoleAndNavigate(currentUser.getUid());
            return; // Dừng không cần setContentView
        }

        // Nếu chưa login
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        txtSignup = findViewById(R.id.txt_signup);
        txtForgot = findViewById(R.id.txtForgot);
        roleGroup = findViewById(R.id.roleGroup);

        loginButton.setOnClickListener(v -> loginUser());

        txtSignup.setOnClickListener(v ->
                Toast.makeText(this, "Liên hệ admin để tạo tài khoản", Toast.LENGTH_SHORT).show());

        txtForgot.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());
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
        String selectedRole = selectedRoleBtn.getTag() != null
                ? selectedRoleBtn.getTag().toString().toLowerCase()
                : selectedRoleBtn.getText().toString().toLowerCase();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            usersRef.child(user.getUid()).child("role")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                String registeredRole = snapshot.getValue(String.class);
                                                if (registeredRole != null && registeredRole.equalsIgnoreCase(selectedRole)) {
                                                    // Lưu role vào SharedPreferences
                                                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                                    prefs.edit().putString(KEY_ROLE, registeredRole).apply();

                                                    navigateToRoleScreen(registeredRole);
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Vai trò không khớp với tài khoản đã đăng ký", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Không tìm thấy vai trò người dùng", Toast.LENGTH_SHORT).show();
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

    private void checkSavedRoleAndNavigate(String uid) {
        usersRef.child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.getValue(String.class);
                            if (role != null) {
                                navigateToRoleScreen(role);
                            } else {
                                Toast.makeText(LoginActivity.this, "Không tìm thấy vai trò người dùng", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToRoleScreen(String role) {
        Intent intent;
        switch (role.toLowerCase()) {
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "staff":
                intent = new Intent(this, StaffActivity.class);
                break;
            default:
                Toast.makeText(this, "Quyền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
        }

        Toast.makeText(this, "Đăng nhập thành công với vai trò: " + role, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }
}
