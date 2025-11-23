package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.model.Voucher;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private Context context;
    private List<Voucher> vouchers;
    private OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onCopyCodeClick(Voucher voucher);
    }

    public VoucherAdapter(Context context, List<Voucher> vouchers, OnVoucherClickListener listener) {
        this.context = context;
        this.vouchers = vouchers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = vouchers.get(position);
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        private TextView voucherTitle, voucherCode, voucherDescription, voucherExpiry;
        private MaterialButton copyCodeButton;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            voucherTitle = itemView.findViewById(R.id.voucherTitle);
            voucherCode = itemView.findViewById(R.id.voucherCode);
            voucherDescription = itemView.findViewById(R.id.voucherDescription);
            voucherExpiry = itemView.findViewById(R.id.voucherExpiry);
            copyCodeButton = itemView.findViewById(R.id.copyCodeButton);

            copyCodeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCopyCodeClick(vouchers.get(position));
                }
            });
        }

        public void bind(Voucher voucher) {
            voucherTitle.setText(voucher.getTitle());
            voucherCode.setText(voucher.getCode());
            voucherDescription.setText(voucher.getDescription());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            voucherExpiry.setText("HSD: " + sdf.format(voucher.getExpiryDate()));
        }
    }
}
