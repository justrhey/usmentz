package com.example.usmentz;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DateLocationDao {
    @Insert
    void insert(DateLocation dateLocation);

    @Update
    void update(DateLocation dateLocation);

    @Delete
    void delete(DateLocation dateLocation);

    @Query("SELECT * FROM date_locations WHERE categoryId = :categoryId ORDER BY position ASC, date DESC")
    LiveData<List<DateLocation>> getMomentsByCategory(int categoryId);

    @Query("SELECT * FROM date_locations WHERE id = :id")
    LiveData<DateLocation> getDateLocationById(int id);

    @Query("SELECT * FROM date_locations ORDER BY position ASC, date DESC")
    LiveData<List<DateLocation>> getAllDateLocations();

    @Query("UPDATE date_locations SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    void updateCategoryForMoments(int oldCategoryId, int newCategoryId);

    @Query("SELECT COUNT(*) FROM date_locations WHERE categoryId = :categoryId")
    int getMomentCountByCategory(int categoryId);

    @Query("SELECT * FROM date_locations WHERE id = :id")
    DateLocation getDateLocationByIdSync(int id);
}