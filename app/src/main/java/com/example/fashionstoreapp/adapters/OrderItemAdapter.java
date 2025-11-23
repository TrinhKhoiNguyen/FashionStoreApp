package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.model.Order;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private Context context;
    private List<Order.OrderItem> items;

    public OrderItemAdapter(Context context, List<Order.OrderItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order.OrderItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductVariant, tvProductPrice, tvProductQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductVariant = itemView.findViewById(R.id.tvProductVariant);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
        }

        public void bind(Order.OrderItem item) {
            tvProductName.setText(item.getProductName());

            // Variant info
            String variant = "";
            if (item.getSize() != null && !item.getSize().isEmpty()) {
                variant += "Size: " + item.getSize();
            }
            if (item.getColor() != null && !item.getColor().isEmpty()) {
                if (!variant.isEmpty())
                    variant += ", ";
                variant += "Màu: " + item.getColor();
            }
            tvProductVariant.setText(variant);

            // Price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvProductPrice.setText(formatter.format(item.getPrice()));

            // Quantity
            tvProductQuantity.setText("x" + item.getQuantity());

            // Image - always load with Glide
            String imageUrl = item.getImageUrl();
            android.util.Log.d("OrderItemAdapter", "Loading image for " + item.getProductName() + ": " + imageUrl);

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(ivProductImage);
        }
    }
}
