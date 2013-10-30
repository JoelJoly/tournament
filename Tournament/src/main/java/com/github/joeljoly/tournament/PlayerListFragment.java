package com.github.joeljoly.tournament;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

/**
 * A list fragment representing a list of Players. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link PlayerDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PlayerListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * Flag to know if selected item must be tracked. Only used on tablets.
     */
    private boolean mTrackSelectedItem = false;

    /**
     * The adapter to present data from database.
     */
    private SimpleCursorAdapter adapter;

    /**
     * Id for the adapter loader.
     */
    private static final int PLAYERS_LIST_LOADER = 0x01;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(long id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
        adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.player, null, uiBindFrom, uiBindTo,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        adapter.setViewBinder(new PlayerWidget.ViewBinder(getActivity()));
        getActivity().getSupportLoaderManager().initLoader(PLAYERS_LIST_LOADER, null, this);

        setListAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        if (mTrackSelectedItem) {
            setActivatedPosition(position);
        }
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setTrackSelectedItem(boolean trackSelectedItem) {
        mTrackSelectedItem = trackSelectedItem;
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(mTrackSelectedItem
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        }
        if (position != ListView.INVALID_POSITION) {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
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
                selection = TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME + " LIKE ?" +
                        " OR " + TournamentDbContract.PlayersEntry.COLUMN_NAME_LAST_NAME + " LIKE ?";
                selectionArgs = new String[] { "%"+searchValue+"%", "%"+searchValue+"%" };
            }
        }
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                PlayersProvider.CONTENT_URI, projection, selection, selectionArgs,
                TournamentDbContract.PlayersEntry.COLUMN_NAME_FIRST_NAME + " ASC");
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_player_list_menu, menu);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            MenuItem searchItem = menu.findItem(R.id.search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setSubmitButtonEnabled(false);
            searchView.setIconifiedByDefault(true);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                private boolean mEmptySearchText = true;
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Bundle bundle = new Bundle();
                    bundle.putString("search", newText);
                    // method gets called when menu is initialized, avoid unnecessary reloads
                    boolean isTextEmpty = newText.isEmpty();
                    if (!isTextEmpty || !mEmptySearchText)
                        getActivity().getSupportLoaderManager().restartLoader(PLAYERS_LIST_LOADER, bundle, PlayerListFragment.this);
                    mEmptySearchText = isTextEmpty;
                    return true;
                }
            });
            // the following should reset the search but is not called
            // see http://stackoverflow.com/questions/9327826/searchviews-oncloselistener-doesnt-work
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    // reset search filter when search is closed
                    Bundle emptyBundle = new Bundle();
                    getActivity().getSupportLoaderManager().restartLoader(PLAYERS_LIST_LOADER, emptyBundle, PlayerListFragment.this);
                    return false; // let normal behavior proceed
                }
            });
            // workaround for the setOnCloseListener() problem above
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem menuItem) {
                    return true; // true if the item should expand
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                    // reset search filter when search item is collapsed
                    Bundle emptyBundle = new Bundle();
                    getActivity().getSupportLoaderManager().restartLoader(PLAYERS_LIST_LOADER, emptyBundle, PlayerListFragment.this);
                    return true; // true if the item should collapse
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_player:
                Intent addIntent = new Intent(getActivity(), PlayerEdit.class);
                startActivityForResult(addIntent, 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            onPlayerModified(data);
        }
    }

    public void onPlayerModified(Intent data) {
        Integer addedPlayer = data.getIntExtra("added", -1);
        Integer removedPlayer = data.getIntExtra("removed", -1);
        // check if player has been removed
        if (addedPlayer == -1 && removedPlayer != -1) {
            TournamentDataDbHelper database = new TournamentDataDbHelper(getActivity());
            database.deletePlayer(removedPlayer);
        }
        if (mTrackSelectedItem) {
            long newItemToSelect = addedPlayer;
            if (addedPlayer == -1 && mActivatedPosition != ListView.INVALID_POSITION) {
                long selectedItemId = getListView().getItemIdAtPosition(mActivatedPosition);
                if (selectedItemId == removedPlayer) {
                    int newPosition = ListView.INVALID_POSITION;
                    // select next player (if possible)
                    if (mActivatedPosition + 1 < getListAdapter().getCount()) {
                        newPosition = mActivatedPosition + 1;
                    } // otherwise select previous player (if possible)
                    else if (mActivatedPosition > 0){
                        newPosition = mActivatedPosition - 1;
                        setActivatedPosition(newPosition);
                    }
                    newItemToSelect = newPosition != ListView.INVALID_POSITION ?
                            getListAdapter().getItemId(newPosition) : -1;
                }
            }
            mCallbacks.onItemSelected(newItemToSelect);
        }
        // if we only updated one player, a data changed notification will be enough
        if (addedPlayer == removedPlayer)
            adapter.notifyDataSetChanged();
        else
            // otherwise refresh the view by querying a new loader
            getActivity().getSupportLoaderManager().restartLoader(PLAYERS_LIST_LOADER, null, this);
    }

}
