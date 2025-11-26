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
import com.bumptech.glide.request.RequestOptions;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Category;

import java.util.ArrayList;
import java.util.List;

public class FeaturedCategoryAdapter extends RecyclerView.Adapter<FeaturedCategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public FeaturedCategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        if (categories != null)
            this.categories.addAll(categories);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category c = categories.get(position);
        holder.name.setText(c.getName() != null ? c.getName() : "");

        // Load image - support URL or drawable resource name
        String image = c.getImageUrl();
        if (image != null && !image.isEmpty()) {
            // Try to load as URL first
            try {
                Glide.with(context)
                        .load(image)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.baseline_category_24)
                        .into(holder.image);
            } catch (Exception e) {
                // Fallback: load from drawable resources by name
                int resId = context.getResources().getIdentifier(image, "drawable", context.getPackageName());
                if (resId != 0) {
                    Glide.with(context)
                            .load(resId)
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.image);
                } else {
                    Glide.with(context)
                            .load(R.drawable.baseline_category_24)
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.image);
                }
            }
        } else {
            Glide.with(context)
                    .load(R.drawable.baseline_category_24)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onCategoryClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateData(List<Category> newCategories) {
        this.categories.clear();
        if (newCategories != null)
            this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.featuredCategoryImage);
            name = itemView.findViewById(R.id.featuredCategoryName);
        }
    }
}
