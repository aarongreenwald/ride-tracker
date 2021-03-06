package com.greenwald.aaron.ridetracker

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView


class StatsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    private lateinit var  stats: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stats = view.findViewById<LinearLayout>(R.id.stats)
        updateData(activity!!)
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        (activity as TripActivity).onDataUpdated { tripActivity -> updateData(tripActivity) }
    }

    private fun updateData(activity: Activity) {
        AsyncTask.execute {
            val ds = DataStore(activity.applicationContext)
            val trip =  ds.getTripWithDetails(arguments!!.getLong("tripId"))

            activity.runOnUiThread {
                stats.removeAllViews()
                stats.addView(createSingleStat(R.string.trip_distance, trip.distance.toString()))

                stats.addView(createSingleStat(R.string.total_time, trip.elapsedTime.toString()))
                stats.addView(createSingleStat(R.string.riding_time, trip.ridingTime.toString()))
                stats.addView(createSingleStat(R.string.stop_time, trip.stoppedTime.toString()))

                stats.addView(createSingleStat(R.string.avg_riding_speed, trip.averageRidingSpeed.toString()))
                stats.addView(createSingleStat(R.string.max_speed, trip.maxSpeed.toString()))


//                        stats.addView(createSingleStat(R.string.max_segment_time, "01:23"))
//                        stats.addView(createSingleStat(R.string.max_segment_distance, "100.5 km"))
//                stats.addView(createSingleStat(R.string.max_slope, "10deg"))
//                stats.addView(createSingleStat(R.string.min_slope, "-3deg"))
                stats.addView(createSingleStat(R.string.max_altitude, trip.maxAltitude.toString()))
                stats.addView(createSingleStat(R.string.min_altitude, trip.minAltitude.toString()))
                stats.addView(createSingleStat(R.string.altitude_range, trip.altitudeRange.toString()))
//                stats.addView(createSingleStat(R.string.total_ascent, "4000m"))
//                stats.addView(createSingleStat(R.string.total_descent, "-3000m"))


            }
        }
    }
    private fun createSingleStat(label: Int, value: String): View? {
        val view = layoutInflater.inflate(R.layout.single_stat, null)
        view.findViewById<TextView>(R.id.labelTextView).setText(label)
        view.findViewById<TextView>(R.id.valueTextView).setText(value)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

}
