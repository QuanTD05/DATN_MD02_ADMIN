package com.example.datn_md02_admim;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.datn_md02_admim.StaffFragment.ContactFragment;
import com.example.datn_md02_admim.StaffFragment.HomeFragment;
import com.example.datn_md02_admim.StaffFragment.InventoryFragment;
import com.example.datn_md02_admim.StaffFragment.OrdersFragment;
import com.example.datn_md02_admim.StaffFragment.ProdcutFragment;
import com.example.datn_md02_admim.StaffFragment.ProfileFragment;
import com.example.datn_md02_admim.StaffFragment.ReviewsFragment;
import com.example.datn_md02_admim.StaffFragment.StatisticsFragment;
import com.example.datn_md02_admim.StaffFragment.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

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
                        .replace(R.id.main_content, new OrdersFragment()).commit();
            } else if (id == R.id.nav_users) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new UsersFragment()).commit();
            } else if (id == R.id.nav_furniture) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new ProdcutFragment()).commit();
            } else if (id == R.id.nav_reviews) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new ReviewsFragment()).commit();
            } else if (id == R.id.nav_statistics) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new StatisticsFragment()).commit();
            } else if (id == R.id.nav_warehouse) { // ✅ Mục mới
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new InventoryFragment()).commit();
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(StaffActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
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
                // Người dùng cho phép thông báo
            } else {
                // Người dùng từ chối
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
