package com.example.usmentz;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {
    private CategoryRepository repository;
    private LiveData<List<CategoryDialog.Category>> allCategories;

    public CategoryViewModel(Application application) {
        super(application);
        repository = new CategoryRepository(application);
        allCategories = repository.getAllCategories();
    }

    public LiveData<List<CategoryDialog.Category>> getAllCategories() {
        return allCategories;
    }

    public void insert(CategoryDialog.Category category) {
        repository.insert(category);
    }

    public void update(CategoryDialog.Category category) {
        repository.update(category);
    }

    public void delete(CategoryDialog.Category category) {
        repository.delete(category);
    }
}