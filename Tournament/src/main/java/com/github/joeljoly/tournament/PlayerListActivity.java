package com.github.joeljoly.tournament;

import android.app.Activity;
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
 * to listen for item selections and
 * {@link PlayerDetailFragment.Callbacks} interface
 * to listen for player modifications.
 */
public class PlayerListActivity extends FragmentActivity
        implements PlayerListFragment.Callbacks, PlayerDetailFragment.Callbacks {

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
     * Store the activity result data.
     */
    private Intent mActivityResultData;

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
                    .setTrackSelectedItem(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link PlayerListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(long id) {
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
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PlayerDetailActivity.class);
            detailIntent.putExtra(PlayerDetailFragment.ARG_ITEM_ID, id);
            startActivityForResult(detailIntent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // don't act on result yet, this may cause exception when doing fragment transaction
            // see http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
            mActivityResultData = data;
        }
    }

    @Override
    public void onPlayerChanged(Intent data) {
        ((PlayerListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.player_list))
                .onPlayerModified(data);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mActivityResultData != null) {
            // Commit your transactions here.
            onPlayerChanged(mActivityResultData);
        }
        // Reset the data back to null for next time.
        mActivityResultData = null;
    }
}
