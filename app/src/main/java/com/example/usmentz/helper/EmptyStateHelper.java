package com.example.usmentz.helper;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.usmentz.R;

import java.util.Random;

/**
 * Manages empty state display for RecyclerViews.
 * Extracted from MainActivity to reduce class size.
 */
public class EmptyStateHelper {

    private final LinearLayout emptyStateLayout;
    private final int[] drawables;

    public EmptyStateHelper(LinearLayout emptyStateLayout, int[] drawables) {
        this.emptyStateLayout = emptyStateLayout;
        this.drawables = drawables != null && drawables.length > 0 ? drawables : new int[0];
    }

    public void show(String title, String subtitle) {
        if (emptyStateLayout == null) return;
        emptyStateLayout.setVisibility(View.VISIBLE);

        TextView emptyText = emptyStateLayout.findViewById(R.id.emptyStateText);
        TextView emptySubtext = emptyStateLayout.findViewById(R.id.emptyStateSubtext);
        ImageView emptyImage = emptyStateLayout.findViewById(R.id.emptyStateImage);

        if (emptyText != null) emptyText.setText(title);
        if (emptySubtext != null) emptySubtext.setText(subtitle);
        if (emptyImage != null && drawables.length > 0) {
            emptyImage.setImageResource(drawables[new Random().nextInt(drawables.length)]);
        }
    }

    public void hide() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }
}
