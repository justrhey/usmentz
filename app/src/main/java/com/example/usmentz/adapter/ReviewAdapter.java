package com.example.usmentz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.usmentz.R;
import com.example.usmentz.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews = new ArrayList<>();
    private OnReviewClickListener listener;

    public interface OnReviewClickListener {
        void onReviewClick(Review review);
    }

    public void setOnReviewClickListener(OnReviewClickListener listener) {
        this.listener = listener;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.tvMomentName.setText(review.getMomentName());
        holder.tvReviewText.setText(review.getReviewText());
        holder.tvDate.setText(review.getDate());
        holder.ratingBar.setRating(review.getRating());

        // Show photo if exists
        if (review.getPhotoPath() != null && !review.getPhotoPath().isEmpty()) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(review.getPhotoPath())
                    .centerCrop()
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewClick(review);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvMomentName, tvReviewText, tvDate;
        RatingBar ratingBar;
        ImageView ivPhoto;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMomentName = itemView.findViewById(R.id.tvMomentName);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvDate = itemView.findViewById(R.id.tvDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }
    }
}