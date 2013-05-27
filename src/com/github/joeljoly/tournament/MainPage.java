package com.github.joeljoly.tournament;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
        ScrollView playersScrollView = (ScrollView)findViewById(R.id.scrollView);
        LinearLayout playersLayout = new LinearLayout(this);
        playersLayout.setOrientation(LinearLayout.VERTICAL);
        playersScrollView.addView(playersLayout);
        PlayerWidget player1 = new PlayerWidget(playersLayout.getContext());
        playersLayout.addView(player1);
        PlayerWidget player2 = new PlayerWidget(playersLayout.getContext());
        playersLayout.addView(player2);
    }
}
