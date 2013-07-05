package com.github.joeljoly.tournament;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.Arrays;
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
    private LinearLayout playersLayout;
    private Class parentIntent;
    private HashMap<Integer, PlayerWidget> idToWidget;
    private static final int PLAYERS_LIST_LOADER = 0x01;
    private SimpleCursorAdapter adapter;
    private ScrollView playersScrollView;

    private interface ActionsDialogFragmentListener {
        public void onActionClick(int which);
    }
    public class ActionsDialogFragment extends DialogFragment {
        ListView choiceListView;
        ActionsDialogFragmentListener listener;
        ArrayList<String> choices;
        Player player;
        ActionsDialogFragment(Player actionPlayer, ArrayList<String> actionChoices, ActionsDialogFragmentListener actionListener)
        {
            listener = actionListener;
            choices = actionChoices;
            player = actionPlayer;
        }
        /** The system calls this to get the DialogFragment's layout, regardless
         of whether it's being displayed as a dialog or an embedded fragment. */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            getDialog().setTitle(R.string.select_player_management_action);
            // Inflate the layout to use as dialog or embedded fragment
            View view = inflater.inflate(R.layout.player_actions, container, false);
            choiceListView = (ListView) view.findViewById(R.id.actionsListView);
            PlayerWidget playerPreview = (PlayerWidget) view.findViewById(R.id.playerPreview);
            playerPreview.setPlayer(player);
            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
            arrayAdapter.addAll(choices);
            choiceListView.setAdapter(arrayAdapter);
            choiceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listener.onActionClick(position);
                }
            });
            return view;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_management);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
//            doMySearch(query);
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Bundle activityParameters;
        activityParameters = getIntent().getExtras();
        if (activityParameters != null && activityParameters.containsKey("caller"))
            parentIntent = (Class) activityParameters.get("caller");

        String[] uiBindFrom = {
                TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS
        };
        int[] uiBindTo = {
                R.id.playerNameView,
                R.id.playerPointsView,
                R.id.rankTextView
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
                final Integer playerId = (int)id;
                final TournamentDataDbHelper database = new TournamentDataDbHelper(PlayerManagement.this);
                final Player clickedPlayer = database.getPlayer(playerId);
                Resources res = getResources();
                String[] choices = {
                        res.getString(R.string.select_player_management_remove),
                        res.getString(R.string.select_player_management_edit)
                };
                DialogFragment newFragment = new ActionsDialogFragment(clickedPlayer, new ArrayList<String>(Arrays.asList(choices)), new ActionsDialogFragmentListener() {
                    @Override
                    public void onActionClick(int which) {
                        switch (which) {
                            case 0:
                                new AlertDialog.Builder(PlayerManagement.this)
                                        .setTitle(R.string.confirm_player_remove_title)
                                        .setMessage(getString(R.string.confirm_player_remove, clickedPlayer.getFirstName(), clickedPlayer.getLastName()))
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .setPositiveButton(R.string.validate_player_remove,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        database.deletePlayer(playerId);
                                                        getSupportLoaderManager().restartLoader(PlayerManagement.PLAYERS_LIST_LOADER,
                                                                null, PlayerManagement.this);
                                                    }
                                                })
                                        .show();
                                break;
                            case 1:
                                Intent editIntent = new Intent(PlayerManagement.this, PlayerEdit.class);
                                editIntent.putExtra("playerId", playerId);
                                startActivityForResult(editIntent, 2);
                                break;
                        }
                    }
                });
                newFragment.show(getSupportFragmentManager(), "actions");

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_management_menu, menu);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Bundle bundle = new Bundle();
                    bundle.putString("search", newText);
                    getSupportLoaderManager().restartLoader(PLAYERS_LIST_LOADER, bundle, PlayerManagement.this);
                    return true;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                TournamentDbContract.PlayersEntry._ID,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_LAST_NAME,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_POINTS
        };
        String selection = null;
        String[] selectionArgs = null;
        if (args != null && args.containsKey("search")) {
            String searchValue = (String) args.get("search");
            if (!searchValue.isEmpty()) {
                selection = TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME + " LIKE ?";
                selectionArgs = new String[] { "%"+searchValue+"%" };
            }
        }
        CursorLoader cursorLoader = new CursorLoader(this,
                PlayersProvider.CONTENT_URI, projection, selection, selectionArgs, null);
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
