package com.example.usmentz.model;

public class Review {
    private int momentId;
    private String momentName;
    private String reviewText;
    private float rating;
    private String photoPath;
    private String date;

    public Review(int momentId, String momentName, String reviewText, float rating, String photoPath, String date) {
        this.momentId = momentId;
        this.momentName = momentName;
        this.reviewText = reviewText;
        this.rating = rating;
        this.photoPath = photoPath;
        this.date = date;
    }

    public int getMomentId() { return momentId; }
    public String getMomentName() { return momentName; }
    public String getReviewText() { return reviewText; }
    public float getRating() { return rating; }
    public String getPhotoPath() { return photoPath; }
    public String getDate() { return date; }
}