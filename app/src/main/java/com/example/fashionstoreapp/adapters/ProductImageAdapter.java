package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

/**
 * Adapter for product image carousel with zoom/pinch support
 */
public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imageUrls;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl, int position);
    }

    public ProductImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        holder.bind(imageUrl, position);
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public void updateImages(List<String> newImages) {
        this.imageUrls = newImages;
        notifyDataSetChanged();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }

        void bind(String imageUrl, int position) {
            // Enable zoom and pinch gestures
            photoView.setMaximumScale(5.0f);
            photoView.setMediumScale(2.5f);
            photoView.setMinimumScale(1.0f);

            // Load image
            if (imageUrl != null && imageUrl.startsWith("http")) {
                // Load from URL (Firebase Storage)
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(photoView);
            } else if (imageUrl != null) {
                // Load from drawable
                int resId = context.getResources().getIdentifier(imageUrl, "drawable", context.getPackageName());
                if (resId != 0) {
                    photoView.setImageResource(resId);
                } else {
                    photoView.setImageResource(R.drawable.ic_launcher_background);
                }
            }

            // Click listener
            photoView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(imageUrl, position);
                }
            });
        }
    }
}
