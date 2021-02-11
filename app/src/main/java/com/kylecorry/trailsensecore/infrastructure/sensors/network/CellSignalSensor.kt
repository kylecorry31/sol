package com.kylecorry.trailsensecore.infrastructure.sensors.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.*
import androidx.core.content.getSystemService
import androidx.core.math.MathUtils
import com.kylecorry.trailsensecore.domain.network.CellNetwork
import com.kylecorry.trailsensecore.domain.network.CellSignal
import com.kylecorry.trailsensecore.domain.units.Quality
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils
import java.time.Instant
import java.util.concurrent.Executors

class CellSignalSensor(private val context: Context) : AbstractSensor(), ICellSignalSensor {

    private val telephony by lazy { context.getSystemService<TelephonyManager>() }

    override val hasValidReading: Boolean
        get() = hasReading

    override val signals: List<CellSignal>
        get() = _signals

    private var _signals = listOf<CellSignal>()
    private var oldSignals = listOf<RawCellSignal>()
    private var hasReading = false

    private val listener = object : PhoneStateListener() {
        @SuppressLint("MissingPermission")
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            if (!PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                telephony?.requestCellInfoUpdate(
                    Executors.newSingleThreadExecutor(),
                    object : TelephonyManager.CellInfoCallback() {
                        override fun onCellInfo(cellInfo: MutableList<CellInfo>) {
                            Handler(Looper.getMainLooper()).post {
                                updateCellInfo(cellInfo)
                            }
                        }

                    })
            }

            val cells = telephony?.allCellInfo ?: return
            updateCellInfo(cells)
        }
    }

    @Suppress("DEPRECATION")
    private fun updateCellInfo(cells: List<CellInfo>) {
        synchronized(this) {
            hasReading = true
            val newSignals = cells.mapNotNull {
                when {
                    it is CellInfoWcdma -> {
                        RawCellSignal(
                            it.cellIdentity.cid.toString(),
                            Instant.ofEpochMilli(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) it.timestampMillis else (it.timeStamp / 1000000)),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Wcdma,
                            it.isRegistered
                        )
                    }
                    it is CellInfoGsm -> {
                        RawCellSignal(
                            it.cellIdentity.cid.toString(),
                            Instant.ofEpochMilli(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) it.timestampMillis else (it.timeStamp / 1000000)),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Gsm,
                            it.isRegistered
                        )
                    }
                    it is CellInfoLte -> {
                        RawCellSignal(
                            it.cellIdentity.ci.toString(),
                            Instant.ofEpochMilli(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) it.timestampMillis else (it.timeStamp / 1000000)),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Lte,
                            it.isRegistered
                        )
                    }
                    it is CellInfoCdma -> {
                        RawCellSignal(
                            it.cellIdentity.basestationId.toString(),
                            Instant.ofEpochMilli(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) it.timestampMillis else (it.timeStamp / 1000000)),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Cdma,
                            it.isRegistered
                        )
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoNr -> {
                        RawCellSignal(
                            it.cellIdentity.operatorAlphaLong.toString(),
                            Instant.ofEpochMilli(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) it.timestampMillis else (it.timeStamp / 1000000)),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Nr,
                            it.isRegistered
                        )
                    }
                    else -> null
                }
            }

            val latestSignals = newSignals.map {
                val old = oldSignals.find { signal -> it.id == signal.id }
                if (old == null) {
                    it
                } else {
                    if (old.time > it.time) old else it
                }
            }

            oldSignals = latestSignals

            _signals = latestSignals.map {
                CellSignal(it.id, it.percent, it.dbm, it.quality, it.network, it.registered)
            }

            notifyListeners()
        }
    }

    override fun startImpl() {
        telephony?.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    override fun stopImpl() {
        telephony?.listen(listener, PhoneStateListener.LISTEN_NONE)
    }

    data class RawCellSignal(
        val id: String,
        val time: Instant,
        val dbm: Int,
        val level: Int,
        val network: CellNetwork,
        val registered: Boolean
    ) {
        val percent: Float
            get() {
                return MathUtils.clamp(
                    100f * (dbm - network.minDbm) / (network.maxDbm - network.minDbm).toFloat(),
                    0f,
                    100f
                )
            }

        val quality: Quality
            get() = when (level) {
                3, 4 -> Quality.High
                2 -> Quality.Medium
                0 -> Quality.Unknown
                else -> Quality.Low
            }
    }
}