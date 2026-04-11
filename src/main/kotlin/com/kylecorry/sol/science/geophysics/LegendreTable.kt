/*
 * Copyright (C) 2009 The Android Open Source Project
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
 *
 * This file was converted to Kotlin by Kyle Corry.
 */

package com.kylecorry.sol.science.geophysics

import kotlin.math.cos
import kotlin.math.sin

internal class LegendreTable(maxN: Int, thetaRad: Float) {
    val mP: Array<FloatArray> = Array(maxN + 1) { FloatArray(it + 1) }
    val mPDeriv: Array<FloatArray> = Array(maxN + 1) { FloatArray(it + 1) }

    init {
        val cos = cos(thetaRad)
        val sin = sin(thetaRad)
        mP[0][0] = 1.0f
        mPDeriv[0][0] = 0.0f

        for (n in 1..maxN) {
            for (m in 0..n) {
                when {
                    n == m -> {
                        mP[n][m] = sin * mP[n - 1][m - 1]
                        mPDeriv[n][m] = cos * mP[n - 1][m - 1] + sin * mPDeriv[n - 1][m - 1]
                    }

                    n == 1 || m == n - 1 -> {
                        mP[n][m] = cos * mP[n - 1][m]
                        mPDeriv[n][m] = -sin * mP[n - 1][m] + cos * mPDeriv[n - 1][m]
                    }

                    else -> {
                        val k = ((n - 1) * (n - 1) - m * m).toFloat() / ((2 * n - 1) * (2 * n - 3))
                        mP[n][m] = cos * mP[n - 1][m] - k * mP[n - 2][m]
                        mPDeriv[n][m] = -sin * mP[n - 1][m] + cos * mPDeriv[n - 1][m] - k * mPDeriv[n - 2][m]
                    }
                }
            }
        }
    }
}
