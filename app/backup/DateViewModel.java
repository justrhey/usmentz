package com.example.usmentz;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

public class DateViewModel extends AndroidViewModel {
    private DateRepository repository;
    private LiveData<List<DateLocation>> allMoments;
    private MutableLiveData<Integer> currentCategoryId = new MutableLiveData<>();

    public DateViewModel(Application application) {
        super(application);
        repository = new DateRepository(application);

        // Transform based on current category
        allMoments = Transformations.switchMap(currentCategoryId, categoryId -> {
            if (categoryId == null || categoryId == 0) {
                return repository.getAllMoments(); // Show all if no category
            } else {
                return repository.getMomentsByCategory(categoryId);
            }
        });
    }

    public LiveData<List<DateLocation>> getMoments() {
        return allMoments;
    }

    public void setCurrentCategory(int categoryId) {
        currentCategoryId.setValue(categoryId);
    }

    public void insert(DateLocation dateLocation) {
        repository.insert(dateLocation);
    }

    public void delete(DateLocation dateLocation) {
        repository.delete(dateLocation);
    }

    public void update(DateLocation dateLocation) {
        repository.update(dateLocation);
    }

    public void moveMomentsToCategory(int oldCategoryId, int newCategoryId) {
        repository.updateCategoryForMoments(oldCategoryId, newCategoryId);
    }



    public LiveData<DateLocation> getDateById(int dateId) {
        return repository.getDateById(dateId);
    }

    public void updateReview(int dateId, String review, float rating, String photoPath) {
        repository.updateReview(dateId, review, rating, photoPath);
    }
}