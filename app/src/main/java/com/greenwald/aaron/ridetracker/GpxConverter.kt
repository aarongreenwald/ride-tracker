package com.greenwald.aaron.ridetracker

import com.greenwald.aaron.ridetracker.model.Segment
import com.greenwald.aaron.ridetracker.model.Trip
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class GpxConverter {

    fun tripsToGpx(trips: Array<Trip>, filePath: String) {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = builder.newDocument()
        val gpx = writeDocumentHeader(doc)

        trips.forEach { trip -> addTripToDocument(doc, gpx, trip) }

        val source = DOMSource(doc)
        val transformer = TransformerFactory.newInstance().newTransformer()
        val result = StreamResult(File(filePath))
        transformer.transform(source, result)
    }

    private fun writeDocumentHeader(doc: Document): Element {
        val gpx = doc.createElement("gpx")

        gpx.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1")
        gpx.setAttribute("xmlns:ridetracker", "https://raw.githubusercontent.com/aarongreenwald/ride-tracker/master/ridetracker.xsd")
        gpx.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        gpx.setAttribute("xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd")
        gpx.setAttribute("creator", "RideTracker (github.com/aarongreenwald/ride-tracker)")
        gpx.setAttribute("version", "1.1")

        doc.appendChild(gpx)

        return gpx
    }

    private fun addTripToDocument(doc: Document, root: Element, trip: Trip) {
        val trk = doc.createElement("trk")
        insertElement(doc, trk, "name", trip.name)

        trip.segments.forEach { segment -> addSegmentToTrack(doc, trk, segment) }

        root.appendChild(trk)
    }

    private fun addSegmentToTrack(doc: Document, trk: Element, segment: Segment) {
        val trkseg = doc.createElement("trkseg")

        segment.segmentPoints.forEach { sp ->
            val trkpt = doc.createElement("trkpt")
            trkpt.setAttribute("lat", sp.latitude.toString())
            trkpt.setAttribute("lng", sp.longitude.toString())
            insertElement(doc, trkpt, "ele", sp.altitude.value.toString())
            insertElement(doc, trkpt, "time", sp.dateTime.toString())
            insertElement(doc, trkpt, "hdop", sp.accuracy.toString())
            trkseg.appendChild(trkpt)
        }

        addSegmentDetails(doc, segment, trkseg)

        trk.appendChild(trkseg)
    }

    private fun addSegmentDetails(doc: Document, segment: Segment, trkseg: Element) {
        val ext = doc.createElement("extensions")
        val seg = doc.createElement("ridetracker:segment")
        seg.setAttribute("started", segment.startedTimestamp.toString())
        if (segment.stoppedTimestamp != null)
            seg.setAttribute("stopped", segment.stoppedTimestamp.toString())

        ext.appendChild(seg)
        trkseg.appendChild(ext)
    }


    private fun insertElement(doc: Document, parent: Element, name: String, value: String) {
        val element = doc.createElement(name)
        element.textContent = value
        parent.appendChild(element)
    }


}