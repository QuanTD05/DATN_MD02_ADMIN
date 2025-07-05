package com.example.datn_md02_admim.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.R;
import com.example.datn_md02_admim.Model.User;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {
    private Context context;
    private ArrayList<User> userList;

    public StaffAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.staff_item, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        User user = userList.get(position);
        holder.staffName.setText(user.getFullName());
        holder.staffEmail.setText(user.getEmail());

        holder.menuButton.setOnClickListener(v -> {
            String[] options = {"Sửa", "Xoá"};
            new AlertDialog.Builder(context)
                    .setTitle("Tuỳ chọn")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) editUser(user);
                        else deleteUser(user);
                    }).show();
        });
    }

    private void editUser(User user) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_user_form, null);
        EditText inputName = view.findViewById(R.id.inputName);
        EditText inputEmail = view.findViewById(R.id.inputEmail);
        EditText inputPassword = view.findViewById(R.id.inputPassword);
        EditText inputPhone = view.findViewById(R.id.inputPhone);
        EditText inputRole = view.findViewById(R.id.inputRole);

        inputName.setText(user.getFullName());
        inputEmail.setText(user.getEmail());
        inputPassword.setText(user.getPassword());
        inputPhone.setText(user.getPhone());
        inputRole.setText(user.getRole());

        new AlertDialog.Builder(context)
                .setTitle("Sửa nhân viên")
                .setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    user.setFullName(inputName.getText().toString());
                    user.setEmail(inputEmail.getText().toString());
                    user.setPassword(inputPassword.getText().toString());
                    user.setPhone(inputPhone.getText().toString());
                    user.setRole(inputRole.getText().toString());

                    FirebaseDatabase.getInstance().getReference("users")
                            .child(user.getUser_id()).setValue(user);
                    Toast.makeText(context, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteUser(User user) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUser_id()).removeValue();
        Toast.makeText(context, "Đã xoá", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView staffName, staffEmail;
        ImageButton menuButton;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            staffName = itemView.findViewById(R.id.staffName);
            staffEmail = itemView.findViewById(R.id.staffEmail);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}