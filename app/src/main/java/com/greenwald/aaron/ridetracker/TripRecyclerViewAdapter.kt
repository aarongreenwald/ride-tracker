package com.greenwald.aaron.ridetracker

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.greenwald.aaron.ridetracker.TripListFragment.OnListFragmentInteractionListener
import com.greenwald.aaron.ridetracker.model.Trip

class TripRecyclerViewAdapter internal constructor(private val trips: List<Trip>, private val interactionListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<TripRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_trip_listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.trip = trips[position]

        if (LocationTrackingService.recordingTripId == holder.trip!!.id) {
            holder.nameTextView.setTextColor(Color.GREEN)
            holder.distanceTextView.setTextColor(Color.GREEN)
        }


        holder.nameTextView.text = trips[position].name
        holder.distanceTextView.text = trips[position].distance.toString()

        holder.view.setOnClickListener {
            interactionListener?.onListFragmentInteraction(holder.trip!!)
        }
    }

    override fun getItemCount() = trips.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tripName)
        val distanceTextView: TextView = view.findViewById(R.id.tripDistance)
        var trip: Trip? = null

        override fun toString(): String {
            return "${super.toString()} '${nameTextView.text}'"
        }
    }
}
