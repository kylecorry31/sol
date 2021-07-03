package com.kylecorry.trailsensecore.domain.geo.cartography

import com.kylecorry.trailsensecore.domain.geo.Coordinate

internal class MapSiteService {

    fun getUrl(coordinate: Coordinate, site: MapSite): String {
        return when(site){
            MapSite.Google -> "https://www.google.com/maps/@${coordinate.latitude},${coordinate.longitude}"
            MapSite.OSM -> "https://www.openstreetmap.org/#map=${coordinate.latitude}/${coordinate.longitude}"
            MapSite.Bing -> "https://www.bing.com/maps?cp=${coordinate.latitude}~${coordinate.longitude}"
            MapSite.Apple -> "http://maps.apple.com/?ll=${coordinate.latitude},${coordinate.longitude}"
            MapSite.Caltopo -> "https://caltopo.com/map.html#ll=${coordinate.latitude},${coordinate.longitude}"
        }
    }

}