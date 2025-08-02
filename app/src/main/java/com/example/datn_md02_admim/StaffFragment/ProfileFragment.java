package com.example.datn_md02_admim.StaffFragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone;
    private ImageView avatar;
    private LinearLayout rowName, rowPhone;

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    private ActivityResultLauncher<android.content.Intent> imagePickerLauncher;
    private AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_staff, container, false);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Bind views
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        avatar = view.findViewById(R.id.imgAvatar);
        rowName = view.findViewById(R.id.rowName);
        rowPhone = view.findViewById(R.id.rowPhone);

        if (firebaseUser == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
        } else {
            String uid = firebaseUser.getUid();
            tvEmail.setText(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "Chưa có email");

            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String currentName = snapshot.child("fullName").getValue(String.class);
                    String currentPhone = snapshot.child("phone").getValue(String.class);
                    String imageUrl = snapshot.child("avatar").getValue(String.class);

                    if (currentName != null) tvName.setText("Tên\n" + currentName);
                    if (currentPhone != null) tvPhone.setText("Số điện thoại\n" + currentPhone);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_user)
                                .circleCrop()
                                .into(avatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }

        rowName.setOnClickListener(v -> {
            if (firebaseUser == null || userRef == null) return;
            String current = tvName.getText().toString().replace("Tên\n", "");
            showEditDialog("Tên", current, "fullName", tvName, "Tên");
        });

        rowPhone.setOnClickListener(v -> {
            if (firebaseUser == null || userRef == null) return;
            String current = tvPhone.getText().toString().replace("Số điện thoại\n", "");
            showEditDialog("Số điện thoại", current, "phone", tvPhone, "Số điện thoại");
        });

        avatar.setOnClickListener(v -> {
            if (firebaseUser == null) {
                Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            openImagePicker();
        });

        setupImagePickerLauncher();
        return view;
    }

    private void showEditDialog(String title, String currentValue, String fieldKey,
                                @Nullable TextView targetView, @Nullable String labelPrefix) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chỉnh sửa " + title);

        final EditText input = new EditText(requireContext());
        if ("phone".equals(fieldKey)) {
            input.setInputType(InputType.TYPE_CLASS_PHONE);
        } else {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        input.setText(currentValue);
        input.setSelection(currentValue.length());
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        builder.setView(input);

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.setPositiveButton("Lưu", null); // sẽ override sau

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveBtn.setOnClickListener(v -> {
                String newValue = input.getText().toString().trim();
                if (newValue.isEmpty()) {
                    input.setError(title + " không được để trống");
                    return;
                }

                // Nếu không thay đổi thì báo
                if (newValue.equals(currentValue)) {
                    Toast.makeText(getContext(), "Không có thay đổi để lưu", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate theo field
                if ("fullName".equals(fieldKey)) {
                    if (!isValidName(newValue)) {
                        input.setError("Tên chỉ chứa chữ, dấu, và dài 2–50 ký tự");
                        return;
                    }
                } else if ("phone".equals(fieldKey)) {
                    if (!isValidPhone(newValue)) {
                        input.setError("Số điện thoại không hợp lệ (ví dụ: 0921234567)");
                        return;
                    }
                }

                // Cập nhật vào Firebase
                if (userRef != null) {
                    userRef.child(fieldKey).setValue(newValue)
                            .addOnSuccessListener(aVoid -> {
                                if (targetView != null && labelPrefix != null) {
                                    targetView.setText(labelPrefix + "\n" + newValue);
                                }
                                Toast.makeText(getContext(), title + " đã được cập nhật", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), "Không thể kết nối tới người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private boolean isValidName(String name) {
        // Cho phép chữ (unicode), dấu cách, '.', '-', '\'' và dài 2-50
        return name.matches("^[\\p{L} .'-]{2,50}$");
    }

    private boolean isValidPhone(String phone) {
        // Điện thoại Việt Nam bắt đầu 03,05,07,08,09 và tổng 10 chữ số
        return phone.matches("^(0(3|5|7|8|9)\\d{8})$");
    }

    private void openImagePicker() {
        android.content.Intent intent = new android.content.Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri == null) {
                            Toast.makeText(getContext(), "Không chọn được ảnh", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        uploadImageToFirebase(imageUri);
                    }
                }
        );
    }

    private void showLoading(String message) {
        if (loadingDialog == null) {
            AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
            b.setCancelable(false);
            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(50, 40, 50, 40);
            ProgressBar pb = new ProgressBar(requireContext());
            pb.setIndeterminate(true);
            TextView tv = new TextView(requireContext());
            tv.setText(" " + message);
            tv.setTextSize(16);
            layout.addView(pb);
            layout.addView(tv);
            b.setView(layout);
            loadingDialog = b.create();
        }
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (firebaseUser == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUri == null) {
            Toast.makeText(getContext(), "Ảnh không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Đang tải ảnh...");

        String uid = firebaseUser.getUid();
        StorageReference avatarRef = storageRef.child("avatars/" + uid + ".jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            if (userRef != null) {
                                userRef.child("avatar").setValue(downloadUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            Glide.with(requireContext())
                                                    .load(downloadUrl)
                                                    .placeholder(R.drawable.ic_user)
                                                    .circleCrop()
                                                    .into(avatar);
                                            Toast.makeText(getContext(), "Ảnh đại diện đã cập nhật", Toast.LENGTH_SHORT).show();
                                            hideLoading();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Lưu ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            hideLoading();
                                        });
                            } else {
                                Toast.makeText(getContext(), "Không có tham chiếu người dùng", Toast.LENGTH_SHORT).show();
                                hideLoading();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Lấy URL thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            hideLoading();
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Tải ảnh lên thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    hideLoading();
                });
    }
}
