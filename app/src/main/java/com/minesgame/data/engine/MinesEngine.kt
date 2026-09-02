package com.minesgame.data.engine

import java.security.SecureRandom

/**
 * Pure, Android-free math + board generation for Mines.
 *
 * Board: 25 tiles (5x5), `mines` mines (1..24).
 * Multiplier uses a 1% house edge (99% RTP).
 */
object MinesEngine {

    const val MIN_BOARD_SIZE = 4
    const val MAX_BOARD_SIZE = 6
    const val DEFAULT_BOARD_SIZE = 5
    const val TILES = 25
    const val GRID_SIZE = DEFAULT_BOARD_SIZE
    const val MAX_MINES = TILES - 1
    const val HOUSE_EDGE = 0.99

    private val random = SecureRandom()

    fun totalTiles(boardSize: Int = DEFAULT_BOARD_SIZE): Int = boardSize * boardSize

    fun maxMinesForBoard(boardSize: Int = DEFAULT_BOARD_SIZE): Int = totalTiles(boardSize) - 1

    /** Returns a set of mine tile indices. */
    fun generateMinePositions(mines: Int, boardSize: Int = DEFAULT_BOARD_SIZE): Set<Int> {
        val total = totalTiles(boardSize)
        require(mines in 1..maxMinesForBoard(boardSize)) {
            "mines must be in 1..${maxMinesForBoard(boardSize)} for a ${boardSize}x${boardSize} board"
        }
        val indices = (0 until total).toMutableList()
        for (i in total - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = indices[i]
            indices[i] = indices[j]
            indices[j] = tmp
        }
        return indices.take(mines).toSet()
    }

    fun mineChancePercentage(boardSize: Int = DEFAULT_BOARD_SIZE, mines: Int): Double =
        (mines.toDouble() / totalTiles(boardSize).toDouble()) * 100.0

    fun safeChancePercentage(boardSize: Int = DEFAULT_BOARD_SIZE, mines: Int): Double =
        100.0 - mineChancePercentage(boardSize, mines)

    /** Probability that the next pick is safe after `revealed` safe picks. */
    fun safeProbability(mines: Int, revealed: Int, boardSize: Int = DEFAULT_BOARD_SIZE): Double {
        val total = totalTiles(boardSize)
        return (total - mines - revealed).toDouble() / (total - revealed)
    }

    /** Multiplier after `revealed` safe picks. */
    fun multiplierAt(mines: Int, revealed: Int, boardSize: Int = DEFAULT_BOARD_SIZE): Double {
        var survival = 1.0
        for (i in 0 until revealed) {
            survival *= safeProbability(mines, i, boardSize)
        }
        return HOUSE_EDGE / survival
    }

    fun potentialWin(bet: Double, mines: Int, revealed: Int, boardSize: Int = DEFAULT_BOARD_SIZE): Double =
        bet * multiplierAt(mines, revealed, boardSize)
}
