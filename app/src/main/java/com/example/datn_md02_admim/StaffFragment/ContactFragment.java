package com.example.datn_md02_admim.StaffFragment;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactFragment extends Fragment {

    public interface OnUnreadCountChangeListener {
        void onUnreadCountChange(int count);
    }

    private OnUnreadCountChangeListener unreadListener;

    private RecyclerView recyclerView;
    private ChatStaffAdapter adapter;
    private final List<ChatStaff> staffList = new ArrayList<>();

    private DatabaseReference usersRef, chatsRef;
    private ValueEventListener usersListener, chatsListener;
    private DataSnapshot lastChatsSnap;
    private String currentEmail;

    public ContactFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnUnreadCountChangeListener) {
            unreadListener = (OnUnreadCountChangeListener) context;
        }
    }

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

                // Khi click vào thì đánh dấu đã đọc
                markMessagesAsSeen(staff.getEmail());
            }
        });
        recyclerView.setAdapter(adapter);

        currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        listenUserListRealtime();
        listenChatsRealtime();

        return view;
    }

    private void listenUserListRealtime() {
        if (usersListener != null) usersRef.removeEventListener(usersListener);

        usersListener = usersRef.orderByChild("role").equalTo("user")
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        staffList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String email    = safe(child.child("email").getValue(String.class));
                            String fullName = safe(child.child("fullName").getValue(String.class));
                            String avatar   = safe(child.child("avatar").getValue(String.class));

                            if (email.isEmpty()) continue;

                            ChatStaff s = new ChatStaff();
                            s.setEmail(email);
                            s.setFullName(fullName);
                            s.setAvatar(avatar);
                            s.setOnline(false);
                            s.setLastMessageText("Chưa có tin nhắn");
                            s.setLastMessageTimestamp(0);
                            s.setUnread(false);
                            staffList.add(s);

                            String key = sanitizeEmail(email);
                            FirebaseDatabase.getInstance()
                                    .getReference("status")
                                    .child(key)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override public void onDataChange(@NonNull DataSnapshot snap) {
                                            Boolean on = snap.getValue(Boolean.class);
                                            s.setOnline(on != null && on);
                                            adapter.notifyDataSetChanged();
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }

                        if (lastChatsSnap != null) {
                            applyChatsToList(lastChatsSnap);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi tải danh sách", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void listenChatsRealtime() {
        if (chatsListener != null) chatsRef.removeEventListener(chatsListener);

        chatsListener = chatsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                lastChatsSnap = snapshot;
                applyChatsToList(snapshot);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyChatsToList(DataSnapshot snap) {
        int totalUnread = 0;

        for (ChatStaff s : staffList) {
            long lastTs = 0;
            String lastText = null;
            boolean unread = false;

            for (DataSnapshot c : snap.getChildren()) {
                String sender = safe(c.child("sender").getValue(String.class));
                String receiver = safe(c.child("receiver").getValue(String.class));
                String content = safe(c.child("content").getValue(String.class));
                Boolean image = c.child("image").getValue(Boolean.class);
                Long ts = c.child("timestamp").getValue(Long.class);
                Boolean seen = c.child("seen").getValue(Boolean.class);

                if (ts == null) continue;

                boolean between =
                        (sender.equalsIgnoreCase(s.getEmail()) && receiver.equalsIgnoreCase(currentEmail)) ||
                                (receiver.equalsIgnoreCase(s.getEmail()) && sender.equalsIgnoreCase(currentEmail));
                if (!between) continue;

                if (ts > lastTs) {
                    lastTs = ts;
                    boolean isImage = image != null && image;
                    lastText = isImage ? "[Hình ảnh]" : content;
                }

                if (sender.equalsIgnoreCase(s.getEmail()) && !Boolean.TRUE.equals(seen)) {
                    unread = true;
                }
            }

            if (unread) totalUnread++;

            s.setLastMessageTimestamp(lastTs);
            s.setLastMessageText(lastText != null ? lastText : "Chưa có tin nhắn");
            s.setUnread(unread);
        }

        if (unreadListener != null) {
            unreadListener.onUnreadCountChange(totalUnread);
        }

        Collections.sort(staffList, (a, b) ->
                Long.compare(b.getLastMessageTimestamp(), a.getLastMessageTimestamp()));
        adapter.notifyDataSetChanged();
    }

    private void markMessagesAsSeen(String userEmail) {
        chatsRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot c : snapshot.getChildren()) {
                String sender = safe(c.child("sender").getValue(String.class));
                String receiver = safe(c.child("receiver").getValue(String.class));
                if (sender.equalsIgnoreCase(userEmail) && receiver.equalsIgnoreCase(currentEmail)) {
                    c.getRef().child("seen").setValue(true);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usersListener != null) usersRef.removeEventListener(usersListener);
        if (chatsListener != null) chatsRef.removeEventListener(chatsListener);
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private String sanitizeEmail(String email) {
        return email == null ? "" : email.replaceAll("[.#\\$\\[\\]]", ",");
    }
}
