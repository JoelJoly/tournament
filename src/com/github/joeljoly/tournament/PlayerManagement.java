package com.github.joeljoly.tournament;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 6/2/13
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerManagement extends FragmentActivity {
    TournamentDataDbHelper database;
    LinearLayout playersLayout;
    Class parentIntent;
    HashMap<Integer, PlayerWidget> idToWidget;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_management);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Bundle activityParameters;
        activityParameters = getIntent().getExtras();
        if (activityParameters != null && activityParameters.containsKey("caller"))
            parentIntent = (Class) activityParameters.get("caller");

        playersLayout = (LinearLayout) findViewById(R.id.playersLayout);
        idToWidget = new HashMap<Integer, PlayerWidget>();
        database = new TournamentDataDbHelper(this);
        List<Player> players;
        players = database.getAllPlayers();
        for (final Player player : players)
        {
            PlayerWidget    widget;
            widget = new PlayerWidget(playersLayout.getContext());
            playersLayout.addView(widget);
            widget.setPlayer(player);
            widget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent  editIntent;
                    editIntent = new Intent(PlayerManagement.this, PlayerEdit.class);
                    editIntent.putExtra("playerId", player.getId());
                    startActivityForResult(editIntent, 2);
                }
            });
            idToWidget.put(player.getId(), widget);
        }
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