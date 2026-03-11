package com.example.usmentz.category;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usmentz.R;

public class CategoryDialog {

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(Category category);
    }

    public static void show(Context context, OnCategoryCreatedListener listener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
            builder.setView(dialogView);

            EditText etName = dialogView.findViewById(R.id.etCategoryName);
            Button btnIconDropdown = dialogView.findViewById(R.id.btnIconDropdown);
            ImageView ivIconPreview = dialogView.findViewById(R.id.ivIconPreview);
            TextView tvIconPreview = dialogView.findViewById(R.id.tvIconPreview);
            Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);
            Button btnCreate = dialogView.findViewById(R.id.btnCreate);

            if (etName == null || btnIconDropdown == null || spinnerColor == null ||
                    btnCancel == null || btnCreate == null) {
                Toast.makeText(context, "Error loading dialog", Toast.LENGTH_SHORT).show();
                return;
            }

            // Define PNG image resources and their names
            final int[] imageResources = {
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

            final String[] imageNames = {
                    "folder", "heart", "star", "food", "travel", "movie", "music", "book", "gift"
            };

            final String[] imageDisplayNames = {
                    "Folder", "Heart", "Star", "Food", "Travel", "Movie", "Music", "Book", "Gift"
            };

            final int[] currentImageIndex = {0};

            // Set initial image
            btnIconDropdown.setText(imageDisplayNames[0]);
            if (ivIconPreview != null) {
                ivIconPreview.setImageResource(imageResources[0]);
            }
            if (tvIconPreview != null) {
                tvIconPreview.setText("Selected: " + imageDisplayNames[0]);
            }

            btnIconDropdown.setOnClickListener(v -> {
                currentImageIndex[0] = (currentImageIndex[0] + 1) % imageResources.length;
                btnIconDropdown.setText(imageDisplayNames[currentImageIndex[0]]);
                if (ivIconPreview != null) {
                    ivIconPreview.setImageResource(imageResources[currentImageIndex[0]]);
                }
                if (tvIconPreview != null) {
                    tvIconPreview.setText("Selected: " + imageDisplayNames[currentImageIndex[0]]);
                }
            });

            // Color options
            String[] colors = {"Purple", "Red", "Blue", "Green", "Orange", "Pink"};
            int[] colorValues = {0xFF9C27B0, 0xFFF44336, 0xFF2196F3, 0xFF4CAF50, 0xFFFF9800, 0xFFE91E63};

            ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, colors);
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerColor.setAdapter(colorAdapter);

            AlertDialog dialog = builder.create();
            dialog.show();

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnCreate.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    etName.setError("Category name required");
                    return;
                }

                String iconName = imageNames[currentImageIndex[0]];
                int selectedColor = colorValues[spinnerColor.getSelectedItemPosition()];

                Category category = new Category(name, iconName, selectedColor);
                listener.onCategoryCreated(category);
                dialog.dismiss();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Dialog error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}