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
public interface PresentMomentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PresentMoment pm);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PresentMoment> moments);

    @Update
    void update(PresentMoment pm);

    @Delete
    void delete(PresentMoment pm);

    @Query("DELETE FROM present_moments WHERE presentId = :presentId")
    void deleteByPresent(int presentId);

    @Query("SELECT * FROM present_moments WHERE presentId = :presentId ORDER BY position ASC")
    LiveData<List<PresentMoment>> getMomentsForPresent(int presentId);

    @Query("SELECT * FROM present_moments WHERE presentId = :presentId ORDER BY position ASC")
    List<PresentMoment> getMomentsForPresentSync(int presentId);

    @Query("SELECT * FROM present_moments WHERE presentId = :presentId AND momentId = :momentId LIMIT 1")
    PresentMoment getPresentMoment(int presentId, int momentId);

    @Query("UPDATE present_moments SET status = 1, completedAt = :time WHERE presentId = :presentId AND momentId = :momentId")
    void markDone(int presentId, int momentId, long time);

    @Query("SELECT COUNT(*) FROM present_moments WHERE presentId = :presentId")
    int getTotalCount(int presentId);

    @Query("SELECT COUNT(*) FROM present_moments WHERE presentId = :presentId AND status = 1")
    int getCompletedCount(int presentId);

    @Query("DELETE FROM present_moments")
    void deleteAll();
}