package com.kylecorry.trailsensecore.infrastructure.gpx

import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.math.toDoubleCompat
import com.kylecorry.trailsensecore.domain.math.toFloatCompat
import com.kylecorry.trailsensecore.infrastructure.xml.XMLConvert
import com.kylecorry.trailsensecore.infrastructure.xml.XMLNode

class GPXParser {

    fun toGPX(waypoints: List<GPXWaypoint>, creator: String = "Trail Sense"): String {
        val children = mutableListOf<XMLNode>()
        for (waypoint in waypoints){
            children.add(toXML(waypoint))
        }

        val gpx = XMLNode(
            "gpx", mapOf(
                "version" to "1.1",
                "creator" to creator,
                "xmlns:xsi" to "http://www.w3.org/2001/XMLSchema-instance",
                "xmlns" to "http://www.topografix.com/GPX/1/1",
                "xsi:schemaLocation" to "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd"
            ),
            null,
            children
        )

        return XMLConvert.toString(gpx, true)
    }

    fun getWaypoints(gpx: String): List<GPXWaypoint> {
        val tree = try {
            XMLConvert.parse(gpx)
        } catch (e: Exception) {
            return listOf()
        }
        return tree.children.map {
            val lat =
                if (it.attributes.containsKey("lat")) it.attributes["lat"]?.toDoubleCompat() else null
            val lon =
                if (it.attributes.containsKey("lon")) it.attributes["lon"]?.toDoubleCompat() else null
            val name = it.children.firstOrNull { it.tag == "name" }?.text
            val desc = it.children.firstOrNull { it.tag == "desc" }?.text
            val ele = it.children.firstOrNull { it.tag == "ele" }?.text?.toFloatCompat()
            val extensions = it.children.firstOrNull { it.tag == "extensions" }
            val group = extensions?.children?.firstOrNull { it.tag == "trailsense:group" }?.text

            if (lat == null || lon == null || name == null) {
                return@map null
            }

            return@map GPXWaypoint(Coordinate(lat, lon), name, ele, desc, group)
        }.filterNotNull()
    }

    private fun toXML(waypoint: GPXWaypoint): XMLNode {
        val children = mutableListOf<XMLNode>()
        if (waypoint.elevation != null){
            children.add(XMLNode.text("ele", waypoint.elevation.toString()))
        }
        children.add(XMLNode.text("name", waypoint.name))
        if (waypoint.comment != null){
            children.add(XMLNode.text("desc", waypoint.comment))
        }
        if (waypoint.group != null){
            children.add(XMLNode("extensions", mapOf(), null, listOf(
                XMLNode.text("trailsense:group", waypoint.group)
            )))
        }

        return XMLNode("wpt", mapOf(
            "lat" to waypoint.coordinate.latitude.toString(),
            "lon" to waypoint.coordinate.longitude.toString()
        ), null, children)
    }
}