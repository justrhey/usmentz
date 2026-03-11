package com.example.usmentz;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CategoryEditDialog {

    public interface OnCategoryEditListener {
        void onCategoryEdited(CategoryDialog.Category category);
    }

    public static void show(Context context, CategoryDialog.Category category, OnCategoryEditListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        Button btnEmojiDropdown = dialogView.findViewById(R.id.btnEmojiDropdown);
        Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        // Change button text to "Update"
        btnCreate.setText("Update");

        // Set current values
        etName.setText(category.getName());
        btnEmojiDropdown.setText(category.getEmoji());

        // Emoji options
        String[] emojis = {"📁", "❤️", "⭐", "🍕", "✈️", "🎬", "🎵", "📚", "🎁", "👤", "🏠", "💼", "🎓", "⚽", "🎨"};

        btnEmojiDropdown.setOnClickListener(v -> {
            AlertDialog.Builder emojiBuilder = new AlertDialog.Builder(context);
            emojiBuilder.setTitle("Choose Emoji");
            emojiBuilder.setItems(emojis, (dialog, which) -> {
                btnEmojiDropdown.setText(emojis[which]);
            });
            emojiBuilder.show();
        });

        // Color options
        String[] colors = {"Purple", "Red", "Blue", "Green", "Orange", "Pink"};
        int[] colorValues = {0xFF9C27B0, 0xFFF44336, 0xFF2196F3, 0xFF4CAF50, 0xFFFF9800, 0xFFE91E63};

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);

        // Set current color selection
        int currentColor = category.getColor();
        for (int i = 0; i < colorValues.length; i++) {
            if (colorValues[i] == currentColor) {
                spinnerColor.setSelection(i);
                break;
            }
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Category name required");
                return;
            }

            String selectedEmoji = btnEmojiDropdown.getText().toString();
            int selectedColor = colorValues[spinnerColor.getSelectedItemPosition()];

            // Update category
            category.setName(name);
            category.setEmoji(selectedEmoji);
            category.setColor(selectedColor);

            listener.onCategoryEdited(category);
            dialog.dismiss();
        });
    }
}