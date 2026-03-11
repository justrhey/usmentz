package com.example.usmentz.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.usmentz.date.DateLocation;
import com.example.usmentz.repo.DateRepository;

import java.util.List;

public class DateViewModel extends AndroidViewModel {
    private DateRepository repository;
    private LiveData<List<DateLocation>> allMoments;
    private MutableLiveData<Integer> currentCategoryId = new MutableLiveData<>();

    public DateViewModel(Application application) {
        super(application);
        repository = new DateRepository(application);

        allMoments = Transformations.switchMap(currentCategoryId, categoryId -> {
            if (categoryId == null || categoryId == 0) {
                return repository.getAllMoments();
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

    public LiveData<List<DateLocation>> getAllMoments() {
        return repository.getAllMoments();
    }

    public void moveMomentsToCategory(int oldCategoryId, int newCategoryId) {
        repository.updateCategoryForMoments(oldCategoryId, newCategoryId);
    }

    // Add this method for ReviewActivity
    public LiveData<DateLocation> getDateById(int dateId) {
        return repository.getDateById(dateId);
    }



    // Add this method for ReviewActivity
    public void updateReview(int dateId, String review, float rating, String photoPath) {
        repository.updateReview(dateId, review, rating, photoPath);
    }
}