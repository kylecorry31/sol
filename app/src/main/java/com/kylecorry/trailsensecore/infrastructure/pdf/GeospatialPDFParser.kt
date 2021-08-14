package com.kylecorry.trailsensecore.infrastructure.pdf

import android.content.Context
import android.net.Uri
import com.kylecorry.andromeda.core.units.Coordinate
import com.kylecorry.andromeda.files.ExternalFileService
import com.kylecorry.trailsensecore.domain.geo.cartography.MapCalibrationPoint
import com.kylecorry.trailsensecore.domain.math.toDoubleCompat
import com.kylecorry.trailsensecore.domain.math.toFloatCompat
import com.kylecorry.trailsensecore.domain.pixels.PercentCoordinate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeospatialPDFParser {

    suspend fun getCalibrationPoints(context: Context, uri: Uri): List<MapCalibrationPoint> {
        return withContext(Dispatchers.IO) {
            // TODO: Only load the heading
            val text = ExternalFileService(context).read(uri) ?: return@withContext listOf<MapCalibrationPoint>()
            val geoMatches = Regex("/GPTS\\s*\\[(.*)]").find(text)
            val viewportMatches = Regex("/BBox\\s*\\[(.*)]").find(text)
            val mediaBox = Regex("/MediaBox\\s*\\[(.*)]").find(text)
            if (geoMatches != null && viewportMatches != null && mediaBox != null) {
                val geo = geoMatches.groupValues[1].split(" ").mapNotNull { it.toDoubleCompat() }
                val box = mediaBox.groupValues[1].split(" ").mapNotNull { it.toFloatCompat() }
                val viewport =
                    viewportMatches.groupValues[1].split(" ").mapNotNull { it.toFloatCompat() }
                if (geo.size == 8 && box.size == 4 && viewport.size == 4) {
                    val width = box[2]
                    val height = box[3]
                    val topLeftPct =
                        PercentCoordinate(viewport[0] / width, 1 - viewport[1] / height)
                    val bottomRightPct =
                        PercentCoordinate(viewport[2] / width, 1 - viewport[3] / height)
                    val topLeft = Coordinate(geo[2], geo[3])
                    val bottomRight = Coordinate(geo[6], geo[7])
                    return@withContext listOf(
                        MapCalibrationPoint(topLeft, topLeftPct),
                        MapCalibrationPoint(bottomRight, bottomRightPct)
                    )
                }
            }
            listOf()
        }
    }

}