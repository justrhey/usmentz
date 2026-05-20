package com.example.usmentz;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.usmentz.category.Category;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.List;

public class AddMomentDialog extends DialogFragment {

    private DateViewModel dateViewModel;
    private CategoryViewModel categoryViewModel;
    private Category selectedCategory;
    private OnMomentAddedListener listener;
    private OnCategoryCreatedListener categoryListener;
    private ChipGroup chipGroupCategory;

    // Feeling options
    private static final String[] FEELINGS = {"Cozy", "Romantic", "Fun", "Adventurous", "Relaxing", "Exciting"};

    public interface OnMomentAddedListener {
        void onMomentAdded(DateLocation moment);
    }

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(Category category);
    }

    public static AddMomentDialog newInstance(OnMomentAddedListener listener) {
        return newInstance(listener, null);
    }

    public static AddMomentDialog newInstance(OnMomentAddedListener listener, OnCategoryCreatedListener categoryListener) {
        AddMomentDialog dialog = new AddMomentDialog();
        dialog.listener = listener;
        dialog.categoryListener = categoryListener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dateViewModel = new ViewModelProvider(requireActivity()).get(DateViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_moment, null);

        EditText etName = view.findViewById(R.id.etName);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);
        ChipGroup chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        Chip chipAddCategory = view.findViewById(R.id.chipAddCategory);
        ChipGroup chipGroupFeeling = view.findViewById(R.id.chipGroupFeeling);
        TextView tvMoreDetails = view.findViewById(R.id.tvMoreDetails);
        LinearLayout detailsSection = view.findViewById(R.id.detailsSection);
        Button btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        EditText etCost = view.findViewById(R.id.etCost);
        EditText etReviewNotes = view.findViewById(R.id.etReviewNotes);
        SwitchMaterial switchDoAgain = view.findViewById(R.id.switchDoAgain);

        // Make it a bottom sheet style
        Dialog dialog = new Dialog(requireContext(), R.style.BottomSheetDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Auto-focus name field and show keyboard
        etName.requestFocus();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        // Load categories as chips
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);

        // Setup feeling chips
        setupFeelingChips(chipGroupFeeling);

        // Toggle more details
        tvMoreDetails.setOnClickListener(v -> {
            if (detailsSection.getVisibility() == View.GONE) {
                detailsSection.setVisibility(View.VISIBLE);
                tvMoreDetails.setText("- Hide details");
            } else {
                detailsSection.setVisibility(View.GONE);
                tvMoreDetails.setText("+ Add more details");
            }
        });

        // Add photo (placeholder for now)
        btnAddPhoto.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Photo picker coming soon", Toast.LENGTH_SHORT).show();
        });

        // Inline add category
        chipAddCategory.setOnClickListener(v -> {
            dialog.dismiss();
            showAddCategoryInline();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Enter a place name");
                etName.requestFocus();
                return;
            }

            int catId = selectedCategory != null ? selectedCategory.getId() : 1;
            String catName = selectedCategory != null ? selectedCategory.getName() : "Uncategorized";

            Calendar cal = Calendar.getInstance();
            DateLocation moment = new DateLocation(name, "", "", cal.getTime());
            moment.setCategoryId(catId);

            // Set feeling
            int checkedFeelingId = chipGroupFeeling.getCheckedChipId();
            if (checkedFeelingId != View.NO_ID) {
                Chip checkedChip = chipGroupFeeling.findViewById(checkedFeelingId);
                if (checkedChip != null) {
                    moment.setFeeling(checkedChip.getText().toString());
                }
            }

            // Set cost
            String costText = etCost.getText().toString().trim();
            if (!TextUtils.isEmpty(costText)) {
                try {
                    moment.setCost(Float.parseFloat(costText));
                } catch (NumberFormatException e) {
                    moment.setCost(0f);
                }
            }

            // Set review notes
            String reviewNotes = etReviewNotes.getText().toString().trim();
            if (!TextUtils.isEmpty(reviewNotes)) {
                moment.setReviewNotes(reviewNotes);
            }

            // Set do again
            moment.setDoAgain(switchDoAgain.isChecked());

            dateViewModel.insert(moment);

            Toast.makeText(getContext(), "Added to " + catName, Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onMomentAdded(moment);
            }
            dialog.dismiss();
        });

        return dialog;
    }

    private void setupFeelingChips(ChipGroup chipGroup) {
        for (String feeling : FEELINGS) {
            Chip chip = new Chip(requireContext());
            chip.setText(feeling);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeWidth(1.5f);
            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(0xFF9B5CFF));
            chip.setTextColor(0xFF9B5CFF);
            chipGroup.addView(chip);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCategories(chipGroupCategory);
    }

    private void loadCategories(ChipGroup chipGroupCategory) {
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (!isAdded() || chipGroupCategory == null) return;
            chipGroupCategory.removeAllViews();

            if (categories == null || categories.isEmpty()) {
                Chip chip = new Chip(requireContext());
                chip.setText("Create your first category");
                chip.setCheckable(true);
                chip.setChipBackgroundColorResource(android.R.color.transparent);
                chip.setChipStrokeWidth(1.5f);
                chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(0xFF9B5CFF));
                chip.setTextColor(0xFF9B5CFF);
                chip.setOnClickListener(v -> showAddCategoryInline());
                chipGroupCategory.addView(chip);
                selectedCategory = null;
                return;
            }

            for (int i = 0; i < categories.size(); i++) {
                Category cat = categories.get(i);
                Chip chip = new Chip(requireContext());
                chip.setText(cat.getName());
                chip.setCheckable(true);
                chip.setCheckedIconVisible(true);
                chip.setChipBackgroundColorResource(android.R.color.transparent);
                chip.setChipStrokeWidth(1.5f);

                try {
                    int color = cat.getColor();
                    chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(color));
                    chip.setTextColor(color);
                    chip.setCheckedIconTint(android.content.res.ColorStateList.valueOf(color));
                } catch (Exception e) {
                    chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(0xFF9B5CFF));
                    chip.setTextColor(0xFF9B5CFF);
                }

                if (i == 0) {
                    chip.setChecked(true);
                    selectedCategory = cat;
                }

                chip.setOnClickListener(v -> selectedCategory = cat);
                chipGroupCategory.addView(chip);
            }
        });
    }

    private void showAddCategoryInline() {
        if (!isAdded()) return;

        View inlineForm = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_category_inline, null);

        EditText etCatName = inlineForm.findViewById(R.id.etCategoryName);
        LinearLayout colorPicker = inlineForm.findViewById(R.id.colorPickerContainer);
        View[] colorDots = new View[6];
        colorDots[0] = inlineForm.findViewById(R.id.colorPurple);
        colorDots[1] = inlineForm.findViewById(R.id.colorRed);
        colorDots[2] = inlineForm.findViewById(R.id.colorBlue);
        colorDots[3] = inlineForm.findViewById(R.id.colorGreen);
        colorDots[4] = inlineForm.findViewById(R.id.colorOrange);
        colorDots[5] = inlineForm.findViewById(R.id.colorPink);
        Button btnCancel = inlineForm.findViewById(R.id.btnCancel);
        Button btnCreate = inlineForm.findViewById(R.id.btnCreate);

        int[] colorValues = {0xFF9B5CFF, 0xFFFF5252, 0xFF2196F3, 0xFF4CAF50, 0xFFFF9800, 0xFFE91E63};
        final int[] selectedColor = {colorValues[0]};

        updateColorSelection(colorDots, 0, colorValues);

        for (int i = 0; i < colorDots.length; i++) {
            final int index = i;
            final int color = colorValues[i];
            colorDots[i].setOnClickListener(v -> {
                selectedColor[0] = color;
                updateColorSelection(colorDots, index, colorValues);
            });
        }

        AlertDialog catDialog = new AlertDialog.Builder(requireContext())
                .setView(inlineForm)
                .create();

        btnCancel.setOnClickListener(v -> catDialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            if (!isAdded()) return;
            String name = etCatName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etCatName.setError("Name required");
                return;
            }

            Category newCat = new Category(name, "folder", selectedColor[0]);
            if (categoryListener != null) {
                categoryListener.onCategoryCreated(newCat);
            } else {
                categoryViewModel.insert(newCat);
                Toast.makeText(requireContext(), "Category created", Toast.LENGTH_SHORT).show();
            }
            catDialog.dismiss();
        });

        catDialog.show();
    }

    private void updateColorSelection(View[] dots, int selected, int[] colors) {
        for (int i = 0; i < dots.length; i++) {
            int color = colors[i];
            int sizePx = dpToPx(i == selected ? 36 : 28);
            int marginPx = dpToPx(6);

            android.widget.LinearLayout.LayoutParams params =
                    new android.widget.LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMargins(marginPx, 0, marginPx, 0);
            dots[i].setLayoutParams(params);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            if (i == selected) {
                bg.setStroke(dpToPx(3), android.graphics.Color.WHITE);
                dots[i].setElevation(dpToPx(4));
            } else {
                bg.setStroke(dpToPx(1), 0x33000000);
                dots[i].setElevation(dpToPx(1));
            }
            dots[i].setBackground(bg);
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }
}
