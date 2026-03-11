package com.example.usmentz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.date.DateLocation;
import com.example.usmentz.R;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private List<DateLocation> dates = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemDeleteListener deleteListener;
    private OnItemMoveListener moveListener;
    private OnItemCompleteListener completeListener;
    private OnRatingChangeListener ratingChangeListener;
    private OnReviewClickListener reviewClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(DateLocation dateLocation);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(DateLocation dateLocation);
    }

    public interface OnItemMoveListener {
        void onItemMove(int fromPosition, int toPosition);
    }

    public interface OnItemCompleteListener {
        void onItemComplete(DateLocation dateLocation, boolean isCompleted);
    }

    public interface OnRatingChangeListener {
        void onRatingChange(DateLocation dateLocation, float rating);
    }

    public interface OnReviewClickListener {
        void onReviewClick(DateLocation dateLocation);
    }

    // No-arg constructor
    public DateAdapter() {}

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnItemMoveListener(OnItemMoveListener listener) {
        this.moveListener = listener;
    }

    public void setOnItemCompleteListener(OnItemCompleteListener listener) {
        this.completeListener = listener;
    }

    public void setOnRatingChangeListener(OnRatingChangeListener listener) {
        this.ratingChangeListener = listener;
    }

    public void setOnReviewClickListener(OnReviewClickListener listener) {
        this.reviewClickListener = listener;
    }

    public List<DateLocation> getDates() {
        return dates;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateLocation currentDate = dates.get(position);
        holder.tvName.setText(currentDate.getName());
        holder.tvAddress.setText(currentDate.getAddress());
        holder.tvDate.setText(dateFormat.format(currentDate.getDate()));

        // Set completion checkbox
        holder.checkComplete.setChecked(currentDate.isCompleted());

        // Show/hide rating container based on completion
        if (currentDate.isCompleted()) {
            holder.ratingContainer.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(currentDate.getRating());

            if (currentDate.getReview() != null && !currentDate.getReview().isEmpty()) {
                holder.tvReview.setVisibility(View.VISIBLE);
            } else {
                holder.tvReview.setVisibility(View.GONE);
            }
        } else {
            holder.ratingContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public void setDates(List<DateLocation> dates) {
        this.dates = dates;
        notifyDataSetChanged();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(dates, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(dates, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        if (moveListener != null) {
            moveListener.onItemMove(fromPosition, toPosition);
        }
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvAddress;
        private TextView tvDate;
        private CheckBox checkComplete;
        private LinearLayout ratingContainer;
        private RatingBar ratingBar;
        private TextView tvReview;
        private TextView btnDelete;
        private TextView dragHandle;
        private MaterialCardView cardView;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDate = itemView.findViewById(R.id.tvDate);
            checkComplete = itemView.findViewById(R.id.checkComplete);
            ratingContainer = itemView.findViewById(R.id.ratingContainer);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvReview = itemView.findViewById(R.id.tvReview);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            cardView = (MaterialCardView) itemView;

            // Item click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(dates.get(position));
                }
            });

            // Delete button listener
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (deleteListener != null && position != RecyclerView.NO_POSITION) {
                    deleteListener.onItemDelete(dates.get(position));
                }
            });

            // Completion checkbox listener
            checkComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (completeListener != null && position != RecyclerView.NO_POSITION) {
                    DateLocation date = dates.get(position);
                    date.setCompleted(isChecked);
                    completeListener.onItemComplete(date, isChecked);

                    if (isChecked) {
                        ratingContainer.setVisibility(View.VISIBLE);
                    } else {
                        ratingContainer.setVisibility(View.GONE);
                    }
                }
            });

            // Rating bar change listener
            ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                int position = getAdapterPosition();
                if (fromUser && ratingChangeListener != null && position != RecyclerView.NO_POSITION) {
                    DateLocation date = dates.get(position);
                    date.setRating(rating);
                    ratingChangeListener.onRatingChange(date, rating);
                }
            });

            // Review click listener
            tvReview.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (reviewClickListener != null && position != RecyclerView.NO_POSITION) {
                    reviewClickListener.onReviewClick(dates.get(position));
                }
            });
        }

        public TextView getDragHandle() {
            return dragHandle;
        }
    }
}