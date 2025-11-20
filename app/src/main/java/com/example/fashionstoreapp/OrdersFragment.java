package com.example.fashionstoreapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OrdersFragment extends Fragment {

    private static final String ARG_IS_HISTORY = "is_history";
    private boolean isHistory;

    public static OrdersFragment newInstance(boolean isHistory) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_HISTORY, isHistory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHistory = getArguments().getBoolean(ARG_IS_HISTORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        TextView emptyText = view.findViewById(R.id.emptyText);
        if (isHistory) {
            emptyText.setText("Chưa có lịch sử mua hàng");
        } else {
            emptyText.setText("Bạn chưa có đơn hàng nào");
        }

        return view;
    }
}
