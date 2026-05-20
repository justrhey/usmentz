package com.example.usmentz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.R;
import com.example.usmentz.category.Category;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CategoryPortraitAdapter extends RecyclerView.Adapter<CategoryPortraitAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    private int getIconResource(String iconName) {
        if (iconName == null) return R.drawable.folder;
        switch (iconName) {
            case "folder": return R.drawable.folder;
            case "heart": return R.drawable.heart;
            case "star": return R.drawable.star;
            case "food": return R.drawable.food;
            case "travel": return R.drawable.travel;
            case "movie": return R.drawable.movie;
            case "music": return R.drawable.music;
            case "book": return R.drawable.book;
            case "gift": return R.drawable.gift;
            default: return R.drawable.folder;
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_portrait, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        int iconResId = getIconResource(category.getIconName());
        holder.ivIcon.setImageResource(iconResId);

        holder.tvName.setText(category.getName());
        holder.tvCount.setText(category.getItemCount() + " moments");

        int colorWithAlpha = (category.getColor() & 0x00FFFFFF) | 0x4D000000;
        holder.cardView.setCardBackgroundColor(colorWithAlpha);

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvName;
        private TextView tvCount;
        private MaterialCardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvCount = itemView.findViewById(R.id.tvMomentCount);
            cardView = (MaterialCardView) itemView;
        }
    }
}
