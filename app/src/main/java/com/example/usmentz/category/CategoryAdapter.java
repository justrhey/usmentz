package com.example.usmentz.category;

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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

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

        holder.tvName.setText(category.getName());
        holder.tvCount.setText(category.getItemCount() + " moments");

        int iconResId = getIconResource(category.getIconName());
        holder.ivIcon.setImageResource(iconResId);

        int gradientRes = getGradientDrawable(category.getColor());
        if (holder.categoryColorBg != null) {
            holder.categoryColorBg.setBackgroundResource(gradientRes);
        }

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
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

    private int getGradientDrawable(int color) {
        if (color == 0xFF9B5CFF) return R.drawable.bg_gradient_purple;
        else if (color == 0xFFFF5252) return R.drawable.bg_gradient_red;
        else if (color == 0xFF2196F3) return R.drawable.bg_gradient_blue;
        else if (color == 0xFF4CAF50) return R.drawable.bg_gradient_green;
        else if (color == 0xFFFF9800) return R.drawable.bg_gradient_orange;
        else if (color == 0xFFE91E63) return R.drawable.bg_gradient_pink;
        else return R.drawable.bg_gradient_purple;
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvCount;
        private ImageView ivIcon;
        private View categoryColorBg;
        private MaterialCardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvCount = itemView.findViewById(R.id.tvMomentCount);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            categoryColorBg = itemView.findViewById(R.id.categoryColorBg);
            cardView = (MaterialCardView) itemView;
        }
    }
}
