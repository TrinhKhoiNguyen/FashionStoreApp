package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> products;
    private OnAdminProductClickListener listener;

    public interface OnAdminProductClickListener {
        void onEditProduct(Product product);

        void onDeleteProduct(Product product);
    }

    public AdminProductAdapter(Context context, List<Product> products, OnAdminProductClickListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.productName.setText(product.getName());
        holder.productCategory.setText(product.getCategoryName());

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.productPrice.setText(formatter.format(product.getCurrentPrice()) + "₫");
        holder.productStock.setText(String.valueOf(product.getStockQuantity()));

        // Set stock color based on quantity
        if (product.getStockQuantity() < 10) {
            holder.productStock.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else if (product.getStockQuantity() < 20) {
            holder.productStock.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.productStock.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }

        // Load image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.placeholder_image);
        }

        // Click listeners
        holder.editButton.setOnClickListener(v -> listener.onEditProduct(product));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteProduct(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    /**
     * Update adapter data
     */
    public void updateData(List<Product> newProducts) {
        this.products.clear();
        this.products.addAll(newProducts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productCategory, productPrice, productStock;
        ImageButton editButton, deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.adminProductImage);
            productName = itemView.findViewById(R.id.adminProductName);
            productCategory = itemView.findViewById(R.id.adminProductCategory);
            productPrice = itemView.findViewById(R.id.adminProductPrice);
            productStock = itemView.findViewById(R.id.adminProductStock);
            editButton = itemView.findViewById(R.id.adminEditProductButton);
            deleteButton = itemView.findViewById(R.id.adminDeleteProductButton);
        }
    }
}
