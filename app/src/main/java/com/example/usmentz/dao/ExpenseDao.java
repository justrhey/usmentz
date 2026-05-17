package com.example.usmentz.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.usmentz.fina.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses WHERE momentId = :momentId ORDER BY id ASC")
    LiveData<List<Expense>> getExpensesByMoment(int momentId);

    // Synchronous version for use in background threads
    @Query("SELECT * FROM expenses WHERE momentId = :momentId ORDER BY id ASC")
    List<Expense> getExpensesForMomentSync(int momentId);

    @Query("SELECT SUM(amount) FROM expenses WHERE momentId = :momentId AND type = 'expenses'")
    LiveData<Double> getTotalByMoment(int momentId);

    @Query("SELECT SUM(amount) FROM expenses")
    LiveData<Double> getTotalSpent();

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    LiveData<List<Expense>> getAllExpenses();

    @Query("SELECT SUM(amount) FROM expenses WHERE type = :type")
    LiveData<Double> getTotalByType(String type);

    @Query("DELETE FROM expenses WHERE momentId = :momentId")
    void deleteAllForMoment(int momentId);

    // Delete all expenses (for sync)
    @Query("DELETE FROM expenses")
    void deleteAll();
}
