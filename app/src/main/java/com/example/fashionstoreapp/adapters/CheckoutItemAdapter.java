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
import com.example.fashionstoreapp.models.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> items;
    private NumberFormat currencyFormat;

    public CheckoutItemAdapter(Context context, List<CartItem> items) {
        this.context = context;
        this.items = items;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvVariant, tvPrice, tvQuantity;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivCheckoutItemImage);
            tvName = itemView.findViewById(R.id.tvCheckoutItemName);
            tvVariant = itemView.findViewById(R.id.tvCheckoutItemVariant);
            tvPrice = itemView.findViewById(R.id.tvCheckoutItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvCheckoutItemQuantity);
        }

        void bind(CartItem item) {
            // Load product image
            if (item.getProduct() != null && item.getProduct().getImageUrl() != null) {
                Glide.with(context)
                        .load(item.getProduct().getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.placeholder_image);
            }

            // Set product name
            if (item.getProduct() != null) {
                tvName.setText(item.getProduct().getName());
            }

            // Set variant info
            StringBuilder variantBuilder = new StringBuilder();
            if (item.getSize() != null && !item.getSize().isEmpty()) {
                variantBuilder.append("Size: ").append(item.getSize());
            }
            if (item.getColor() != null && !item.getColor().isEmpty()) {
                if (variantBuilder.length() > 0) {
                    variantBuilder.append(", ");
                }
                variantBuilder.append("Màu: ").append(item.getColor());
            }

            if (variantBuilder.length() > 0) {
                tvVariant.setText(variantBuilder.toString());
                tvVariant.setVisibility(View.VISIBLE);
            } else {
                tvVariant.setVisibility(View.GONE);
            }

            // Set price - format as Vietnamese currency
            double price = item.getProduct() != null ? item.getProduct().getCurrentPrice() : 0;
            tvPrice.setText(String.format("%,.0f₫", price));

            // Set quantity
            tvQuantity.setText("x" + item.getQuantity());
        }
    }
}
