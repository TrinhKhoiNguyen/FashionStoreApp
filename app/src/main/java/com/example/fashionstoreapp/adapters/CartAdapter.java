package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItem item);

        void onItemRemoved(CartItem item);

        void onItemSelected(CartItem item, boolean isSelected);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems(List<CartItem> newItems) {
        this.cartItems = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
        }
    }

    public List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getTotalPrice();
            }
        }
        return total;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageView productImage, removeButton;
        TextView productName, productPrice, productSize, productColor;
        TextView quantityText, decreaseButton, increaseButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cartItemCheckbox);
            productImage = itemView.findViewById(R.id.cartProductImage);
            productName = itemView.findViewById(R.id.cartProductName);
            productPrice = itemView.findViewById(R.id.cartProductPrice);
            productSize = itemView.findViewById(R.id.cartProductSize);
            productColor = itemView.findViewById(R.id.cartProductColor);
            quantityText = itemView.findViewById(R.id.cartQuantityText);
            decreaseButton = itemView.findViewById(R.id.cartDecreaseButton);
            increaseButton = itemView.findViewById(R.id.cartIncreaseButton);
            removeButton = itemView.findViewById(R.id.cartRemoveButton);
        }

        public void bind(CartItem item) {
            // Set checkbox
            checkBox.setChecked(item.isSelected());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (listener != null) {
                    listener.onItemSelected(item, isChecked);
                }
            });

            // Set product info
            productName.setText(item.getProduct().getName());
            productPrice.setText(item.getProduct().getFormattedCurrentPrice());
            productSize.setText("Size: " + item.getSize());
            productColor.setText("Màu: " + item.getColor());
            quantityText.setText(String.valueOf(item.getQuantity()));

            // Load product image (placeholder for now)
            productImage.setImageResource(android.R.drawable.ic_menu_gallery);

            // Quantity controls
            decreaseButton.setEnabled(item.canDecreaseQuantity());
            increaseButton.setEnabled(item.canIncreaseQuantity());

            decreaseButton.setOnClickListener(v -> {
                if (item.canDecreaseQuantity()) {
                    item.decreaseQuantity();
                    quantityText.setText(String.valueOf(item.getQuantity()));
                    decreaseButton.setEnabled(item.canDecreaseQuantity());
                    increaseButton.setEnabled(item.canIncreaseQuantity());
                    if (listener != null) {
                        listener.onQuantityChanged(item);
                    }
                }
            });

            increaseButton.setOnClickListener(v -> {
                if (item.canIncreaseQuantity()) {
                    item.increaseQuantity();
                    quantityText.setText(String.valueOf(item.getQuantity()));
                    decreaseButton.setEnabled(item.canDecreaseQuantity());
                    increaseButton.setEnabled(item.canIncreaseQuantity());
                    if (listener != null) {
                        listener.onQuantityChanged(item);
                    }
                }
            });

            // Remove button
            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(item);
                    removeItem(getAdapterPosition());
                }
            });
        }
    }
}
