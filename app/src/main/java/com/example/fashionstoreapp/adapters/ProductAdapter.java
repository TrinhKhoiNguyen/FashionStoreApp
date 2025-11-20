package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fashionstoreapp.ProductDetailActivity;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);

        void onAddToCartClick(Product product);

        void onFavoriteClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.productList = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addProducts(List<Product> newProducts) {
        if (newProducts != null && !newProducts.isEmpty()) {
            int startPosition = this.productList.size();
            this.productList.addAll(newProducts);
            notifyItemRangeInserted(startPosition, newProducts.size());
        }
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, favoriteIcon, addToCartIcon;
        TextView productName, currentPrice, originalPrice;
        TextView discountBadge, newBadge, voucherBadge;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            addToCartIcon = itemView.findViewById(R.id.addToCartIcon);
            productName = itemView.findViewById(R.id.productName);
            currentPrice = itemView.findViewById(R.id.currentPrice);
            originalPrice = itemView.findViewById(R.id.originalPrice);
            discountBadge = itemView.findViewById(R.id.discountBadge);
            newBadge = itemView.findViewById(R.id.newBadge);
            voucherBadge = itemView.findViewById(R.id.voucherBadge);
        }

        public void bind(Product product) {
            // Set product name
            productName.setText(product.getName());

            // Set current price
            currentPrice.setText(product.getFormattedCurrentPrice());

            // Set original price with strikethrough if there's a discount
            if (product.hasDiscount()) {
                originalPrice.setVisibility(View.VISIBLE);
                originalPrice.setText(product.getFormattedOriginalPrice());
                originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // Show discount badge
                discountBadge.setVisibility(View.VISIBLE);
                discountBadge.setText("-" + product.getDiscountPercent() + "%");
            } else {
                originalPrice.setVisibility(View.GONE);
                discountBadge.setVisibility(View.GONE);
            }

            // Show "New" badge
            if (product.isNew()) {
                newBadge.setVisibility(View.VISIBLE);
            } else {
                newBadge.setVisibility(View.GONE);
            }

            // Show voucher badge
            if (product.isHasVoucher() && product.getVoucherText() != null) {
                voucherBadge.setVisibility(View.VISIBLE);
                voucherBadge.setText(product.getVoucherText());
            } else {
                voucherBadge.setVisibility(View.GONE);
            }

            // Set favorite icon
            if (product.isFavorite()) {
                favoriteIcon.setImageResource(R.drawable.baseline_favorite_24);
                favoriteIcon.setColorFilter(context.getResources().getColor(R.color.accent_burgundy, null));
            } else {
                favoriteIcon.setImageResource(R.drawable.baseline_favorite_border_24);
                favoriteIcon.setColorFilter(context.getResources().getColor(R.color.text_primary, null));
            }

            // Load product image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                // Check if it's a URL (starts with http)
                if (product.getImageUrl().startsWith("http")) {
                    // Load from URL using Glide
                    Glide.with(context)
                            .load(product.getImageUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(productImage);
                } else {
                    // Load from drawable by resource name
                    int resourceId = context.getResources().getIdentifier(
                            product.getImageUrl(),
                            "drawable",
                            context.getPackageName());

                    if (resourceId != 0) {
                        productImage.setImageResource(resourceId);
                    } else {
                        // Fallback to placeholder if image not found
                        productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            } else {
                // No image URL, use placeholder
                productImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                // Open ProductDetailActivity
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product", product);
                context.startActivity(intent);

                // Also notify listener if available
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            addToCartIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(product);
                }
            });

            favoriteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    product.setFavorite(!product.isFavorite());
                    listener.onFavoriteClick(product);
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }
}
