package com.example.usmentz.fina;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "expenses")
public class Expense implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String description;
    private double amount;
    private int momentId; // Links to the moment

    public Expense() {}

    @Ignore
    public Expense(String description, double amount, int momentId) {
        this.description = description;
        this.amount = amount;
        this.momentId = momentId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getMomentId() { return momentId; }
    public void setMomentId(int momentId) { this.momentId = momentId; }
}