package com.example.usmentz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.date.DateLocation;
import com.example.usmentz.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;

import android.graphics.Paint;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private List<DateLocation> dates = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemDeleteListener deleteListener;
    private OnItemMoveListener moveListener;
    private OnItemCompleteListener completeListener;
    private OnRatingChangeListener ratingChangeListener;
    private OnReviewClickListener reviewClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public DateAdapter() {
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return dates.get(position).getId();
    }

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
        
        // Format date as label outside the card
        if (currentDate.getDate() != null) {
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(currentDate.getDate());
            Calendar today = Calendar.getInstance();
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            
            if (isSameDay(dateCal, today)) {
                holder.tvDate.setText("Today");
            } else if (isSameDay(dateCal, yesterday)) {
                holder.tvDate.setText("Yesterday");
            } else {
                holder.tvDate.setText(dateFormat.format(currentDate.getDate()));
            }
        } else {
            holder.tvDate.setText("");
        }
        
        holder.tvName.setText(currentDate.getName());
        holder.tvAddress.setText(currentDate.getAddress());

        // Completion circle — always visible, toggles unicode symbol
        holder.completionDot.setText(currentDate.isCompleted() ? "●" : "○");

        // Name strikethrough when done
        if (currentDate.isCompleted()) {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Star + rating when completed and has rating/review
        boolean hasReview = currentDate.getReview() != null && !currentDate.getReview().isEmpty();
        boolean hasRating = currentDate.getRating() > 0;
        if (currentDate.isCompleted() && (hasReview || hasRating)) {
            holder.tvReview.setVisibility(View.VISIBLE);
            holder.tvRating.setVisibility(View.VISIBLE);
            holder.tvRating.setText(String.valueOf((int) currentDate.getRating()));
        } else {
            holder.tvReview.setVisibility(View.GONE);
            holder.tvRating.setVisibility(View.GONE);
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
        private ImageView tvReview;
        private ImageView btnDelete;
        private TextView completionDot;
        private TextView tvRating;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRating = itemView.findViewById(R.id.tvRating);
            completionDot = itemView.findViewById(R.id.completionDot);
            // Hidden compatibility views
            checkComplete = itemView.findViewById(R.id.checkComplete);
            ratingContainer = itemView.findViewById(R.id.ratingContainer);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvReview = itemView.findViewById(R.id.tvReview);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // Completion toggle
            completionDot.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                if (dates == null || position >= dates.size()) return;
                DateLocation date = dates.get(position);
                if (date == null) return;
                boolean newState = !date.isCompleted();
                date.setCompleted(newState);
                // Toggle unicode circle
                completionDot.setText(newState ? "●" : "○");
                if (newState) {
                    tvName.setPaintFlags(tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    tvName.setPaintFlags(tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                if (completeListener != null) {
                    completeListener.onItemComplete(date, newState);
                }
            });

            // Item click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener == null || position == RecyclerView.NO_POSITION) return;
                if (dates == null || position >= dates.size()) return;
                DateLocation date = dates.get(position);
                if (date != null) {
                    listener.onItemClick(date);
                }
            });

            // Delete button listener
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (deleteListener == null || position == RecyclerView.NO_POSITION) return;
                if (dates == null || position >= dates.size()) return;
                DateLocation date = dates.get(position);
                if (date != null) {
                    deleteListener.onItemDelete(date);
                }
            });

            // Review click listener
            tvReview.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (reviewClickListener == null || position == RecyclerView.NO_POSITION) return;
                if (dates == null || position >= dates.size()) return;
                DateLocation date = dates.get(position);
                if (date != null) {
                    reviewClickListener.onReviewClick(date);
                }
            });
        }
    }
    
    // Helper to compare calendar days
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}