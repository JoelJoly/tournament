package com.github.joeljoly.tournament;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 6/3/13
 * Time: 13:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerEdit extends Activity {
    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private EditText idEdit;
    private EditText pointsEdit;
    private CheckBox allowIdEdit;
    Integer originaId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_edit);
        ActionBar actionBar;
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        firstNameEdit = (EditText) findViewById(R.id.firstNameEdit);
        lastNameEdit = (EditText) findViewById(R.id.lastNameEdit);
        idEdit = (EditText) findViewById(R.id.idEdit);
        pointsEdit = (EditText) findViewById(R.id.pointEdit);
        allowIdEdit = (CheckBox) findViewById(R.id.allowIdEdit);
        CheckBox.OnClickListener listener;
        listener = new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                idEdit.setInputType(checkBox.isChecked() ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_NULL);
                idEdit.setEnabled(checkBox.isChecked());
            }
        };
        allowIdEdit.setOnClickListener(listener);

        Bundle activityParameters;
        activityParameters = getIntent().getExtras();
        if (activityParameters != null && activityParameters.containsKey("playerId"))
        {
            Integer playerId;
            playerId = (Integer) activityParameters.get("playerId");
            TournamentDataDbHelper database;
            database = new TournamentDataDbHelper(this);
            Player player;
            player = database.getPlayer(playerId);
            if (player == null)
                throw new IllegalStateException("Cannot find player based on the given Id");
            firstNameEdit.setText(player.getFirstName());
            lastNameEdit.setText(player.getLastName());
            originaId = player.getId();
            idEdit.setText(originaId.toString());
            pointsEdit.setText(player.getPoints().toString());
            allowIdEdit.setChecked(false);
        }
        else
        {
            allowIdEdit.setChecked(true);
            allowIdEdit.setVisibility(View.INVISIBLE);
        }
        listener.onClick(allowIdEdit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_add_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent backIntent;
                backIntent = new Intent(this, PlayerManagement.class);
                backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backIntent);
                return true;
            case R.id.player_add_validate:
                Integer licenceNumber;
                try
                {
                    licenceNumber = Integer.valueOf(idEdit.getText().toString());
                }
                catch(NumberFormatException e)
                {
                    displayError(R.string.invalid_player_licence_message);
                    idEdit.requestFocus();
                    return true;
                }
                String  firstName;
                firstName = firstNameEdit.getText().toString();
                if (firstName.isEmpty())
                {
                    displayError(R.string.invalid_player_first_name_message);
                    firstNameEdit.requestFocus();
                    return true;
                }
                String  lastName;
                lastName = lastNameEdit.getText().toString();
                if (lastName.isEmpty())
                {
                    displayError(R.string.invalid_player_last_name_message);
                    lastNameEdit.requestFocus();
                    return true;
                }
                Integer points;
                String pointsAsString = pointsEdit.getText().toString();
                if (pointsAsString.isEmpty())
                {
                    pointsAsString = pointsEdit.getHint().toString();
                }
                try
                {
                    points = Integer.valueOf(pointsAsString);
                }
                catch(NumberFormatException e)
                {
                    displayError(R.string.invalid_player_points_message);
                    pointsEdit.requestFocus();
                    return true;
                }
                Player newPlayer;
                newPlayer = new Player(licenceNumber, firstName, lastName, points);
                TournamentDataDbHelper database;
                database = new TournamentDataDbHelper(this);
                // editing existing player
                if (originaId != null)
                {
                    // id hasn't changed, just update contact data
                    if (originaId.equals(licenceNumber))
                    {
                        database.updateContact(newPlayer);
                    }
                    else
                    {
                        // otherwise it will get complicated, begin to make sure new id is valid
                        if (database.addPlayer(newPlayer) >= 0)
                        {
                            // when other tables are created, update every table that references the old id with the new one

                            // remove old entry
                            database.deletePlayer(originaId);
                        }
                        else
                            newPlayer = null;
                    }
                }
                else
                {
                    if (database.addPlayer(newPlayer) < 0)
                        newPlayer = null;
                }
                if (newPlayer != null)
                {
                    Intent returnIntent;
                    returnIntent = new Intent();
                    returnIntent.putExtra("added", newPlayer.getId());
                    if (originaId != null)
                        returnIntent.putExtra("removed", originaId);
                    setResult(RESULT_OK,returnIntent);
                    this.finish();
                }
                else
                {
                    displayError(R.string.duplicate_player_message);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayError(int resId)
    {
        displayError(this.getString(resId));
    }
    private void displayError(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(R.string.duplicate_player_title);
        // display an "OK" button with nothing to do on click
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}