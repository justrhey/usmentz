package com.example.usmentz.repo;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;

import com.example.usmentz.dao.ExpenseDao;
import com.example.usmentz.database.DateDatabase;
import com.example.usmentz.fina.Expense;
import com.example.usmentz.firestore.FirestoreHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private static final String TAG = "ExpenseRepository";
    private final ExpenseDao expenseDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final FirestoreHelper firestore;

    public ExpenseRepository(Application application) {
        DateDatabase db = DateDatabase.getDatabase(application);
        expenseDao = db.expenseDao();
        firestore = FirestoreHelper.getInstance();
    }

    public void insert(Expense expense) {
        executor.execute(() -> {
            try {
                expenseDao.insert(expense);
                
                // Sync to Firestore
                firestore.saveExpense(expense, new FirestoreHelper.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Expense synced to Firestore");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to sync expense to Firestore", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "insert failed", e);
            }
        });
    }

    public void update(Expense expense) {
        executor.execute(() -> {
            try {
                expenseDao.update(expense);
                
                // Sync to Firestore
                firestore.saveExpense(expense, new FirestoreHelper.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Expense updated in Firestore");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to update expense in Firestore", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "update failed", e);
            }
        });
    }

    public void delete(Expense expense) {
        executor.execute(() -> {
            try {
                int localId = expense.getId();
                expenseDao.delete(expense);
                
                // Sync deletion to Firestore
                if (localId > 0) {
                    firestore.deleteExpense(localId, new FirestoreHelper.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Expense deleted from Firestore");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to delete expense from Firestore", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "delete failed", e);
            }
        });
    }

    public void deleteAllForMoment(int momentId) {
        executor.execute(() -> {
            try {
                // First get all expenses for this moment to delete from Firestore
                List<Expense> expenses = expenseDao.getExpensesForMomentSync(momentId);
                expenseDao.deleteAllForMoment(momentId);
                
                // Delete from Firestore
                for (Expense exp : expenses) {
                    if (exp.getId() > 0) {
                        final int localId = exp.getId();
                        firestore.deleteExpense(localId, new FirestoreHelper.SyncCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Expense deleted from Firestore for moment, localId: " + localId);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to delete expense from Firestore", e);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "deleteAllForMoment failed", e);
            }
        });
    }

    public LiveData<List<Expense>> getExpensesByMoment(int momentId) {
        return expenseDao.getExpensesByMoment(momentId);
    }

    public LiveData<Double> getTotalByMoment(int momentId) {
        return expenseDao.getTotalByMoment(momentId);
    }

    public LiveData<Double> getTotalSpent() {
        return expenseDao.getTotalSpent();
    }

    // Get all expenses and filter by month in Java (to avoid schema changes)
    public LiveData<List<Expense>> getAllExpenses() {
        return expenseDao.getAllExpenses();
    }

    public LiveData<Double> getTotalByType(String type) {
        return expenseDao.getTotalByType(type);
    }

    // ─────────────────────────────────────────────
    // Sync from Firestore to Room (on login)
    // ─────────────────────────────────────────────
    public void syncFromFirestore(Runnable onComplete) {
        firestore.loadExpenses(new FirestoreHelper.SyncListCallback<Expense>() {
            @Override
            public void onSuccess(List<Expense> expenses) {
                executor.execute(() -> {
                    try {
                        // Clear old data first so no previous user's data leaks through
                        expenseDao.deleteAll();

                        // Insert all Firestore expenses into Room
                        for (Expense exp : expenses) {
                            expenseDao.insert(exp);
                        }
                        Log.d(TAG, "Synced " + expenses.size() + " expenses from Firestore to Room");
                        if (onComplete != null) onComplete.run();
                    } catch (Exception e) {
                        Log.e(TAG, "syncFromFirestore failed", e);
                        if (onComplete != null) onComplete.run();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load expenses from Firestore", e);
                if (onComplete != null) onComplete.run();
            }
        });
    }
}
