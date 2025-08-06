package com.example.datn_md02_admim;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.datn_md02_admim.StaffFragment.ContactFragment;
import com.example.datn_md02_admim.StaffFragment.FurnitureFragment;
import com.example.datn_md02_admim.StaffFragment.HomeFragment;
import com.example.datn_md02_admim.StaffFragment.ProfileFragment;
import com.example.datn_md02_admim.StaffFragment.ReviewsFragment;
import com.example.datn_md02_admim.StaffFragment.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class StaffActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private BottomNavigationView bottomNav;
    private ImageButton btnOpenMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        // ✅ Kiểm tra quyền thông báo
        checkNotificationPermission();

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        bottomNav = findViewById(R.id.bottom_navigation);
        btnOpenMenu = findViewById(R.id.btn_open_menu);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content, new HomeFragment()).commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home_staff) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new HomeFragment()).commit();
                return true;
            } else if (id == R.id.nav_contact_staff) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new ContactFragment()).commit();
                return true;
            } else if (id == R.id.nav_profile_staff) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new ProfileFragment()).commit();
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_home_staff);

        btnOpenMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_orders) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new HomeFragment()).commit();
            } else if (id == R.id.nav_users) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new UsersFragment()).commit();
            } else if (id == R.id.nav_furniture) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new FurnitureFragment()).commit();
            } else if (id == R.id.nav_reviews) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new ReviewsFragment()).commit();
            } else if (id == R.id.nav_logout) {
                logout(); // ✅ Gọi hàm logout mới
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void logout() {
        // 1. Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut();

        // 2. Xóa SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("USER_PREF", MODE_PRIVATE).edit();
        editor.clear(); // Xóa toàn bộ thông tin người dùng
        editor.apply();

        // 3. Quay về LoginActivity và xóa tất cả stack
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa backstack
        startActivity(intent);
        finish();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Cần quyền thông báo")
                            .setMessage("Ứng dụng cần quyền để hiển thị thông báo tin nhắn và đơn hàng.")
                            .setPositiveButton("Đồng ý", (dialog, which) -> {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                        REQUEST_NOTIFICATION_PERMISSION);
                            })
                            .setNegativeButton("Huỷ", null)
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_NOTIFICATION_PERMISSION);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Từ chối quyền thông báo", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
