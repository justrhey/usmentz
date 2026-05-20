package com.example.usmentz.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.date.DateLocation;
import com.example.usmentz.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private List<DateLocation> dates = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemDeleteListener deleteListener;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    // Feeling colors for badge backgrounds
    private static final int FEELING_COLOR_DEFAULT = 0xFF9B5CFF;
    private static final int FEELING_COLOR_COZY = 0xFFFF9800;
    private static final int FEELING_COLOR_ROMANTIC = 0xFFE91E63;
    private static final int FEELING_COLOR_FUN = 0xFF4CAF50;
    private static final int FEELING_COLOR_ADVENTUROUS = 0xFF2196F3;
    private static final int FEELING_COLOR_RELAXING = 0xFF00BCD4;
    private static final int FEELING_COLOR_EXCITING = 0xFFFF5722;

    public interface OnItemClickListener {
        void onItemClick(DateLocation dateLocation);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(DateLocation dateLocation);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
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
        DateLocation current = dates.get(position);

        // Date label
        if (current.getDate() != null) {
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(current.getDate());
            Calendar today = Calendar.getInstance();
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);

            if (isSameDay(dateCal, today)) {
                holder.tvDate.setText("Today");
            } else if (isSameDay(dateCal, yesterday)) {
                holder.tvDate.setText("Yesterday");
            } else {
                holder.tvDate.setText(dateFormat.format(current.getDate()));
            }
        } else {
            holder.tvDate.setText("");
        }

        // Place name + address
        holder.tvName.setText(current.getName());
        holder.tvAddress.setText(current.getAddress());

        // Feeling badge
        String feeling = current.getFeeling();
        if (feeling != null && !feeling.isEmpty()) {
            holder.tvFeelingBadge.setText(feeling);
            holder.tvFeelingBadge.setVisibility(View.VISIBLE);
            int bgColor = getFeelingColor(feeling);
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dpToPx(holder.itemView.getContext(), 20));
            bg.setColor(bgColor);
            holder.tvFeelingBadge.setBackground(bg);
        } else {
            holder.tvFeelingBadge.setVisibility(View.GONE);
        }

        // Review snippet
        String review = current.getReviewNotes();
        if (review != null && !review.isEmpty()) {
            holder.tvReviewSnippet.setText(review);
            holder.tvReviewSnippet.setVisibility(View.VISIBLE);
        } else {
            holder.tvReviewSnippet.setVisibility(View.GONE);
        }

        // Cost
        float cost = current.getCost();
        if (cost > 0) {
            holder.tvCost.setText(String.format(Locale.getDefault(), "$%.0f", cost));
            holder.tvCost.setVisibility(View.VISIBLE);
        } else {
            holder.tvCost.setVisibility(View.GONE);
        }

        // Do-again heart
        if (current.isDoAgain()) {
            holder.ivDoAgain.setVisibility(View.VISIBLE);
        } else {
            holder.ivDoAgain.setVisibility(View.GONE);
        }

        // Photo placeholder (no actual photo yet)
        if (current.getPhotoPath() != null && !current.getPhotoPath().isEmpty()) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.photoPlaceholder.setVisibility(View.GONE);
            // TODO: Load actual photo with Glide/Picasso
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
            holder.photoPlaceholder.setVisibility(View.VISIBLE);
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

    private int getFeelingColor(String feeling) {
        String lower = feeling.toLowerCase(Locale.getDefault());
        if (lower.contains("cozy")) return FEELING_COLOR_COZY;
        if (lower.contains("romantic")) return FEELING_COLOR_ROMANTIC;
        if (lower.contains("fun")) return FEELING_COLOR_FUN;
        if (lower.contains("adventur")) return FEELING_COLOR_ADVENTUROUS;
        if (lower.contains("relax")) return FEELING_COLOR_RELAXING;
        if (lower.contains("excit")) return FEELING_COLOR_EXCITING;
        return FEELING_COLOR_DEFAULT;
    }

    private int dpToPx(android.content.Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvName, tvAddress, tvReviewSnippet, tvCost, tvFeelingBadge;
        ImageView ivPhoto, ivDoAgain, btnDelete, photoPlaceholder;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvReviewSnippet = itemView.findViewById(R.id.tvReviewSnippet);
            tvCost = itemView.findViewById(R.id.tvCost);
            tvFeelingBadge = itemView.findViewById(R.id.tvFeelingBadge);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivDoAgain = itemView.findViewById(R.id.ivDoAgain);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            photoPlaceholder = itemView.findViewById(R.id.photoPlaceholder);

            // Item click
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener == null || pos == RecyclerView.NO_POSITION || dates == null || pos >= dates.size()) return;
                DateLocation date = dates.get(pos);
                if (date != null) listener.onItemClick(date);
            });

            // Delete button
            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (deleteListener == null || pos == RecyclerView.NO_POSITION || dates == null || pos >= dates.size()) return;
                DateLocation date = dates.get(pos);
                if (date != null) deleteListener.onItemDelete(date);
            });
        }
    }
}
