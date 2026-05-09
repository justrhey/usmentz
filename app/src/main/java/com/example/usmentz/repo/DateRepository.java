package com.example.usmentz.repo;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.usmentz.database.DateDatabase;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.dao.CategoryDao;
import com.example.usmentz.dao.DateLocationDao;
import com.example.usmentz.firestore.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DateRepository {

    private static final String TAG = "DateRepository";
    private DateLocationDao dateLocationDao;
    private CategoryDao categoryDao;
    private LiveData<List<DateLocation>> allMoments;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FirestoreHelper firestore;

    public DateRepository(Application application) {
        DateDatabase database = DateDatabase.getDatabase(application);
        dateLocationDao = database.dateLocationDao();
        categoryDao = database.categoryDao();
        allMoments = dateLocationDao.getAllDateLocations();
        firestore = FirestoreHelper.getInstance();
    }

    public LiveData<List<DateLocation>> getAllMoments() {
        return allMoments;
    }

    public LiveData<List<DateLocation>> getMomentsByCategory(int categoryId) {
        return dateLocationDao.getMomentsByCategory(categoryId);
    }

    public void insert(DateLocation dateLocation) {
        executorService.execute(() -> {
            try {
                // Keep local ID for Room
                dateLocationDao.insert(dateLocation);

                // Update category count
                updateCategoryCount(dateLocation.getCategoryId());

                // Sync to Firestore (offline-capable)
                firestore.saveMoment(dateLocation, new FirestoreHelper.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Moment synced to Firestore");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to sync moment to Firestore", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "insert failed", e);
            }
        });
    }

    public void delete(DateLocation dateLocation) {
        executorService.execute(() -> {
            try {
                int categoryId = dateLocation.getCategoryId();
                dateLocationDao.delete(dateLocation);
                updateCategoryCount(categoryId);

                // Sync deletion to Firestore
                if (dateLocation.getId() > 0) {
                    firestore.deleteMoment(dateLocation.getId(), new FirestoreHelper.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Moment deleted from Firestore");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to delete moment from Firestore", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "delete failed", e);
            }
        });
    }

    public void update(DateLocation dateLocation) {
        executorService.execute(() -> {
            try {
                int oldCategoryId = -1;
                DateLocation existing = dateLocationDao.getDateLocationByIdSync(dateLocation.getId());
                if (existing != null) {
                    oldCategoryId = existing.getCategoryId();
                }

                dateLocationDao.update(dateLocation);

                if (oldCategoryId != -1 && oldCategoryId != dateLocation.getCategoryId()) {
                    updateCategoryCount(oldCategoryId);
                }
                updateCategoryCount(dateLocation.getCategoryId());

                // Sync to Firestore
                firestore.saveMoment(dateLocation, new FirestoreHelper.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Moment updated in Firestore");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to update moment in Firestore", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "update failed", e);
            }
        });
    }

    public void updateCategoryForMoments(int oldCategoryId, int newCategoryId) {
        executorService.execute(() -> {
            try {
                dateLocationDao.updateCategoryForMoments(oldCategoryId, newCategoryId);
                updateCategoryCount(oldCategoryId);
                updateCategoryCount(newCategoryId);
            } catch (Exception e) {
                Log.e(TAG, "updateCategoryForMoments failed", e);
            }
        });
    }

    public LiveData<DateLocation> getDateById(int dateId) {
        return dateLocationDao.getDateLocationById(dateId);
    }

    public void updateReview(int dateId, String review, float rating, String photoPath) {
        executorService.execute(() -> {
            try {
                DateLocation dateLocation = dateLocationDao.getDateLocationByIdSync(dateId);
                if (dateLocation != null) {
                    dateLocation.setReview(review);
                    dateLocation.setRating(rating);
                    if (photoPath != null && !photoPath.isEmpty()) {
                        dateLocation.setPhotoPath(photoPath);
                    }
                    dateLocationDao.update(dateLocation);
                    updateCategoryCount(dateLocation.getCategoryId());

                    // Sync review to Firestore
                    firestore.saveMoment(dateLocation, new FirestoreHelper.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Review synced to Firestore");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to sync review to Firestore", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "updateReview failed", e);
            }
        });
    }

    private void updateCategoryCount(int categoryId) {
        if (categoryId <= 0) return;
        try {
            int count = dateLocationDao.getMomentCountByCategory(categoryId);
            categoryDao.updateItemCount(categoryId, count);
        } catch (Exception e) {
            Log.e(TAG, "updateCategoryCount failed", e);
        }
    }

    // ─────────────────────────────────────────────
    // Sync from Firestore to Room (on login)
    // ─────────────────────────────────────────────
    public void syncFromFirestore(Runnable onComplete) {
        firestore.loadMoments(new FirestoreHelper.SyncListCallback<DateLocation>() {
            @Override
            public void onSuccess(List<DateLocation> moments) {
                executorService.execute(() -> {
                    try {
                        // Upsert each Firestore moment into Room
                        for (DateLocation moment : moments) {
                            DateLocation existing = dateLocationDao.getDateLocationByIdSync(moment.getId());
                            if (existing == null) {
                                dateLocationDao.insert(moment);
                            } else {
                                existing.setName(moment.getName());
                                existing.setAddress(moment.getAddress());
                                existing.setDescription(moment.getDescription());
                                existing.setDate(moment.getDate());
                                existing.setRating(moment.getRating());
                                existing.setReview(moment.getReview());
                                existing.setPhotoPath(moment.getPhotoPath());
                                existing.setCompleted(moment.isCompleted());
                                existing.setCategoryId(moment.getCategoryId());
                                existing.setPosition(moment.getPosition());
                                dateLocationDao.update(existing);
                            }
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
                Log.e(TAG, "Failed to load moments from Firestore", e);
                if (onComplete != null) onComplete.run();
            }
        });
    }
}