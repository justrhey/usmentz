package com.example.usmentz.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.usmentz.category.Category;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.fina.Expense;
import com.example.usmentz.dao.CategoryDao;
import com.example.usmentz.dao.DateLocationDao;
import com.example.usmentz.dao.ExpenseDao;

@Database(
        entities = {DateLocation.class, Category.class, Expense.class}, // Make sure Expense is here
        version = 12, // Increment version if needed
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class DateDatabase extends RoomDatabase {
    public abstract DateLocationDao dateLocationDao();
    public abstract CategoryDao categoryDao();
    public abstract ExpenseDao expenseDao(); // Make sure this exists

    private static volatile DateDatabase INSTANCE;

    public static DateDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DateDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    DateDatabase.class, "usmentz_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}