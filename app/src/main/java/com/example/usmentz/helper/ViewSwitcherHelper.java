package com.example.usmentz.helper;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.usmentz.category.Category;

/**
 * Manages switching between categories list view and moments detail view.
 * Extracted from MainActivity to reduce class size.
 */
public class ViewSwitcherHelper {

    private final RecyclerView categoriesRecyclerView;
    private final RecyclerView momentsRecyclerView;
    private final Button btnAddCategory;
    private final View momentsHeader;
    private final TextView tvCategoryTitle;
    private final View floatingNavbarContainer;
    private final View oldNavbarContainer;

    private final EmptyStateHelper emptyStateHelper;
    private final NavbarAutoHideHelper navbarHelper;
    private final CategoryStateHelper categoryStateHelper;

    private boolean isInMomentsMode = false;
    private Category currentCategory;

    private OnModeChangeListener modeChangeListener;

    public interface OnModeChangeListener {
        void onEnterMomentsMode(Category category);
        void onExitMomentsMode();
    }

    public ViewSwitcherHelper(RecyclerView categoriesRecyclerView,
                              RecyclerView momentsRecyclerView,
                              Button btnAddCategory,
                              View momentsHeader,
                              TextView tvCategoryTitle,
                              View floatingNavbarContainer,
                              View oldNavbarContainer,
                              EmptyStateHelper emptyStateHelper,
                              NavbarAutoHideHelper navbarHelper,
                              CategoryStateHelper categoryStateHelper) {
        this.categoriesRecyclerView = categoriesRecyclerView;
        this.momentsRecyclerView = momentsRecyclerView;
        this.btnAddCategory = btnAddCategory;
        this.momentsHeader = momentsHeader;
        this.tvCategoryTitle = tvCategoryTitle;
        this.floatingNavbarContainer = floatingNavbarContainer;
        this.oldNavbarContainer = oldNavbarContainer;
        this.emptyStateHelper = emptyStateHelper;
        this.navbarHelper = navbarHelper;
        this.categoryStateHelper = categoryStateHelper;
    }

    public void setModeChangeListener(OnModeChangeListener listener) {
        this.modeChangeListener = listener;
    }

    public void enterMomentsMode(Category category) {
        if (category == null) return;
        currentCategory = category;
        isInMomentsMode = true;

        // Switch RecyclerViews
        if (categoriesRecyclerView != null) categoriesRecyclerView.setVisibility(View.GONE);
        if (momentsRecyclerView != null) {
            momentsRecyclerView.setVisibility(View.VISIBLE);
        }
        if (btnAddCategory != null) btnAddCategory.setVisibility(View.GONE);

        emptyStateHelper.hide();

        // Show moments header with animation
        if (momentsHeader != null) {
            momentsHeader.setVisibility(View.VISIBLE);
            momentsHeader.setAlpha(0f);
            momentsHeader.animate().alpha(1f).setDuration(200).start();
        }

        // Set category title
        if (tvCategoryTitle != null) {
            tvCategoryTitle.setText(category.getName());
        }

        // Show floating navbar with animation
        if (floatingNavbarContainer != null) {
            floatingNavbarContainer.setVisibility(View.VISIBLE);
            floatingNavbarContainer.setAlpha(0f);
            floatingNavbarContainer.animate().alpha(1f).setDuration(200).start();
        }

        // Hide old navbar
        if (oldNavbarContainer != null) oldNavbarContainer.setVisibility(View.GONE);

        // Save and notify
        categoryStateHelper.saveCategory(category);
        navbarHelper.startHideTimer();

        if (modeChangeListener != null) {
            modeChangeListener.onEnterMomentsMode(category);
        }
    }

    public void exitMomentsMode() {
        isInMomentsMode = false;
        currentCategory = null;

        navbarHelper.cancelHideTimer();

        // Switch RecyclerViews back
        if (momentsRecyclerView != null) momentsRecyclerView.setVisibility(View.GONE);
        if (categoriesRecyclerView != null) categoriesRecyclerView.setVisibility(View.VISIBLE);
        if (btnAddCategory != null) btnAddCategory.setVisibility(View.VISIBLE);

        // Hide moments header
        if (momentsHeader != null) momentsHeader.setVisibility(View.GONE);

        // Hide floating navbar, show old navbar
        if (floatingNavbarContainer != null) floatingNavbarContainer.setVisibility(View.GONE);
        if (oldNavbarContainer != null) oldNavbarContainer.setVisibility(View.VISIBLE);

        // Clear saved category so next launch starts fresh
        categoryStateHelper.clearCategory();

        if (modeChangeListener != null) {
            modeChangeListener.onExitMomentsMode();
        }
    }

    public boolean isInMomentsMode() {
        return isInMomentsMode;
    }

    public Category getCurrentCategory() {
        return currentCategory;
    }

    public RecyclerView getMomentsRecyclerView() {
        return momentsRecyclerView;
    }
}
