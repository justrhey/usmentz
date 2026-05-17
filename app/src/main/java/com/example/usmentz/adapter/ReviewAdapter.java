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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.usmentz.R;
import com.example.usmentz.model.Review;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews = new ArrayList<>();
    private OnReviewClickListener listener;

    public ReviewAdapter() {
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

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
        boolean isFirst = position == 0;
        boolean isLast = position == reviews.size() - 1;

        holder.tvMomentName.setText(review.getMomentName());
        holder.tvDate.setText(review.getDate());
        holder.ratingBar.setRating(review.getRating());
        holder.tvRating.setText(String.valueOf(review.getRating()));

        // Show/hide timeline lines based on position
        holder.topLine.setVisibility(isFirst ? View.INVISIBLE : View.VISIBLE);
        holder.bottomLine.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);

        // Show/hide review text
        if (review.getReviewText() != null && !review.getReviewText().isEmpty()) {
            holder.tvReviewText.setVisibility(View.VISIBLE);
            holder.tvReviewText.setText(review.getReviewText());
        } else {
            holder.tvReviewText.setVisibility(View.GONE);
        }

        // Show photo if exists
        if (review.getPhotoPath() != null && !review.getPhotoPath().isEmpty()) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            
            File imgFile = new File(review.getPhotoPath());
            if (imgFile.exists()) {
                Glide.with(holder.itemView.getContext())
                        .load(imgFile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .into(holder.ivPhoto);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(review.getPhotoPath())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .into(holder.ivPhoto);
            }
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
        TextView tvMomentName, tvReviewText, tvDate, tvRating;
        RatingBar ratingBar;
        ImageView ivPhoto;
        View topLine, bottomLine;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMomentName = itemView.findViewById(R.id.tvMomentName);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRating = itemView.findViewById(R.id.tvRating);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            topLine = itemView.findViewById(R.id.topLine);
            bottomLine = itemView.findViewById(R.id.bottomLine);
        }
    }
}