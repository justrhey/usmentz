package com.example.usmentz.fina;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "expenses")
public class Expense implements Serializable {
    public static final String TYPE_EXPENSES = "expenses";
    public static final String TYPE_FUNDS = "funds";
    public static final String TYPE_SAVINGS = "savings";
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String description;
    private double amount;
    private int momentId;
    private long createdAt;
    private String type = TYPE_EXPENSES; // expenses, funds, or savings
    private String paymentMethod = "Cash"; // PayMaya, GCash, BPI, Other Banks, Cash

    public Expense() {}

    @Ignore
    public Expense(String description, double amount, int momentId) {
        this(description, amount, momentId, TYPE_EXPENSES);
    }

    @Ignore
    public Expense(String description, double amount, int momentId, String type) {
        this.description = description;
        this.amount = amount;
        this.momentId = momentId;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public Expense(String description, double amount, int momentId, String type, String paymentMethod) {
        this.description = description;
        this.amount = amount;
        this.momentId = momentId;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getMomentId() { return momentId; }
    public void setMomentId(int momentId) { this.momentId = momentId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}