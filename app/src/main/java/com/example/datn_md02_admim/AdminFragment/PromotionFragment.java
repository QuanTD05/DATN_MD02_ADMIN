package com.example.datn_md02_admim.AdminFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.datn_md02_admim.Adapter.PromotionAdapter;
import com.example.datn_md02_admim.AddPromotionActivity;
import com.example.datn_md02_admim.Model.Promotion;
import com.example.datn_md02_admim.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.*;

public class PromotionFragment extends Fragment {

    private Spinner spinnerFilter;
    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private List<Promotion> fullList = new ArrayList<>();
    private PromotionAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promotion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        recyclerView = view.findViewById(R.id.recyclerViewPromotion);
        btnAdd = view.findViewById(R.id.btnAddPromo);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PromotionAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AddPromotionActivity.class);
            startActivity(i);
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterData(spinnerFilter.getSelectedItem().toString());
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadPromotions();
    }

    private void loadPromotions() {
        FirebaseDatabase.getInstance().getReference("promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        fullList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Promotion p = snap.getValue(Promotion.class);
                            if (p != null) fullList.add(p);
                        }
                        filterData(spinnerFilter.getSelectedItem().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void filterData(String filter) {
        List<Promotion> filtered = new ArrayList<>();
        for (Promotion p : fullList) {
            if (filter.equals("Tất cả") || (filter.equals("Đang hoạt động") && p.is_active)
                    || (filter.equals("Đã tắt") && !p.is_active)) {
                filtered.add(p);
            }
        }
        adapter = new PromotionAdapter(filtered, getContext());
        recyclerView.setAdapter(adapter);
    }
}
