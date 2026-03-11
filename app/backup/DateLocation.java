package com.example.usmentz;

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
    private float rating;
    private String review;
    private String photoPath;
    private int position;
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
        this.review = "";
        this.photoPath = "";
        this.position = 0;
        this.categoryId = 1;
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

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}