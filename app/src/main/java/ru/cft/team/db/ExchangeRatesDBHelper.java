package ru.cft.team.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ExchangeRatesDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "exchangeRates.db";
    private static final int DB_VERSION = 1;

    public ExchangeRatesDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ExchangeRatesContract.ExchangeRatesEntry.CREATE_COMMAND);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ExchangeRatesContract.ExchangeRatesEntry.DROP_COMMAND);
        onCreate(db);
    }
}
