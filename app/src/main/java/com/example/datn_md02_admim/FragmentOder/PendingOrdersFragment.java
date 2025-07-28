package com.example.datn_md02_admim.FragmentOder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.R;

public class PendingOrdersFragment extends Fragment {

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_orders, container, false);

        recyclerView = view.findViewById(R.id.recycler_orders);

        // Gắn LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Nếu có adapter, set luôn ở đây:
        // recyclerView.setAdapter(new YourAdapter(...));

        return view;
    }
}
