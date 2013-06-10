package com.github.joeljoly.tournament;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by Joel on 10/06/13.
 */
public class PlayersProvider extends ContentProvider {
    private TournamentDataDbHelper  mDatabase;

    private static final String AUTHORITY = "com.github.joeljoly.tournament.PlayersProvider";
    public static final int PLAYERS = 100;
    public static final int PLAYER_ID = 110;
    private static final String PLAYERS_BASE_PATH = "players";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PLAYERS_BASE_PATH);
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/mt-player";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/mt-player";
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, PLAYERS_BASE_PATH, PLAYERS);
        sURIMatcher.addURI(AUTHORITY, PLAYERS_BASE_PATH + "/#", PLAYER_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabase = new TournamentDataDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TournamentDbContract.PlayersEntry.TABLE_NAME);
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case PLAYER_ID:
                // filter request based on player id
                queryBuilder.appendWhere(TournamentDbContract.PlayersEntry.COLUMN_NAME_ID + "="
                        + uri.getLastPathSegment());
                break;
            case PLAYERS:
                // no filter
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = queryBuilder.query(mDatabase.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = mDatabase.getWritableDatabase().update(TournamentDbContract.PlayersEntry.TABLE_NAME,
                values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
        int rowsAffected = 0;
        switch (sURIMatcher.match(uri)) {
            case PLAYERS:
                rowsAffected = sqlDB.delete(TournamentDbContract.PlayersEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case PLAYER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(TournamentDbContract.PlayersEntry.TABLE_NAME,
                            TournamentDbContract.PlayersEntry.COLUMN_NAME_ID + "=" + id, null);
                } else {
                    rowsAffected = sqlDB.delete(TournamentDbContract.PlayersEntry.TABLE_NAME,
                            selection + " and " + TournamentDbContract.PlayersEntry.COLUMN_NAME_ID + "=" + id,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = mDatabase.getWritableDatabase().insert(TournamentDbContract.PlayersEntry.TABLE_NAME,
                null, values);
        if (rowID > 0) {
            Uri uriWithId = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(uriWithId, null);
            return (uriWithId);
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case PLAYERS:
                return "com.github.joeljoly.cursor.dir/playersprovider";
            case PLAYER_ID:
                return "com.github.joeljoly.cursor.item/playersprovider";
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
    }
}

