/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.dstl.geo.osgb

/**
 * <b>Constants that define different coordinate systems and ellipsoids</b>
 *
 * <p>Values taken from
 * https://www.ordnancesurvey.co.uk/documents/resources/guide-coordinate-systems-great-britain.pdf
 */
object Constants {
    const val NATIONALGRID_F0 = 0.9996012717
    // In degrees
    const val NATIONALGRID_LAT0 = 49.0
    // In degrees
    const val NATIONALGRID_LON0 = -2.0
    const val NATIONALGRID_E0 = 400000.0
    const val NATIONALGRID_N0 = -100000.0

    const val ELLIPSOID_AIRY1830_MAJORAXIS = 6377563.396
    const val ELLIPSOID_AIRY1830_MINORAXIS = 6356256.909

    const val ELLIPSOID_GRS80_MAJORAXIS = 6378137.000
    const val ELLIPSOID_GRS80_MINORAXIS = 6356752.3141
}
