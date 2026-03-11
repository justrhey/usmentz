package com.example.usmentz.repo;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.usmentz.category.Category;
import com.example.usmentz.database.DateDatabase;
import com.example.usmentz.dao.CategoryDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private CategoryDao categoryDao;
    private LiveData<List<Category>> allCategories;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CategoryRepository(Application application) {
        DateDatabase database = DateDatabase.getDatabase(application);
        categoryDao = database.categoryDao();
        allCategories = categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public void insert(Category category) {
        executorService.execute(() -> categoryDao.insert(category));
    }

    public void update(Category category) {
        executorService.execute(() -> categoryDao.update(category));
    }

    public void delete(Category category) {
        executorService.execute(() -> categoryDao.delete(category));
    }
}