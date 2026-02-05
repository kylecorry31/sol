# Sol
A Kotlin library for science and math in the real world.

The core logic behind [Trail Sense](https://github.com/kylecorry31/Trail-Sense). This is intended for my use only at this point, so there isn't any documentation on it. 

## Installation (Gradle)
Available on Maven Central.

```gradle
dependencies {
    implementation 'com.kylecorry:sol:<version>'
}
```

## References
Astronomy algorithms are a combination of custom made algorithms and implementations derived from Jean Meeus (Astronomical Algorithms 2nd Edition), J. L. Lawrence (Celestial Calculations: A Gentle Introduction to Computational Astronomy), and NASA.

- [osgb](https://github.com/kylecorry31/osgb/blob/master/LICENSE)
- [Geo-Coordinate-Conversion-Java](https://github.com/kylecorry31/Geo-Coordinate-Conversion-Java/blob/master/GDAL_License.TXT)
- Tide corrections are computed using [pyTMD](https://github.com/tsutterley/pyTMD)
- The Vincenty distance calculation is derived from the [Android Open Source Project](https://source.android.com/) (Copyright 2007, Apache License 2.0), modified by Kyle Corry in 2021
