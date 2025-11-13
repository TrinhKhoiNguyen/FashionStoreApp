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
import com.example.fashionstoreapp.models.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<Banner> banners;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(Banner banner);
    }

    public BannerAdapter(Context context, List<Banner> banners, OnBannerClickListener listener) {
        this.context = context;
        this.banners = banners;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = banners.get(position);
        holder.bind(banner);
    }

    @Override
    public int getItemCount() {
        return banners.size();
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        TextView titleText, subtitleText;
        View bannerContainer;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
            titleText = itemView.findViewById(R.id.bannerTitle);
            subtitleText = itemView.findViewById(R.id.bannerSubtitle);
            bannerContainer = itemView.findViewById(R.id.bannerContainer);
        }

        public void bind(Banner banner) {
            titleText.setText(banner.getTitle());
            subtitleText.setText(banner.getSubtitle());

            // Set background color
            if (banner.getBackgroundColor() != 0) {
                bannerContainer.setBackgroundColor(banner.getBackgroundColor());
            }

            // Load banner image
            if (banner.getImageResource() != 0) {
                // Nếu có drawable resource thì dùng
                bannerImage.setImageResource(banner.getImageResource());
                bannerImage.setVisibility(View.VISIBLE);
            } else if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
                // TODO: Load image from URL using Glide or Picasso
                // Glide.with(context).load(banner.getImageUrl()).into(bannerImage);
                bannerImage.setImageResource(android.R.drawable.ic_menu_gallery);
                bannerImage.setVisibility(View.VISIBLE);
            } else {
                // Không có ảnh thì ẩn đi
                bannerImage.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBannerClick(banner);
                }
            });
        }
    }
}
