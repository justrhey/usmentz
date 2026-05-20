package com.example.usmentz.present;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import java.util.List;

@Dao
public interface PresentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Present present);

    @Update
    void update(Present present);

    @Delete
    void delete(Present present);

    @Query("SELECT * FROM presents ORDER BY createdAt DESC")
    LiveData<List<Present>> getAllPresents();

    @Query("SELECT * FROM presents WHERE id = :id")
    LiveData<Present> getPresentById(int id);

    @Query("SELECT * FROM presents WHERE id = :id")
    Present getPresentByIdSync(int id);

    @Query("SELECT * FROM presents WHERE isActive = 1 LIMIT 1")
    LiveData<Present> getActivePresent();

    @Query("SELECT * FROM presents WHERE isActive = 1 LIMIT 1")
    Present getActivePresentSync();

    @Query("UPDATE presents SET isActive = 0")
    void deactivateAllPresents();

    @Query("UPDATE presents SET isActive = 1 WHERE id = :id")
    void activatePresent(int id);

    @Query("DELETE FROM presents")
    void deleteAll();
}