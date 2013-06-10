package com.github.joeljoly.tournament;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 6/1/13
 * Time: 11:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class TournamentDataDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
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
        // convert from version 1 to version 2
        if (oldVersion == 1)
        {
            db.beginTransaction();

            try {
                String temporaryTableName = "tempCopyForIdRename";
                // rename table with a temporary name
                db.execSQL("ALTER TABLE " + TournamentDbContract.PlayersEntry.TABLE_NAME + " RENAME TO " +  temporaryTableName);
                // recreate the table with correct columns name
                TournamentDbContract.PlayersEntry.sqlCreateEntries(db);
                String originalIdName = "id";
                // copy rows from temp table to new one
                db.execSQL("INSERT INTO " + TournamentDbContract.PlayersEntry.TABLE_NAME + "(" +
                        TournamentDbContract.PlayersEntry._ID + "," +
                        TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME + "," +
                        TournamentDbContract.PlayersEntry.COLUMN_NAME_LAST_NAME + "," +
                        TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS + ") " +
                        "SELECT " +
                        originalIdName + "," +
                        TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME + "," +
                        TournamentDbContract.PlayersEntry.COLUMN_NAME_LAST_NAME + "," +
                        TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS +
                        " FROM " + temporaryTableName + ";");
                // erase temporary table
                db.execSQL("DROP TABLE " + temporaryTableName);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                db.endTransaction();
            }
        }
    }

    private ContentValues contentFromPlayer(Player player) {
        ContentValues values = new ContentValues();
        values.put(TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME, player.getFirstName());
        values.put(TournamentDbContract.PlayersEntry.COLUMN_NAME_LAST_NAME, player.getLastName());
        values.put(TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS, player.getPoints());
        values.put(TournamentDbContract.PlayersEntry._ID, player.getId());
        return values;
    }
    long addPlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values;
        values = contentFromPlayer(player);

        // Inserting Row
        long rowId;
        rowId = db.insert(TournamentDbContract.PlayersEntry.TABLE_NAME, null, values);
        db.close(); // Closing database connection
        return rowId;
    }

    private Player createPlayerFromCursor(Cursor cursor) {
        return new Player(
            cursor.getInt(cursor.getColumnIndex(TournamentDbContract.PlayersEntry.COLUMN_NAME_ID)),
            cursor.getString(cursor.getColumnIndex(TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME)),
            cursor.getString(cursor.getColumnIndex(TournamentDbContract.PlayersEntry.COLUMN_NAME_LAST_NAME)),
            cursor.getInt(cursor.getColumnIndex(TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS)));
    }
    // Getting single contact
    Player getPlayer(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TournamentDbContract.PlayersEntry.TABLE_NAME,
            new String[] {
                TournamentDbContract.PlayersEntry._ID,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_LAST_NAME,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS,
            },
            TournamentDbContract.PlayersEntry._ID + " = ?",
            new String[] { String.valueOf(id) },
            null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        // return player
        return createPlayerFromCursor(cursor);
    }

    // Getting all players
    public List<Player> getAllPlayers() {
        List<Player> playerList = new ArrayList<Player>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TournamentDbContract.PlayersEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // create and add player
                playerList.add(createPlayerFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return player list
        return playerList;
    }

    // Updating single player
    public int updateContact(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values;
        values = contentFromPlayer(player);

        // updating row
        return db.update(
            TournamentDbContract.PlayersEntry.TABLE_NAME,
            values,
            TournamentDbContract.PlayersEntry._ID + " = ?",
            new String[] { String.valueOf(player.getId()) });
    }

    // Deleting single player
    public void deletePlayer(Player player) {
        deletePlayer(player.getId());
    }
    // Deleting single player
    public void deletePlayer(Integer playerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(
                TournamentDbContract.PlayersEntry.TABLE_NAME,
                TournamentDbContract.PlayersEntry._ID + " = ?",
                new String[] { String.valueOf(playerId) });
        db.close();
    }

    // Getting players count
    public int getPlayersCount() {
        String countQuery = "SELECT * FROM " + TournamentDbContract.PlayersEntry.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        // return count
        return count;
    }

}
