package com.github.joeljoly.tournament;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    RankViewer rankViewer;

    public static class ViewBinder implements SimpleCursorAdapter.ViewBinder
    {
        TournamentDataDbHelper databaseHelper;
        ViewBinder(Context context)
        {
            databaseHelper = new TournamentDataDbHelper(context);
        }
        public boolean setViewValue(View view, Cursor cursor, int columnIndex)
        {
            Player  player = databaseHelper.createPlayerFromCursor(cursor);
            switch (view.getId())
            {
                case R.id.playerNameView:
                    TextView playerNameView = (TextView) view;
                    playerNameView.setText(player.getFirstName() + " " + player.getLastName());
                    return true;
                case R.id.playerPointsView:
                    TextView playerPointsView = (TextView) view;
                    playerPointsView.setText(player.getPoints().toString());
                    return true;
                case R.id.rankTextView:
                    RankViewer.setPoints((TextView) view, player.getPoints());
                    return true;
            }
            return false;
        }
    }

    public static class RankViewer extends RelativeLayout
    {
        TextView    rankView;
        public RankViewer(Context context)
        {
            super(context);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View inflate = inflater.inflate(R.layout.player_rank, this);
            rankView = (TextView) findViewById(R.id.rankTextView);
        }
        public RankViewer(Context context, AttributeSet attributeSet)
        {
            super(context, attributeSet);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View inflate = inflater.inflate(R.layout.player_rank, this);
            rankView = (TextView) findViewById(R.id.rankTextView);
        }
        public void setPoints(Integer points)
        {
            setPoints(rankView, points);
        }
        static void setPoints(TextView view, Integer points)
        {
            Integer rankFromPoint = points/100;
            view.setText(rankFromPoint.toString());
        }
    }

    public PlayerWidget(Context context)
    {
        super(context);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View inflate = inflater.inflate(R.layout.player, this);
        playerNameView = (TextView) findViewById(R.id.playerNameView);
        playerPointsView = (TextView) findViewById(R.id.playerPointsView);
        rankViewer = (RankViewer) findViewById(R.id.playerRankView);
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
        rankViewer.setPoints(player.getPoints());
    }
}
