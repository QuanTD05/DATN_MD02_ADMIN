package com.example.datn_md02_admim.StaffFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.ChatStaffAdapter;
import com.example.datn_md02_admim.ChatActivity;
import com.example.datn_md02_admim.Model.ChatStaff;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatStaffAdapter adapter;
    private final List<ChatStaff> staffList = new ArrayList<>();

    private DatabaseReference usersRef;
    private ValueEventListener usersListener;

    public ContactFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_staff, container, false);

        recyclerView = view.findViewById(R.id.recycler_chat_user);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChatStaffAdapter(staffList, staff -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("receiver_email", staff.getEmail());
                intent.putExtra("receiver_name", staff.getFullName());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        listenUserListRealtime();

        return view;
    }

    private void listenUserListRealtime() {
        // Nếu đã có listener thì remove trước để tránh trùng
        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }

        // Lắng nghe realtime danh sách users có role = "user"
        usersListener = usersRef.orderByChild("role").equalTo("user")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        staffList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ChatStaff staff = child.getValue(ChatStaff.class);
                            if (staff != null) {
                                staffList.add(staff);
                            }
                        }
                        // Sắp xếp giảm dần theo lastMessageTimestamp
                        staffList.sort((s1, s2) -> Long.compare(s2.getLastMessageTimestamp(), s1.getLastMessageTimestamp()));
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi tải danh sách", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove listener khi view bị destroy để tránh memory leak
        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }
}
