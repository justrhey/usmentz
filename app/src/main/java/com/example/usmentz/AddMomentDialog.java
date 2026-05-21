package com.example.usmentz;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.usmentz.category.Category;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.viewmodel.CategoryViewModel;
import com.example.usmentz.viewmodel.DateViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddMomentDialog extends DialogFragment {

    private DateViewModel dateViewModel;
    private CategoryViewModel categoryViewModel;
    private Category selectedCategory;
    private OnMomentAddedListener listener;

    // Photo
    private FrameLayout photoArea;
    private LinearLayout photoPlaceholder;
    private ShapeableImageView ivPhotoPreview;
    private TextView btnChangePhoto;
    private Uri photoUri;

    // Border picker
    private LinearLayout borderPickerSection;
    private String selectedBorder = "clean";

    // Details
    private TextInputEditText etName, etCost, etReviewNotes;
    private ChipGroup chipGroupFeeling;
    private Button btnCancel, btnSave;

    // Vibrator for haptics
    private Vibrator vibrator;

    // Feeling options
    private static final String[] FEELINGS = {"Cozy", "Romantic", "Fun", "Adventurous", "Relaxing", "Exciting"};

    // Photo pickers
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    photoUri = uri;
                    showPhotoPreview();
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoUri != null) {
                    showPhotoPreview();
                }
            });

    public interface OnMomentAddedListener {
        void onMomentAdded(DateLocation moment);
    }

    public static AddMomentDialog newInstance(OnMomentAddedListener listener) {
        AddMomentDialog dialog = new AddMomentDialog();
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dateViewModel = new ViewModelProvider(requireActivity()).get(DateViewModel.class);
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        // Init vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vm.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        }

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_photo_booth, null);

        // Photo area
        photoArea = view.findViewById(R.id.photoArea);
        photoPlaceholder = view.findViewById(R.id.photoPlaceholder);
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);

        // Border picker
        borderPickerSection = view.findViewById(R.id.borderPickerSection);
        setupBorderPicker(view);

        // Details
        etName = view.findViewById(R.id.etName);
        etCost = view.findViewById(R.id.etCost);
        etReviewNotes = view.findViewById(R.id.etReviewNotes);
        chipGroupFeeling = view.findViewById(R.id.chipGroupFeeling);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);

        // Setup feeling chips
        setupFeelingChips();

        // Photo tap → pick
        photoArea.setOnClickListener(v -> showPhotoPickerDialog());
        btnChangePhoto.setOnClickListener(v -> showPhotoPickerDialog());

        // Cancel
        btnCancel.setOnClickListener(v -> {
            hapticClick();
            dismiss();
        });

        // Save
        btnSave.setOnClickListener(v -> {
            hapticClick();
            saveMoment();
        });

        // Style as bottom sheet
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

        return dialog;
    }

    private void setupBorderPicker(View root) {
        int[] borderIds = {R.id.borderClean, R.id.borderPolaroid, R.id.borderVintage};
        final String[] borderNames = {"clean", "polaroid", "vintage"};

        for (int i = 0; i < borderIds.length; i++) {
            final int idx = i;
            View borderOption = root.findViewById(borderIds[i]);
            borderOption.setOnClickListener(v -> {
                hapticClick();
                selectedBorder = borderNames[idx];
                applyBorderSelection(root, borderIds, idx);
            });
        }

        // Default: clean selected
        applyBorderSelection(root, borderIds, 0);
    }

    private void applyBorderSelection(View root, int[] borderIds, int selectedIdx) {
        int purple = 0xFF9B5CFF;
        int gray = 0xFFBDBDBD;

        for (int i = 0; i < borderIds.length; i++) {
            View option = root.findViewById(borderIds[i]);
            View inner = ((FrameLayout) option).getChildAt(0);
            if (i == selectedIdx) {
                option.setAlpha(1.0f);
                inner.setElevation(8f);
                // Add purple ring via background
                inner.setBackgroundResource(getBorderDrawable(borderIds[i]));
                // We'll overlay a ring using the parent's foreground
                option.setForeground(requireContext().getDrawable(R.drawable.border_selected_ring));
            } else {
                option.setAlpha(0.5f);
                inner.setElevation(0f);
                option.setForeground(null);
            }
        }
    }

    private int getBorderDrawable(int borderId) {
        if (borderId == R.id.borderClean) return R.drawable.border_clean;
        if (borderId == R.id.borderPolaroid) return R.drawable.border_polaroid;
        if (borderId == R.id.borderVintage) return R.drawable.border_vintage;
        return R.drawable.border_clean;
    }

    private void showPhotoPreview() {
        if (ivPhotoPreview != null && photoUri != null) {
            photoPlaceholder.setVisibility(View.GONE);
            ivPhotoPreview.setVisibility(View.VISIBLE);
            btnChangePhoto.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(photoUri)
                    .centerCrop()
                    .into(ivPhotoPreview);

            // Show border picker after photo is picked
            if (borderPickerSection != null) {
                borderPickerSection.setVisibility(View.VISIBLE);
            }

            // Apply selected border style to photo
            applyBorderToPhoto();
        }
    }

    private void applyBorderToPhoto() {
        if (ivPhotoPreview == null) return;

        int bgRes = R.drawable.border_clean; // default
        switch (selectedBorder) {
            case "polaroid": bgRes = R.drawable.border_polaroid; break;
            case "vintage": bgRes = R.drawable.border_vintage; break;
        }
        ivPhotoPreview.setBackgroundResource(bgRes);
    }

    private void saveMoment() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter a name");
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

        // Set photo path
        if (photoUri != null) {
            moment.setPhotoPath(photoUri.toString());
        }
        // Always set border style (default "clean" even without photo)
        moment.setBorderStyle(selectedBorder != null ? selectedBorder : "clean");

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

        dateViewModel.insert(moment);

        Toast.makeText(getContext(), "Memory saved to " + catName, Toast.LENGTH_SHORT).show();

        if (listener != null) {
            listener.onMomentAdded(moment);
        }
        dismiss();
    }

    private void hapticClick() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (SecurityException e) {
            // Silently ignore
        }
    }

    private void showPhotoPickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        takePhoto();
                    } else {
                        pickFromGallery();
                    }
                })
                .show();
    }

    // Photobooth Launcher
    private final ActivityResultLauncher<Intent> photoboothLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    String uriString = result.getData().getStringExtra("photo_uri");
                    if (uriString != null) {
                        photoUri = Uri.parse(uriString);
                        showPhotoPreview();
                    }
                }
            });

    private void takePhoto() {
        Intent intent = new Intent(requireContext(), PhotoboothActivity.class);
        photoboothLauncher.launch(intent);
    }

    private void pickFromGallery() {
        galleryLauncher.launch("image/*");
    }

    private void setupFeelingChips() {
        chipGroupFeeling.removeAllViews();
        for (String feeling : FEELINGS) {
            Chip chip = new Chip(requireContext());
            chip.setText(feeling);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeWidth(1.5f);
            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(0xFF9B5CFF));
            chip.setTextColor(0xFF9B5CFF);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    hapticClick();
                }
            });
            chipGroupFeeling.addView(chip);
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
}
