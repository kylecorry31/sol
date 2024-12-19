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
package uk.gov.dstl.geo.osgb;

import static uk.gov.dstl.geo.osgb.CartesianConversion.*;

/**
 * <b>Convert between OSGB36 and WGS84 coordinate systems</b>
 *
 * <p>Values taken from
 * https://www.ordnancesurvey.co.uk/documents/resources/guide-coordinate-systems-great-britain.pdf
 */
public class OSGB36 {
  private static final double tX = -446.448;
  private static final double tY = 125.157;
  private static final double tZ = -542.060;
  private static final double s =
      20.4894 / 1000000; // Value given by OS in ppm, so convert to a unitless quantity
  private static final double rX =
      -0.1502 * (Math.PI / 648000); // Value given by OS in arcseconds, so convert to radians
  private static final double rY =
      -0.2470 * (Math.PI / 648000); // Value given by OS in arcseconds, so convert to radians
  private static final double rZ =
      -0.8421 * (Math.PI / 648000); // Value given by OS in arcseconds, so convert to radians

  private OSGB36() {
    // Utility class - private constructor
  }

  /**
   * Convert to WGS86 Lat Lon from OSGB.
   *
   * @param lat Latitude in OSGB36 coordinates
   * @param lon Longitude in OSGB36 coordinates
   * @return Array of coordinates [lat, long] in WGS84
   */
  public static double[] toWGS84(double lat, double lon) {
    double[] cartesian =
        fromLatLon(
            new double[] {lat, lon, 0},
            Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
            Constants.ELLIPSOID_AIRY1830_MINORAXIS);
    double[] transformed = helmertTransformation(cartesian, -tX, -tY, -tZ, -s, -rX, -rY, -rZ);
    double[] ret =
        toLatLon(
            transformed,
            Constants.ELLIPSOID_GRS80_MAJORAXIS,
            Constants.ELLIPSOID_GRS80_MINORAXIS,
            0.00000001);

    return new double[] {ret[0], ret[1]};
  }

  /**
   * Convert from WGS86 Lat Lon to OSGB.
   *
   * @param lat Latitude in WGS84 coordinates
   * @param lon Longitude in WGS84 coordinates
   * @return Array of coordinates [lat, lon] in OSGB36
   */
  public static double[] fromWGS84(double lat, double lon) {
    double[] cartesian =
        fromLatLon(
            new double[] {lat, lon, 0},
            Constants.ELLIPSOID_GRS80_MAJORAXIS,
            Constants.ELLIPSOID_GRS80_MINORAXIS);
    double[] transformed = helmertTransformation(cartesian, tX, tY, tZ, s, rX, rY, rZ);
    double[] ret =
        toLatLon(
            transformed,
            Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
            Constants.ELLIPSOID_AIRY1830_MINORAXIS,
            0.00000001);

    return new double[] {ret[0], ret[1]};
  }
}
