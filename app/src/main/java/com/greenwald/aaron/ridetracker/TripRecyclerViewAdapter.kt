package com.greenwald.aaron.ridetracker

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.greenwald.aaron.ridetracker.TripListFragment.OnListFragmentInteractionListener
import com.greenwald.aaron.ridetracker.model.Trip
import android.view.GestureDetector
import android.view.MotionEvent
import android.animation.ObjectAnimator
import android.animation.AnimatorSet

class TripRecyclerViewAdapter internal constructor(private val trips: List<Trip>,
                                                   private val interactionListener: OnListFragmentInteractionListener?,
                                                   private val recyclerView: ViewGroup) :
        RecyclerView.Adapter<TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_trip_listitem, parent, false)

        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]

        holder.tripRunningIndicator.visibility = if (LocationTrackingService.recordingTripId == trip.id)
            View.VISIBLE else View.GONE

        holder.nameTextView.text = trip.name
        holder.distanceTextView.text = trip.distance.toString()
        holder.tripTimeTextView.text = trip.elapsedTime.toString()

        val gestureListener = GestureListener(trip, position)
        val gestureDetector = GestureDetector(gestureListener)
        holder.view.setOnTouchListener({ _, event -> gestureDetector.onTouchEvent(event) })
    }

    override fun getItemCount() = trips.size

    inner class GestureListener(val trip: Trip, val position: Int) : GestureDetector.OnGestureListener {
        private var actionsAreVisible: Boolean = false

        override fun onDown(e: MotionEvent?) = true
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) = false
        override fun onShowPress(e: MotionEvent?) { }

        private val SWIPE_MIN_DISTANCE = 5
        private val SWIPE_THRESHOLD_VELOCITY = 50

        private fun hideActions() {
            val item = recyclerView.getChildAt(position).findViewById<View>(R.id.list_item)
            val set = AnimatorSet()
            val animation = ObjectAnimator.ofFloat(item, "translationX", 0f)
            animation.duration = 500
            set.play(animation)
            set.start()
            actionsAreVisible = false
            item.findViewById<View>(R.id.editTrip)?.isClickable = false
            item.findViewById<View>(R.id.deleteTrip)?.isClickable = false
        }

        private fun revealActions() {
            val item = recyclerView.getChildAt(position).findViewById<View>(R.id.list_item)
            val set = AnimatorSet()
            val animation = ObjectAnimator.ofFloat(item, "translationX", -500f)
            animation.duration = 500
            set.play(animation)
            set.start()
            actionsAreVisible = true
            item.findViewById<View>(R.id.editTrip)?.isClickable = true
            item.findViewById<View>(R.id.deleteTrip)?.isClickable = true


        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (e1.x - e2.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                revealActions()
                return true
            }
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
            interactionListener!!.onTripLongPress(trip)
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (actionsAreVisible) {
                hideActions()
            } else {
                interactionListener!!.onTripPress(trip)
            }
            return true
        }
    }

}

class TripViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val nameTextView: TextView = view.findViewById(R.id.tripName)
    val distanceTextView: TextView = view.findViewById(R.id.tripDistance)
    val tripTimeTextView = view.findViewById<TextView>(R.id.tripTime)
    val tripRunningIndicator = view.findViewById<ProgressBar>(R.id.tripRunningIndicator)

    override fun toString(): String {
        return "${super.toString()} '${nameTextView.text}'"
    }
}