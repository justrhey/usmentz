package com.example.usmentz.present;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(
    tableName = "present_moments",
    foreignKeys = {
        @ForeignKey(
            entity = Present.class,
            parentColumns = "id",
            childColumns = "presentId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = { @Index("presentId") }
)
public class PresentMoment implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int presentId;
    private int momentId;
    private int position;
    private int status; // 0=pending, 1=done
    private long completedAt;

    public PresentMoment() {
        this.status = 0;
        this.completedAt = 0;
    }

    public PresentMoment(int presentId, int momentId, int position) {
        this.presentId = presentId;
        this.momentId = momentId;
        this.position = position;
        this.status = 0;
        this.completedAt = 0;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPresentId() { return presentId; }
    public void setPresentId(int presentId) { this.presentId = presentId; }

    public int getMomentId() { return momentId; }
    public void setMomentId(int momentId) { this.momentId = momentId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public boolean isDone() { return status == 1; }
    public void markDone() {
        this.status = 1;
        this.completedAt = System.currentTimeMillis();
    }
}