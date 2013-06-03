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
    TournamentDataDbHelper database;
    LinearLayout playersLayout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_management);
        playersLayout = (LinearLayout) findViewById(R.id.playersLayout);
        database = new TournamentDataDbHelper(this);
        List<Player> players;
        players = database.getAllPlayers();
        for (Player player : players)
        {
            PlayerWidget    widget;
            widget = new PlayerWidget(playersLayout.getContext());
            playersLayout.addView(widget);
            widget.setPlayer(player);
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
                startActivityForResult(intent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                Player newPlayer;
                newPlayer = (Player) data.getSerializableExtra("result");
                if (newPlayer == null)
                    throw new IllegalStateException("Returning activity should have set a Player if it returns OK");
                PlayerWidget widget;
                widget = new PlayerWidget(playersLayout.getContext());
                playersLayout.addView(widget);
                widget.setPlayer(newPlayer);
            }
        }
    }
}