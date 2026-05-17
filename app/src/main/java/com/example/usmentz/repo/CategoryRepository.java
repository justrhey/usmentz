package com.example.usmentz.repo;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;

import com.example.usmentz.category.Category;
import com.example.usmentz.database.DateDatabase;
import com.example.usmentz.dao.CategoryDao;
import com.example.usmentz.firestore.FirestoreHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {

    private static final String TAG = "CategoryRepository";
    private CategoryDao categoryDao;
    private LiveData<List<Category>> allCategories;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FirestoreHelper firestore;

    public CategoryRepository(Application application) {
        DateDatabase database = DateDatabase.getDatabase(application);
        categoryDao = database.categoryDao();
        allCategories = categoryDao.getAllCategories();
        firestore = FirestoreHelper.getInstance();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public void insert(Category category) {
        executorService.execute(() -> {
            try {
                categoryDao.insert(category);

                // Sync to Firestore
                firestore.saveCategory(category, new FirestoreHelper.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Category synced to Firestore");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to sync category to Firestore", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "insert failed", e);
            }
        });
    }

    public void update(Category category) {
        executorService.execute(() -> {
            try {
                categoryDao.update(category);

                // Sync update to Firestore
                firestore.saveCategory(category, new FirestoreHelper.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Category updated in Firestore");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to update category in Firestore", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "update failed", e);
            }
        });
    }

    public void delete(Category category) {
        executorService.execute(() -> {
            try {
                categoryDao.delete(category);

                // Sync deletion to Firestore
                if (category.getId() > 0) {
                    firestore.deleteCategory(category.getId(), new FirestoreHelper.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Category deleted from Firestore");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to delete category from Firestore", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "delete failed", e);
            }
        });
    }

    // ─────────────────────────────────────────────
    // Sync from Firestore to Room (on login)
    // ─────────────────────────────────────────────
    public void syncFromFirestore(Runnable onComplete) {
        firestore.loadCategories(new FirestoreHelper.SyncListCallback<Category>() {
            @Override
            public void onSuccess(List<Category> categories) {
                executorService.execute(() -> {
                    try {
                        // Clear old data first so no previous user's data leaks through
                        categoryDao.deleteAll();

                        // Insert all Firestore categories into Room
                        for (Category cat : categories) {
                            categoryDao.insert(cat);
                        }
                        if (onComplete != null) onComplete.run();
                    } catch (Exception e) {
                        Log.e(TAG, "syncFromFirestore failed", e);
                        if (onComplete != null) onComplete.run();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load categories from Firestore", e);
                if (onComplete != null) onComplete.run();
            }
        });
    }
}