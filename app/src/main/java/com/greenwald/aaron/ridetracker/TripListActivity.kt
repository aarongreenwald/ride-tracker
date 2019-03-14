package com.greenwald.aaron.ridetracker

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.support.v7.view.ActionMode;

import com.greenwald.aaron.ridetracker.model.Trip
import java.text.SimpleDateFormat

import java.util.Date

class TripListActivity : AppCompatActivity(), TripListFragment.OnListFragmentInteractionListener    {

    private var actionMode: Boolean = false
    private var selectedTrips: MutableList<Trip> = mutableListOf()
    private lateinit var menu: Menu
    private lateinit var mode: ActionMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_trip_list)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setOnClickListener { showCredits() }
        setSupportActionBar(toolbar)

        val fab = findViewById<FloatingActionButton>(R.id.newTripFab)
        fab.setOnClickListener { createNewTrip() }
    }

    private fun showCredits() {
        val intent = Intent(this, CreditsActivity::class.java)
        startActivity(intent)
    }

    override fun onTripPress(tripListItem: TripListItem) {
        if (actionMode) {
            toggleTripSelection(tripListItem)
        } else {
            openTripActivity(tripListItem.trip)
        }
    }

    override fun onTripLongPress(tripListItem: TripListItem): Boolean {

        if (!actionMode)
            startSupportActionMode(this)

        toggleTripSelection(tripListItem)
        return true
    }

    override fun onSupportActionModeStarted(mode: ActionMode) {
        super.onSupportActionModeStarted(mode)
        actionMode = true
    }

    private fun toggleTripSelection(tripListItem: TripListItem) {
        tripListItem.selected = !tripListItem.selected
        if (selectedTrips.contains(tripListItem.trip)) {
            selectedTrips.remove(tripListItem.trip)
        } else {
            selectedTrips.add(tripListItem.trip)
        }

        val rv = this.findViewById<RecyclerView>(R.id.list)
        rv.adapter?.notifyDataSetChanged()

        editButton().isVisible = selectedTrips.size <= 1
        if (selectedTrips.isEmpty())
            mode.finish()

    }

    private fun editButton() = menu.getItem(0)

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater: MenuInflater = mode.menuInflater
        inflater.inflate(R.menu.trip_list_menu, menu)
        this.menu = menu
        this.mode = mode
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem) =
        when (item.itemId) {
            R.id.editTrip -> {
                renameTrip(selectedTrips.get(0))
                mode.finish()
                true
            }
            else -> false
        }


    override fun onDestroyActionMode(mode: ActionMode) {
        selectedTrips.clear()
        actionMode = false
    }

    private fun createNewTrip() {
        showTripDialog(title = getString(R.string.createNewTrip),
                inputText = SimpleDateFormat("yyyy-MM-dd").format(Date()),
                confirmCta = getString(R.string.create),
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
        showTripDialog(title = getString(R.string.editTrip),
                inputText = trip.name,
                confirmCta = getString(R.string.save),
                confirmAction = { input -> { _, _ ->
                    val newName = input.text.toString()
                    saveTripName(trip, newName)

                    refreshList()
                } }
        )
    }

    private fun refreshList() {
        val rv = this.findViewById<RecyclerView>(R.id.list)
        val savedScrollPosition = rv.getLayoutManager()?.onSaveInstanceState();

        val ds = DataStore(this.applicationContext)
        val trips = ds.trips
        rv.adapter = TripRecyclerViewAdapter(trips.map {trip -> TripListItem(trip, false)},this)

        rv.getLayoutManager()?.onRestoreInstanceState(savedScrollPosition);
    }

    private fun saveTripName(trip:Trip, newName: String) {
        val ds = DataStore(applicationContext)
        ds.setTripName(trip.id, newName)
    }

    private fun openTripActivity(trip: Trip) {
        val intent = Intent(this@TripListActivity, TripActivity::class.java)
        intent.putExtra("tripId", trip.id)
        intent.putExtra("tripName", trip.name)
        startActivity(intent)
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
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}
data class TripListItem(val trip: Trip, var selected: Boolean)