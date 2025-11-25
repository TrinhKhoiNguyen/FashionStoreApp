package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.model.Order;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnAdminOrderClickListener listener;

    public interface OnAdminOrderClickListener {
        void onViewOrder(Order order);

        void onUpdateOrderStatus(Order order);
    }

    public AdminOrderAdapter(Context context, List<Order> orders, OnAdminOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.orderId.setText(order.getOrderId());
        holder.orderStatus.setText(order.getStatus());

        // Set status background color
        int statusColor;
        switch (order.getStatus()) {
            case "Đang xử lý":
                statusColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "Đang giao":
                statusColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case "Hoàn thành":
                statusColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "Đã hủy":
                statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = context.getResources().getColor(android.R.color.darker_gray);
        }
        holder.orderStatus.setBackgroundColor(statusColor);

        // Customer info - using userId as fallback
        String customerName = order.getUserId() != null ? "ID: " + order.getUserId().substring(0, 8) : "Khách hàng";
        holder.orderCustomer.setText("Khách hàng: " + customerName);

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(order.getCreatedAt()));
        holder.orderDate.setText(dateStr);

        // Format total
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.orderTotal.setText("Tổng: " + formatter.format(order.getTotal()) + "₫");

        // Click listeners
        holder.viewButton.setOnClickListener(v -> listener.onViewOrder(order));
        holder.updateButton.setOnClickListener(v -> listener.onUpdateOrderStatus(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    /**
     * Update adapter data
     */
    public void updateData(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderStatus, orderCustomer, orderDate, orderTotal;
        MaterialButton viewButton, updateButton;

        ViewHolder(View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.adminOrderId);
            orderStatus = itemView.findViewById(R.id.adminOrderStatus);
            orderCustomer = itemView.findViewById(R.id.adminOrderCustomer);
            orderDate = itemView.findViewById(R.id.adminOrderDate);
            orderTotal = itemView.findViewById(R.id.adminOrderTotal);
            viewButton = itemView.findViewById(R.id.adminViewOrderButton);
            updateButton = itemView.findViewById(R.id.adminUpdateStatusButton);
        }
    }
}
