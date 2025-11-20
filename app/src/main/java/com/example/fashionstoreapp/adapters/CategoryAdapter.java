package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView categoryImage;
        private TextView categoryName;
        private TextView categoryCount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryCount = itemView.findViewById(R.id.categoryCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCategoryClick(categories.get(position));
                }
            });
        }

        public void bind(Category category) {
            String displayName = formatCategoryName(category.getName());
            categoryName.setText(displayName);

            // Hide product count for now
            categoryCount.setVisibility(View.GONE);

            // TODO: Load category image from URL or use category icon
            // For now, using default icon
            categoryImage.setImageResource(R.drawable.baseline_category_24);
        }

        private String formatCategoryName(String name) {
            if (name == null) return "";
            
            // Format common category IDs to proper names
            switch (name.toLowerCase()) {
                case "ao-hoodie":
                    return "Áo Hoodie";
                case "ao-thun":
                    return "Áo Thun";
                case "ao-polo":
                    return "Áo Polo";
                case "ao-so-mi":
                    return "Áo Sơ Mi";
                case "ao-khoac":
                    return "Áo Khoác";
                case "quan-sot":
                    return "Quần Sọt";
                case "quan-tay":
                    return "Quần Tây";
                case "retro-sports":
                    return "Retro Sports";
                default:
                    return name;
            }
        }
    }
}
