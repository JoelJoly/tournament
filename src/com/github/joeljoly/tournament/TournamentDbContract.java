package com.github.joeljoly.tournament;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 6/2/13
 * Time: 12:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class TournamentDbContract {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String PRIMARY_KEY = " PRIMARY KEY,";
    private static final String COMMA_SEP = ",";

    public static abstract class PlayersEntry implements BaseColumns {
        public static final String TABLE_NAME = "players";
        public static final String COLUMN_NAME_ID = BaseColumns._ID;
        public static final String COLUMN_NAME_FIRST_NAME = "firstName";
        public static final String COLUMN_NAME_LAST_NAME = "lastName";
        // this field may change, so each tournament should copy the points of a player when it is created
        public static final String COLUMN_NAME_POINTS = "currentPoints";

        // Do not instantiate this helper class
        private PlayersEntry() {}

        // create the table for players data
        public static void sqlCreateEntries(SQLiteDatabase db) {
            final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + INT_TYPE + PRIMARY_KEY +
                    COLUMN_NAME_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_LAST_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_POINTS + INT_TYPE +
                " )";
            db.execSQL(SQL_CREATE_ENTRIES);
        }
    }
}
