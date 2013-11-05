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

        Button managePlayersButton = (Button) findViewById(R.id.managePlayersButton);
        managePlayersButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent managePlayersIntent;
                managePlayersIntent = new Intent(MainPage.this, PlayerListActivity.class);
                MainPage.this.startActivity(managePlayersIntent);
            }
        });

        Button scrapePlayersButton = (Button) findViewById(R.id.scrapingButton);
        scrapePlayersButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent scrapePlayersIntent;
                scrapePlayersIntent = new Intent(MainPage.this, ScrapingActivity.class);
                MainPage.this.startActivity(scrapePlayersIntent);
            }
        });
    }
}
