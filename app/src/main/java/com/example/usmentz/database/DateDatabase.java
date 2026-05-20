package com.example.usmentz.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.usmentz.category.Category;
import com.example.usmentz.date.DateLocation;
import com.example.usmentz.fina.Expense;
import com.example.usmentz.dao.CategoryDao;
import com.example.usmentz.dao.DateLocationDao;
import com.example.usmentz.dao.ExpenseDao;

@Database(
        entities = {DateLocation.class, Category.class, Expense.class},
        version = 20,
        exportSchema = false
    )
@TypeConverters({Converters.class})
public abstract class DateDatabase extends RoomDatabase {
    public abstract DateLocationDao dateLocationDao();
    public abstract CategoryDao categoryDao();
    public abstract ExpenseDao expenseDao();

    private static volatile DateDatabase INSTANCE;

    // Migration from 17 to 18: Add Expense.paymentMethod column
    static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE expenses ADD COLUMN paymentMethod TEXT DEFAULT 'Cash'");
        }
    };

    // Migration from 18 to 19: Add DateLocation.position column (if not exists)
    static final Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                database.execSQL("ALTER TABLE date_locations ADD COLUMN position INTEGER NOT NULL DEFAULT 0");
            } catch (Exception e) {
                // Column already exists, skip
            }
        }
    };

    // Migration from 19 to 20: Add feeling, doAgain, cost, reviewNotes to DateLocation
    static final Migration MIGRATION_19_20 = new Migration(19, 20) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE date_locations ADD COLUMN feeling TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE date_locations ADD COLUMN doAgain INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE date_locations ADD COLUMN cost REAL DEFAULT 0.0");
            database.execSQL("ALTER TABLE date_locations ADD COLUMN reviewNotes TEXT DEFAULT ''");
        }
    };

    public static DateDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DateDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    DateDatabase.class, "usmentz_database")
                            .addMigrations(MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
