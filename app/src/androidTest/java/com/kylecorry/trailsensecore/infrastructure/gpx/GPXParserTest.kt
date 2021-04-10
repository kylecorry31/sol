package com.kylecorry.trailsensecore.infrastructure.gpx

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.infrastructure.xml.XMLConvert
import org.junit.Assert.*
import org.junit.Test

class GPXParserTest {
    @Test
    fun fromGPX(){
        val gpx = """<?xml version="1.0"?>
<gpx version="1.1" creator="Trail Sense" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/1" xmlns:trailsense="https://kylecorry.com/Trail-Sense" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd https://kylecorry.com/Trail-Sense https://kylecorry.com/Trail-Sense/trailsense.xsd">
    <wpt lat="37.778259000" lon="-122.391386000">
        <ele>3.4</ele>
        <name>Beacon 1</name>
        <desc>A test comment</desc>
        <extensions>
            <trailsense:group>Test Group</trailsense:group>
        </extensions>
    </wpt>
    <wpt lat="31" lon="100">
        <name>Beacon 2</name>
    </wpt>
</gpx>"""
        val waypoints = GPXParser().getWaypoints(gpx)
        assertEquals(
            listOf(
                GPXWaypoint(Coordinate(37.778259000, -122.391386000), "Beacon 1", 3.4f, "A test comment", "Test Group"),
                GPXWaypoint(Coordinate(31.0, 100.0), "Beacon 2", null, null, null),
            ),
            waypoints
        )
    }

    @Test
    fun toGPX(){
        val xml = """<?xml version="1.0"?><gpx version="1.1" creator="Trail Sense" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/1" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd"><wpt lat="37.778259" lon="-122.391386"><ele>3.4</ele><name>Beacon 1</name><desc>A test comment</desc><extensions><trailsense:group>Test Group</trailsense:group></extensions></wpt><wpt lat="31.0" lon="100.0"><name>Beacon 2</name></wpt></gpx>"""
        val waypoints = listOf(
            GPXWaypoint(Coordinate(37.778259, -122.391386), "Beacon 1", 3.4f, "A test comment", "Test Group"),
            GPXWaypoint(Coordinate(31.0, 100.0), "Beacon 2", null, null, null),
        )
        println(xml)
        println(GPXParser().toGPX(waypoints, "Trail Sense"))
        assertEquals(xml, GPXParser().toGPX(waypoints, "Trail Sense"))
    }





}