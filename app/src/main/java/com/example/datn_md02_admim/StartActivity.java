package com.example.datn_md02_admim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_FIRST_RUN = "first_run";

    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);

        if (!isFirstRun) {
            // Nếu không phải lần đầu -> chuyển sang LoginActivity hoặc MainActivity
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Nếu là lần đầu, hiển thị giao diện Start
        setContentView(R.layout.activity_start);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            // Lưu trạng thái là đã chạy lần đầu
            prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();

            // Mở LoginActivity
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
        });
    }
}
