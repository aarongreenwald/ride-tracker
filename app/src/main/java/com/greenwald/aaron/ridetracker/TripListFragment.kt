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



class TripListFragment : Fragment() {
    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trip_list, container, false)
        loadList(view)


//        val pullToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.pullToRefresh)
//        pullToRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
//            loadList(view)
//            pullToRefresh.setRefreshing(false)
//        })


        return view
    }

    private fun loadList(view: View) {
        val ds = DataStore(context)
        val trips = ds.trips
        if (view is RecyclerView) {
            view.adapter = TripRecyclerViewAdapter(trips, listener)
        }
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

    interface OnListFragmentInteractionListener {
        fun onTripPress(trip: Trip)
        fun onTripLongPress(trip: Trip): Boolean
    }
}
