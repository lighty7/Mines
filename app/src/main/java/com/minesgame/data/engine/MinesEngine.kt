package com.minesgame.data.engine

import java.security.SecureRandom

/**
 * Pure, Android-free math + board generation for Mines.
 *
 * Board: 25 tiles (5x5), `mines` mines (1..24).
 * Multiplier uses a 1% house edge (99% RTP).
 */
object MinesEngine {

    const val TILES = 25
    const val GRID_SIZE = 5
    const val MAX_MINES = 24
    const val HOUSE_EDGE = 0.99

    private val random = SecureRandom()

    /** Returns a set of mine tile indices. */
    fun generateMinePositions(mines: Int): Set<Int> {
        require(mines in 1..MAX_MINES) { "mines must be in 1..$MAX_MINES" }
        val indices = (0 until TILES).toMutableList()
        for (i in TILES - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = indices[i]
            indices[i] = indices[j]
            indices[j] = tmp
        }
        return indices.take(mines).toSet()
    }

    /** Probability that the next pick is safe after `revealed` safe picks. */
    fun safeProbability(mines: Int, revealed: Int): Double =
        (TILES - mines - revealed).toDouble() / (TILES - revealed)

    /** Multiplier after `revealed` safe picks. */
    fun multiplierAt(mines: Int, revealed: Int): Double {
        var survival = 1.0
        for (i in 0 until revealed) {
            survival *= safeProbability(mines, i)
        }
        return HOUSE_EDGE / survival
    }

    fun potentialWin(bet: Double, mines: Int, revealed: Int): Double =
        bet * multiplierAt(mines, revealed)
}
