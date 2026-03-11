package com.example.usmentz.repo;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.usmentz.database.DateDatabase;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.dao.CategoryDao;
import com.example.usmentz.dao.DateLocationDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DateRepository {
    private DateLocationDao dateLocationDao;
    private CategoryDao categoryDao;
    private LiveData<List<DateLocation>> allMoments;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DateRepository(Application application) {
        DateDatabase database = DateDatabase.getDatabase(application);
        dateLocationDao = database.dateLocationDao();
        categoryDao = database.categoryDao();
        allMoments = dateLocationDao.getAllDateLocations();
    }

    public LiveData<List<DateLocation>> getAllMoments() {
        return allMoments;
    }

    public LiveData<List<DateLocation>> getMomentsByCategory(int categoryId) {
        return dateLocationDao.getMomentsByCategory(categoryId);
    }

    public void insert(DateLocation dateLocation) {
        executorService.execute(() -> {
            dateLocationDao.insert(dateLocation);
            updateCategoryCount(dateLocation.getCategoryId());
        });
    }

    public void delete(DateLocation dateLocation) {
        executorService.execute(() -> {
            int categoryId = dateLocation.getCategoryId();
            dateLocationDao.delete(dateLocation);
            updateCategoryCount(categoryId);
        });
    }

    public void update(DateLocation dateLocation) {
        executorService.execute(() -> {
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
        });
    }

    public void updateCategoryForMoments(int oldCategoryId, int newCategoryId) {
        executorService.execute(() -> {
            dateLocationDao.updateCategoryForMoments(oldCategoryId, newCategoryId);
            updateCategoryCount(oldCategoryId);
            updateCategoryCount(newCategoryId);
        });
    }

    public LiveData<DateLocation> getDateById(int dateId) {
        return dateLocationDao.getDateLocationById(dateId);
    }

    public void updateReview(int dateId, String review, float rating, String photoPath) {
        executorService.execute(() -> {
            DateLocation dateLocation = dateLocationDao.getDateLocationByIdSync(dateId);
            if (dateLocation != null) {
                dateLocation.setReview(review);
                dateLocation.setRating(rating);
                if (photoPath != null && !photoPath.isEmpty()) {
                    dateLocation.setPhotoPath(photoPath);
                }
                dateLocationDao.update(dateLocation);
                updateCategoryCount(dateLocation.getCategoryId());
            }
        });
    }

    private void updateCategoryCount(int categoryId) {
        int count = dateLocationDao.getMomentCountByCategory(categoryId);
        categoryDao.updateItemCount(categoryId, count);
    }
}