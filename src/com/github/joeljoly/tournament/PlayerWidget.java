package com.github.joeljoly.tournament;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 5/27/13
 * Time: 10:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerWidget extends LinearLayout
{
    TextView playerNameView;
    TextView playerPointsView;

    public PlayerWidget(Context context)
    {
        super(context);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View inflate = inflater.inflate(R.layout.player, this);
        playerNameView = (TextView) findViewById(R.id.playerNameView);
        playerPointsView = (TextView) findViewById(R.id.playerPointsView);
    }
    public PlayerWidget(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.player, this);
    }

    public void setPlayer(Player player)
    {
        playerNameView.setText(player.getFirstName() + " " + player.getLastName());
        playerPointsView.setText(player.getPoints().toString());
    }
}
