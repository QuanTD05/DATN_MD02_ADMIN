package com.example.datn_md02_admim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.AdminFragment.HomeFragment;
import com.example.datn_md02_admim.AdminFragment.ProfileFragment;
import com.example.datn_md02_admim.AdminFragment.PromotionFragment;
import com.example.datn_md02_admim.AdminFragment.ReviewsFragment;
import com.example.datn_md02_admim.AdminFragment.StaffFragment;
import com.example.datn_md02_admim.AdminFragment.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        // Hiển thị thông tin user ở header
        setupNavHeader();

        btnOpenMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content, new HomeFragment())
                    .commit();
        }

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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_content, new ReviewsFragment())
                        .commit();
            } else if (id == R.id.nav_logout) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_home_admin);
    }

    private void setupNavHeader() {
        View headerView = navView.getHeaderView(0);
        ImageView imgAvatar = headerView.findViewById(R.id.imgUserAvatar);
        TextView tvName = headerView.findViewById(R.id.tvUserName);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            tvName.setText("Chưa đăng nhập");
            imgAvatar.setImageResource(R.drawable.logo);
            return;
        }

        String email = currentUser.getEmail();
        if (email == null) {
            tvName.setText("Không có email");
            imgAvatar.setImageResource(R.drawable.logo);
            return;
        }

        // Lấy thông tin từ Realtime Database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                String name = userSnap.child("fullname").getValue(String.class);
                                String avatarUrl = userSnap.child("avatar").getValue(String.class);

                                tvName.setText(name != null && !name.isEmpty() ? name : email);

                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    Glide.with(AdminActivity.this)
                                            .load(avatarUrl)
                                            .placeholder(R.drawable.logo)
                                            .error(R.drawable.logo)
                                            .circleCrop()
                                            .into(imgAvatar);
                                } else {
                                    imgAvatar.setImageResource(R.drawable.logo);
                                }
                            }
                        } else {
                            // Nếu không tìm thấy user trong DB, fallback dùng FirebaseAuth
                            tvName.setText(currentUser.getDisplayName() != null ?
                                    currentUser.getDisplayName() : email);

                            if (currentUser.getPhotoUrl() != null) {
                                Glide.with(AdminActivity.this)
                                        .load(currentUser.getPhotoUrl())
                                        .placeholder(R.drawable.logo)
                                        .error(R.drawable.logo)
                                        .circleCrop()
                                        .into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.logo);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvName.setText(email);
                        imgAvatar.setImageResource(R.drawable.logo);
                    }
                });
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences.Editor editor = getSharedPreferences("USER_PREF", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
