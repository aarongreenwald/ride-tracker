package com.greenwald.aaron.ridetracker

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.widget.EditText

import com.greenwald.aaron.ridetracker.model.Trip
import java.text.SimpleDateFormat

import java.util.Date

class TripListActivity : AppCompatActivity(), TripListFragment.OnListFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_list)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.newTripFab)
        fab.setOnClickListener { createNewTrip() }
    }

    override fun onTripPress(trip: Trip) {
        openTripActivity(trip)
    }

    override fun onTripLongPress(trip: Trip): Boolean {
        renameTrip(trip)
        //how to refresh list?
        return true

    }

    private fun createNewTrip() {
        showTripDialog(title = "Create New Trip",
                inputText = SimpleDateFormat("yyyy-MM-dd").format(Date()),
                confirmCta = "Create",
                confirmAction = { input -> { _, _ ->
                    val text = input.text.toString()
                    createTripAndOpenActivity(text)
                } }
        )
    }

    private fun createTripAndOpenActivity(text: String) {
        val ds = DataStore(applicationContext)
        val trip = ds.createTrip(text)
        openTripActivity(trip)
    }

    private fun renameTrip(trip: Trip) {
        showTripDialog(title = "Edit Trip",
                inputText = trip.name,
                confirmCta = "Save",
                confirmAction = { input -> { _, _ ->
                    val newName = input.text.toString()
                    saveTripName(trip, newName)
                } }
        )
    }

    private fun saveTripName(trip:Trip, newName: String) {
        val ds = DataStore(applicationContext)
        ds.setTripName(trip.id, newName)
    }

    private fun openTripActivity(trip: Trip) {
        val intent = Intent(this@TripListActivity, TripActivity::class.java)
        intent.putExtra("tripId", trip.id)
        this@TripListActivity.startActivity(intent)
    }

    private fun showTripDialog(title: String,
                               inputText: String,
                               confirmCta: String,
                               confirmAction: (EditText) -> (DialogInterface, Int) -> Unit) {
        val builder = AlertDialog.Builder(this@TripListActivity)
        builder.setTitle(title)
        val input = EditText(applicationContext)
        input.setText(inputText)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton(confirmCta, confirmAction(input))
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}
