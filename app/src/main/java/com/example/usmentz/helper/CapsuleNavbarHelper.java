package com.example.usmentz.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.usmentz.CalendarActivity;
import com.example.usmentz.FavoritesActivity;
import com.example.usmentz.HomeActivity;
import com.example.usmentz.MainActivity;
import com.example.usmentz.ProfileActivity;
import com.example.usmentz.R;

public class CapsuleNavbarHelper {

    public static void setup(Activity activity, LinearLayout navContainer,
                             LinearLayout navItemHome, LinearLayout navItemCategories,
                             LinearLayout navItemCalendar, LinearLayout navItemFavorites, LinearLayout navItemSettings,
                             ImageView navIconHome, ImageView navIconCategories, ImageView navIconCalendar,
                             ImageView navIconFavorites, ImageView navIconSettings,
                             TextView navLabelHome, TextView navLabelCategories, TextView navLabelCalendar,
                             TextView navLabelFavorites, TextView navLabelSettings,
                             int activeIndex) {

        // Store references for the click listener
        final LinearLayout[] items = { navItemHome, navItemCategories, navItemCalendar, navItemFavorites, navItemSettings };
        final ImageView[] icons = { navIconHome, navIconCategories, navIconCalendar, navIconFavorites, navIconSettings };
        final TextView[] labels = { navLabelHome, navLabelCategories, navLabelCalendar, navLabelFavorites, navLabelSettings };

        View.OnClickListener clickListener = v -> {
            int index = -1;
            if (v == navItemHome) index = 0;
            else if (v == navItemCategories) index = 1;
            else if (v == navItemCalendar) index = 2;
            else if (v == navItemFavorites) index = 3;
            else if (v == navItemSettings) index = 4;

            if (index != -1 && index != activeIndex) {
                navigate(activity, index);
            }
        };

        navItemHome.setOnClickListener(clickListener);
        navItemCategories.setOnClickListener(clickListener);
        navItemCalendar.setOnClickListener(clickListener);
        navItemFavorites.setOnClickListener(clickListener);
        navItemSettings.setOnClickListener(clickListener);

        // Initial state
        updateState(navContainer, items, icons, labels, activeIndex, activity, false);
    }

    public static void updateState(LinearLayout navContainer, LinearLayout[] items, ImageView[] icons,
                                   TextView[] labels, int activeIndex, Activity activity, boolean animate) {

        if (animate) {
            TransitionManager.beginDelayedTransition(navContainer);
        }

        int activeColor = ContextCompat.getColor(activity, R.color.white);
        int inactiveColor = ContextCompat.getColor(activity, R.color.text_hint);
        int activeBg = R.drawable.bg_nav_active_pill;
        int inactiveBg = Color.TRANSPARENT;
        float activeWeight = 2.5f;
        float inactiveWeight = 1.0f;

        for (int i = 0; i < items.length; i++) {
            boolean isActive = (i == activeIndex);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) items[i].getLayoutParams();
            params.weight = isActive ? activeWeight : inactiveWeight;
            items[i].setLayoutParams(params);

            items[i].setBackgroundResource(isActive ? activeBg : inactiveBg);
            icons[i].setColorFilter(isActive ? activeColor : inactiveColor);
            
            // Icon scale animation
            if (isActive) {
                icons[i].animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
                labels[i].setVisibility(View.VISIBLE);
            } else {
                icons[i].animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                labels[i].setVisibility(View.GONE);
            }
        }
    }

    private static void navigate(Activity activity, int index) {
        Class<?> target = null;
        switch (index) {
            case 0: target = HomeActivity.class; break;
            case 1: target = MainActivity.class; break;
            case 2: target = CalendarActivity.class; break;
            case 3: target = FavoritesActivity.class; break;
            case 4: target = ProfileActivity.class; break;
        }

        if (target != null) {
            Intent intent = new Intent(activity, target);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}
