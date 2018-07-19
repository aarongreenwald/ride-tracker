package com.greenwald.aaron.ridetracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.greenwald.aaron.ridetracker.model.Trip;

import java.time.Instant;
import java.util.Date;

public class TripListActivity extends AppCompatActivity implements TripListFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.newTripFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNewTripClick();
            }
        });
    }

    private void onNewTripClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TripListActivity.this);
        builder.setTitle("Create New Trip");

        final EditText input = new EditText(getApplicationContext());

        input.setText(Date.from(Instant.now()).toString());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                createTripAndOpenActivity(text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        builder.show();
    }

    private void createTripAndOpenActivity(String text) {
        DataStore ds = new DataStore(getApplicationContext());
        Trip trip = ds.createTrip(text);
        openTripActivity(trip);
    }

    @Override
    public void onListFragmentInteraction(Trip trip) {
        openTripActivity(trip);
    }

    private void openTripActivity(Trip trip) {
        Intent intent = new Intent(TripListActivity.this, TripActivity.class);
        intent.putExtra("tripId", trip.getId());
        TripListActivity.this.startActivity(intent);
    }
}
