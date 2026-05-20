package com.example.usmentz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREF_NAME = "UsmentzPrefs";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private ViewPager2 viewPager;
    private Button btnNext;
    private TextView btnSkip;
    private LinearLayout dotIndicator;

    private static final int PAGE_COUNT = 3;

    private final OnboardingPage[] pages = {
        new OnboardingPage(
            "Remember what made your dates special",
            "Usmentz helps you capture the little details — the mood, the moments, the memories — so you never forget what made a date perfect.",
            R.drawable.ic_onboarding_guide,
            0xFFF3E8FF
        ),
        new OnboardingPage(
            "Capture the full story",
            "Add a photo, describe what you did, note how it felt, and even what you spent. Every detail becomes part of your shared story.",
            R.drawable.ic_onboarding_capture,
            0xFFE8D5F5
        ),
        new OnboardingPage(
            "Plan your next date, better than the last",
            "Look back at your scrapbook of moments. See patterns in what you both love. Let your past dates inspire your next one.",
            R.drawable.ic_onboarding_reflect,
            0xFFEDE7F6
        )
    };

    static class OnboardingPage {
        String title, description;
        int iconRes, bgColor;

        OnboardingPage(String title, String description, int iconRes, int bgColor) {
            this.title = title;
            this.description = description;
            this.iconRes = iconRes;
            this.bgColor = bgColor;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        dotIndicator = findViewById(R.id.dotIndicator);

        viewPager.setAdapter(new OnboardingAdapter(this));
        viewPager.setOffscreenPageLimit(1);

        setupDots();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                if (position == PAGE_COUNT - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < PAGE_COUNT - 1) {
                viewPager.setCurrentItem(current + 1, true);
            } else {
                completeOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> completeOnboarding());
    }

    private void setupDots() {
        dotIndicator.removeAllViews();
        for (int i = 0; i < PAGE_COUNT; i++) {
            View dot = new View(this);
            int size = dpToPx(i == 0 ? 10 : 8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setBackgroundResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            dotIndicator.addView(dot);
        }
    }

    private void updateDots(int selectedPosition) {
        for (int i = 0; i < dotIndicator.getChildCount(); i++) {
            View dot = dotIndicator.getChildAt(i);
            int size = dpToPx(i == selectedPosition ? 10 : 8);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            params.width = size;
            params.height = size;
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == selectedPosition ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void completeOnboarding() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply();
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private class OnboardingAdapter extends FragmentStateAdapter {
        OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            OnboardingPage page = pages[position];
            return OnboardingPageFragment.newInstance(page.title, page.description, page.iconRes, page.bgColor);
        }

        @Override
        public int getItemCount() {
            return PAGE_COUNT;
        }
    }
}
