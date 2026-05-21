package com.example.usmentz.helper;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

/**
 * iOS-style Swipe-to-Go-Back Helper.
 * Detects swipes starting from the left edge of the screen.
 * When a swipe is detected, it triggers the activity's back navigation.
 */
public class SwipeBackHelper {

    private final Activity activity;
    private float startX;
    private float startY;
    private static final int EDGE_THRESHOLD_DP = 30; // Width of the swipe zone
    private static final int SWIPE_DISTANCE_DP = 60; // Minimum distance to trigger
    private boolean isSwiping = false;

    public SwipeBackHelper(Activity activity) {
        this.activity = activity;
        View root = activity.findViewById(android.R.id.content);
        if (root != null) {
            root.setOnTouchListener(this::handleTouch);
        }
    }

    private boolean handleTouch(View v, MotionEvent event) {
        float edgeThreshold = EDGE_THRESHOLD_DP * v.getResources().getDisplayMetrics().density;
        float swipeDistance = SWIPE_DISTANCE_DP * v.getResources().getDisplayMetrics().density;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                // Only activate if touch is within the left edge zone
                if (startX < edgeThreshold) {
                    isSwiping = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isSwiping) {
                    float currentX = event.getX();
                    float currentY = event.getY();
                    float dx = currentX - startX;
                    float dy = currentY - startY;

                    // If vertical movement is significant, cancel swipe (it's likely a scroll)
                    if (Math.abs(dy) > Math.abs(dx)) {
                        isSwiping = false;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isSwiping) {
                    float endX = event.getX();
                    if (endX - startX > swipeDistance) {
                        // Swipe successful -> Go Back
                        activity.onBackPressed();
                        return true;
                    }
                }
                isSwiping = false;
                break;
        }
        return false;
    }
}
