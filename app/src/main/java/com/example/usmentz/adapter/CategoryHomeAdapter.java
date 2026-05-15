package com.example.usmentz.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        h.tvCategoryName.setText(cat.getName());
        h.tvItemCount.setText(cat.getItemCount() + " moment" + (cat.getItemCount() != 1 ? "s" : ""));

        // Show icon name as emoji/text
        String icon = cat.getIconName();
        h.tvIcon.setText(icon != null && !icon.isEmpty() ? icon : "✦");

        // Amount placeholder - could be linked to expense totals later
        h.tvAmount.setText("");

        // Icon background color from category color
        if (cat.getColor() != 0) {
            h.viewIconBg.getBackground().setTint(cat.getColor());
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(cat);
        });
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewIconBg;
        TextView tvIcon;
        TextView tvCategoryName;
        TextView tvItemCount;
        TextView tvAmount;

        ViewHolder(@NonNull View v) {
            super(v);
            viewIconBg = v.findViewById(R.id.viewIconBg);
            tvIcon = v.findViewById(R.id.tvIcon);
            tvCategoryName = v.findViewById(R.id.tvCategoryName);
            tvItemCount = v.findViewById(R.id.tvItemCount);
            tvAmount = v.findViewById(R.id.tvAmount);
        }
    }
}