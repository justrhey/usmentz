package com.example.usmentz;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class CategoryTabAdapter {
    private TabLayout tabLayout;
    private List<CategoryDialog.Category> categories = new ArrayList<>();
    private OnCategorySelectedListener listener;
    private int selectedPosition = 0;

    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryDialog.Category category, int position);
    }

    // Constructor that takes TabLayout
    public CategoryTabAdapter(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
        setupTabLayout();
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (listener != null && position < categories.size()) {
                    selectedPosition = position;
                    listener.onCategorySelected(categories.get(position), position);
                }
                updateTabStyles();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateTabStyles() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                TextView tvEmoji = tab.getCustomView().findViewById(R.id.tvTabEmoji);
                TextView tvName = tab.getCustomView().findViewById(R.id.tvTabName);

                if (tvEmoji != null && tvName != null) {
                    if (i == selectedPosition) {
                        tvEmoji.setTextColor(0xFFFFFFFF);
                        tvName.setTextColor(0xFFFFFFFF);
                    } else {
                        tvEmoji.setTextColor(0xFFB3B3B3);
                        tvName.setTextColor(0xFFB3B3B3);
                    }
                }
            }
        }
    }

    public void setCategories(List<CategoryDialog.Category> categories) {
        this.categories = categories;
        tabLayout.removeAllTabs();

        for (int i = 0; i < categories.size(); i++) {
            CategoryDialog.Category category = categories.get(i);
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setCustomView(createTabView(category));
            tabLayout.addTab(tab);
        }

        if (!categories.isEmpty() && selectedPosition < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(selectedPosition);
            if (tab != null) {
                tab.select();
                updateTabStyles();
            }
        }
    }

    private View createTabView(CategoryDialog.Category category) {
        View view = LayoutInflater.from(tabLayout.getContext()).inflate(R.layout.item_category_tab, null);
        TextView tvEmoji = view.findViewById(R.id.tvTabEmoji);
        TextView tvName = view.findViewById(R.id.tvTabName);

        if (tvEmoji != null) {
            tvEmoji.setText(category.getEmoji());
        }

        if (tvName != null) {
            tvName.setText(category.getName());
        }

        return view;
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    public void selectCategory(int position) {
        if (position >= 0 && position < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                tab.select();
                selectedPosition = position;
                updateTabStyles();
            }
        }
    }

    public CategoryDialog.Category getCurrentCategory() {
        if (!categories.isEmpty() && selectedPosition < categories.size()) {
            return categories.get(selectedPosition);
        }
        return null;
    }

    public CategoryDialog.Category getCategoryAtPosition(int position) {
        if (position >= 0 && position < categories.size()) {
            return categories.get(position);
        }
        return null;
    }
}