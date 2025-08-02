package com.example.datn_md02_admim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.datn_md02_admim.AdminFragment.ContactFragment;
import com.example.datn_md02_admim.AdminFragment.HomeFragment;
import com.example.datn_md02_admim.AdminFragment.ProfileFragment;
import com.example.datn_md02_admim.AdminFragment.PromotionFragment;
import com.example.datn_md02_admim.AdminFragment.StaffFragment;
import com.example.datn_md02_admim.AdminFragment.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton btnOpenMenu;
    private BottomNavigationView bottomNav;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        drawerLayout = findViewById(R.id.drawer_layout);
        btnOpenMenu = findViewById(R.id.btn_open_menu);
        bottomNav = findViewById(R.id.bottom_navigation);
        navView = findViewById(R.id.nav_view);

        // Set Toolbar


        // Nút menu bên phải
        btnOpenMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Mặc định load HomeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content, new HomeFragment())
                    .commit();
        }

        // Xử lý bottom nav
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home_admin) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new HomeFragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_contact_admin) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new StaffFragment())
                        .commit();
                return true;
            } else if (id == R.id.nav_profile_admin) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new ProfileFragment())
                        .commit();
                return true;
            }
            return false;
        });

        // Xử lý drawer menu
        navView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_users) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new UsersFragment())
                        .commit();
            } else if (id == R.id.nav_staff) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new StaffFragment())
                        .commit();
            } else if (id == R.id.nav_promotion) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new PromotionFragment())
                        .commit();
            } else if (id == R.id.nav_statistics) {
                // TODO: thêm Fragment nếu có
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        bottomNav.setSelectedItemId(R.id.bottom_navigation); // đảm bảo chọn tab đúng
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
