package com.example.usmentz.helper;

import android.os.Handler;
import android.view.View;

/**
 * Manages auto-hide/show behavior for navbar and header views.
 * Extracted from MainActivity to reduce class size.
 */
public class NavbarAutoHideHelper {

    private final Handler hideHandler = new Handler();
    private Runnable hideRunnable;
    private boolean isNavVisible = true;
    private final long hideDelayMs;

    private final View navView;
    private final View headerView;

    public NavbarAutoHideHelper(View navView, View headerView, long hideDelayMs) {
        this.navView = navView;
        this.headerView = headerView;
        this.hideDelayMs = hideDelayMs;
    }

    public void startHideTimer() {
        cancelHideTimer();
        hideRunnable = () -> {
            isNavVisible = false;
            animateOut(navView);
            animateOut(headerView);
        };
        hideHandler.postDelayed(hideRunnable, hideDelayMs);
    }

    public void cancelHideTimer() {
        if (hideRunnable != null) {
            hideHandler.removeCallbacks(hideRunnable);
        }
    }

    public void showAndResetTimer() {
        if (!isNavVisible) {
            isNavVisible = true;
            animateIn(navView);
            animateIn(headerView);
        }
        startHideTimer();
    }

    public boolean isVisible() {
        return isNavVisible;
    }

    public void cleanup() {
        cancelHideTimer();
    }

    private void animateOut(View view) {
        if (view == null) return;
        view.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    private void animateIn(View view) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(200).start();
    }
}
