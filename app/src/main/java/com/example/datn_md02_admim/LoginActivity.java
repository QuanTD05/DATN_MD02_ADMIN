// LoginActivity.java (cập nhật: dùng FirebaseAuth + lấy dữ liệu người dùng để lưu vào SharedPreferences)
package com.example.datn_md02_admim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView txtSignup, txtForgot;
    private RadioGroup roleGroup;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Giao diện
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
                            String uid = user.getUid();
                            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String registeredRole = snapshot.child("role").getValue(String.class);
                                        if (registeredRole != null && registeredRole.equalsIgnoreCase(selectedRole)) {

                                            // Lưu vào SharedPreferences
                                            SharedPreferences.Editor editor = getSharedPreferences("USER_PREF", MODE_PRIVATE).edit();
                                            editor.putString("user_id", uid);
                                            editor.putString("fullName", snapshot.child("fullName").getValue(String.class));
                                            editor.putString("email", snapshot.child("email").getValue(String.class));
                                            editor.putString("phone", snapshot.child("phone").getValue(String.class));
                                            editor.putString("password", snapshot.child("password").getValue(String.class));
                                            editor.putString("role", registeredRole);
                                            editor.apply();

                                            navigateToRoleScreen(registeredRole);
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Vai trò không khớp với tài khoản đã đăng ký", Toast.LENGTH_SHORT).show();
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
