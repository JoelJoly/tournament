package com.github.joeljoly.tournament;

import android.app.ActionBar;
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
    TournamentDataDbHelper database;
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
                TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME
        };
        int[] uiBindTo = {
                R.id.playerNameView
        };
        getSupportLoaderManager().initLoader(PLAYERS_LIST_LOADER, null, this);
        adapter = new SimpleCursorAdapter(this.getApplicationContext(),
                R.layout.player, null, uiBindFrom, uiBindTo,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                TournamentDbContract.PlayersEntry._ID,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME
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
            Player newPlayer;
            newPlayer = (Player) data.getSerializableExtra("result");
            if (newPlayer == null)
                throw new IllegalStateException("Returning activity should have set a Player if it returns OK");
            PlayerWidget widget;
            switch (requestCode)
            {
                case 1:
                    // create and insert new widget
                    widget = new PlayerWidget(playersLayout.getContext());
                    playersLayout.addView(widget);
                    widget.setPlayer(newPlayer);
                    break;
                case 2:
                    // search a potential already existing widget
                    widget = idToWidget.get(newPlayer.getId());
                    if (widget == null)
                    {
                        Integer previousId;
                        previousId = (Integer) data.getSerializableExtra("previousId");
                        if (previousId == null)
                            throw new IllegalStateException("Returning activity should have set a previousId if the player id has changed");
                        widget = idToWidget.get(previousId);
                        if (widget == null)
                            throw new IllegalStateException("Returning activity returned an invalid previousId");
                    }
                    widget.setPlayer(newPlayer);
                    break;
            }
        }
    }
}
