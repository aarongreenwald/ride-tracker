package com.greenwald.aaron.ridetracker

import android.content.Context
import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.greenwald.aaron.ridetracker.model.Trip
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.view.ActionMode;

class TripListFragment : Fragment() {
    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trip_list, container, false)
        loadList(view)


        val pullToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
        pullToRefresh.setOnRefreshListener {
            loadList(view)
            pullToRefresh.isRefreshing = false
        }


        return view
    }

    private fun loadList(view: View) {
        val ds = DataStore(context)
        val trips = ds.trips
        val rv = view.findViewById<RecyclerView>(R.id.list)
        rv.adapter = TripRecyclerViewAdapter(trips.map { trip -> TripListItem(trip, false) }, listener)
        rv.scrollToPosition(0)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener: ActionMode.Callback {
        fun onTripPress(tripListItem: TripListItem)
        fun onTripLongPress(tripListItem: TripListItem): Boolean
    }
}
