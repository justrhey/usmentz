package com.example.usmentz;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private CategoryDao categoryDao;
    private LiveData<List<CategoryDialog.Category>> allCategories;
    private ExecutorService executorService;

    public CategoryRepository(Application application) {
        DateDatabase database = DateDatabase.getDatabase(application);
        categoryDao = database.categoryDao();
        allCategories = categoryDao.getAllCategories();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<CategoryDialog.Category>> getAllCategories() {
        return allCategories;
    }

    public void insert(CategoryDialog.Category category) {
        executorService.execute(() -> categoryDao.insert(category));
    }

    public void update(CategoryDialog.Category category) {
        executorService.execute(() -> categoryDao.update(category));
    }

    public void delete(CategoryDialog.Category category) {
        executorService.execute(() -> categoryDao.delete(category));
    }
}