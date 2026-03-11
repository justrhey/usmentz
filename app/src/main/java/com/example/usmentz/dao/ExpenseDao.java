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

    @Query("SELECT SUM(amount) FROM expenses WHERE momentId = :momentId")
    LiveData<Double> getTotalByMoment(int momentId);

    @Query("DELETE FROM expenses WHERE momentId = :momentId")
    void deleteAllForMoment(int momentId);
}