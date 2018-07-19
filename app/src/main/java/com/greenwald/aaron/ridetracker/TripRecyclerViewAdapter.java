package com.greenwald.aaron.ridetracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.greenwald.aaron.ridetracker.TripListFragment.OnListFragmentInteractionListener;
import com.greenwald.aaron.ridetracker.model.Trip;

import java.util.List;

public class TripRecyclerViewAdapter extends RecyclerView.Adapter<TripRecyclerViewAdapter.ViewHolder> {

    private final List<Trip> trips;
    private final OnListFragmentInteractionListener interactionListener;

    TripRecyclerViewAdapter(List<Trip> items, OnListFragmentInteractionListener listener) {
        trips = items;
        interactionListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_trip_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.trip = trips.get(position);
        holder.textView.setText(trips.get(position).getName());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != interactionListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    interactionListener.onListFragmentInteraction(holder.trip);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView textView;
        public Trip trip;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            textView = view.findViewById(R.id.tripName);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textView.getText() + "'";
        }
    }
}
