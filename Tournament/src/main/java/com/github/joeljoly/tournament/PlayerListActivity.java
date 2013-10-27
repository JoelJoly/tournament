package com.github.joeljoly.tournament;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


/**
 * An activity representing a list of Players. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PlayerDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PlayerListFragment} and the item details
 * (if present) is a {@link PlayerDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PlayerListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class PlayerListActivity extends FragmentActivity
        implements PlayerListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * The fragment argument representing the current player ID.
     */
    private static final String ARG_PLAYER_ID = "player_id";

    /**
     * The id of the currently selected player.
     */
    private long mCurrentPlayerId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);

        if (findViewById(R.id.player_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((PlayerListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.player_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    protected void onSaveInstanceState(Bundle outBundle)
    {
        outBundle.putLong(ARG_PLAYER_ID, mCurrentPlayerId);
        super.onSaveInstanceState(outBundle);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        mCurrentPlayerId = savedInstanceState.getLong(ARG_PLAYER_ID, 0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Callback method from {@link PlayerListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(long id) {
        mCurrentPlayerId = id;
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(PlayerDetailFragment.ARG_ITEM_ID, id);
            PlayerDetailFragment fragment = new PlayerDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_detail_container, fragment)
                    .commit();
            // rebuild the menu
            supportInvalidateOptionsMenu();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PlayerDetailActivity.class);
            detailIntent.putExtra(PlayerDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTwoPane) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.player_list_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mTwoPane) {
            boolean enabled = mCurrentPlayerId != 0;
            MenuItem editAction = menu.findItem(R.id.edit_player);
            editAction.setEnabled(enabled);
            editAction.getIcon().setAlpha(enabled ? 255 : 64);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_player:
                if (mCurrentPlayerId != 0) {
                    Intent editIntent = new Intent(this, PlayerEdit.class);
                    editIntent.putExtra("playerId", (int)mCurrentPlayerId);
                    startActivityForResult(editIntent, 2);
                    return true;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
