package com.example.usmentz.helper;

import android.view.View;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Manages scroll-based slide animation for navbar and FAB.
 * When user scrolls DOWN, navbar/FAB slide down (translate Y).
 * When user scrolls UP, navbar/FAB slide back up to original position.
 * 
 * Works with both NestedScrollView and RecyclerView.
 */
public class NavbarScrollHelper {

    private final View navbarView;
    private final View fabView;
    private boolean isNavbarVisible = true;
    private float navbarTranslationY = 0f;
    private float fabTranslationY = 0f;
    private static final int ANIM_DURATION = 250;

    public NavbarScrollHelper(View navbarView, View fabView) {
        this.navbarView = navbarView;
        this.fabView = fabView;
    }

    /**
     * Attach to a NestedScrollView for scroll-based slide animation.
     */
    public void attachToScrollView(NestedScrollView scrollView) {
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                // Scrolling DOWN - hide navbar/FAB
                hideNavbar();
            } else if (scrollY < oldScrollY) {
                // Scrolling UP - show navbar/FAB
                showNavbar();
            }
        });
    }

    /**
     * Attach to a RecyclerView for scroll-based slide animation.
     */
    public void attachToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling DOWN - hide navbar/FAB
                    hideNavbar();
                } else if (dy < 0) {
                    // Scrolling UP - show navbar/FAB
                    showNavbar();
                }
            }
        });
    }

    private void hideNavbar() {
        if (isNavbarVisible && navbarView != null) {
            isNavbarVisible = false;
            navbarTranslationY = navbarView.getHeight() + 20f;
            navbarView.animate()
                    .translationY(navbarTranslationY)
                    .setDuration(ANIM_DURATION)
                    .start();
        }
        if (fabView != null) {
            fabView.animate()
                    .translationY(fabView.getHeight() + 20f)
                    .setDuration(ANIM_DURATION)
                    .start();
        }
    }

    private void showNavbar() {
        if (!isNavbarVisible && navbarView != null) {
            isNavbarVisible = true;
            navbarView.animate()
                    .translationY(0f)
                    .setDuration(ANIM_DURATION)
                    .start();
        }
        if (fabView != null) {
            fabView.animate()
                    .translationY(0f)
                    .setDuration(ANIM_DURATION)
                    .start();
        }
    }

    public boolean isVisible() {
        return isNavbarVisible;
    }

    /**
     * Force show navbar (e.g., on activity resume).
     */
    public void forceShow() {
        isNavbarVisible = true;
        if (navbarView != null) {
            navbarView.setTranslationY(0f);
        }
        if (fabView != null) {
            fabView.setTranslationY(0f);
        }
    }

    public void cleanup() {
        // No handlers to clean up, but kept for API consistency
    }
}
