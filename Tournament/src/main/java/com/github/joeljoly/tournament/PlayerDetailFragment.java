package com.github.joeljoly.tournament;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

/**
 * A fragment representing a single Player detail screen.
 * This fragment is either contained in a {@link PlayerListActivity}
 * in two-pane mode (on tablets) or a {@link PlayerDetailActivity}
 * on handsets.
 */
public class PlayerDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The player content this fragment is presenting.
     */
    private Player mItem;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of player
     * modifications.
     */
    public interface Callbacks {
        /**
         * Callback for when a player is modified.
         */
        public void onPlayerChanged(Intent data);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onPlayerChanged(Intent data) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            int playerId = Long.valueOf(getArguments().getLong(ARG_ITEM_ID)).intValue();
            if (playerId >= 0) {
                try {
                    mItem = new TournamentDataDbHelper(getActivity()).getPlayer(playerId);
                } catch (java.lang.Exception e) {
                    Toast.makeText(getActivity(),
                            getString(R.string.error_loading_player, playerId, e.getMessage()),
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_detail, container, false);

        if (mItem != null) {
            TextView nameEdit;
            nameEdit = (TextView) rootView.findViewById(R.id.nameEdit);
            TextView idEdit;
            idEdit = (TextView) rootView.findViewById(R.id.idEdit);
            TextView pointsEdit;
            pointsEdit = (TextView) rootView.findViewById(R.id.pointEdit);

            nameEdit.setText(mItem.getFirstName() + " " + mItem.getLastName());
            idEdit.setText(mItem.getId().toString());
            pointsEdit.setText(mItem.getPoints().toString());
        }

        return rootView;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mItem != null) {
            inflater.inflate(R.menu.player_detail_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_player:
                Intent editIntent = new Intent(getActivity(), PlayerEdit.class);
                editIntent.putExtra("playerId", mItem.getId());
                startActivityForResult(editIntent, 2);
                return true;
            case R.id.erase_player:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirm_player_remove_title)
                        .setMessage(getString(R.string.confirm_player_remove, mItem.getFirstName(), mItem.getLastName()))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.validate_player_remove,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent returnIntent;
                                        returnIntent = new Intent();
                                        returnIntent.putExtra("removed", mItem.getId());
                                        mCallbacks.onPlayerChanged(returnIntent);
                                    }
                                })
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            mCallbacks.onPlayerChanged(data);
        }
    }
}
