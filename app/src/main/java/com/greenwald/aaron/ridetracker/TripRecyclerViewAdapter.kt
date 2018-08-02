package com.greenwald.aaron.ridetracker

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
        holder.textView.text = trips[position].name

        holder.view.setOnClickListener {
            interactionListener?.onListFragmentInteraction(holder.trip!!)
        }
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        var trip: Trip? = null

        init {
            textView = view.findViewById(R.id.tripName)
        }

        override fun toString(): String {
            return super.toString() + " '" + textView.text + "'"
        }
    }
}
