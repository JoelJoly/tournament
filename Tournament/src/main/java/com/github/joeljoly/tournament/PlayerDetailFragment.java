package com.github.joeljoly.tournament;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.github.joeljoly.tournament.dummy.DummyContent;

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
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayerDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            int playerId = new Long(getArguments().getLong(ARG_ITEM_ID)).intValue();
            mItem = new TournamentDataDbHelper(getActivity()).getPlayer(playerId);
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
}
