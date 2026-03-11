package com.example.usmentz.repo;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.usmentz.dao.ExpenseDao;
import com.example.usmentz.database.DateDatabase;
import com.example.usmentz.fina.Expense;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private final ExpenseDao expenseDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExpenseRepository(Application application) {
        DateDatabase db = DateDatabase.getDatabase(application);
        expenseDao = db.expenseDao();
    }

    public void insert(Expense expense) {
        executor.execute(() -> expenseDao.insert(expense));
    }

    public void update(Expense expense) {
        executor.execute(() -> expenseDao.update(expense));
    }

    public void delete(Expense expense) {
        executor.execute(() -> expenseDao.delete(expense));
    }

    public void deleteAllForMoment(int momentId) {
        executor.execute(() -> expenseDao.deleteAllForMoment(momentId));
    }

    public LiveData<List<Expense>> getExpensesByMoment(int momentId) {
        return expenseDao.getExpensesByMoment(momentId);
    }

    public LiveData<Double> getTotalByMoment(int momentId) {
        return expenseDao.getTotalByMoment(momentId);
    }
}