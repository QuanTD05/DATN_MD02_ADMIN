package com.example.datn_md02_admim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.datn_md02_admim.Util.BgInit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pusher.pushnotifications.PushNotifications;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private RadioGroup roleGroup;
    private RadioButton rbAdmin, rbStaff;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("USER_PREF", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            String role = prefs.getString("role", "");
            navigateToRoleScreen(role);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        roleGroup = findViewById(R.id.roleGroup);
        rbAdmin = findViewById(R.id.rb_admin);
        rbStaff = findViewById(R.id.rb_staff);

        rbAdmin.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_tint));
        rbStaff.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_tint));

        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_admin) {
                rbAdmin.setTextColor(ContextCompat.getColor(this, R.color.green));
                rbStaff.setTextColor(ContextCompat.getColor(this, R.color.gray));
            } else {
                rbStaff.setTextColor(ContextCompat.getColor(this, R.color.green));
                rbAdmin.setTextColor(ContextCompat.getColor(this, R.color.gray));
            }
        });

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String pwd   = passwordEditText.getText().toString().trim();
        int checkedId = roleGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Vui lòng chọn vai trò", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedRole = ((RadioButton) findViewById(checkedId)).getTag().toString().toLowerCase();
        if (email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;
            String uid = user.getUid();

            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(LoginActivity.this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String role = snapshot.child("role").getValue(String.class);
                    if (role == null || !role.equalsIgnoreCase(selectedRole)) {
                        Toast.makeText(LoginActivity.this, "Vai trò không phù hợp", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SharedPreferences.Editor ed = getSharedPreferences("USER_PREF", MODE_PRIVATE).edit();
                    ed.putString("user_id", uid);
                    ed.putString("role", role);
                    ed.putBoolean("isLoggedIn", true);
                    ed.apply();

                    // Đăng ký interest Pusher Beams để nhận thông báo
                    PushNotifications.addDeviceInterest("user_" + uid);

                    navigateToRoleScreen(role);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void navigateToRoleScreen(String role) {
        BgInit.startAll(this);
        Intent it = "admin".equalsIgnoreCase(role)
                ? new Intent(this, AdminActivity.class)
                : new Intent(this, StaffActivity.class);
        startActivity(it); finish();
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;
        public SimpleTextWatcher(Runnable onChange) { this.onChange = onChange; }
        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) { onChange.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}