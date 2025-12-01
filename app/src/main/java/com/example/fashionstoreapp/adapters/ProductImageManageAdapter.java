package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.R;
import java.util.List;

public class ProductImageManageAdapter extends RecyclerView.Adapter<ProductImageManageAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imageUrls;
    private OnImageActionListener listener;

    public interface OnImageActionListener {
        void onRemoveClick(int position);

        void onImageClick(String imageUrl, int position);
    }

    public ProductImageManageAdapter(Context context, List<String> imageUrls, OnImageActionListener listener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Load image from URL
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(holder.imageView);

        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(holder.getAdapterPosition());
            }
        });

        holder.imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageUrl, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton removeButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImage);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
