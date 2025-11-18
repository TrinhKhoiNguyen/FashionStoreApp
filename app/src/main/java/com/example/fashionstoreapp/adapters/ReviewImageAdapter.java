package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.R;

import java.util.List;

public class ReviewImageAdapter extends RecyclerView.Adapter<ReviewImageAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imageUrls;
    private boolean showRemoveButton;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl, int position);

        void onRemoveClick(int position);
    }

    public ReviewImageAdapter(Context context, List<String> imageUrls, boolean showRemoveButton) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.showRemoveButton = showRemoveButton;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        holder.bind(imageUrl, position);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private ImageView btnRemove;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            btnRemove = itemView.findViewById(R.id.btnRemoveImage);
        }

        public void bind(String imageUrl, int position) {
            // Load image with Glide
            if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                // Local URI
                Glide.with(context)
                        .load(Uri.parse(imageUrl))
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imageView);
            } else {
                // Remote URL
                Glide.with(context)
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imageView);
            }

            // Show/hide remove button
            btnRemove.setVisibility(showRemoveButton ? View.VISIBLE : View.GONE);

            // Click listeners
            imageView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(imageUrl, position);
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(position);
                }
            });
        }
    }

    public void updateImages(List<String> newImages) {
        this.imageUrls = newImages;
        notifyDataSetChanged();
    }
}
