package com.example.usmentz.category;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.usmentz.R;
import com.example.usmentz.widget.ColorWheelView;

public class CategoryEditDialog {

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

    public interface OnCategoryEditListener {
        void onCategoryEdited(Category category);
    }

    public static void show(Context context, Category category, OnCategoryEditListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);

        EditText etName = view.findViewById(R.id.etCategoryName);
        LinearLayout iconRow = view.findViewById(R.id.iconRow);
        ColorWheelView colorWheel = view.findViewById(R.id.colorWheel);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnCreate = view.findViewById(R.id.btnCreate);

        // Change button text for edit mode
        btnCreate.setText("Update");

        // Build bottom sheet dialog
        AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        // Set current values
        etName.setText(category.getName());
        colorWheel.setColor(category.getColor());

        // Find current icon index
        int currentIndex = 0;
        String[] iconNames = {"folder", "heart", "star", "food", "travel", "movie", "music", "book", "gift"};
        for (int i = 0; i < iconNames.length; i++) {
            if (iconNames[i].equals(category.getIconName())) {
                currentIndex = i;
                break;
            }
        }

        // Icon selection
        final int[] selectedIconIndex = {currentIndex};
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
            iv.setAlpha(i == currentIndex ? 1.0f : 0.5f);

            if (i == currentIndex) {
                iv.setBackground(createIconBg(category.getColor()));
            }

            final int index = i;
            iv.setOnClickListener(v -> {
                for (ImageView img : iconViews) {
                    img.setBackground(null);
                    img.setAlpha(0.5f);
                }
                iv.setAlpha(1.0f);
                iv.setBackground(createIconBg(colorWheel.getColor()));
                selectedIconIndex[0] = index;
            });

            iconViews[i] = iv;
            iconRow.addView(iv);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }

            String iconName = iconNames[selectedIconIndex[0]];
            int color = colorWheel.getColor();

            category.setName(name);
            category.setIconName(iconName);
            category.setColor(color);

            listener.onCategoryEdited(category);
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
}