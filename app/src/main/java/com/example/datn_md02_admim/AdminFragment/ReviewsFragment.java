package com.example.datn_md02_admim.AdminFragment;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.Adapter.BannerAdapter;
import com.example.datn_md02_admim.Model.Banner;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReviewsFragment extends Fragment {

    private ImageView ivPreview;
    private Button btnPickImage, btnAddBanner, btnUpdateBanner;
    private RecyclerView rvBanners;
    private ProgressBar progress;

    private BannerAdapter adapter;
    private List<Banner> bannerList = new ArrayList<>();

    private DatabaseReference bannersRef;
    private FirebaseStorage storage;

    private Uri pickedImageUri;
    private String editBannerId;
    private String oldImageUrl;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        ivPreview = view.findViewById(R.id.ivPreview);
        btnPickImage = view.findViewById(R.id.btnPickImage);
        btnAddBanner = view.findViewById(R.id.btnAddBanner);
        btnUpdateBanner = view.findViewById(R.id.btnUpdateBanner);
        rvBanners = view.findViewById(R.id.rvBanners);
        progress = view.findViewById(R.id.progress);

        bannersRef = FirebaseDatabase.getInstance().getReference("banners");
        storage = FirebaseStorage.getInstance();

        adapter = new BannerAdapter(bannerList, new BannerAdapter.OnBannerAction() {
            @Override
            public void onEdit(Banner banner) {
                editBannerId = banner.getId();
                oldImageUrl = banner.getImageUrl();
                btnAddBanner.setVisibility(View.GONE);
                btnUpdateBanner.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Chọn ảnh mới để cập nhật", Toast.LENGTH_SHORT).show();
                pickImageLauncher.launch("image/*");
            }

            @Override
            public void onDelete(Banner banner) {
                confirmDelete(banner);
            }
        });

        rvBanners.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBanners.setAdapter(adapter);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pickedImageUri = uri;
                        Glide.with(this).load(uri).into(ivPreview);
                    }
                }
        );

        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnAddBanner.setOnClickListener(v -> addBanner());
        btnUpdateBanner.setOnClickListener(v -> updateBanner());

        loadBanners();

        return view;
    }

    private void loadBanners() {
        bannersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bannerList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = child.getKey();
                    String url = child.child("imageUrl").getValue(String.class);
                    if (id != null && url != null) {
                        bannerList.add(new Banner(id, url));
                    }
                }
                adapter.setData(bannerList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void addBanner() {
        if (pickedImageUri == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        String fileName = "banners/" + System.currentTimeMillis() + "_" + UUID.randomUUID();
        StorageReference sRef = storage.getReference(fileName);

        sRef.putFile(pickedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return sRef.getDownloadUrl();
                })
                .addOnSuccessListener(url -> {
                    String key = bannersRef.push().getKey();
                    if (key != null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("imageUrl", url.toString());
                        bannersRef.child(key).setValue(data);
                        resetForm();
                    }
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Upload thất bại", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBanner() {
        if (TextUtils.isEmpty(editBannerId) || pickedImageUri == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ảnh mới!", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        if (!TextUtils.isEmpty(oldImageUrl)) {
            try {
                StorageReference oldRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
                oldRef.delete();
            } catch (Exception ignored) { }
        }

        String fileName = "banners/" + System.currentTimeMillis() + "_" + UUID.randomUUID();
        StorageReference sRef = storage.getReference(fileName);

        sRef.putFile(pickedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return sRef.getDownloadUrl();
                })
                .addOnSuccessListener(url -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("imageUrl", url.toString());
                    bannersRef.child(editBannerId).updateChildren(update);
                    resetForm();
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDelete(Banner banner) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa banner")
                .setMessage("Bạn có chắc muốn xóa banner này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteBanner(banner))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteBanner(Banner banner) {
        setLoading(true);
        try {
            StorageReference imgRef = FirebaseStorage.getInstance().getReferenceFromUrl(banner.getImageUrl());
            imgRef.delete();
        } catch (Exception ignored) { }
        bannersRef.child(banner.getId()).removeValue()
                .addOnCompleteListener(task -> setLoading(false));
    }

    private void resetForm() {
        pickedImageUri = null;
        editBannerId = null;
        oldImageUrl = null;
        ivPreview.setImageResource(android.R.color.darker_gray);
        btnAddBanner.setVisibility(View.VISIBLE);
        btnUpdateBanner.setVisibility(View.GONE);
    }

    private void setLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPickImage.setEnabled(!show);
        btnAddBanner.setEnabled(!show);
        btnUpdateBanner.setEnabled(!show);
    }
}
