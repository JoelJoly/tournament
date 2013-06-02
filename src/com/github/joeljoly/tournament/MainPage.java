package com.github.joeljoly.tournament;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MainPage extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button managePlayersButton = (Button) findViewById(R.id.managePlayersButton);
        managePlayersButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent managePlayersIntent;
                managePlayersIntent = new Intent(MainPage.this, PlayerManagement.class);
                MainPage.this.startActivity(managePlayersIntent);
            }
        });
    }
}
