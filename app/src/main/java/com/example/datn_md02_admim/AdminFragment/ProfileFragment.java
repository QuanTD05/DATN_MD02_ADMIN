package com.example.datn_md02_admim.AdminFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.UUID;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone, tvPassword, tvGender, tvChangePhoto;
    private ImageView imgAvatar;
    private Uri imageUri;
    private String userId;

    private DatabaseReference userRef;
    private StorageReference storageRef;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil_admin, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvPassword = view.findViewById(R.id.tvPassword);
        tvGender = view.findViewById(R.id.tvGender);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvChangePhoto = view.findViewById(R.id.tvChangePhoto);

        // Khởi tạo Firebase
        userRef = FirebaseDatabase.getInstance().getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference("avatars");

        // Đăng ký nhận ảnh từ gallery
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        // Bo tròn ảnh mới từ URI
                        Glide.with(this)
                                .load(imageUri)
                                .circleCrop()
                                .into(imgAvatar);

                        uploadImageToFirebase(imageUri);
                    }
                }
        );

        // Bắt sự kiện đổi ảnh
        tvChangePhoto.setOnClickListener(v -> openGallery());

        loadUserInfo();

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri uri) {
        if (uri == null || userId == null) return;

        StorageReference fileRef = storageRef.child(userId + "_" + UUID.randomUUID() + ".jpg");
        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    // Lưu vào Firebase Database
                    userRef.child(userId).child("avatar").setValue(downloadUri.toString());

                    // Lưu vào SharedPreferences
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE).edit();
                    editor.putString("avatar", downloadUri.toString());
                    editor.apply();

                    Toast.makeText(getContext(), "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi upload ảnh", Toast.LENGTH_SHORT).show());
    }

    private void loadUserInfo() {
        SharedPreferences preferences = getActivity().getSharedPreferences("USER_PREF", Context.MODE_PRIVATE);

        String fullName = preferences.getString("fullName", "");
        String email = preferences.getString("email", "");
        String phone = preferences.getString("phone", "");
        String password = preferences.getString("password", "");
        String avatarUrl = preferences.getString("avatar", "");
        userId = preferences.getString("user_id", "");

        tvName.setText(fullName);
        tvEmail.setText(email);
        tvPhone.setText(phone);
        tvPassword.setText("**********");
        tvGender.setText("Nam");

        if (!avatarUrl.isEmpty()) {
            // Hiển thị ảnh bo tròn từ URL
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .into(imgAvatar);
        }
    }
}
