package com.example.fashionstoreapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviews;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvDate, tvComment;
        private RatingBar ratingBar;
        private RecyclerView rvReviewImages;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvComment = itemView.findViewById(R.id.tvComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            rvReviewImages = itemView.findViewById(R.id.rvReviewImages);
        }

        public void bind(Review review) {
            tvUserName.setText(review.getUserName());
            tvDate.setText(review.getFormattedDate());
            tvComment.setText(review.getComment());
            ratingBar.setRating((float) review.getRating());

            // Show review images if available
            if (review.hasImages()) {
                rvReviewImages.setVisibility(View.VISIBLE);
                LinearLayoutManager layoutManager = new LinearLayoutManager(
                        itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
                rvReviewImages.setLayoutManager(layoutManager);

                ReviewImageAdapter imageAdapter = new ReviewImageAdapter(
                        itemView.getContext(), review.getImageUrls(), false);
                rvReviewImages.setAdapter(imageAdapter);
            } else {
                rvReviewImages.setVisibility(View.GONE);
            }
        }
    }
}
