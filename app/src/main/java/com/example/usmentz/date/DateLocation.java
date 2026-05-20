package com.example.usmentz.date;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "date_locations")
public class DateLocation implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String address;
    private String description;
    private Date date;
    private boolean isCompleted;

    // Legacy fields (kept for backward compatibility)
    @Deprecated
    private float rating;
    private String photoPath;
    private int position;

    // New fields for the scrapbook vision
    private String feeling;        // Mood/emoji: "cozy", "romantic", "fun", etc.
    private boolean doAgain;       // "Would you do this again?" signal
    private float cost;            // Optional expense for this moment
    private String reviewNotes;    // Private journal entry
    private String borderStyle;    // Photo border: "clean", "polaroid", "vintage", "heart", "film", "minimal"

    private int categoryId;

    // Constructor for Room (no-arg constructor)
    public DateLocation() {}

    // Constructor for app usage
    @Ignore
    public DateLocation(String name, String address, String description, Date date) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.date = date;
        this.isCompleted = false;
        this.rating = 0f;
        this.photoPath = "";
        this.position = 0;
        this.categoryId = 1;
        this.feeling = "";
        this.doAgain = false;
        this.cost = 0f;
        this.reviewNotes = "";
        this.borderStyle = "clean";
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    @Deprecated
    public float getRating() { return rating; }
    @Deprecated
    public void setRating(float rating) { this.rating = rating; }

    // Photo URI (legacy name kept for DB compatibility)
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    // Convenience alias for photoPath
    public String getPhotoUri() { return photoPath; }
    public void setPhotoUri(String photoUri) { this.photoPath = photoUri; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    // New fields
    public String getFeeling() { return feeling; }
    public void setFeeling(String feeling) { this.feeling = feeling; }

    public boolean isDoAgain() { return doAgain; }
    public void setDoAgain(boolean doAgain) { this.doAgain = doAgain; }

    public float getCost() { return cost; }
    public void setCost(float cost) { this.cost = cost; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public String getBorderStyle() { return borderStyle; }
    public void setBorderStyle(String borderStyle) { this.borderStyle = borderStyle; }

    // Legacy alias for backward compatibility
    @Deprecated
    public String getReview() { return reviewNotes != null ? reviewNotes : ""; }
    @Deprecated
    public void setReview(String review) { this.reviewNotes = review; }
}
