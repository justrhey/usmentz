package com.example.usmentz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryDialog.Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;
    private OnCategoryEditListener editListener;
    private OnCategoryDeleteListener deleteListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryDialog.Category category);
    }

    public interface OnCategoryEditListener {
        void onCategoryEdit(CategoryDialog.Category category);
    }

    public interface OnCategoryDeleteListener {
        void onCategoryDelete(CategoryDialog.Category category);
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

    public void setCategories(List<CategoryDialog.Category> categories) {
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
        CategoryDialog.Category category = categories.get(position);
        holder.tvEmoji.setText(category.getEmoji());
        holder.tvName.setText(category.getName());
        holder.tvCount.setText(category.getItemCount() + " moments");

        // Set card background color with transparency
        int colorWithAlpha = (category.getColor() & 0x00FFFFFF) | 0x4D000000;
        holder.cardView.setCardBackgroundColor(colorWithAlpha);

        // Card click
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        // Menu click
        if (holder.btnMenu != null) {
            holder.btnMenu.setOnClickListener(v -> {
                showCategoryMenu(holder.itemView.getContext(), category);
            });
        }
    }

    private void showCategoryMenu(android.content.Context context, CategoryDialog.Category category) {
        String[] options = {"Edit Category", "Delete Category"};

        new MaterialAlertDialogBuilder(context)
                .setTitle(category.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (editListener != null) {
                            editListener.onCategoryEdit(category);
                        }
                    } else {
                        if (deleteListener != null) {
                            if (category.getId() <= 6) {
                                new MaterialAlertDialogBuilder(context)
                                        .setTitle("Cannot Delete")
                                        .setMessage("Default categories cannot be deleted.")
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
        private TextView tvEmoji;
        private TextView tvName;
        private TextView tvCount;
        private ImageView btnMenu;
        private MaterialCardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvCategoryEmoji);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvCount = itemView.findViewById(R.id.tvMomentCount);
            cardView = (MaterialCardView) itemView;


            try {
                btnMenu = itemView.findViewById(R.id.btnCategoryMenu);
            } catch (Exception e) {
                btnMenu = null;
            }
        }
    }
}