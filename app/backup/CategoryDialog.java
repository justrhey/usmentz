package com.example.usmentz;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

public class CategoryDialog {

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(Category category);
    }

    public static void show(Context context, OnCategoryCreatedListener listener) {
        // Check if context is valid
        if (context == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        // Initialize views with null checks
        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        Button btnEmojiDropdown = dialogView.findViewById(R.id.btnEmojiDropdown);
        Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        // Check if any view is null
        if (etName == null || btnEmojiDropdown == null || spinnerColor == null ||
                btnCancel == null || btnCreate == null) {
            Toast.makeText(context, "Error loading dialog", Toast.LENGTH_SHORT).show();
            return;
        }

        // Emoji options
        String[] emojis = {"📁 Folder", "❤️ Date", "⭐ Star", "🍕 Food", "✈️ Travel",
                "🎬 Movie", "🎵 Music", "📚 Book", "🎁 Gift", "👤 Person",
                "🏠 Home", "💼 Work", "🎓 School", "⚽ Sports", "🎨 Art"};

        // Show emoji selection dialog
        btnEmojiDropdown.setOnClickListener(v -> {
            AlertDialog.Builder emojiBuilder = new AlertDialog.Builder(context);
            emojiBuilder.setTitle("Choose Emoji");
            emojiBuilder.setItems(emojis, (dialog, which) -> {
                btnEmojiDropdown.setText(emojis[which]);
            });
            emojiBuilder.show();
        });

        // Color options
        String[] colors = {"Purple", "Blue", "Green", "Red", "Orange", "Pink"};
        int[] colorValues = {0xFF6C3483, 0xFF2980B9, 0xFF27AE60, 0xFFC0392B, 0xFFE67E22, 0xFF8E44AD};

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Category name required");
                return;
            }

            // Get selected emoji (split to get emoji only)
            String fullText = btnEmojiDropdown.getText().toString();
            String selectedEmoji = fullText.split(" ")[0];

            // Get selected color
            int selectedPosition = spinnerColor.getSelectedItemPosition();
            int selectedColor = colorValues[selectedPosition];

            // Create and return category
            Category category = new Category(name, selectedEmoji, selectedColor);
            listener.onCategoryCreated(category);
            dialog.dismiss();
        });
    }

    @Entity(tableName = "categories")
    public static class Category implements Serializable {
        @PrimaryKey(autoGenerate = true)
        private int id;
        private String name;
        private String emoji;
        private int color;
        private int itemCount;

        // Constructor for Room
        public Category() {}

        // Constructor for app usage
        @Ignore
        public Category(String name, String emoji, int color) {
            this.name = name;
            this.emoji = emoji;
            this.color = color;
            this.itemCount = 0; // Always start with 0 moments
        }

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmoji() { return emoji; }
        public void setEmoji(String emoji) { this.emoji = emoji; }

        public int getColor() { return color; }
        public void setColor(int color) { this.color = color; }

        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    }
}