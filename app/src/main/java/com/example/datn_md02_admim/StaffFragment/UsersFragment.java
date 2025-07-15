package com.example.datn_md02_admim.StaffFragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.UserAdapter;
import com.example.datn_md02_admim.Model.User;
import com.example.datn_md02_admim.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class UsersFragment extends Fragment {

    private RecyclerView staffRecyclerView;
    private EditText searchEmail;
    private FloatingActionButton btnAddStaff;
    private ArrayList<User> userList, filteredList;
    private UserAdapter adapter;
    private DatabaseReference userRef;
    private FirebaseAuth auth;

    public UsersFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        staffRecyclerView = view.findViewById(R.id.staffRecyclerView);
        searchEmail = view.findViewById(R.id.searchEmail);
        btnAddStaff = view.findViewById(R.id.btnAddStaff);

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UserAdapter(getContext(), filteredList);

        staffRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        staffRecyclerView.setAdapter(adapter);

        userRef = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();

        loadData();

        btnAddStaff.setOnClickListener(v -> showAddDialog());

        searchEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEmail(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && "user".equalsIgnoreCase(user.getRole())) {
                        user.setUser_id(data.getKey());
                        userList.add(user);
                    }
                }
                filterEmail(searchEmail.getText().toString());
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterEmail(String email) {
        filteredList.clear();
        for (User user : userList) {
            if (user.getEmail().toLowerCase().contains(email.toLowerCase())) {
                filteredList.add(user);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_user_form, null);
        EditText inputName = view.findViewById(R.id.inputName);
        EditText inputEmail = view.findViewById(R.id.inputEmail);
        EditText inputPassword = view.findViewById(R.id.inputPassword);
        EditText inputPhone = view.findViewById(R.id.inputPhone);
        EditText inputRole = view.findViewById(R.id.inputRole);

        inputRole.setText("user");
        inputRole.setVisibility(View.GONE);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm khách hàng")
                .setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String email = inputEmail.getText().toString().trim();
                    String password = inputPassword.getText().toString().trim();
                    String name = inputName.getText().toString().trim();
                    String phone = inputPhone.getText().toString().trim();

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    String uid = firebaseUser.getUid();

                                    User user = new User(uid, name, email, password, phone, "user");
                                    userRef.child(uid).setValue(user);

                                    auth.signOut(); // đăng xuất tài khoản vừa tạo
                                    Toast.makeText(getContext(), "Đã tạo tài khoản khách hàng", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
