package com.example.usmentz;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {DateLocation.class, CategoryDialog.Category.class},
        version = 9, // Increment version
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class DateDatabase extends RoomDatabase {
    public abstract DateLocationDao dateLocationDao();
    public abstract CategoryDao categoryDao();

    private static volatile DateDatabase INSTANCE;

    static DateDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DateDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    DateDatabase.class, "usmentz_database")
                            .fallbackToDestructiveMigration() // This will delete old database
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}