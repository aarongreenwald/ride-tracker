package com.greenwald.aaron.ridetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.widget.EditText

import com.greenwald.aaron.ridetracker.model.Trip

import java.time.Instant
import java.util.Date

class TripListActivity : AppCompatActivity(), TripListFragment.OnListFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_list)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.newTripFab)
        fab.setOnClickListener { onNewTripClick() }
    }

    private fun onNewTripClick() {
        val builder = AlertDialog.Builder(this@TripListActivity)
        builder.setTitle("Create New Trip")

        val input = EditText(applicationContext)

        input.setText(Date.from(Instant.now()).toString())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, _ ->
            val text = input.text.toString()
            createTripAndOpenActivity(text)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }


        builder.show()
    }

    private fun createTripAndOpenActivity(text: String) {
        val ds = DataStore(applicationContext)
        val trip = ds.createTrip(text)
        openTripActivity(trip)
    }

    override fun onListFragmentInteraction(trip: Trip) {
        openTripActivity(trip)
    }

    private fun openTripActivity(trip: Trip) {
        val intent = Intent(this@TripListActivity, TripActivity::class.java)
        intent.putExtra("tripId", trip.id)
        this@TripListActivity.startActivity(intent)
    }
}
