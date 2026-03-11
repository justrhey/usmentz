package com.example.usmentz;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ReviewDialog {

    public static void show(AppCompatActivity activity, DateLocation dateLocation,
                            OnReviewSavedListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.rating_bar, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText editReview = dialogView.findViewById(R.id.editReview);
        Button btnAddPhoto = dialogView.findViewById(R.id.btnAddPhoto);
        ImageView reviewImage = dialogView.findViewById(R.id.reviewImage);

        // Set existing values
        ratingBar.setRating(dateLocation.getRating());
        editReview.setText(dateLocation.getReview());
        if (dateLocation.getPhotoPath() != null && !dateLocation.getPhotoPath().isEmpty()) {
            reviewImage.setVisibility(View.VISIBLE);
            reviewImage.setImageURI(Uri.parse(dateLocation.getPhotoPath()));
        }

        // Photo picker
        ActivityResultLauncher<Intent> photoPicker = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        reviewImage.setVisibility(View.VISIBLE);
                        reviewImage.setImageURI(selectedImage);
                        dateLocation.setPhotoPath(selectedImage.toString());
                    }
                });

        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            photoPicker.launch(intent);
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnSave = dialogView.findViewById(R.id.btnAddPhoto);
        btnSave.setText("Save Review");
        btnSave.setOnClickListener(v -> {
            dateLocation.setRating(ratingBar.getRating());
            dateLocation.setReview(editReview.getText().toString());
            listener.onReviewSaved(dateLocation);
            dialog.dismiss();
        });
    }

    public interface OnReviewSavedListener {
        void onReviewSaved(DateLocation dateLocation);
    }
}