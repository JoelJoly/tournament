package com.github.joeljoly.tournament;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 6/2/13
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerManagement extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    LinearLayout playersLayout;
    Class parentIntent;
    HashMap<Integer, PlayerWidget> idToWidget;
    private static final int PLAYERS_LIST_LOADER = 0x01;
    private SimpleCursorAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_management);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Bundle activityParameters;
        activityParameters = getIntent().getExtras();
        if (activityParameters != null && activityParameters.containsKey("caller"))
            parentIntent = (Class) activityParameters.get("caller");

        String[] uiBindFrom = {
                TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS
        };
        int[] uiBindTo = {
                R.id.playerNameView,
                R.id.playerPointsView
        };
        getSupportLoaderManager().initLoader(PLAYERS_LIST_LOADER, null, this);
        adapter = new SimpleCursorAdapter(this.getApplicationContext(),
                R.layout.player, null, uiBindFrom, uiBindTo,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        adapter.setViewBinder(new PlayerWidget.ViewBinder(this));

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editIntent = new Intent(PlayerManagement.this, PlayerEdit.class);
                editIntent.putExtra("playerId", (int)id);
                startActivityForResult(editIntent, 2);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PlayerWidget playerWidget = new PlayerWidget(PlayerManagement.this);
                final Integer playerId = (int)id;
                final TournamentDataDbHelper database = new TournamentDataDbHelper(PlayerManagement.this);
                Player clickedPlayer = database.getPlayer(playerId);
                playerWidget.setPlayer(clickedPlayer);
                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(PlayerManagement.this);
                alert.setTitle(R.string.select_player_management_action)
                        .setView(playerWidget)
                        .setPositiveButton(R.string.select_player_management_edit,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent editIntent = new Intent(PlayerManagement.this, PlayerEdit.class);
                                        editIntent.putExtra("playerId", playerId);
                                        startActivityForResult(editIntent, 2);
                                    }
                                })
                        .setNegativeButton(R.string.select_player_management_remove,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        database.deletePlayer(playerId);
                                        getSupportLoaderManager().restartLoader(PlayerManagement.PLAYERS_LIST_LOADER,
                                                null, PlayerManagement.this);
                                    }
                                })
                        .show();
                return true;
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                TournamentDbContract.PlayersEntry._ID,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_LAST_NAME,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS
        };
        CursorLoader cursorLoader = new CursorLoader(this,
                PlayersProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_management_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent backIntent;
                backIntent = new Intent(this, parentIntent != null ? parentIntent : MainPage.class);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backIntent);
                return true;
            case R.id.add_player:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, PlayerEdit.class);
                startActivityForResult(intent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK)
        {
            Integer addedPlayer = data.getIntExtra("added", -1);
            Integer removedPlayer = data.getIntExtra("removed", -1);
            // if we only updated one player, a data changed notification will be enough
            if (addedPlayer == removedPlayer)
                adapter.notifyDataSetChanged();
            else
                // otherwise refresh the view by querying a new loader
                getSupportLoaderManager().restartLoader(PLAYERS_LIST_LOADER, null, this);
        }
    }
}
