package com.example.usmentz.present;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "presents")
public class Present implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private boolean isActive;
    private long createdAt;

    public Present() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = false;
    }

    public Present(String name, String description, Date startDate, Date endDate) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}