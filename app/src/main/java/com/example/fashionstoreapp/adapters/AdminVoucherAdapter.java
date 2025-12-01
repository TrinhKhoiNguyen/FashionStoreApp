package com.example.fashionstoreapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Voucher;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminVoucherAdapter extends RecyclerView.Adapter<AdminVoucherAdapter.VoucherViewHolder> {

    private List<Voucher> vouchers;
    private List<Voucher> vouchersFiltered;
    private OnVoucherActionListener listener;

    public interface OnVoucherActionListener {
        void onEdit(Voucher voucher);

        void onDelete(Voucher voucher);

        void onToggleStatus(Voucher voucher);
    }

    public AdminVoucherAdapter(OnVoucherActionListener listener) {
        this.vouchers = new ArrayList<>();
        this.vouchersFiltered = new ArrayList<>();
        this.listener = listener;
    }

    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
        this.vouchersFiltered = new ArrayList<>(vouchers);
        notifyDataSetChanged();
    }

    public void filter(String query, String filterType) {
        vouchersFiltered.clear();

        for (Voucher voucher : vouchers) {
            boolean matchesQuery = query.isEmpty() ||
                    voucher.getCode().toLowerCase().contains(query.toLowerCase());

            boolean matchesFilter = filterType.equals("all");
            if (!matchesFilter) {
                switch (filterType) {
                    case "active":
                        matchesFilter = voucher.isActive() && !voucher.isExpired();
                        break;
                    case "inactive":
                        matchesFilter = !voucher.isActive();
                        break;
                    case "expired":
                        matchesFilter = voucher.isExpired();
                        break;
                    case "percent":
                        matchesFilter = "percent".equals(voucher.getType());
                        break;
                    case "fixed":
                        matchesFilter = "fixed".equals(voucher.getType());
                        break;
                }
            }

            if (matchesQuery && matchesFilter) {
                vouchersFiltered.add(voucher);
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = vouchersFiltered.get(position);
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return vouchersFiltered.size();
    }

    class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvVoucherCode, tvVoucherType, tvMaxDiscount, tvDescription;
        TextView tvMinOrder, tvQuantity, tvDateRange;
        Chip chipStatus;
        MaterialButton btnToggleStatus, btnEdit;
        ImageButton btnMore;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVoucherCode = itemView.findViewById(R.id.tvVoucherCode);
            tvVoucherType = itemView.findViewById(R.id.tvVoucherType);
            tvMaxDiscount = itemView.findViewById(R.id.tvMaxDiscount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMinOrder = itemView.findViewById(R.id.tvMinOrder);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        public void bind(Voucher voucher) {
            // Code
            tvVoucherCode.setText(voucher.getCode());

            // Type and Amount
            tvVoucherType.setText(voucher.getTypeDisplayName());

            // Max Discount (only for percent type)
            if ("percent".equals(voucher.getType()) && voucher.getMaxDiscount() > 0) {
                tvMaxDiscount.setVisibility(View.VISIBLE);
                tvMaxDiscount.setText("(Tối đa: " + formatCurrency(voucher.getMaxDiscount()) + ")");
            } else {
                tvMaxDiscount.setVisibility(View.GONE);
            }

            // Description
            if (voucher.getDescription() != null && !voucher.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(voucher.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Min Order
            tvMinOrder.setText(formatCurrency(voucher.getMinOrder()));

            // Quantity
            int remaining = voucher.getRemainingQuantity();
            tvQuantity.setText(remaining + " / " + voucher.getQuantity());

            // Change color based on remaining quantity
            if (remaining == 0) {
                tvQuantity.setTextColor(Color.parseColor("#F44336")); // Red
            } else if (remaining < voucher.getQuantity() * 0.2) {
                tvQuantity.setTextColor(Color.parseColor("#FF9800")); // Orange
            } else {
                tvQuantity.setTextColor(Color.parseColor("#4CAF50")); // Green
            }

            // Date Range
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDate = sdf.format(new Date(voucher.getStartAt()));
            String endDate = sdf.format(new Date(voucher.getEndAt()));
            tvDateRange.setText(startDate + " - " + endDate);

            // Status
            updateStatusChip(voucher);

            // Toggle Status Button
            if (voucher.isActive()) {
                btnToggleStatus.setText("Vô hiệu hóa");
                btnToggleStatus.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                btnToggleStatus.setText("Kích hoạt");
                btnToggleStatus.setIconResource(android.R.drawable.ic_menu_add);
            }

            btnToggleStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleStatus(voucher);
                }
            });

            // Edit Button
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(voucher);
                }
            });

            // More Options
            btnMore.setOnClickListener(v -> showMoreMenu(v, voucher));
        }

        private void updateStatusChip(Voucher voucher) {
            String statusText = voucher.getStatusText();
            chipStatus.setText(statusText);

            // Set color based on status
            int backgroundColor;
            if ("Active".equals(statusText)) {
                backgroundColor = Color.parseColor("#4CAF50"); // Green
            } else if ("Hết hạn".equals(statusText)) {
                backgroundColor = Color.parseColor("#F44336"); // Red
            } else if ("Hết lượt".equals(statusText)) {
                backgroundColor = Color.parseColor("#FF9800"); // Orange
            } else {
                backgroundColor = Color.parseColor("#9E9E9E"); // Gray
            }
            chipStatus.setChipBackgroundColorResource(android.R.color.transparent);
            chipStatus.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(backgroundColor));
        }

        private void showMoreMenu(View view, Voucher voucher) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.menu_voucher_actions, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit_voucher) {
                    if (listener != null) {
                        listener.onEdit(voucher);
                    }
                    return true;
                } else if (itemId == R.id.action_delete_voucher) {
                    if (listener != null) {
                        listener.onDelete(voucher);
                    }
                    return true;
                } else if (itemId == R.id.action_toggle_voucher) {
                    if (listener != null) {
                        listener.onToggleStatus(voucher);
                    }
                    return true;
                }
                return false;
            });

            popup.show();
        }

        private String formatCurrency(double amount) {
            return String.format(Locale.getDefault(), "%,.0f₫", amount);
        }
    }
}
