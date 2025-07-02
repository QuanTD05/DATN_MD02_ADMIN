package com.example.datn_md02_admim.StaffFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.datn_md02_admim.R;

public class ProdcutFragment extends Fragment {
    public ProdcutFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_furniture, container, false);
    }
}
