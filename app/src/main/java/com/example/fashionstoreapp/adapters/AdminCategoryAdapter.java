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
import com.example.fashionstoreapp.models.Category;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> categories;
    private OnAdminCategoryClickListener listener;

    public interface OnAdminCategoryClickListener {
        void onEditCategory(Category category);

        void onDeleteCategory(Category category);
    }

    public AdminCategoryAdapter(Context context, List<Category> categories, OnAdminCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        android.util.Log.d("AdminCategoryAdapter",
                "Binding category at position " + position + ": " + category.getName());

        holder.categoryName.setText(category.getName());
        holder.categoryDescription.setText(category.getDescription());
        holder.categoryOrder.setText("Thứ tự: " + category.getDisplayOrder());

        // Status indicator
        if (category.isActive()) {
            holder.categoryStatus.setText("✓ Đang hoạt động");
            holder.categoryStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.categoryStatus.setText("✗ Đã ẩn");
            holder.categoryStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Load image
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.baseline_category_24)
                    .into(holder.categoryImage);
        } else {
            holder.categoryImage.setImageResource(R.drawable.baseline_category_24);
        }

        // Click listeners
        holder.editButton.setOnClickListener(v -> listener.onEditCategory(category));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteCategory(category));
    }

    @Override
    public int getItemCount() {
        int count = categories.size();
        android.util.Log.d("AdminCategoryAdapter", "getItemCount: " + count);
        return count;
    }

    public void updateData(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryName, categoryDescription, categoryOrder, categoryStatus;
        ImageButton editButton, deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.adminCategoryImage);
            categoryName = itemView.findViewById(R.id.adminCategoryName);
            categoryDescription = itemView.findViewById(R.id.adminCategoryDescription);
            categoryOrder = itemView.findViewById(R.id.adminCategoryOrder);
            categoryStatus = itemView.findViewById(R.id.adminCategoryStatus);
            editButton = itemView.findViewById(R.id.adminEditCategoryButton);
            deleteButton = itemView.findViewById(R.id.adminDeleteCategoryButton);
        }
    }
}
