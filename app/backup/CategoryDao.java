package com.example.usmentz;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    void insert(CategoryDialog.Category category);

    @Update
    void update(CategoryDialog.Category category);

    @Delete
    void delete(CategoryDialog.Category category);

    @Query("SELECT * FROM categories ORDER BY id ASC")
    LiveData<List<CategoryDialog.Category>> getAllCategories();

    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<CategoryDialog.Category> getCategoryById(int id);

    @Query("UPDATE categories SET itemCount = (SELECT COUNT(*) FROM date_locations WHERE categoryId = :categoryId) WHERE id = :categoryId")
    void updateItemCount(int categoryId);

    @Query("SELECT * FROM categories WHERE id = :id")
    CategoryDialog.Category getCategorySync(int id); // Synchronous version for updates
}