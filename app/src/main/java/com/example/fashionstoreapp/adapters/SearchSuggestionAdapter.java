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

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.SuggestionViewHolder> {

    private Context context;
    private List<Product> suggestions;
    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Product product);
    }

    public SearchSuggestionAdapter(Context context, List<Product> suggestions, OnSuggestionClickListener listener) {
        this.context = context;
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        Product product = suggestions.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void updateSuggestions(List<Product> newSuggestions) {
        this.suggestions = newSuggestions != null ? newSuggestions : new ArrayList<>();
        notifyDataSetChanged();
    }

    class SuggestionViewHolder extends RecyclerView.ViewHolder {
        ImageView suggestionImage;
        TextView suggestionName, suggestionCurrentPrice, suggestionOriginalPrice;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestionImage = itemView.findViewById(R.id.suggestionImage);
            suggestionName = itemView.findViewById(R.id.suggestionName);
            suggestionCurrentPrice = itemView.findViewById(R.id.suggestionCurrentPrice);
            suggestionOriginalPrice = itemView.findViewById(R.id.suggestionOriginalPrice);
        }

        public void bind(Product product) {
            // Set product name
            suggestionName.setText(product.getName());

            // Set current price
            suggestionCurrentPrice.setText(product.getFormattedCurrentPrice());

            // Set original price with strikethrough if there's a discount
            if (product.hasDiscount()) {
                suggestionOriginalPrice.setVisibility(View.VISIBLE);
                suggestionOriginalPrice.setText(product.getFormattedOriginalPrice());
                suggestionOriginalPrice
                        .setPaintFlags(suggestionOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                suggestionOriginalPrice.setVisibility(View.GONE);
            }

            // Load product image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                if (product.getImageUrl().startsWith("http")) {
                    Glide.with(context)
                            .load(product.getImageUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(suggestionImage);
                } else {
                    int resourceId = context.getResources().getIdentifier(
                            product.getImageUrl(),
                            "drawable",
                            context.getPackageName());

                    if (resourceId != 0) {
                        suggestionImage.setImageResource(resourceId);
                    } else {
                        suggestionImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            } else {
                suggestionImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSuggestionClick(product);
                }

                // Open ProductDetailActivity
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product", product);
                context.startActivity(intent);
            });
        }
    }
}
