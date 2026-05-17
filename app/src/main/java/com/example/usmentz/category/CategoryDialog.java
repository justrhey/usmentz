package com.example.usmentz.category;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.usmentz.R;
import com.example.usmentz.widget.ColorWheelView;

public class CategoryDialog {

    private static final int[] ICON_RESOURCES = {
            R.drawable.folder,
            R.drawable.heart,
            R.drawable.star,
            R.drawable.food,
            R.drawable.travel,
            R.drawable.movie,
            R.drawable.music,
            R.drawable.book,
            R.drawable.gift
    };

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(Category category);
    }

    public static void show(Context context, OnCategoryCreatedListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);

        EditText etName = view.findViewById(R.id.etCategoryName);
        LinearLayout iconRow = view.findViewById(R.id.iconRow);
        ColorWheelView colorWheel = view.findViewById(R.id.colorWheel);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnCreate = view.findViewById(R.id.btnCreate);

        // Build bottom sheet dialog
        AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();

        // Style as bottom sheet
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        // Icon selection — build horizontal row
        final int[] selectedIconIndex = {0};
        ImageView[] iconViews = new ImageView[ICON_RESOURCES.length];

        int iconSize = (int) (44 * context.getResources().getDisplayMetrics().density);
        int iconPadding = (int) (10 * context.getResources().getDisplayMetrics().density);

        for (int i = 0; i < ICON_RESOURCES.length; i++) {
            ImageView iv = new ImageView(context);
            iv.setImageResource(ICON_RESOURCES[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
            params.setMargins(iconPadding, 0, iconPadding, 0);
            iv.setLayoutParams(params);
            iv.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);

            // First icon selected by default
            if (i == 0) {
                iv.setBackground(createIconBg(0xFF9B5CFF));
            }

            final int index = i;
            iv.setOnClickListener(v -> {
                // Deselect all
                for (ImageView img : iconViews) {
                    img.setBackground(null);
                    img.setAlpha(0.5f);
                }
                // Select this one
                iv.setAlpha(1.0f);
                selectedIconIndex[0] = index;
                // Update color wheel default if needed
            });

            iconViews[i] = iv;
            iconRow.addView(iv);
        }

        // Start with first icon selected
        iconViews[0].setAlpha(1.0f);
        for (int i = 1; i < iconViews.length; i++) {
            iconViews[i].setAlpha(0.5f);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }

            String iconName = getIconName(ICON_RESOURCES[selectedIconIndex[0]]);
            int color = colorWheel.getColor();

            Category category = new Category(name, iconName, color);
            if (listener != null) {
                listener.onCategoryCreated(category);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private static GradientDrawable createIconBg(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.argb(30, Color.red(color), Color.green(color), Color.blue(color)));
        bg.setStroke((int) (2 * android.content.res.Resources.getSystem().getDisplayMetrics().density), color);
        return bg;
    }

    private static String getIconName(int resId) {
        if (resId == R.drawable.folder) return "folder";
        if (resId == R.drawable.heart) return "heart";
        if (resId == R.drawable.star) return "star";
        if (resId == R.drawable.food) return "food";
        if (resId == R.drawable.travel) return "travel";
        if (resId == R.drawable.movie) return "movie";
        if (resId == R.drawable.music) return "music";
        if (resId == R.drawable.book) return "book";
        if (resId == R.drawable.gift) return "gift";
        return "folder";
    }
}