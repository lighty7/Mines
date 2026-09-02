package com.minesgame.data.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MinesEngineTest {

    @Test
    fun `mine positions are within board and unique`() {
        repeat(100) {
            val positions = MinesEngine.generateMinePositions(10)
            assertEquals(10, positions.size)
            positions.forEach { assertTrue(it in 0 until MinesEngine.TILES) }
        }
    }

    @Test
    fun `multiplier increases with each safe pick`() {
        var previous = MinesEngine.multiplierAt(mines = 5, revealed = 0)
        for (k in 1..10) {
            val current = MinesEngine.multiplierAt(mines = 5, revealed = k)
            assertTrue("multiplier should increase", current > previous)
            previous = current
        }
    }

    @Test
    fun `multiplier matches 99 percent RTP formula`() {
        // 5 mines, 1 safe pick: 0.99 / (20/25) = 1.2375
        assertEquals(1.2375, MinesEngine.multiplierAt(mines = 5, revealed = 1), 1e-9)
    }

    @Test
    fun `safe probability is bounded`() {
        assertEquals(0.8, MinesEngine.safeProbability(mines = 5, revealed = 0), 1e-9)
        assertEquals(0.5, MinesEngine.safeProbability(mines = 1, revealed = 23), 1e-9)
    }
}
