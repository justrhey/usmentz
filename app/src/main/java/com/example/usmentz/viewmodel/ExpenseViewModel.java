package com.example.usmentz.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.usmentz.fina.Expense;
import com.example.usmentz.repo.ExpenseRepository;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private ExpenseRepository repository;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
    }

    public LiveData<List<Expense>> getExpensesForMoment(int momentId) {
        return repository.getExpensesByMoment(momentId);
    }

    public LiveData<Double> getTotalSpentForMoment(int momentId) {
        return repository.getTotalByMoment(momentId);
    }

    public void insert(Expense expense) {
        repository.insert(expense);
    }

    public void update(Expense expense) {
        repository.update(expense);
    }

    public void delete(Expense expense) {
        repository.delete(expense);
    }

    public void deleteAllExpensesForMoment(int momentId) {
        repository.deleteAllForMoment(momentId);
    }
}