package com.greenwald.aaron.ridetracker

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView


class StatsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stats = view.findViewById<LinearLayout>(R.id.stats)
        stats.addView(createSingleStat(R.string.trip_distance, "430.5 km"))
        stats.addView(createSingleStat(R.string.riding_time, "04:32"))
        stats.addView(createSingleStat(R.string.stop_time, "04:32"))
        stats.addView(createSingleStat(R.string.total_time, "08:34"))
        ///////////
        stats.addView(createSingleStat(R.string.max_segment_time, "01:23"))
        stats.addView(createSingleStat(R.string.max_segment_distance, "100.5 km"))

        //////////
        stats.addView(createSingleStat(R.string.max_slope, "10deg"))
        stats.addView(createSingleStat(R.string.min_slope, "-3deg"))
        stats.addView(createSingleStat(R.string.max_altitude, "2000m"))
        stats.addView(createSingleStat(R.string.min_altitude, "-100m"))
        stats.addView(createSingleStat(R.string.altitude_range, "1800m")) //calc at display timef
        stats.addView(createSingleStat(R.string.total_ascent, "4000m"))
        stats.addView(createSingleStat(R.string.total_descent, "-3000m"))


    }

    private fun createSingleStat(label: Int, value: String): View? {
        val view = getLayoutInflater().inflate(R.layout.single_stat, null)
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
