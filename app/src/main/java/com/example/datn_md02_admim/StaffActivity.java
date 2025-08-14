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
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StaffActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private BottomNavigationView bottomNav;
    private ImageButton btnOpenMenu;

    private BadgeDrawable messageBadge;
    private DatabaseReference chatsRef;
    private ValueEventListener chatsListener;

    private final String currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        checkNotificationPermission();

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        bottomNav = findViewById(R.id.bottom_navigation);
        btnOpenMenu = findViewById(R.id.btn_open_menu);

        // Badge hiển thị số tin nhắn chưa đọc trên icon Liên hệ
        messageBadge = bottomNav.getOrCreateBadge(R.id.nav_contact_staff);
        messageBadge.setBackgroundColor(getColor(R.color.pink));
        messageBadge.setBadgeTextColor(getColor(android.R.color.white));
        messageBadge.setVisible(false);

        // Lắng nghe tin nhắn chưa đọc từ users -> admin
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        chatsListener = chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String sender = child.child("sender").getValue(String.class);
                    String receiver = child.child("receiver").getValue(String.class);
                    Boolean seen = child.child("seen").getValue(Boolean.class);

                    if (sender != null && receiver != null
                            && receiver.equalsIgnoreCase(currentEmail)
                            && !Boolean.TRUE.equals(seen)) {
                        unreadCount++;
                    }
                }

                if (unreadCount > 0) {
                    messageBadge.setVisible(true);
                    messageBadge.setNumber(unreadCount);
                } else {
                    messageBadge.setVisible(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

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
                // Khi mở tab liên hệ, badge sẽ tắt ngay
                messageBadge.setVisible(false);
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
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
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
    protected void onDestroy() {
        super.onDestroy();
        if (chatsRef != null && chatsListener != null) {
            chatsRef.removeEventListener(chatsListener);
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
