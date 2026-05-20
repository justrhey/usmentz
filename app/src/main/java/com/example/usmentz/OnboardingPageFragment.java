package com.example.usmentz;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingPageFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_ICON_RES = "icon_res";
    private static final String ARG_BG_COLOR = "bg_color";

    public static OnboardingPageFragment newInstance(String title, String description, int iconRes, int bgColor) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putInt(ARG_ICON_RES, iconRes);
        args.putInt(ARG_BG_COLOR, bgColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        ImageView ivIllustration = view.findViewById(R.id.ivIllustration);
        View illustrationBg = view.findViewById(R.id.illustrationBg);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE));
            tvDescription.setText(getArguments().getString(ARG_DESCRIPTION));
            ivIllustration.setImageResource(getArguments().getInt(ARG_ICON_RES));

            int bgColor = getArguments().getInt(ARG_BG_COLOR);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(bgColor);
            illustrationBg.setBackground(bg);
        }
    }
}
