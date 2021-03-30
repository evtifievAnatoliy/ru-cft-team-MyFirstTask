package ru.cft.team.dao;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import ru.cft.team.models.ExchangeRate;

@Database(entities = {ExchangeRate.class}, version = 1, exportSchema = false)
public abstract class ExchangeRatesDatabase extends RoomDatabase {

    private static  ExchangeRatesDatabase database;
    private static final String DB_NAME = "exchangeRatesRoom.db";
    private static final Object LOCK = new Object();

    public static ExchangeRatesDatabase getInstance(Context context){
        synchronized (LOCK) {
            if (database == null) {
                database = Room.databaseBuilder(context, ExchangeRatesDatabase.class, DB_NAME)
                        .allowMainThreadQueries() //удалить!!!
                        .build();
            }
        }
        return database;
    }

    public abstract IExchangeRatesDao exchangeRatesDao();

}
