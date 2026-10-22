package com.findtrack.app

import com.findtrack.app.ble.DistanceEstimator
import com.findtrack.app.ble.SignalLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceEstimatorTest {

    @Test
    fun `signal at 1m produces approximately 1 meter`() {
        val d = DistanceEstimator.distanceFromRssi(-59)
        assertTrue("expected ~1m, got $d", d in 0.8..1.3)
    }

    @Test
    fun `weaker signal produces larger distance`() {
        val strong = DistanceEstimator.distanceFromRssi(-60)
        val weak = DistanceEstimator.distanceFromRssi(-90)
        assertTrue(weak > strong)
    }

    @Test
    fun `distance is clamped to sane bounds`() {
        val tooWeak = DistanceEstimator.distanceFromRssi(-127)
        assertEquals(DistanceEstimator.MAX_DISTANCE_M, tooWeak, 0.001)
        val tooStrong = DistanceEstimator.distanceFromRssi(0)
        // rssi == 0 is treated as an invalid reading and clamped to max
        assertEquals(DistanceEstimator.MAX_DISTANCE_M, tooStrong, 0.001)
    }

    @Test
    fun `signal levels follow thresholds`() {
        assertEquals(SignalLevel.VERY_STRONG, DistanceEstimator.signalLevel(-50))
        assertEquals(SignalLevel.STRONG, DistanceEstimator.signalLevel(-65))
        assertEquals(SignalLevel.MODERATE, DistanceEstimator.signalLevel(-78))
        assertEquals(SignalLevel.WEAK, DistanceEstimator.signalLevel(-100))
    }

    @Test
    fun `strength progress stays within meter bounds`() {
        assertEquals(0.05f, DistanceEstimator.strengthProgress(-120), 0.001f)
        assertEquals(1.0f, DistanceEstimator.strengthProgress(-40), 0.001f)
        assertEquals(0.64f, DistanceEstimator.strengthProgress(-68), 0.01f)
        assertEquals(0.05f, DistanceEstimator.strengthProgress(null), 0.001f)
    }

    @Test
    fun `distance formatting matches design copy`() {
        assertEquals("≈ 12 m", DistanceEstimator.formatDistance(11.6))
        assertEquals("≈ 1.2 km", DistanceEstimator.formatDistance(1234.0))
    }
}
