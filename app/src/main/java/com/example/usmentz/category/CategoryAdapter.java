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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;
    private OnCategoryEditListener editListener;
    private OnCategoryDeleteListener deleteListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public interface OnCategoryEditListener {
        void onCategoryEdit(Category category);
    }

    public interface OnCategoryDeleteListener {
        void onCategoryDelete(Category category);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setOnCategoryEditListener(OnCategoryEditListener listener) {
        this.editListener = listener;
    }

    public void setOnCategoryDeleteListener(OnCategoryDeleteListener listener) {
        this.deleteListener = listener;
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

        // Set category name and count
        holder.tvName.setText(category.getName());
        holder.tvCount.setText(category.getItemCount() + " moments");

        // Set category icon based on iconName
        int iconResId = getIconResource(category.getIconName());
        holder.ivIcon.setImageResource(iconResId);

        // Set card background color with transparency
        int colorWithAlpha = (category.getColor() & 0x00FFFFFF) | 0x4D000000;
        holder.cardView.setCardBackgroundColor(colorWithAlpha);

        // Card click - view category moments
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        // Menu button click
        holder.btnMenu.setOnClickListener(v -> {
            showCategoryMenu(holder.itemView.getContext(), category);
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

    private void showCategoryMenu(android.content.Context context, Category category) {
        String[] options = {"Edit Category", "Delete Category"};

        new MaterialAlertDialogBuilder(context)
                .setTitle(category.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit - Allow for ALL categories
                        if (editListener != null) {
                            editListener.onCategoryEdit(category);
                        }
                    } else {
                        // Delete
                        if (deleteListener != null) {
                            // Check if category has moments
                            if (category.getItemCount() > 0) {
                                new MaterialAlertDialogBuilder(context)
                                        .setTitle("Cannot Delete")
                                        .setMessage("This category has " + category.getItemCount() +
                                                " moments. Delete all moments first.")
                                        .setPositiveButton("OK", null)
                                        .show();
                            } else {
                                deleteListener.onCategoryDelete(category);
                            }
                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvCount;
        private ImageView ivIcon;
        private ImageView btnMenu;
        private MaterialCardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvCount = itemView.findViewById(R.id.tvMomentCount);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            btnMenu = itemView.findViewById(R.id.btnCategoryMenu);
            cardView = (MaterialCardView) itemView;
        }
    }
}