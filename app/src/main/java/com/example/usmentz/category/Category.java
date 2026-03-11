package com.example.usmentz.category;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "categories")
public class Category implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String iconName; // Changed from emoji to iconName
    private int color;
    private int itemCount;

    // Required empty constructor for Room
    public Category() {}

    // Constructor for creating new categories
    @Ignore
    public Category(String name, String iconName, int color) {
        this.name = name;
        this.iconName = iconName;
        this.color = color;
        this.itemCount = 0;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}