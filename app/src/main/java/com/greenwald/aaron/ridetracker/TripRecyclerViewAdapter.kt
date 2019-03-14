package com.greenwald.aaron.ridetracker

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.greenwald.aaron.ridetracker.TripListFragment.OnListFragmentInteractionListener

class TripRecyclerViewAdapter internal constructor(private val tripItems: List<TripListItem>,
                                                   private val interactionListener: OnListFragmentInteractionListener?) :
        RecyclerView.Adapter<TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_trip_listitem, parent, false)

        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val tripItem = tripItems[position]
        val trip = tripItem.trip

        holder.tripRunningIndicator.visibility = if (LocationTrackingService.recordingTripId == trip.id)
            View.VISIBLE else View.GONE

        holder.nameTextView.text = trip.name
        holder.distanceTextView.text = trip.distance.toString()
        holder.tripTimeTextView.text = trip.elapsedTime.toString()
        holder.container.setBackgroundColor(if (tripItems[position].selected) Color.LTGRAY else Color.WHITE)

        holder.view.setOnClickListener { _ -> interactionListener!!.onTripPress(tripItem) }
        holder.view.setOnLongClickListener { _ -> interactionListener!!.onTripLongPress(tripItem) }
    }

    override fun getItemCount() = tripItems.size

}

class TripViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val nameTextView: TextView = view.findViewById(R.id.tripName)
    val distanceTextView: TextView = view.findViewById(R.id.tripDistance)
    val tripTimeTextView = view.findViewById<TextView>(R.id.tripTime)
    val tripRunningIndicator = view.findViewById<ProgressBar>(R.id.tripRunningIndicator)
    val container = view.findViewById<LinearLayout>(R.id.list_item)

    override fun toString(): String = "${super.toString()} '${nameTextView.text}'"
}