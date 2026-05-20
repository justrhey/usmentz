package com.example.usmentz.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.usmentz.category.Category;

/**
 * Manages saving and restoring the last viewed category via SharedPreferences.
 * Extracted from MainActivity to reduce class size.
 */
public class CategoryStateHelper {

    private static final String PREF_NAME = "UsmentzPrefs";
    private static final String KEY_LAST_CATEGORY_ID = "last_category_id";
    private static final String KEY_LAST_CATEGORY_NAME = "last_category_name";
    private static final String KEY_LAST_CATEGORY_ICON = "last_category_icon";
    private static final String KEY_LAST_CATEGORY_COLOR = "last_category_color";

    private final SharedPreferences sharedPreferences;

    public CategoryStateHelper(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveCategory(Category category) {
        if (category == null) return;
        sharedPreferences.edit()
                .putInt(KEY_LAST_CATEGORY_ID, category.getId())
                .putString(KEY_LAST_CATEGORY_NAME, category.getName())
                .putString(KEY_LAST_CATEGORY_ICON, category.getIconName())
                .putInt(KEY_LAST_CATEGORY_COLOR, category.getColor())
                .apply();
    }

    public Category restoreCategory() {
        int savedId = sharedPreferences.getInt(KEY_LAST_CATEGORY_ID, -1);
        if (savedId == -1) return null;

        String savedName = sharedPreferences.getString(KEY_LAST_CATEGORY_NAME, "");
        String savedIcon = sharedPreferences.getString(KEY_LAST_CATEGORY_ICON, "folder");
        int savedColor   = sharedPreferences.getInt(KEY_LAST_CATEGORY_COLOR, 0xFF9C27B0);

        if (savedName.isEmpty()) return null;

        Category category = new Category(savedName, savedIcon, savedColor);
        category.setId(savedId);
        return category;
    }

    public void clearCategory() {
        sharedPreferences.edit()
                .remove(KEY_LAST_CATEGORY_ID)
                .remove(KEY_LAST_CATEGORY_NAME)
                .remove(KEY_LAST_CATEGORY_ICON)
                .remove(KEY_LAST_CATEGORY_COLOR)
                .apply();
    }
}
