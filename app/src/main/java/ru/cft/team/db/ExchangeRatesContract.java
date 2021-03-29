package ru.cft.team.db;

import android.provider.BaseColumns;

public class ExchangeRatesContract {

    public static final class ExchangeRatesEntry implements BaseColumns{
        public static final String TABLE_NAME = "exchangeRates";
        public static final String COLUMN_ID_FROM_SERVICE ="idFromService";
        public static final String COLUMN_CHAR_CODE ="charCode";
        public static final String COLUMN_NOMINAL ="nominal";
        public static final String COLUMN_NAME ="name";
        public static final String COLUMN_VALUE ="value";
        public static final String COLUMN_REPEAT_INDEX ="repeatIndex";

        public static final String TYPE_TEXT = "TEXT";
        public static final String TYPE_INTEGER = "INTEGER";
        public static final String TYPE_DOUBLE = "REAL";

        public static final String CREATE_COMMAND = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + "(" + _ID + " " + TYPE_INTEGER + " PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ID_FROM_SERVICE + " " + TYPE_TEXT + ", " +
                COLUMN_CHAR_CODE + " " + TYPE_TEXT + ", " +
                COLUMN_NOMINAL + " " + TYPE_INTEGER + ", " +
                COLUMN_NAME + " " + TYPE_TEXT + ", " +
                COLUMN_VALUE + " " + TYPE_DOUBLE + ", " +
                COLUMN_REPEAT_INDEX + " " + TYPE_INTEGER + ")";

        public static final String DROP_COMMAND = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }
}
