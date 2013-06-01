package com.github.joeljoly.tournament;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 6/1/13
 * Time: 11:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class TournamentDataDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Tournament.db";

    public TournamentDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TournamentDbContract.PlayersEntry.sqlCreateEntries(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // when we'll have different versions, rewrite this to convert data, for now drop everything
        db.execSQL("DROP TABLE IF EXISTS " + TournamentDbContract.PlayersEntry.TABLE_NAME);
        onCreate(db);
    }
}
