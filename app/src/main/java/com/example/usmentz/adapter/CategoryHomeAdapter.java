package com.example.usmentz.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.R;
import com.example.usmentz.category.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.ViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener l) { this.listener = l; }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Category cat = categories.get(pos);
        Context ctx = h.itemView.getContext();

        h.tvCategoryName.setText(cat.getName());
        h.tvItemCount.setText(cat.getItemCount() + " moment" + (cat.getItemCount() != 1 ? "s" : ""));

        // Load icon drawable by name
        int iconRes = getDrawableResId(ctx, cat.getIconName());
        h.ivIcon.setImageResource(iconRes);

        // Icon circle background with gradient drawable
        int gradientRes = getGradientDrawable(cat.getColor());
        h.viewIconBg.setBackgroundResource(gradientRes);

        // Top strip with category color
        GradientDrawable stripBg = new GradientDrawable();
        stripBg.setCornerRadii(new float[]{
                dpToPx(18), dpToPx(18), 0, 0, 0, 0, 0, 0
        });
        stripBg.setColor(cat.getColor() != 0 ? cat.getColor() : 0xFF9B5CFF);
        h.topStrip.setBackground(stripBg);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(cat);
        });
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

    private int getDrawableResId(Context ctx, String iconName) {
        if (iconName == null || iconName.isEmpty()) return R.drawable.folder;
        switch (iconName) {
            case "heart":     return R.drawable.heart;
            case "star":      return R.drawable.star;
            case "food":      return R.drawable.food;
            case "travel":    return R.drawable.travel;
            case "movie":     return R.drawable.movie;
            case "music":     return R.drawable.music;
            case "book":      return R.drawable.book;
            case "gift":      return R.drawable.gift;
            case "folder":    return R.drawable.folder;
            default:          return R.drawable.folder;
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * android.content.res.Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View topStrip;
        View viewIconBg;
        ImageView ivIcon;
        TextView tvCategoryName;
        TextView tvItemCount;

        ViewHolder(@NonNull View v) {
            super(v);
            topStrip = v.findViewById(R.id.topStrip);
            viewIconBg = v.findViewById(R.id.viewIconBg);
            ivIcon = v.findViewById(R.id.ivIcon);
            tvCategoryName = v.findViewById(R.id.tvCategoryName);
            tvItemCount = v.findViewById(R.id.tvItemCount);
        }
    }
}