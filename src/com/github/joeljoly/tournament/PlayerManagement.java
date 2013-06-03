package com.github.joeljoly.tournament;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 6/2/13
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerManagement extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_management);
        LinearLayout  layout = (LinearLayout) findViewById(R.id.playersLayout);
        TournamentDataDbHelper database;
        database = new TournamentDataDbHelper(this);
        List<Player> players;
        players = database.getAllPlayers();
        for (Player player : players)
        {
            PlayerWidget    widget;
            widget = new PlayerWidget(layout.getContext());
            layout.addView(widget);
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
            case R.id.add_player:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, PlayerAdd.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }}